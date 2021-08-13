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

import io.netty.buffer.Unpooled
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ByteBufPayloadTest {

    @Test
    fun `Encode-decode validation`() {
        val buffer = Unpooled.wrappedBuffer(byteArrayOf(0, 1, 2, 3))
        val payload = ByteBufPayload(buffer)
        val encoded = payload.encode()
        assertEquals(buffer.capacity(), encoded.capacity())

        assertEquals(buffer, encoded)
    }
}
