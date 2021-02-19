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

package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.sip3.commons.util.remainingCapacity
import io.sip3.commons.util.writeTlv
import java.nio.charset.Charset

class RecordingPayload : Encodable, Decodable {

    companion object {

        const val FIXED_PAYLOAD_LENGTH = 10

        const val TYPE_RTP: Byte = 0x00
        const val TYPE_RTP_GDPR: Byte = 0x01
        const val TYPE_RTCP: Byte = 0x02

        const val TAG_TYPE = 1
        const val TAG_CALL_ID = 2
        const val TAG_PAYLOAD = 3
    }

    var type: Byte = -1

    lateinit var callId: String
    lateinit var payload: ByteArray

    override fun encode(): ByteBuf {
        val bufferSize = FIXED_PAYLOAD_LENGTH + callId.length + payload.size

        return Unpooled.buffer(bufferSize).apply {
            writeTlv(TAG_TYPE, type)
            writeTlv(TAG_CALL_ID, callId)
            writeTlv(TAG_PAYLOAD, payload)
        }
    }

    override fun decode(buffer: ByteBuf) {
        while (buffer.remainingCapacity() > 0) {
            // Tag
            val tag = buffer.readByte()
            // Length
            val length = buffer.readShort() - 3
            // Value
            when (tag.toInt()) {
                TAG_TYPE -> type = buffer.readByte()
                TAG_CALL_ID -> callId = buffer.readCharSequence(length, Charset.defaultCharset()).toString()
                TAG_PAYLOAD -> {
                    payload = ByteArray(length)
                    buffer.readBytes(payload)
                }
            }
        }
    }
}