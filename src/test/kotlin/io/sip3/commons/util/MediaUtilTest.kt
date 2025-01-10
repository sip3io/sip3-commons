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

package io.sip3.commons.util

import io.sip3.commons.util.MediaUtil.rtpStreamId
import io.sip3.commons.util.MediaUtil.sdpSessionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaUtilTest {

    @Test
    fun `RTP Stream Id`() {
        assertEquals(2814835666452481000, rtpStreamId(10000, 20000, 1000L))
    }

    @Test
    fun `Session Id for IPv4 and IPv6`() {
        assertEquals("127.0.0.1:10000", sdpSessionId(byteArrayOf(0x7F.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte()), 10000))
        assertEquals("127.0.0.1:10000", sdpSessionId("127.0.0.1", 10000))
        assertEquals("fe80::4e96:1401:f4a7:7fc0:20000", sdpSessionId(byteArrayOf(
            0xfe.toByte(), 0x80.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x4e.toByte(), 0x96.toByte(), 0x14.toByte(), 0x01.toByte(), 0xf4.toByte(), 0xa7.toByte(),
            0x7f.toByte(), 0xc0.toByte()
        ), 20000))
        assertEquals("fe80::4e96:1401:f4a7:7fc0:20000", sdpSessionId("fe80::4e96:1401:f4a7:7fc0", 20000))
    }
}