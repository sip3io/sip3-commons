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

package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.sip3.commons.util.writeTlv

open class TelephoneEventPayload : Encodable, Decodable {

    companion object {

        const val BASE_PAYLOAD_LENGTH = 55

        const val TYPE_DTMF_EVENT = 0.toByte()
        const val TYPE_FAX_RELATED_EVENT = 1.toByte()
        const val TYPE_LINE_EVENT = 2.toByte()
        const val TYPE_EXTENDED_LINE_EVENT = 3.toByte()
        const val TYPE_TRUNK_EVENT = 4.toByte()

        const val TAG_EVENT_TYPE = 1
        const val TAG_PAYLOAD_TYPE = 2
        const val TAG_SSRC = 3
        const val TAG_SAMPLING_RATE = 4

        const val TAG_CREATED_AT = 5
        const val TAG_VOLUME = 6
        const val TAG_DURATION = 7
        const val TAG_END = 8
    }

    var eventType: Byte = -1
    var payloadType: Byte = -1
    var ssrc: Long = 0
    var samplingRate: Int = 0

    var createdAt: Long = 0
    var volume: Int = 0
    var duration: Int = 0
    var end: Boolean = false

    override fun encode(): ByteBuf {
        return Unpooled.buffer(BASE_PAYLOAD_LENGTH).apply {
            writeTlv(TAG_EVENT_TYPE, eventType)
            writeTlv(TAG_PAYLOAD_TYPE, payloadType)
            writeTlv(TAG_SSRC, ssrc)
            writeTlv(TAG_SAMPLING_RATE, samplingRate)

            writeTlv(TAG_CREATED_AT, createdAt)
            writeTlv(TAG_VOLUME, volume)
            writeTlv(TAG_DURATION, duration)
            writeTlv(TAG_END, end)
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
                TAG_EVENT_TYPE -> this.eventType = buffer.readByte()
                TAG_PAYLOAD_TYPE -> payloadType = buffer.readByte()
                TAG_SSRC -> ssrc = buffer.readLong()
                TAG_SAMPLING_RATE -> samplingRate = buffer.readInt()

                TAG_CREATED_AT -> createdAt = buffer.readLong()
                TAG_VOLUME -> volume = buffer.readInt()
                TAG_DURATION -> duration = buffer.readInt()
                TAG_END -> end = buffer.readBoolean()
            }
        }
    }
}
