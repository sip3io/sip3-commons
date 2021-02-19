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

package io.sip3.commons

object PacketTypes {

    // Real-Time Transport Protocol
    const val RTCP: Byte = 1

    // Real-time Transport Control Protocol
    const val RTP: Byte = 2

    // Session Initiation Protocol
    const val SIP: Byte = 3

    // Internet Control Message Protocol
    const val ICMP: Byte = 4

    // Real-Time Transport Protocol Report (Internal SIP3 protocol)
    const val RTPR: Byte = 5

    // Short Message Peer-to-Peer
    const val SMPP: Byte = 6

    // Recording (Internal SIP3 protocol)
    const val REC: Byte = 7
}