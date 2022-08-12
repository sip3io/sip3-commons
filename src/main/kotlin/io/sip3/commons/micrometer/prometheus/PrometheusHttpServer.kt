package io.sip3.commons.micrometer.prometheus

import io.micrometer.core.instrument.Metrics
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.sip3.commons.vertx.annotations.ConditionalOnProperty
import io.sip3.commons.vertx.annotations.Instance
import io.sip3.commons.vertx.util.closeAndExitProcess
import io.vertx.core.AbstractVerticle
import mu.KotlinLogging

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
        }.listen(port, addr) { asr ->
            if (asr.failed()) {
                logger.error(asr.cause()) { "PrometheusHttpServer 'start()' failed." }
                vertx.closeAndExitProcess()
            }
        }
    }
}