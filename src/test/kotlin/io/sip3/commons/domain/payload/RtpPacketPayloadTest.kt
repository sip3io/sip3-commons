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

class RtpPacketPayloadTest {

    @Test
    fun `Encode-decode validation`() {

        val payload = RtpPacketPayload().apply {
            payloadType = 1
            sequenceNumber = 2
            ssrc = 3
            timestamp = 4
            marker = true
            event = 128
            recorded = true
        }
        val encoded = payload.encode()
        assertEquals(encoded.writerIndex(), encoded.capacity())
        assertEquals(27, encoded.capacity())

        val decoded = RtpPacketPayload().apply { decode(encoded) }
        payload.apply {
            assertEquals(payloadType, decoded.payloadType)
            assertEquals(sequenceNumber, decoded.sequenceNumber)
            assertEquals(ssrc, decoded.ssrc)
            assertEquals(timestamp, decoded.timestamp)
            assertEquals(marker, decoded.marker)
            assertEquals(event, decoded.event)
            assertEquals(recorded, decoded.recorded)
        }
    }
}