/*
 * Copyright 2018-2023 SIP3.IO, Corp.
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

package io.sip3.commons.domain.media

import io.sip3.commons.util.MediaUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaAddressTest {

    @Test
    fun `Validate Ids for rtp and rtcp`() {
        val mediaAddress = MediaAddress().apply {
            addr = "127.0.0.1"
            rtpPort = 1000
            rtcpPort = 1001
        }

        assertEquals(MediaUtil.sdpSessionId(mediaAddress.addr, mediaAddress.rtpPort), mediaAddress.rtpId)
        assertEquals(MediaUtil.sdpSessionId(mediaAddress.addr, mediaAddress.rtcpPort), mediaAddress.rtcpId)
    }
}