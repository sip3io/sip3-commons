/*
 * Copyright 2018-2025 SIP3.IO, Corp.
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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class RtpEventPayloadTest {

    companion object {

        val RTP_EVENT_PAYLOAD = RtpEventPayload().apply {
            payloadType = 0
            ssrc = 1L
            callId = "call-id"
            samplingRate = 8000

            createdAt = System.currentTimeMillis()
            terminatedAt = createdAt + 1000
            event = 1
            volume = 5
            duration = 1000
        }
        val CALL_ID_VALUE_LENGTH = RTP_EVENT_PAYLOAD.callId!!.length + 3
        val EXPECTED_LENGTH = RtpEventPayload.BASE_PAYLOAD_LENGTH + CALL_ID_VALUE_LENGTH
    }

    @Test
    fun `Encode-decode validation`() {
        val byteBuf = RTP_EVENT_PAYLOAD.encode()
        assertEquals(byteBuf.writerIndex(), byteBuf.capacity())
        assertEquals(EXPECTED_LENGTH, byteBuf.capacity())

        val decoded = RtpEventPayload().apply { decode(byteBuf) }
        assertEquals(0, byteBuf.readableBytes())

        RTP_EVENT_PAYLOAD.apply {
            assertEquals(payloadType, decoded.payloadType)
            assertEquals(ssrc, decoded.ssrc)
            assertEquals(callId, decoded.callId)
            assertEquals(samplingRate, decoded.samplingRate)
            assertEquals(createdAt, decoded.createdAt)
            assertEquals(terminatedAt, decoded.terminatedAt)
            assertEquals(event, decoded.event)
            assertEquals(volume, decoded.volume)
            assertEquals(duration, decoded.duration)
        }
    }

    @Test
    fun `Encode RtpEventPayload and validate tag, length and value`() {
        val byteBuf = RTP_EVENT_PAYLOAD.encode()
        assertEquals(byteBuf.writerIndex(), byteBuf.capacity())
        assertEquals(EXPECTED_LENGTH, byteBuf.capacity())

        RTP_EVENT_PAYLOAD.apply {
            assertEquals(RtpEventPayload.TAG_PAYLOAD_TYPE, byteBuf.readByte().toInt())
            assertEquals(4, byteBuf.readShort())
            assertEquals(payloadType, byteBuf.readByte())

            assertEquals(RtpEventPayload.TAG_SSRC, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(ssrc, byteBuf.readLong())

            assertEquals(RtpEventPayload.TAG_CALL_ID, byteBuf.readByte().toInt())
            assertEquals(CALL_ID_VALUE_LENGTH, byteBuf.readShort().toInt())
            val callIdBytes = ByteArray(RTP_EVENT_PAYLOAD.callId!!.length)
            byteBuf.readBytes(callIdBytes)
            val decodedCallId = callIdBytes.toString(Charset.defaultCharset())
            assertEquals(callId, decodedCallId)

            assertEquals(RtpEventPayload.TAG_SAMPLING_RATE, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort().toInt())
            assertEquals(samplingRate, byteBuf.readInt())

            assertEquals(RtpEventPayload.TAG_CREATED_AT, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(createdAt, byteBuf.readLong())

            assertEquals(RtpEventPayload.TAG_TERMINATED_AT, byteBuf.readByte().toInt())
            assertEquals(11, byteBuf.readShort())
            assertEquals(terminatedAt, byteBuf.readLong())

            assertEquals(RtpEventPayload.TAG_EVENT, byteBuf.readByte().toInt())
            assertEquals(4, byteBuf.readShort())
            assertEquals(event, byteBuf.readByte())

            assertEquals(RtpEventPayload.TAG_VOLUME, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort().toInt())
            assertEquals(volume, byteBuf.readInt())

            assertEquals(RtpEventPayload.TAG_DURATION, byteBuf.readByte().toInt())
            assertEquals(7, byteBuf.readShort().toInt())
            assertEquals(duration, byteBuf.readInt())
        }
        assertEquals(0, byteBuf.readableBytes())
    }
}