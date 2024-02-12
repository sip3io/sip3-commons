/*
 * Copyright 2018-2024 SIP3.IO, Corp.
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

package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.sip3.commons.util.writeTlv

class RawPayload : Encodable, Decodable {

    companion object {

        const val FIXED_PAYLOAD_LENGTH = 3

        const val TAG_PAYLOAD = 1
    }

    lateinit var payload: ByteArray

    override fun encode(): ByteBuf {
        val bufferSize = FIXED_PAYLOAD_LENGTH + payload.size

        return Unpooled.buffer(bufferSize).apply {
            writeTlv(TAG_PAYLOAD, payload)
        }
    }

    override fun decode(buffer: ByteBuf) {
        while (buffer.readableBytes() > 0) {
            // Tag
            val tag = buffer.readByte()
            // Length
            val length = buffer.readShort() - 3
            // Value
            when (tag.toInt()) {
                TAG_PAYLOAD -> {
                    payload = ByteArray(length)
                    buffer.readBytes(payload)
                }
            }
        }
    }
}