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

import io.vertx.core.net.SocketAddress
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SocketAddressUtilTest {

    @Test
    fun `Validate URI creation from SocketAddress`() {
        val ipv4 = SocketAddress.inetSocketAddress(5060, "127.0.0.1")
        assertEquals("tcp://127.0.0.1:5060", ipv4.toURI("tcp").toString())
        assertEquals("ws://127.0.0.1:5060", ipv4.toURI("ws").toString())

        val ipv6 = SocketAddress.inetSocketAddress(5060, "[fe80::1]")
        assertEquals("tcp://[fe80::1]:5060", ipv6.toURI("tcp").toString())
        assertEquals("ws://[fe80::1]:5060", ipv6.toURI("ws").toString())

        val ipv6withTail = SocketAddress.inetSocketAddress(5060, "fe80:0:0:0:0:0:0:1%1")
        assertEquals("tcp://[fe80:0:0:0:0:0:0:1]:5060", ipv6withTail.toURI("tcp").toString())
        assertEquals("tcp://[fe80:0:0:0:0:0:0:1%1]:5060", ipv6withTail.toURI("tcp", false).toString())
    }
}