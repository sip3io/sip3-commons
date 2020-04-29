/*
 * Copyright 2018-2020 SIP3.IO, Inc.
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

package io.sip3.commons.vertx.util

import io.sip3.commons.vertx.test.VertxTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EventBusUtilTest : VertxTest() {

    @Test
    fun `Check 'localRequest()' method`() {
        val answer = BigDecimal(42)
        runTest(
                execute = {
                    vertx.eventBus().localRequest<Any>("question", answer)
                },
                assert = {
                    vertx.eventBus().localConsumer<BigDecimal>("question") { asr ->
                        context.verify {
                            assertEquals(answer, asr.body())
                        }
                        context.completeNow()
                    }
                }
        )
    }

    @Test
    fun `Check 'localPublish()' method`() {
        val answer = BigDecimal(42)
        runTest(
                execute = {
                    vertx.eventBus().localPublish("question", answer)
                },
                assert = {
                    vertx.eventBus().localConsumer<BigDecimal>("question") { asr ->
                        context.verify {
                            assertEquals(answer, asr.body())
                        }
                        context.completeNow()
                    }
                }
        )
    }

    @Test
    fun `Define and test event bus endpoints`() {
        runTest(
                deploy = {
                    vertx.eventBus().localConsumer<String>("test1") {}
                    vertx.eventBus().localConsumer<String>("test2") {}
                },
                assert = {
                    vertx.setPeriodic(100) {
                        val endpoints = vertx.eventBus().endpoints()
                        context.verify {
                            if (endpoints.size == 2) {
                                assertTrue(endpoints.contains("test1"))
                                assertTrue(endpoints.contains("test2"))
                                context.completeNow()
                            }
                        }
                    }
                }
        )
    }
}