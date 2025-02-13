/*
 * Copyright 2018-2025 SIP3.IO, Corp.
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

import com.newrelic.telemetry.micrometer.NewRelicRegistry
import com.newrelic.telemetry.micrometer.NewRelicRegistryConfig
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import io.micrometer.core.instrument.logging.LoggingRegistryConfig
import io.micrometer.core.instrument.util.NamedThreadFactory
import io.micrometer.elastic.ElasticConfig
import io.micrometer.elastic.ElasticMeterRegistry
import io.micrometer.influx.InfluxApiVersion
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxConsistency
import io.micrometer.influx.InfluxMeterRegistry
import io.micrometer.prometheus.HistogramFlavor
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micrometer.statsd.StatsdConfig
import io.micrometer.statsd.StatsdFlavor
import io.micrometer.statsd.StatsdMeterRegistry
import io.micrometer.statsd.StatsdProtocol
import io.sip3.commons.Routes
import io.sip3.commons.vertx.annotations.ConditionalOnProperty
import io.sip3.commons.vertx.annotations.Instance
import io.sip3.commons.vertx.util.*
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.ThreadingModel
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.core.json.pointer.JsonPointer
import io.vertx.kotlin.config.configRetrieverOptionsOf
import io.vertx.kotlin.config.configStoreOptionsOf
import io.vertx.kotlin.core.deploymentOptionsOf
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.micrometer.backends.BackendRegistries
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.reflections.ReflectionUtils
import org.reflections.Reflections
import java.time.Duration
import java.util.jar.Manifest
import kotlin.coroutines.CoroutineContext

open class AbstractBootstrap : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    companion object {

        const val DEFAULT_SCAN_PERIOD = 5000L
    }

    open val configLocations = listOf("config.location")
    open val manifestAttributes = mutableMapOf<String, String>().apply {
        put("Project-Name", "project")
        put("Maven-Version", "version")
        put("Build-Timestamp", "built_at")
        put("git_commit_id", "git_commit_id")
    }

    open var scanPeriod = DEFAULT_SCAN_PERIOD

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

        configRetrieverOptions()
            .onFailure { t ->
                logger.error(t) { "AbstractBootstrap 'configRetrieverOptions()' failed." }
                vertx.closeAndExitProcess()
            }
            .onSuccess { configRetrieverOptions ->
                val configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions)
                configRetriever.config
                    .map { config ->
                        if (!config.containsKebabCase()) {
                            return@map config
                        }
                        logger.warn { "Config contains keys in `kebab-case`." }
                        return@map config.toSnakeCase()
                    }
                    .map { it.mergeIn(config()) }
                    .onFailure { t ->
                        logger.error(t) { "ConfigRetriever 'getConfig()' failed." }
                        vertx.closeAndExitProcess()
                    }
                    .onSuccess { config ->
                        addManifestAttrs(config)
                        logger.info("Configuration:\n ${config.encodePrettily()}")
                        deployMeterRegistries(config)
                        GlobalScope.launch(vertx.dispatcher() as CoroutineContext) {
                            deployVerticles(config)
                        }
                    }

                configRetriever.listen { change ->
                    val config = change.newConfiguration.toSnakeCase()
                    addManifestAttrs(config)
                    logger.info("Configuration changed:\n ${config.encodePrettily()}")
                    vertx.eventBus().localPublish(Routes.config_change, config)
                }
            }
    }

    open fun configRetrieverOptions(): Future<ConfigRetrieverOptions> {
        val type = System.getProperty("config.type") ?: "local"
        return getConfigStores(type).map { stores ->
            val configStoreOptions = mutableListOf<ConfigStoreOptions>().apply {
                // Add default config from classpath
                val options = configStoreOptionsOf(
                    optional = true,
                    type = "file",
                    format = "yaml",
                    config = JsonObject().put("path", "application.yml")
                )
                add(options)

                // Add main config stores
                addAll(stores)
            }

            return@map configRetrieverOptionsOf(
                stores = configStoreOptions,
                scanPeriod = scanPeriod
            )
        }
    }

    open fun getConfigStores(type: String): Future<List<ConfigStoreOptions>> {
        val configStoreOptions = configStoreOptionsOf(
            optional = true,
            type = "file",
            format = "yaml",
            config = JsonObject().put("path", System.getProperty("config.location") ?: "application.yml")
        )
        val configRetrieverOptions = configRetrieverOptionsOf(stores = listOf(configStoreOptions))
        val configRetriever = ConfigRetriever.create(vertx, configRetrieverOptions)

        return configRetriever.config
            .map { it.getJsonObject("config") }
            .map { config ->
                config?.getLong("scan_period")?.let {
                    scanPeriod = it
                }

                val stores = mutableListOf<ConfigStoreOptions>()
                when (type) {
                    "local" -> configLocations.mapNotNull { System.getProperty(it) }
                        .map { path ->
                            configStoreOptionsOf(
                                optional = true,
                                type = "file",
                                format = "yaml",
                                config = JsonObject().put("path", path)
                            )
                        }.let { stores.addAll(it) }

                    else -> {
                        // Add Custom config store
                        stores.add(
                            configStoreOptionsOf(
                                optional = false,
                                type = type,
                                format = config?.getString("format") ?: "json",
                                config = config
                            )
                        )
                    }
                }

                // Add System properties config store if configured
                config?.getJsonObject("sys")?.let { sys ->
                    stores.add(
                        configStoreOptionsOf(
                            optional = true,
                            type = "sys",
                            config = sys
                        )
                    )
                }

                return@map stores
            }
    }

    open fun addManifestAttrs(config: JsonObject) {
        try {
            this.javaClass.classLoader.getResources("META-INF/MANIFEST.MF")?.nextElement()?.openStream()
                ?.use { Manifest(it).mainAttributes }
                ?.let { attrs ->
                    manifestAttributes.forEach { (key, mapped) ->
                        attrs.getValue(key)?.let {
                            config.put(mapped, it)
                        }
                    }
                }
        } catch (e: Exception) {
            logger.error(e) { "Failed to read manifest." }
        }
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
                    override fun step() = logging.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                }, Clock.SYSTEM)
                registry.add(loggingMeterRegistry)
            }

            // InfluxDB
            meters.getJsonObject("influxdb")?.let { influxdb ->
                val influxMeterRegistry = InfluxMeterRegistry(object : InfluxConfig {
                    override fun get(k: String) = null
                    override fun step() = influxdb.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun uri() = influxdb.getString("uri") ?: super.uri()
                    override fun db() = influxdb.getString("db") ?: super.db()
                    override fun retentionPolicy() = influxdb.getString("retention_policy") ?: super.retentionPolicy()
                    override fun retentionDuration() = influxdb.getString("retention_duration") ?: super.retentionDuration()
                    override fun retentionShardDuration() = influxdb.getString("retention_shard_duration") ?: super.retentionShardDuration()
                    override fun retentionReplicationFactor() = influxdb.getInteger("retention_replication_factor") ?: super.retentionReplicationFactor()
                    override fun userName() = influxdb.getString("username") ?: super.userName()
                    override fun password() = influxdb.getString("password") ?: super.password()
                    override fun token() = influxdb.getString("token") ?: super.token()
                    override fun compressed() = influxdb.getBoolean("compressed") ?: super.compressed()
                    override fun autoCreateDb() = influxdb.getBoolean("auto_create_db") ?: super.autoCreateDb()
                    override fun apiVersion(): InfluxApiVersion {
                        val version = influxdb.getString("version") ?: return InfluxApiVersion.V1
                        return try {
                            InfluxApiVersion.valueOf(version.uppercase())
                        } catch (e: Exception) {
                            InfluxApiVersion.V1
                        }
                    }

                    override fun org() = influxdb.getString("org") ?: super.org()
                    override fun bucket() = influxdb.getString("bucket") ?: super.bucket()
                    override fun consistency(): InfluxConsistency {
                        val consistency = influxdb.getString("consistency") ?: return InfluxConsistency.ONE
                        return try {
                            InfluxConsistency.valueOf(consistency.uppercase())
                        } catch (e: Exception) {
                            InfluxConsistency.ONE
                        }
                    }
                }, Clock.SYSTEM)
                registry.add(influxMeterRegistry)
            }

            // StatsD
            meters.getJsonObject("statsd")?.let { statsd ->
                val statsdRegistry = StatsdMeterRegistry(object : StatsdConfig {
                    override fun get(k: String) = null
                    override fun step() = statsd.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun enabled() = statsd.getBoolean("enabled") ?: super.enabled()
                    override fun host() = statsd.getString("host") ?: super.host()
                    override fun port() = statsd.getInteger("port") ?: super.port()
                    override fun protocol(): StatsdProtocol {
                        val protocol = statsd.getString("protocol") ?: return StatsdProtocol.UDP
                        return try {
                            StatsdProtocol.valueOf(protocol.uppercase())
                        } catch (e: Exception) {
                            StatsdProtocol.UDP
                        }
                    }

                    override fun pollingFrequency() = statsd.getLong("step")?.let { Duration.ofMillis(it) } ?: super.pollingFrequency()
                    override fun buffered() = statsd.getBoolean("buffered") ?: super.buffered()
                    override fun maxPacketLength() = statsd.getInteger("max_packet_length") ?: super.maxPacketLength()
                    override fun publishUnchangedMeters() = statsd.getBoolean("publish_unchanged_meters") ?: super.publishUnchangedMeters()
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
                    override fun step() = elastic.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun host() = elastic.getString("host") ?: super.host()
                    override fun index() = elastic.getString("index") ?: super.index()
                    override fun indexDateFormat() = elastic.getString("index_date_format") ?: super.indexDateFormat()
                    override fun indexDateSeparator() = elastic.getString("index_date_separator") ?: super.indexDateSeparator()
                    override fun autoCreateIndex() = elastic.getBoolean("auto_create_index") ?: super.autoCreateIndex()
                    override fun pipeline() = elastic.getString("pipeline") ?: super.pipeline()
                    override fun timestampFieldName() = elastic.getString("timestamp_field_name") ?: super.timestampFieldName()
                    override fun userName() = elastic.getString("user") ?: super.userName()
                    override fun password() = elastic.getString("password") ?: super.password()
                    override fun apiKeyCredentials() = elastic.getString("token") ?: super.apiKeyCredentials()
                }, Clock.SYSTEM)
                registry.add(elasticRegistry)
            }

            // Prometheus
            meters.getJsonObject("prometheus")?.let { prometheus ->
                val prometheusRegistry = PrometheusMeterRegistry(object : PrometheusConfig {
                    override fun get(k: String) = null
                    override fun step() = prometheus.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun descriptions() = prometheus.getBoolean("descriptions") ?: super.descriptions()
                    override fun histogramFlavor(): HistogramFlavor {
                        val flavour = prometheus.getString("histogram_flavour") ?: return HistogramFlavor.Prometheus
                        return try {
                            HistogramFlavor.valueOf(flavour)
                        } catch (e: Exception) {
                            HistogramFlavor.Prometheus
                        }
                    }
                })
                registry.add(prometheusRegistry)
            }

            // New Relic
            meters.getJsonObject("new_relic")?.let { newRelic ->
                val newRelicRegistry = NewRelicRegistry.builder(object : NewRelicRegistryConfig {
                    override fun get(k: String) = null
                    override fun step() = newRelic.getLong("step")?.let { Duration.ofMillis(it) } ?: super.step()
                    override fun uri() = newRelic.getString("uri") ?: super.uri()
                    override fun apiKey() = newRelic.getString("api_key") ?: super.apiKey()
                    override fun serviceName() = newRelic.getString("service_name") ?: super.serviceName()
                    override fun useLicenseKey() = newRelic.getBoolean("use_license_key") ?: super.useLicenseKey()
                    override fun enableAuditMode() = newRelic.getBoolean("enable_audit_mode") ?: super.enableAuditMode()
                }).build().apply {
                    start(NamedThreadFactory("newrelic.micrometer.registry"))
                }
                registry.add(newRelicRegistry)
            }
        }
    }

    open suspend fun deployVerticles(config: JsonObject) {
        val packages = mutableListOf<String>().apply {
            config.getJsonObject("vertx")?.getJsonArray("base_packages")?.forEach { basePackage ->
                add(basePackage as String)
            }

            if (isEmpty()) {
                add("io.sip3")
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

            val threadingModel = when {
                instanceAnnotation.worker -> ThreadingModel.WORKER
                instanceAnnotation.virtual -> ThreadingModel.VIRTUAL_THREAD
                else -> ThreadingModel.EVENT_LOOP
            }

            val deploymentOptions = deploymentOptionsOf(config = config, instances = instances, threadingModel = threadingModel)
            try {
                vertx.deployVerticle(clazz, deploymentOptions).coAwait()
            } catch (e: Exception) {
                logger.error(e) { "Vertx 'deployVerticle()' failed. Verticle: $clazz" }
                vertx.closeAndExitProcess()
            }
        }
    }
}