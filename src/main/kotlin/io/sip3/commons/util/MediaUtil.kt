/*
 * Copyright 2018-2021 SIP3.IO, Inc.
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

object MediaUtil {

    fun rtpSessionId(srcPort: Int, dstPort: Int, ssrc: Long): Long {
        return (srcPort.toLong() shl 48) or (dstPort.toLong() shl 32) or ssrc
    }

    fun sdpSessionId(address: String, port: Int): Long {
        try {
            return (IpUtil.convertToInt(address).toLong() shl 32) or port.toLong()
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't retrieve session ID. Address: $address, Port: $port", e)
        }
    }
}