/*
 * Copyright 2018-2021 SIP3.IO, Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sip3.commons.vertx

import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.elastic.ElasticConfig
import io.micrometer.elastic.ElasticMeterRegistry
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import io.micrometer.statsd.StatsdConfig
import io.micrometer.statsd.StatsdFlavor
import io.micrometer.statsd.StatsdMeterRegistry
import io.sip3.commons.Routes
import io.sip3.commons.vertx.annotations.ConditionalOnProperty
import io.sip3.commons.vertx.annotations.Instance
import io.sip3.commons.vertx.util.closeAndExitProcess
import io.sip3.commons.vertx.util.localPublish
import io.sip3.commons.vertx.util.registerLocalCodec
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.core.json.pointer.JsonPointer
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.micrometer.backends.BackendRegistries
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import java.time.Duration
import kotlin.coroutines.CoroutineContext

open class AbstractBootstrap : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    open val configLocations = emptyList<String>()

    override fun start() {
        // By design Vert.x has default codecs for byte arrays, strings and JSON objects only.
        // Define `local` codec to avoid serialization costs within the application.
        vertx.registerLocalCodec()
        vertx.exceptionHandler { t ->
            when (t) {
                is OutOfMemoryError -> {
                    logger.error { "Shutting down the process due to OOM." }
                    vertx.closeAndExitProcess()
                }
                else -> {
                    logger.error(t) { "Got unhandled exception." }
                }
            }
        }

        val configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions())
        configRetriever.getConfig { asr ->
            if (asr.failed()) {
                logger.error("ConfigRetriever 'getConfig()' failed.", asr.cause())
                vertx.closeAndExitProcess()
            } else {
                val config = asr.result().mergeIn(config())
                logger.info("Configuration:\n ${config.encodePrettily()}")
                deployMeterRegistries(config)
                GlobalScope.launch(vertx.dispatcher() as CoroutineContext) {
                    deployVerticles(config)
                }
            }
        }
        configRetriever.listen { change ->
            val config = change.newConfiguration
            logger.info("Configuration changed:\n ${config.encodePrettily()}")
            vertx.eventBus().localPublish(Routes.config_change, config)
        }
    }

    private fun configRetrieverOptions(): ConfigRetrieverOptions {
        val configStoreOptions = mutableListOf<ConfigStoreOptions>()
        configStoreOptions.apply {
            var options = configStoreOptionsOf(
                optional = true,
                type = "file",
                format = "yaml",
                config = JsonObject().put("path", "application.yml")
            )
            add(options)
            configLocations.mapNotNull { System.getProperty(it) }.forEach { path ->
                options = configStoreOptionsOf(
                    optional = true,
                    type = "file",
                    format = "yaml",
                    config = JsonObject().put("path", path)
                )
                add(options)
            }
        }
        return configRetrieverOptionsOf(stores = configStoreOptions)
    }

    open fun deployMeterRegistries(config: JsonObject) {
        val registry = Metrics.globalRegistry
        config.getString("name")?.let { name ->
            registry.config().commonTags("name", name)

            if (vertx.isMetricsEnabled) {
                BackendRegistries.getDefaultNow()
                    .config()
                    .commonTags("name", name)
            }
        }
        config.getJsonObject("metrics")?.let { meters ->

            // Logging
            meters.getJsonObject("logging")?.let { logging ->
                val loggingMeterRegistry = LoggingMeterRegistry(object : LoggingRegistryConfig {
                    override fun get(k: String) = null
                    override fun step() = Duration.ofMillis(logging.getLong("step"))
                }, Clock.SYSTEM)
                registry.add(loggingMeterRegistry)
            }

            // InfluxDB
            meters.getJsonObject("influxdb")?.let { influxdb ->
                val influxMeterRegistry = InfluxMeterRegistry(object : InfluxConfig {
                    override fun get(k: String) = null
                    override fun uri() = influxdb.getString("uri") ?: super.uri()
                    override fun db() = influxdb.getString("db") ?: super.db()
                    override fun step() = influxdb.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun retentionPolicy() = influxdb.getString("retention-policy") ?: super.retentionPolicy()
                    override fun retentionDuration() = influxdb.getString("retention-duration") ?: super.retentionDuration()
                    override fun retentionShardDuration() = influxdb.getString("retention-shard-duration") ?: super.retentionShardDuration()
                    override fun retentionReplicationFactor() = influxdb.getInteger("retention-replication-factor") ?: super.retentionReplicationFactor()
                }, Clock.SYSTEM)
                registry.add(influxMeterRegistry)
            }

            // StatsD
            meters.getJsonObject("statsd")?.let { statsd ->
                val statsdRegistry = StatsdMeterRegistry(object : StatsdConfig {
                    override fun get(k: String) = null
                    override fun host() = statsd.getString("host") ?: super.host()
                    override fun port() = statsd.getInteger("port") ?: super.port()
                    override fun step() = Duration.ofMillis(statsd.getLong("step")) ?: super.step()
                    override fun pollingFrequency() = Duration.ofMillis(statsd.getLong("step")) ?: super.pollingFrequency()
                    override fun buffered() = statsd.getBoolean("buffered") ?: super.buffered()
                    override fun maxPacketLength() = statsd.getInteger("max-packet-length") ?: super.maxPacketLength()
                    override fun flavor(): StatsdFlavor {
                        val flavour = statsd.getString("flavour") ?: return StatsdFlavor.DATADOG
                        return try {
                            StatsdFlavor.valueOf(flavour.uppercase())
                        } catch (e: Exception) {
                            StatsdFlavor.DATADOG
                        }
                    }
                }, Clock.SYSTEM)
                registry.add(statsdRegistry)
            }

            // ELK
            meters.getJsonObject("elastic")?.let { elastic ->
                val elasticRegistry = ElasticMeterRegistry(object : ElasticConfig {
                    override fun get(k: String) = null
                    override fun host() = elastic.getString("host") ?: super.host()
                    override fun index() = elastic.getString("index") ?: super.index()
                    override fun step() = Duration.ofMillis(elastic.getLong("step")) ?: super.step()
                    override fun indexDateFormat() = elastic.getString("index-date-format") ?: super.indexDateFormat()
                    override fun indexDateSeparator() = elastic.getString("index-date-separator") ?: super.indexDateSeparator()
                    override fun autoCreateIndex() = elastic.getBoolean("auto-create-index") ?: super.autoCreateIndex()
                    override fun pipeline() = elastic.getString("pipeline") ?: super.pipeline()
                    override fun timestampFieldName() = elastic.getString("timestamp-field-name") ?: super.timestampFieldName()
                    override fun userName() = elastic.getString("user") ?: super.userName()
                    override fun password() = elastic.getString("password") ?: super.password()
                }, Clock.SYSTEM)
                registry.add(elasticRegistry)
            }
        }
    }

    open suspend fun deployVerticles(config: JsonObject) {
        val packages = mutableListOf<String>().apply {
            add("io.sip3")
            config.getJsonObject("vertx")?.getString("base-package")?.let { basePackage ->
                if (!basePackage.startsWith("io.sip3")) {
                    add(basePackage)
                }
            }
        }

        packages.map { Reflections(it) }.flatMap { reflections ->
            reflections.getTypesAnnotatedWith(Instance::class.java)
                .filter { clazz ->
                    // Filter by 'Verticle' super type
                    ReflectionUtils.getAllSuperTypes(clazz).map { it.name }.contains("io.vertx.core.Verticle")
                }
                .filter { clazz ->
                    // Filter by children
                    reflections.getSubTypesOf(clazz).isEmpty()
                }
                .filter { clazz ->
                    // Filter by `ConditionalOnProperty` annotation
                    clazz.getDeclaredAnnotation(ConditionalOnProperty::class.java)?.let { conditionalOnPropertyAnnotation ->
                        val value = JsonPointer.from(conditionalOnPropertyAnnotation.pointer).queryJson(config)?.toString()
                        return@filter value != null && Regex(conditionalOnPropertyAnnotation.matcher).matches(value)
                    } ?: true
                }
                .filterIsInstance<Class<out Verticle>>()
        }.sortedBy { clazz ->
            // Sort by `Instance` order
            val instanceAnnotation = clazz.getDeclaredAnnotation(Instance::class.java)
            return@sortedBy instanceAnnotation.order
        }.forEach { clazz ->
            val instanceAnnotation = clazz.getDeclaredAnnotation(Instance::class.java)

            val instances = when (instanceAnnotation.singleton) {
                true -> 1
                else -> config.getJsonObject("vertx")?.getInteger("instances") ?: 1
            }
            val worker = instanceAnnotation.worker

            val deploymentOptions = deploymentOptionsOf(config = config, instances = instances, worker = worker)
            try {
                vertx.deployVerticle(clazz, deploymentOptions).await()
            } catch (e: Exception) {
                logger.error(e) { "Vertx 'deployVerticle()' failed. Verticle: $clazz" }
                vertx.closeAndExitProcess()
            }
        }
    }
}