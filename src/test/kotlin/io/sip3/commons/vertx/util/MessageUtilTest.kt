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

package io.sip3.commons.vertx.util

import io.sip3.commons.vertx.test.VertxTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MessageUtilTest : VertxTest() {

    @Test
    fun `Check 'localReply()' method`() {
        val answer = BigDecimal(42)
        runTest(
            execute = {
                vertx.eventBus().localRequest<BigDecimal>("question", answer) { response ->
                    context.verify {
                        assertEquals(answer, response.result().body())
                    }
                    context.completeNow()
                }
            },
            assert = {
                vertx.eventBus().localConsumer<BigDecimal>("question") { event ->
                    event.localReply(event.body())
                }
            }
        )
    }
}