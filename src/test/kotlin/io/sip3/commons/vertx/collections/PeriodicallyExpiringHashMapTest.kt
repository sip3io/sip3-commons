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
            .onRemain { _, v -> v.onRemain() }
            .onExpire { _, v -> v.onExpire() }
            .build(Vertx.vertx())

        val value1 = mockk<Value>()
        every { value1.onRemain() }.just(Runs)
        every { value1.onExpire() }.just(Runs)
        val value2 = mockk<Value>()
        every { value2.onRemain() }.just(Runs)
        every { value2.onExpire() }.just(Runs)

        var now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now - 100)
        assertTrue(expiringHashMap.isEmpty())
        expiringHashMap.getOrPut("test") { value1 }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 1) { value1.expireAt() }
        verify(timeout = 1000, exactly = 0) { value1.onRemain() }
        verify(timeout = 1000, exactly = 1) { value1.onExpire() }
        confirmVerified(value1)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now + 200)
        expiringHashMap.getOrPut("test") { value1 }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 3) { value1.expireAt() }
        verify(timeout = 1000, exactly = 1) { value1.onRemain() }
        verify(timeout = 1000, exactly = 2) { value1.onExpire() }
        confirmVerified(value1)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now + 600)
        expiringHashMap.getOrPut("test") { value1 }
        assertFalse(expiringHashMap.isEmpty())
        verify(timeout = 1000, exactly = 6) { value1.expireAt() }
        verify(timeout = 1000, exactly = 3) { value1.onRemain() }
        verify(timeout = 1000, exactly = 3) { value1.onExpire() }
        confirmVerified(value1)
        assertTrue(expiringHashMap.isEmpty())

        now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now + 1200)
        expiringHashMap.getOrPut("test") { value1 }
        verify(timeout = 2000, exactly = 10) { value1.expireAt() }
        verify(timeout = 1000, exactly = 6) { value1.onRemain() }
        verify(timeout = 2000, exactly = 4) { value1.onExpire() }
        confirmVerified(value1)

        now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now + 1200)
        every { value2.expireAt() }.returns(now + 200)
        expiringHashMap.getOrPut("test") { value1 }
        verify(timeout = 2000, exactly = 11) { value1.expireAt() }
        verify(timeout = 1000, exactly = 7) { value1.onRemain() }
        expiringHashMap.put("test", value2)
        verify(timeout = 2000, exactly = 2) { value2.expireAt() }
        verify(timeout = 2000, exactly = 1) { value2.onRemain() }
        verify(timeout = 2000, exactly = 1) { value2.onExpire() }
        confirmVerified(value1)
        confirmVerified(value2)

        now = System.currentTimeMillis()
        every { value1.expireAt() }.returns(now + 1200)
        expiringHashMap.getOrPut("test") { value1 }
        expiringHashMap.clear()
        assertTrue(expiringHashMap.isEmpty())
    }

    @AfterEach
    fun `Unmock all`() {
        unmockkAll()
    }

    inner class Value {
        fun expireAt(): Long = System.currentTimeMillis()
        fun onRemain() {}
        fun onExpire() {}
    }
}