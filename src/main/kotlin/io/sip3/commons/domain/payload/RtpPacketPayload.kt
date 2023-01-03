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

package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled

class RtpPacketPayload : Encodable, Decodable {

    var payloadType: Byte = 0
    var sequenceNumber: Int = 0
    var timestamp: Long = 0
    var ssrc: Long = 0
    var marker: Boolean = false
    var recorded: Boolean = false
    var event: Int = Int.MIN_VALUE

    override fun encode(): ByteBuf {
        return Unpooled.buffer(27).apply {
            writeByte(payloadType.toInt())
            writeInt(sequenceNumber)
            writeLong(timestamp)
            writeLong(ssrc)
            writeBoolean(marker)
            writeBoolean(recorded)
            writeInt(event)
        }
    }

    override fun decode(buffer: ByteBuf) {
        payloadType = buffer.readByte()
        sequenceNumber = buffer.readInt()
        timestamp = buffer.readLong()
        ssrc = buffer.readLong()
        marker = buffer.readBoolean()
        recorded = buffer.readBoolean()
        event = buffer.readInt()
    }
}