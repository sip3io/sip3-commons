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

package io.sip3.commons.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ResourceUtilKtTest {

    @Test
    fun `Validate 'readByteArray()' method`() {
        val byteArray = readByteArray("/raw/hex-string")
        assertEquals(4, byteArray.size)
        assertArrayEquals(byteArrayOf(0x53, 0x49, 0x50, 0x33), byteArray)
    }

    @Test
    fun `Validate 'readByteBuf()' method`() {
        val byteBuf = readByteBuf("/raw/hex-string")
        assertEquals(4, byteBuf.readableBytes())
        assertArrayEquals(byteArrayOf(0x53, 0x49, 0x50, 0x33), byteBuf.getBytes())

    }
}