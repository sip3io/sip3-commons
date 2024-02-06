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
import java.nio.charset.Charset

open class RtpEventPayload : Encodable, Decodable {

    companion object {

        const val BASE_PAYLOAD_LENGTH = 62

        const val TAG_PAYLOAD_TYPE = 1
        const val TAG_SSRC = 2
        const val TAG_CALL_ID = 3
        const val TAG_SAMPLING_RATE = 4

        const val TAG_CREATED_AT = 5
        const val TAG_TERMINATED_AT = 6

        const val TAG_EVENT = 7
        const val TAG_VOLUME = 8
        const val TAG_DURATION = 9
    }

    var payloadType: Byte = -1
    var ssrc: Long = 0
    var callId: String? = null
    var samplingRate: Int = 0

    var createdAt: Long = 0
    var terminatedAt: Long = 0
    var event: Byte = 0
    var volume: Int = 0
    var duration: Int = 0

    override fun encode(): ByteBuf {
        var bufferSize = BASE_PAYLOAD_LENGTH
        callId?.let { bufferSize += it.length + 3 }
        return Unpooled.buffer(bufferSize).apply {
            writeTlv(TAG_PAYLOAD_TYPE, payloadType)
            writeTlv(TAG_SSRC, ssrc)
            callId?.let { writeTlv(TAG_CALL_ID, it) }
            writeTlv(TAG_SAMPLING_RATE, samplingRate)

            writeTlv(TAG_CREATED_AT, createdAt)
            writeTlv(TAG_TERMINATED_AT, terminatedAt)
            writeTlv(TAG_EVENT, event)
            writeTlv(TAG_VOLUME, volume)
            writeTlv(TAG_DURATION, duration)
        }
    }

    override fun decode(buffer: ByteBuf) {
        while (buffer.readableBytes() > 0) {
            // Type
            val type = buffer.readByte()
            // Length
            val length = buffer.readShort() - 3
            // Value
            when (type.toInt()) {
                TAG_PAYLOAD_TYPE -> payloadType = buffer.readByte()
                TAG_SSRC -> ssrc = buffer.readLong()
                TAG_CALL_ID -> callId = buffer.readCharSequence(length, Charset.defaultCharset()).toString()
                TAG_SAMPLING_RATE -> samplingRate = buffer.readInt()

                TAG_CREATED_AT -> createdAt = buffer.readLong()
                TAG_TERMINATED_AT -> terminatedAt = buffer.readLong()
                TAG_EVENT -> event = buffer.readByte()
                TAG_VOLUME -> volume = buffer.readInt()
                TAG_DURATION -> duration = buffer.readInt()
            }
        }
    }
}
