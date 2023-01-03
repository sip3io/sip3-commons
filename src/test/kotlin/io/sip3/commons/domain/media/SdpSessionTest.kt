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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SdpSessionTest {

    @Test
    fun `Validate codec by payload type method`() {
        val first = Codec().apply {
            name = "FIRST"
            payloadTypes = listOf(0,2,3,4)
        }
        val second = Codec().apply {
            name = "SECOND"
            payloadTypes = listOf(1,2,3,4)
        }
        val sdpSession = SdpSession().apply {
            codecs = listOf(first, second)
        }

        assertEquals(sdpSession.codec(0), first)
        assertEquals(sdpSession.codec(1), second)
        assertEquals(sdpSession.codec(3), first)
    }
}