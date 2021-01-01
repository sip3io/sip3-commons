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

package io.sip3.commons.util

import io.sip3.commons.util.MediaUtil.rtpSessionId
import io.sip3.commons.util.MediaUtil.sdpSessionId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class MediaUtilTest {

    @Test
    fun `RTP Session Id`() {
        assertEquals(2814835666452481000, rtpSessionId(10000, 20000, 1000L))
    }

    @Test
    fun `Session Id for IPv4`() {
        assertEquals(9151314447111825168, sdpSessionId("127.0.0.1", 10000))
        assertEquals(-4564387184274026976, sdpSessionId("192.168.10.10", 20000))
    }

    @Test
    fun `Session Id for IPv6`() {
        assertThrows(IllegalArgumentException::class.java) { sdpSessionId("fe80::42:acff:fe12:5", 10000) }
    }
}