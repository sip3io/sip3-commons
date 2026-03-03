package io.sip3.commons.micrometer.prometheus

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Metrics
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.sip3.commons.vertx.annotations.ConditionalOnProperty
import io.sip3.commons.vertx.annotations.Instance
import io.sip3.commons.vertx.util.closeAndExitProcess
import io.vertx.core.AbstractVerticle

@Instance(singleton = true)
@ConditionalOnProperty("/metrics/prometheus")
class PrometheusHttpServer : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    private var addr: String = "0.0.0.0"
    private var port = 8888

    private lateinit var registry: PrometheusMeterRegistry

    override fun start() {
        config().getJsonObject("metrics").getJsonObject("prometheus")?.let { config ->
            config.getString("addr")?.let {
                addr = it
            }
            config.getInteger("port")?.let {
                port = it
            }
        }

        registry = Metrics.globalRegistry.registries.firstNotNullOf { it as? PrometheusMeterRegistry }

        vertx.createHttpServer().requestHandler { req ->
            req.response().end(registry.scrape())
        }.listen(port, addr)
            .onFailure { e ->
                logger.error(e) { "PrometheusHttpServer 'start()' failed." }
                vertx.closeAndExitProcess()
            }
    }
}