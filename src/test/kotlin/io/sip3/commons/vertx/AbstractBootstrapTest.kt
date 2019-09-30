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

import io.micrometer.core.instrument.Metrics
import io.sip3.commons.vertx.test.VertxTest
import io.vertx.core.datagram.DatagramSocketOptions
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Test

class AbstractBootstrapTest : VertxTest() {

    @Test
    fun `Retrieve InfluxDB counters`() {
        runTest(
                deploy = {
                    vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                        put("metrics", JsonObject().apply {
                            put("influxdb", JsonObject().apply {
                                put("uri", "http://127.0.0.1:8086")
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
                    server.listen(8086)
                }
        )
    }

    @Test
    fun `Retrieve Datadog counters`() {
        runTest(
                deploy = {
                    vertx.deployTestVerticle(AbstractBootstrap::class, config = JsonObject().apply {
                        put("metrics", JsonObject().apply {
                            put("statsd", JsonObject().apply {
                                put("host", "127.0.0.1")
                                put("port", 8125)
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
                    socket.listen(8125, "0.0.0.0") { connection ->
                        if (connection.succeeded()) {
                            socket.handler { context.completeNow() }
                        }
                    }
                }
        )
    }
}