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

import io.vertx.core.Vertx

class PeriodicallyExpiringHashMap<K, V> private constructor(
    vertx: Vertx,
    private val delay: Long,
    private val period: Int,
    private val expireAt: (K, V) -> Long,
    private val onExpire: (K, V) -> Unit
) {

    private val expiringSlots = (0 until period).map { mutableMapOf<K, V>() }.toList()
    private var expiringSlotIdx = 0

    private val objects = mutableMapOf<K, V>()

    init {
        vertx.setPeriodic(delay) {
            terminateExpiringSlot()
            expiringSlotIdx += 1
            if (expiringSlotIdx >= period) {
                expiringSlotIdx = 0
            }
        }
    }

    fun put(key: K, value: V): V? {
        val v = objects.put(key, value)
        if (v == null) {
            expiringSlots[expiringSlotIdx][key] = value
        }
        return v
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        return objects.getOrPut(key) {
            defaultValue.invoke().also { expiringSlots[expiringSlotIdx][key] = it }
        }
    }

    fun get(key: K): V? {
        return objects[key]
    }

    fun remove(key: K): V? {
        return objects.remove(key)
    }

    fun isEmpty(): Boolean {
        return objects.isEmpty()
    }

    fun clear() {
        expiringSlots.forEach { it.clear() }
        objects.clear()
    }

    private fun terminateExpiringSlot() {
        val now = System.currentTimeMillis()

        expiringSlots[expiringSlotIdx].apply {
            forEach { (k, v) ->
                val expireAt = expireAt(k, v)

                when {
                    expireAt <= now -> {
                        objects.remove(k)?.let { onExpire(k, it) }
                    }
                    else -> {
                        var shift = ((expireAt - now) / delay).toInt() + 1
                        if (shift >= period) {
                            shift = period - 1
                        }
                        val nextExpiringSlotIdx = (expiringSlotIdx + shift) % period

                        expiringSlots[nextExpiringSlotIdx][k] = v
                    }
                }
            }
            clear()
        }
    }

    data class Builder<K, V>(
        var delay: Long = 1000,
        var period: Int = 60,
        var expireAt: (K, V) -> Long = { _: K, _: V -> Long.MAX_VALUE },
        var onExpire: (K, V) -> Unit = { _: K, _: V -> }
    ) {
        fun delay(delay: Long) = apply { this.delay = delay }
        fun period(period: Int) = apply { this.period = period }
        fun expireAt(expireAt: (K, V) -> Long) = apply { this.expireAt = expireAt }
        fun onExpire(onExpire: (K, V) -> Unit) = apply { this.onExpire = onExpire }

        fun build(vertx: Vertx) = PeriodicallyExpiringHashMap(vertx, delay, period, expireAt, onExpire)
    }
}