/*
 * Copyright 2018-2019 SIP3.IO, Inc.
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
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import io.micrometer.statsd.StatsdConfig
import io.micrometer.statsd.StatsdFlavor
import io.micrometer.statsd.StatsdMeterRegistry
import io.sip3.commons.Routes
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf
import mu.KotlinLogging
import java.time.Duration
import kotlin.reflect.KClass
import kotlin.system.exitProcess

class Bootstrap(private val configLocations: List<String>? = null) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    companion object {

        val USE_LOCAL_CODEC = deliveryOptionsOf(codecName = "local", localOnly = true)
    }

    override fun start() {
        // By design Vert.x has default codecs for byte arrays, strings and JSON objects only.
        // Define `local` codec to avoid serialization costs within the application.
        vertx.registerLocalCodec()

        val configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions())
        configRetriever.getConfig { asr ->
            if (asr.failed()) {
                logger.error("ConfigRetriever 'getConfig()' failed.", asr.cause())
                exitProcess(-1)
            } else {
                val config = asr.result().mergeIn(config())
                logger.info("Configuration:\n ${config.encodePrettily()}")
                deployMeterRegistries(config)
                deployVerticles()
            }
        }
        configRetriever.listen { change ->
            val config = change.newConfiguration
            logger.info("Configuration:\n ${config.encodePrettily()}")
            vertx.eventBus().publish(Routes.config_change, config, USE_LOCAL_CODEC)
        }
    }

    fun configRetrieverOptions(): ConfigRetrieverOptions {
        val configStoreOptions = mutableListOf<ConfigStoreOptions>()
        configStoreOptions.apply {
            var options = configStoreOptionsOf(
                    optional = true,
                    type = "file",
                    format = "yaml",
                    config = JsonObject().put("path", "application.yml")
            )
            add(options)
            configLocations?.forEach { configLocation ->
                System.getProperty(configLocation)?.let { path ->
                    options = configStoreOptionsOf(
                            type = "file",
                            format = "yaml",
                            config = JsonObject().put("path", path)
                    )
                    add(options)
                }
            }
        }
        return configRetrieverOptionsOf(stores = configStoreOptions)
    }

    open fun deployMeterRegistries(config: JsonObject) {
        val registry = Metrics.globalRegistry
        config.getString("name")?.let { name ->
            registry.config().commonTags("name", name)
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
                    override fun batchSize() = influxdb.getInteger("batch-size") ?: super.batchSize()
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
                    override fun flavor(): StatsdFlavor {
                        val flavour = statsd.getString("flavour") ?: return StatsdFlavor.DATADOG
                        return try {
                            StatsdFlavor.valueOf(flavour.toUpperCase())
                        } catch (e: Exception) {
                            StatsdFlavor.DATADOG
                        }
                    }
                }, Clock.SYSTEM)
                registry.add(statsdRegistry)
            }
        }
    }

    open fun deployVerticles() {
        // Do nothing...
    }

    fun Vertx.deployVerticle(verticle: KClass<out Verticle>, config: JsonObject, instances: Int = 1) {
        (0 until instances).forEach { index ->
            val options = deploymentOptionsOf(
                    config = config.copy().put("index", index)
            )
            deployVerticle(verticle.java, options) { asr ->
                if (asr.failed()) {
                    logger.error("Vertx 'deployVerticle()' failed. Verticle: $verticle", asr.cause())
                    exitProcess(-1)
                }
            }
        }
    }
}

fun Vertx.registerLocalCodec() {
    eventBus().unregisterCodec("local")
    eventBus().registerCodec(object : MessageCodec<Any, Any> {
        override fun decodeFromWire(pos: Int, buffer: Buffer?) = throw NotImplementedError()
        override fun encodeToWire(buffer: Buffer?, s: Any?) = throw NotImplementedError()
        override fun transform(s: Any?) = s
        override fun name() = "local"
        override fun systemCodecID(): Byte = -1
    })
}