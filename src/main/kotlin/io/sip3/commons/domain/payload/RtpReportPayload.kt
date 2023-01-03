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

package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.sip3.commons.util.writeTlv
import java.nio.charset.Charset

open class RtpReportPayload : Encodable, Decodable {

    companion object {

        const val BASE_PAYLOAD_LENGTH = 136

        const val SOURCE_RTP = 0.toByte()
        const val SOURCE_RTCP = 1.toByte()

        const val TAG_SOURCE = 1
        const val TAG_PAYLOAD_TYPE = 2
        const val TAG_SSRC = 3
        const val TAG_CALL_ID = 4
        const val TAG_CODEC_NAME = 5
        const val TAG_RECORDED = 6

        const val TAG_EXPECTED_PACKET_COUNT = 11
        const val TAG_RECEIVED_PACKET_COUNT = 12
        const val TAG_LOST_PACKET_COUNT = 13
        const val TAG_REJECTED_PACKET_COUNT = 14

        const val TAG_DURATION = 15

        const val TAG_LAST_JITTER = 16
        const val TAG_AVG_JITTER = 17
        const val TAG_MIN_JITTER = 18
        const val TAG_MAX_JITTER = 19

        const val TAG_R_FACTOR = 20
        const val TAG_MOS = 21
        const val TAG_FRACTION_LOST = 22

        const val TAG_REPORTED_AT = 23
        const val TAG_CREATED_AT = 24

        const val TAG_MARKER_PACKET_COUNT = 25
    }

    var source: Byte = -1

    var payloadType: Byte = -1
    var ssrc: Long = 0
    var callId: String? = null
    var codecName: String? = null
    var recorded = false

    var expectedPacketCount: Int = 0
    var receivedPacketCount: Int = 0
    var lostPacketCount: Int = 0
    var rejectedPacketCount: Int = 0
    var markerPacketCount: Int = 0

    var duration: Int = 0

    var lastJitter: Float = 0F
    var avgJitter: Float = 0F
    var minJitter: Float = 10000F
    var maxJitter: Float = 0F

    var rFactor: Float = 0F
    var mos: Float = 1.0F
    var fractionLost: Float = 0F

    var reportedAt: Long = 0
    var createdAt: Long = 0

    override fun encode(): ByteBuf {
        var bufferSize = BASE_PAYLOAD_LENGTH
        callId?.let { bufferSize += it.length + 3 }
        codecName?.let { bufferSize += it.length + 3 }

        return Unpooled.buffer(bufferSize).apply {
            writeTlv(TAG_SOURCE, source)

            writeTlv(TAG_PAYLOAD_TYPE, payloadType)
            writeTlv(TAG_SSRC, ssrc)
            callId?.let { writeTlv(TAG_CALL_ID, it) }
            codecName?.let { writeTlv(TAG_CODEC_NAME, it) }
            writeTlv(TAG_RECORDED, recorded)

            writeTlv(TAG_EXPECTED_PACKET_COUNT, expectedPacketCount)
            writeTlv(TAG_RECEIVED_PACKET_COUNT, receivedPacketCount)
            writeTlv(TAG_LOST_PACKET_COUNT, lostPacketCount)
            writeTlv(TAG_REJECTED_PACKET_COUNT, rejectedPacketCount)

            writeTlv(TAG_DURATION, duration)

            writeTlv(TAG_LAST_JITTER, lastJitter)
            writeTlv(TAG_AVG_JITTER, avgJitter)
            writeTlv(TAG_MIN_JITTER, minJitter)
            writeTlv(TAG_MAX_JITTER, maxJitter)

            writeTlv(TAG_R_FACTOR, rFactor)
            writeTlv(TAG_MOS, mos)
            writeTlv(TAG_FRACTION_LOST, fractionLost)

            writeTlv(TAG_REPORTED_AT, reportedAt)
            writeTlv(TAG_CREATED_AT, createdAt)

            writeTlv(TAG_MARKER_PACKET_COUNT, markerPacketCount)
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
                TAG_SOURCE -> source = buffer.readByte()
                TAG_PAYLOAD_TYPE -> payloadType = buffer.readByte()
                TAG_SSRC -> ssrc = buffer.readLong()
                TAG_CALL_ID -> callId = buffer.readCharSequence(length, Charset.defaultCharset()).toString()
                TAG_CODEC_NAME -> codecName = buffer.readCharSequence(length, Charset.defaultCharset()).toString()
                TAG_RECORDED -> recorded = buffer.readBoolean()
                TAG_EXPECTED_PACKET_COUNT -> expectedPacketCount = buffer.readInt()
                TAG_RECEIVED_PACKET_COUNT -> receivedPacketCount = buffer.readInt()
                TAG_LOST_PACKET_COUNT -> lostPacketCount = buffer.readInt()
                TAG_REJECTED_PACKET_COUNT -> rejectedPacketCount = buffer.readInt()
                TAG_DURATION -> duration = buffer.readInt()
                TAG_LAST_JITTER -> lastJitter = buffer.readFloat()
                TAG_AVG_JITTER -> avgJitter = buffer.readFloat()
                TAG_MIN_JITTER -> minJitter = buffer.readFloat()
                TAG_MAX_JITTER -> maxJitter = buffer.readFloat()
                TAG_R_FACTOR -> rFactor = buffer.readFloat()
                TAG_MOS -> mos = buffer.readFloat()
                TAG_FRACTION_LOST -> fractionLost = buffer.readFloat()
                TAG_REPORTED_AT -> reportedAt = buffer.readLong()
                TAG_CREATED_AT -> createdAt = buffer.readLong()
                TAG_MARKER_PACKET_COUNT -> markerPacketCount = buffer.readInt()
            }
        }
    }
}
