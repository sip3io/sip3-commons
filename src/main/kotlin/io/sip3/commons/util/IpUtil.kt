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

package io.sip3.commons.util

object IpUtil {

    fun convertToInt(addr: ByteArray): Int {
        if (addr.size != 4) {
            throw UnsupportedOperationException("Can't convert ${addr.size}bytes address to Int")
        }

        var number = 0
        repeat(4) { i ->
            val octet = addr[i].toInt() and 0xff
            number = (number shl 8) or octet
        }
        return number
    }

    fun convertToInt(addr: String): Int {
        val octets = addr.split(".")
        if (octets.size != 4) {
            throw UnsupportedOperationException("Can't convert '$addr' to Int")
        }

        var number = 0
        repeat(4) { i ->
            number = (number shl 8) + octets[i].toInt()
        }

        return number
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun convertToString(addr: ByteArray): String {
        if (addr.size != 4) {
            throw UnsupportedOperationException("Can't convert ${addr.size}bytes address to String")
        }

        return "${addr[0].toUByte()}.${addr[1].toUByte()}.${addr[2].toUByte()}.${addr[3].toUByte()}"
    }
}
