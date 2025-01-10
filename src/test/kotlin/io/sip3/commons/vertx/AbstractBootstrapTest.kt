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

import io.micrometer.core.instrument.Metrics
import io.sip3.commons.vertx.annotations.ConditionalOnProperty
import io.sip3.commons.vertx.annotations.Instance
import io.sip3.commons.vertx.test.VertxTest
import io.sip3.commons.vertx.util.endpoints
import io.sip3.commons.vertx.util.localSend
import io.vertx.core.AbstractVerticle
import io.vertx.core.datagram.DatagramSocketOptions
import io.vertx.core.http.RequestOptions
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AbstractBootstrapTest : VertxTest() {

    @Instance
    open class A : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("A") {}
        }
    }

    @Instance
    class B : A() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("B") {}
        }
    }

    @Instance
    @ConditionalOnProperty("/C")
    open class C : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("C") {}
        }
    }

    @Instance
    @ConditionalOnProperty("/D/D")
    open class D : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("D") {}
        }
    }

    @Instance
    @ConditionalOnProperty(pointer = "/E", matcher = "true")
    open class E : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("E") {}
        }
    }

    @Instance
    @ConditionalOnProperty(pointer = "/F", matcher = "false")
    open class F : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localConsumer<Any>("F") {}
        }
    }

    @Instance
    @ConditionalOnProperty(pointer = "/G")
    open class G : AbstractVerticle() {

        override fun start() {
            vertx.eventBus().localSend("test_check_config", config())
        }
    }

    @Test
    fun `Check auto deployment`() {
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("D", JsonObject().apply {
                        put("D", true)
                    })
                    put("E", true)
                    put("F", true)
                })
            },
            execute = {
                // Do nothing...
            },
            assert = {
                vertx.setPeriodic(100) {
                    val endpoints = vertx.eventBus().endpoints()
                    if (endpoints.size >= 3) {
                        context.verify {
                            assertTrue(endpoints.contains("B"))
                            assertTrue(endpoints.contains("D"))
                            assertTrue(endpoints.contains("E"))
                        }
                        context.completeNow()
                    }
                }
            },
            cleanup = this::removeRegistries
        )
    }

    @Test
    fun `Fetch config from JsonConfigStore`() {
        System.setProperty("config.type", "json")
        System.setProperty("config.location", "src/test/resources/application-test.yml")
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject())
            },
            execute = {
                // Do nothing...
            },
            assert = {
                vertx.setPeriodic(100) {
                    val endpoints = vertx.eventBus().endpoints()
                    if (endpoints.size >= 3) {
                        context.verify {
                            assertTrue(endpoints.contains("B"))
                            assertTrue(endpoints.contains("D"))
                            assertTrue(endpoints.contains("E"))
                        }
                        context.completeNow()
                    }
                }
            },
            cleanup = {
                removeRegistries()
                System.clearProperty("config.type")
                System.clearProperty("config.location")
            }
        )
    }

    @Test
    fun `Check backward compatibility for kebab-case in config`() {
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("G", JsonObject().apply {
                        put("key-1", "value-1")
                        put("key_2", "value_2")
                    })
                })
            },
            execute = {
                // Do nothing...
            },
            assert = {
                vertx.eventBus().localConsumer<JsonObject>("test_check_config") { event ->
                    val config = event.body()
                    context.verify {
                        assertTrue(config.containsKey("G"))
                        config.getJsonObject("G").apply {
                            assertEquals(2, size())
                            assertEquals("value-1", getString("key-1"))
                            assertEquals("value_2", getString("key_2"))
                        }
                    }
                    context.completeNow()
                }
            }
        )
    }

    @Test
    fun `Retrieve InfluxDB counters`() {
        val port = findRandomPort()
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("metrics", JsonObject().apply {
                        put("influxdb", JsonObject().apply {
                            put("uri", "http://127.0.0.1:$port")
                            put("step", 1000)
                        })
                    })
                })
            },
            execute = {
                vertx.setPeriodic(100) { Metrics.counter("test").increment() }
            },
            assert = {
                val server = vertx.createHttpServer()
                server.requestHandler { request ->
                    request.response().end("OK")
                    context.completeNow()
                }
                server.listen(port)
            },
            cleanup = this::removeRegistries
        )
    }

    @Test
    fun `Retrieve Datadog counters`() {
        val port = findRandomPort()
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("metrics", JsonObject().apply {
                        put("statsd", JsonObject().apply {
                            put("host", "127.0.0.1")
                            put("port", port)
                            put("step", 1000)
                        })
                    })
                })
            },
            execute = {
                vertx.setPeriodic(100) { Metrics.counter("test").increment() }
            },
            assert = {
                val socket = vertx.createDatagramSocket(DatagramSocketOptions())
                socket.listen(port, "0.0.0.0") { connection ->
                    if (connection.succeeded()) {
                        socket.handler { context.completeNow() }
                    }
                }
            },
            cleanup = this::removeRegistries
        )
    }

    @Test
    fun `Retrieve ELK counters`() {
        val port = findRandomPort()
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("metrics", JsonObject().apply {
                        put("elastic", JsonObject().apply {
                            put("host", "http://127.0.0.1:$port")
                            put("step", 1000)
                        })
                    })
                })
            },
            execute = {
                vertx.setPeriodic(100) { Metrics.counter("test").increment() }
            },
            assert = {
                val server = vertx.createHttpServer()
                server.requestHandler { request ->
                    request.response().end("OK")
                    context.completeNow()
                }
                server.listen(port)
            },
            cleanup = this::removeRegistries
        )
    }

    @Test
    fun `Retrieve Prometheus counters`() {
        val port = findRandomPort()
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("metrics", JsonObject().apply {
                        put("prometheus", JsonObject().apply {
                            put("host", "127.0.0.1")
                            put("port", port)
                            put("step", 1000)
                        })
                    })
                })
            },
            execute = {
                vertx.setPeriodic(100) { Metrics.counter("test").increment() }
                vertx.setPeriodic(200) {
                    vertx.createHttpClient().request(RequestOptions().apply {
                        this.port = port
                        uri = "http://127.0.0.1:$port/metrics"
                    }).onSuccess { request ->
                        request.send().onSuccess { response ->
                            if (response.statusCode() == 200) {
                                response.body().onSuccess { body ->
                                    context.verify {
                                        assertTrue(body.toString().contains("test_total"))
                                    }
                                    context.completeNow()
                                }
                            } else {
                                context.failNow(response.statusMessage())
                            }
                        }
                    }
                }
            },
            cleanup = this::removeRegistries
        )
    }

    @Test
    fun `Retrieve New Relic counters`() {
        val port = findRandomPort()
        runTest(
            deploy = {
                vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                    put("metrics", JsonObject().apply {
                        put("new_relic", JsonObject().apply {
                            put("uri", "http://127.0.0.1:$port")
                            put("step", 1000)
                            put("api_key", "test")
                        })
                    })
                })
            },
            execute = {
                vertx.setPeriodic(100) { Metrics.counter("test").increment() }
            },
            assert = {
                val server = vertx.createHttpServer()
                server.requestHandler { request ->
                    request.response().end("OK")
                    context.completeNow()
                }
                server.listen(port)
            },
            cleanup = this::removeRegistries
        )
    }

    private fun removeRegistries() {
        Metrics.globalRegistry.registries.iterator().forEach { registry ->
            Metrics.removeRegistry(registry)
            registry.close()
        }
    }
}