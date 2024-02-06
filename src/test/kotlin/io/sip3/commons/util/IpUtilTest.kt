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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IpUtilTest {

    @Test
    fun `Convert valid IPv4 address to String`() {
        val addr = byteArrayOf(
            0x17.toByte(), 0x08.toByte(), 0x14.toByte(), 0x0f.toByte()
        )
        assertEquals("23.8.20.15", IpUtil.convertToString(addr))
    }

    @Test
    fun `Convert valid IPv6 address to String`() {
        val addr1 = byteArrayOf(
            0xfe.toByte(), 0x80.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x4e.toByte(), 0x96.toByte(), 0x14.toByte(), 0x01.toByte(), 0xf4.toByte(), 0xa7.toByte(),
            0x7f.toByte(), 0xc0.toByte()
        )
        assertEquals("fe80::4e96:1401:f4a7:7fc0", IpUtil.convertToString(addr1))

        val addr2 = byteArrayOf(
            0x96.toByte(), 0x14.toByte(), 0x01.toByte(), 0xf4.toByte(), 0xa7.toByte(), 0x7f.toByte(), 0xc0.toByte(),
            0xfe.toByte(), 0x80.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x4e.toByte()
        )
        assertEquals("9614:1f4:a77f:c0fe:8000::4e", IpUtil.convertToString(addr2))

        val addr3 = byteArrayOf(
            0x96.toByte(), 0x14.toByte(), 0x00.toByte(), 0x00.toByte(), 0xc0.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x80.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x4e.toByte()
        )
        assertEquals("9614:0:c000:0:8000::4e", IpUtil.convertToString(addr3))

        val addr4 = byteArrayOf(
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x00.toByte(), 0x00.toByte()
        )
        assertEquals("0:1::1:0:1:0", IpUtil.convertToString(addr4))

        val addr5 = byteArrayOf(
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x01.toByte()
        )
        assertEquals("::1", IpUtil.convertToString(addr5))

        val addr6 = byteArrayOf(
            0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte()
        )
        assertEquals("1::", IpUtil.convertToString(addr6))

        val addr7 = byteArrayOf(
            0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(),
            0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x00.toByte(), 0x01.toByte()
        )
        assertEquals("1:1:1:1:1:1:1:1", IpUtil.convertToString(addr7))
    }
}