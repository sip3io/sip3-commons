/*
 * Copyright 2018-2022 SIP3.IO, Corp.
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

package io.sip3.commons.util

import kotlin.experimental.or

object IpUtil {

    fun convertToString(addr: ByteArray): String {
        return when (addr.size) {
            4 -> {
                convertIpv4ToString(addr)
            }
            16 -> {
                convertIpv6ToString(addr)
            }
            else -> throw UnsupportedOperationException("Unknown IP address format: $addr")
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun convertIpv4ToString(addr: ByteArray): String {
        val sb = StringBuilder()

        (0 until 4).forEach { i ->
            val v = addr[i].toUByte()
            sb.append(v)

            if (i < 3) {
                sb.append(".")
            }
        }

        return sb.toString()
    }

    private fun convertIpv6ToString(addr: ByteArray): String {
        val sb = StringBuilder()

        val (j, l) = findTheLongestZeroSequence(addr)

        (0 until 8).forEach { i ->
            when {
                i == j -> {
                    sb.append("::")
                }
                i < j || i >= j + l -> {
                    val v1 = addr[i * 2]
                    val v2 = addr[i * 2 + 1]

                    if (v1 == 0.toByte()) {
                        sb.append("%x".format(v2))
                    } else {
                        sb.append("%x".format(v1))
                        sb.append("%02x".format(v2))
                    }

                    if (i != j - 1 && i < 7) {
                        sb.append(":")
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun findTheLongestZeroSequence(addr: ByteArray): Pair<Int, Int> {
        // First zero sequence
        var j1 = -1
        var l1 = 0

        // Second zero sequence
        var j2 = -1
        var l2 = 0

        var isZeroSequence = false

        (0 until 8).forEach { i ->
            when (addr[i * 2] or addr[i * 2 + 1]) {
                0.toByte() -> {
                    if (!isZeroSequence) {
                        // New zero sequence
                        when {
                            j2 != -1 -> {
                                if (l1 < l2) {
                                    j1 = j2
                                    l1 = l2
                                }
                                j2 = i
                                l2 = 1
                            }
                            j1 != -1 -> {
                                j2 = i
                                l2++
                            }
                            else -> {
                                j1 = i
                                l1++
                            }
                        }

                        isZeroSequence = true
                    } else {
                        // Existing zero sequence
                        if (j2 != -1) l2++ else l1++
                    }
                }
                else -> {
                    isZeroSequence = false
                }
            }
        }

        return if (l1 < l2) Pair(j2, l2) else Pair(j1, l1)
    }
}