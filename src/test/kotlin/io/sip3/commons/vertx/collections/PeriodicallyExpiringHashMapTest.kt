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

package io.sip3.commons.vertx.collections

import io.mockk.*
import io.mockk.junit5.MockKExtension
import io.sip3.commons.vertx.test.VertxTest
import io.vertx.core.Vertx
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PeriodicallyExpiringHashMapTest : VertxTest() {

    @Test
    fun `Put some objects and wait for them to expire`() {
        val expiringHashMap = PeriodicallyExpiringHashMap.Builder<String, Value>()
            .delay(100)
            .period(5)
            .expireAt { _, v -> v.expireAt() }
            .onExpire { _, v -> v.onExpire() }
            .build(Vertx.vertx())

        val value = mockk<Value>()
        every { value.onExpire() }.just(Runs)

        var now = System.currentTimeMillis()
        every { value.expireAt() }.returns(now - 100)
        assertTrue(expiringHashMap.isEmpty())
        expiringHashMap.getOrPut("test") { value }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 1) { value.expireAt() }
        verify(timeout = 1000, exactly = 1) { value.onExpire() }
        confirmVerified(value)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value.expireAt() }.returns(now + 200)
        expiringHashMap.getOrPut("test") { value }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 3) { value.expireAt() }
        verify(timeout = 1000, exactly = 2) { value.onExpire() }
        confirmVerified(value)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value.expireAt() }.returns(now + 600)
        expiringHashMap.getOrPut("test") { value }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 6) { value.expireAt() }
        verify(timeout = 1000, exactly = 3) { value.onExpire() }
        confirmVerified(value)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value.expireAt() }.returns(now + 1200)
        expiringHashMap.getOrPut("test") { value }
        verify(timeout = 2000, exactly = 10) { value.expireAt() }
        verify(timeout = 2000, exactly = 4) { value.onExpire() }
        confirmVerified(value)
    }

    @AfterEach
    fun `Unmock all`() {
        unmockkAll()
    }

    inner class Value {
        fun expireAt(): Long = System.currentTimeMillis()
        fun onExpire() {}
    }
}