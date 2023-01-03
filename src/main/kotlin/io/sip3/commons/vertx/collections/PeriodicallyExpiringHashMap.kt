/*
 * Copyright 2018-2023 SIP3.IO, Corp.
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
import java.lang.Long.min

class PeriodicallyExpiringHashMap<K, V> private constructor(
    vertx: Vertx,
    private val delay: Long,
    private val period: Int,
    private val expireAt: (K, V) -> Long,
    private val onRemain: (Long, K, V) -> Unit,
    private val onExpire: (Long, K, V) -> Unit
) {

    companion object {

        const val MAX_INT = Int.MAX_VALUE.toLong()
    }

    private val expiringSlots = List(period) { mutableMapOf<K, V>() }
    private var expiringSlotIdx = 0

    private val objects = mutableMapOf<K, V>()
    private val objectSlots = mutableMapOf<K, Int>()

    init {
        vertx.setPeriodic(delay) {
            val currentExpiringSlotIdx = expiringSlotIdx

            expiringSlotIdx += 1
            if (expiringSlotIdx >= period) {
                expiringSlotIdx = 0
            }

            terminateExpiringSlot(currentExpiringSlotIdx)
        }
    }

    fun put(key: K, value: V): V? {
        return objects.put(key, value).also { v ->
            if (v != null) {
                objectSlots.remove(key)?.let { expiringSlots[it].remove(key) }
            }
            objectSlots[key] = expiringSlotIdx
            expiringSlots[expiringSlotIdx][key] = value
        }
    }

    fun getOrPut(key: K, defaultValue: () -> V): V {
        return objects.getOrPut(key) {
            objectSlots[key] = expiringSlotIdx
            defaultValue.invoke().also {
                expiringSlots[expiringSlotIdx][key] = it
            }
        }
    }

    fun touch(key: K) {
        val value = objectSlots.remove(key)?.let { expiringSlots[it].remove(key) }
        if (value != null) {
            objectSlots[key] = expiringSlotIdx
            expiringSlots[expiringSlotIdx][key] = value
        }
    }

    fun get(key: K): V? {
        return objects[key]
    }

    fun forEach(action: (K, V) -> Unit) {
        objects.forEach { (k, v) -> action.invoke(k, v) }
    }

    fun remove(key: K): V? {
        objectSlots.remove(key)?.let { expiringSlots[it].remove(key) }
        return objects.remove(key)
    }

    fun isEmpty(): Boolean {
        return objects.isEmpty()
    }

    fun size(): Int {
        return objects.size
    }

    fun clear() {
        expiringSlots.forEach { it.clear() }
        objectSlots.clear()
        objects.clear()
    }

    private fun terminateExpiringSlot(currentExpiringSlotIdx: Int) {
        val now = System.currentTimeMillis()

        expiringSlots[currentExpiringSlotIdx].forEach { (k, v) ->
            val expireAt = expireAt(k, v)

            if (expireAt <= now) {
                objectSlots.remove(k)
                objects.remove(k)?.let { onExpire(now, k, it) }
            } else {
                var shift = min(((expireAt - now) / delay) + 1, MAX_INT).toInt()
                if (shift >= period) {
                    shift = period - 1
                }
                val nextExpiringSlotIdx = (currentExpiringSlotIdx + shift) % period

                objectSlots[k] = nextExpiringSlotIdx
                expiringSlots[nextExpiringSlotIdx][k] = v

                objects[k]?.let { onRemain(now, k, it) }
            }
        }

        expiringSlots[currentExpiringSlotIdx].clear()
    }

    data class Builder<K, V>(
        var delay: Long = 1000,
        var period: Int = 2,
        var expireAt: (K, V) -> Long = { _: K, _: V -> Long.MAX_VALUE },
        var onRemain: (Long, K, V) -> Unit = { _: Long, _: K, _: V -> },
        var onExpire: (Long, K, V) -> Unit = { _: Long, _: K, _: V -> },
    ) {
        fun delay(delay: Long) = apply {
            if (delay <= 0) throw IllegalArgumentException("'PeriodicallyExpiringHashMap' delay must be greater than 0.")
            this.delay = delay
        }

        fun period(period: Int) = apply {
            if (period <= 1) throw IllegalArgumentException("'PeriodicallyExpiringHashMap' period must be greater than 1.")
            this.period = period
        }

        fun expireAt(expireAt: (K, V) -> Long) = apply { this.expireAt = expireAt }
        fun onRemain(onRemain: (Long, K, V) -> Unit) = apply { this.onRemain = onRemain }
        fun onRemain(onRemain: (K, V) -> Unit) = apply { this.onRemain = { _, k, v -> onRemain.invoke(k, v) } }
        fun onExpire(onExpire: (Long, K, V) -> Unit) = apply { this.onExpire = onExpire }
        fun onExpire(onExpire: (K, V) -> Unit) = apply { this.onExpire = { _, k, v -> onExpire.invoke(k, v) } }

        fun build(vertx: Vertx) = PeriodicallyExpiringHashMap(vertx, delay, period, expireAt, onRemain, onExpire)
    }
}