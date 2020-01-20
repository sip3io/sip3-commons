/*
 * Copyright 2018-2019 SIP3.IO, Inc.
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

import io.sip3.commons.util.remainingCapacity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class RtpReportPayloadTest {

    companion object {

        const val CALL_ID = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com"
        const val CALL_ID_VALUE_LENGTH = CALL_ID.length + 3

        const val CODEC_NAME = "PCMU"
        const val CODEC_NAME_VALUE_LENGTH = CODEC_NAME.length + 3

        private fun getRtpReportPayload(callId: String?, codecName: String?): RtpReportPayload {
            return RtpReportPayload().apply {
                source = RtpReportPayload.SOURCE_RTP
                payloadType = 1
                ssrc = 2
                this.callId = callId
                this.codecName = codecName

                expectedPacketCount = 3
                receivedPacketCount = 4
                lostPacketCount = 5
                rejectedPacketCount = 6

                duration = 7

                lastJitter = 8F
                avgJitter = 9F
                minJitter = 10F
                maxJitter = 11F

                rFactor = 12F
                mos = 13F
                fractionLost = 14F

                createdAt = 1579511172674
                startedAt = 1579522272674
            }
        }
    }

    @Test
    fun `Encode-decode validation`() {
        val rtpReportPayload = getRtpReportPayload(CALL_ID, CODEC_NAME)
        val byteBuf = rtpReportPayload.encode()
        val expectedLength = RtpReportPayload.BASE_PAYLOAD_LENGTH + CALL_ID_VALUE_LENGTH + CODEC_NAME_VALUE_LENGTH
        assertEquals(expectedLength, byteBuf.capacity())

        val decoded = RtpReportPayload().apply { decode(byteBuf) }
        assertEquals(0, byteBuf.remainingCapacity())

        rtpReportPayload.apply {
            assertEquals(source, decoded.source)
            assertEquals(payloadType, decoded.payloadType)
            assertEquals(ssrc, decoded.ssrc)
            assertEquals(callId, decoded.callId)
            assertEquals(codecName, decoded.codecName)

            assertEquals(expectedPacketCount, decoded.expectedPacketCount)
            assertEquals(receivedPacketCount, decoded.receivedPacketCount)
            assertEquals(lostPacketCount, decoded.lostPacketCount)
            assertEquals(rejectedPacketCount, decoded.rejectedPacketCount)

            assertEquals(duration, decoded.duration)

            assertEquals(lastJitter, decoded.lastJitter)
            assertEquals(avgJitter, decoded.avgJitter)
            assertEquals(minJitter, decoded.minJitter)
            assertEquals(maxJitter, decoded.maxJitter)

            assertEquals(rFactor, decoded.rFactor)
            assertEquals(mos, decoded.mos)
            assertEquals(fractionLost, decoded.fractionLost)

            assertEquals(createdAt, decoded.createdAt)
            assertEquals(startedAt, decoded.startedAt)
        }
    }

    @Test
    fun `Encode-decode validation without callId and codecName`() {
        val rtpReportPayload = getRtpReportPayload(null, null)
        val byteBuf = rtpReportPayload.encode()
        assertEquals(RtpReportPayload.BASE_PAYLOAD_LENGTH, byteBuf.capacity())

        val decoded = RtpReportPayload().apply { decode(byteBuf) }
        assertEquals(0, byteBuf.remainingCapacity())

        rtpReportPayload.apply {
            assertEquals(source, decoded.source)
            assertEquals(payloadType, decoded.payloadType)
            assertEquals(ssrc, decoded.ssrc)
            assertNull(decoded.callId)

            assertEquals(expectedPacketCount, decoded.expectedPacketCount)
            assertEquals(receivedPacketCount, decoded.receivedPacketCount)
            assertEquals(lostPacketCount, decoded.lostPacketCount)
            assertEquals(rejectedPacketCount, decoded.rejectedPacketCount)

            assertEquals(duration, decoded.duration)

            assertEquals(lastJitter, decoded.lastJitter)
            assertEquals(avgJitter, decoded.avgJitter)
            assertEquals(minJitter, decoded.minJitter)
            assertEquals(maxJitter, decoded.maxJitter)

            assertEquals(rFactor, decoded.rFactor)
            assertEquals(mos, decoded.mos)
            assertEquals(fractionLost, decoded.fractionLost)

            assertEquals(createdAt, decoded.createdAt)
            assertEquals(startedAt, decoded.startedAt)
        }
    }

    @Test
    fun `Encode RtpReportPayload and validate tag, length and value`() {
        val rtpReportPayload = getRtpReportPayload(CALL_ID, CODEC_NAME)
        val byteBuf = rtpReportPayload.encode()
        val expectedLength = RtpReportPayload.BASE_PAYLOAD_LENGTH + CALL_ID_VALUE_LENGTH + CODEC_NAME_VALUE_LENGTH
        assertEquals(expectedLength, byteBuf.capacity())

        rtpReportPayload.apply {
            assertEquals(RtpReportPayload.TAG_SOURCE, byteBuf.readByte().toInt())
            assertEquals(4, byteBuf.readShort())
            assertEquals(source, byteBuf.readByte())

            assertEquals(RtpReportPayload.TAG_PAYLOAD_TYPE, byteBuf.readByte().toInt())
            assertEquals(4, byteBuf.readShort())
            assertEquals(payloadType, byteBuf.readByte())

            assertEquals(RtpReportPayload.TAG_SSRC, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(ssrc, byteBuf.readLong())

            assertEquals(RtpReportPayload.TAG_CALL_ID, byteBuf.readByte().toInt())
            assertEquals(CALL_ID_VALUE_LENGTH, byteBuf.readShort().toInt())
            val callIdBytes = ByteArray(CALL_ID.length)
            byteBuf.readBytes(callIdBytes)
            val decodedCallId = callIdBytes.toString(Charset.defaultCharset())
            assertEquals(callId, decodedCallId)

            assertEquals(RtpReportPayload.TAG_CODEC_NAME, byteBuf.readByte().toInt())
            assertEquals(CODEC_NAME_VALUE_LENGTH, byteBuf.readShort().toInt())
            val codecNameBytes = ByteArray(CODEC_NAME.length)
            byteBuf.readBytes(codecNameBytes)
            val decodedCodecName = codecNameBytes.toString(Charset.defaultCharset())
            assertEquals(codecName, decodedCodecName)

            assertEquals(RtpReportPayload.TAG_CUMULATIVE, byteBuf.readByte().toInt())
            assertEquals(4, byteBuf.readShort())
            assertEquals(cumulative, byteBuf.readBoolean())

            assertEquals(RtpReportPayload.TAG_EXPECTED_PACKET_COUNT, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(expectedPacketCount, byteBuf.readInt())

            assertEquals(RtpReportPayload.TAG_RECEIVED_PACKET_COUNT, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(receivedPacketCount, byteBuf.readInt())

            assertEquals(RtpReportPayload.TAG_LOST_PACKET_COUNT, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(lostPacketCount, byteBuf.readInt())

            assertEquals(RtpReportPayload.TAG_REJECTED_PACKET_COUNT, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(rejectedPacketCount, byteBuf.readInt())

            assertEquals(RtpReportPayload.TAG_DURATION, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(duration, byteBuf.readInt())

            assertEquals(RtpReportPayload.TAG_LAST_JITTER, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(lastJitter, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_AVG_JITTER, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(avgJitter, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_MIN_JITTER, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(minJitter, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_MAX_JITTER, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(maxJitter, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_R_FACTOR, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(rFactor, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_MOS, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(mos, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_FRACTION_LOST, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort())
            assertEquals(fractionLost, byteBuf.readFloat())

            assertEquals(RtpReportPayload.TAG_CREATED_AT, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(createdAt, byteBuf.readLong())

            assertEquals(RtpReportPayload.TAG_STARTED_AT, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(startedAt, byteBuf.readLong())
        }
        assertEquals(0, byteBuf.remainingCapacity())
    }
}