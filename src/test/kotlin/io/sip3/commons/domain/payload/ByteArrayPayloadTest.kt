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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ByteArrayPayloadTest {

    @Test
    fun `Encode-decode validation`() {
        val bytes = byteArrayOf(0, 1, 2, 3)
        val payload = ByteArrayPayload(bytes)
        val encoded = payload.encode()
        assertEquals(encoded.writerIndex(), encoded.capacity());
        assertEquals(payload.bytes.size, encoded.capacity())

        assertArrayEquals(bytes, encoded.array())
    }
}
