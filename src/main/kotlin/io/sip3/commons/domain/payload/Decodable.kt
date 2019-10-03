package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf

interface Decodable : Payload {

    fun decode(buffer: ByteBuf)
}