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

import io.sip3.commons.PacketTypes
import io.sip3.commons.domain.media.Recording
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordingPayloadTest {

    companion object {

        val RECORDING = RecordingPayload().apply {
            type = PacketTypes.RTP
            mode = Recording.GDPR
            callId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com"
            payload = byteArrayOf(
                0x80.toByte(), 0x08.toByte(), 0x01.toByte(), 0xdd.toByte(), 0x2b.toByte(), 0x76.toByte(), 0x37.toByte(),
                0x40.toByte(), 0x95.toByte(), 0x06.toByte(), 0xb9.toByte(), 0x73.toByte()
            )
        }
    }

    @Test
    fun `Encode-decode validation`() {
        val encoded = RECORDING.encode()
        assertEquals(74, encoded.capacity());

        val decoded = RecordingPayload().apply {
            decode(encoded)
        }

        RECORDING.apply {
            assertEquals(type, decoded.type)
            assertEquals(mode, decoded.mode)
            assertEquals(callId, decoded.callId)
            assertArrayEquals(payload, decoded.payload)
        }
    }
}