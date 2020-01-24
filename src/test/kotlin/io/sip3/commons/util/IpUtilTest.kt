/*
 * Copyright 2018-2020 SIP3.IO, Inc.
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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.InetAddress

class IpUtilTest {

    @Test
    fun `Convert valid IPv4 address to Int`() {
        val addr = InetAddress.getByName("23.8.20.15")
        assertEquals(386405391, IpUtil.convertToInt(addr.address))
    }

    @Test
    fun `Convert invalid address to Int`() {
        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            val addr = byteArrayOf(0x01, 0x02, 0x03)
            IpUtil.convertToInt(addr)
        }
    }

    @Test
    fun `Convert valid string address to Int`() {
        assertEquals(386405391, IpUtil.convertToInt("23.8.20.15"))
    }

    @Test
    fun `Convert invalid string address to Int`() {
        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            IpUtil.convertToInt("23.8.20")
        }
    }

    @Test
    fun `Convert valid IPV4 address to String`() {
        val addr = InetAddress.getByName("23.8.20.15")
        assertEquals("23.8.20.15", IpUtil.convertToString(addr.address))
    }

    @Test
    fun `Convert invalid address to String`() {
        Assertions.assertThrows(UnsupportedOperationException::class.java) {
            val addr = byteArrayOf(0x01, 0x02, 0x03)
            IpUtil.convertToString(addr)
        }
    }
}