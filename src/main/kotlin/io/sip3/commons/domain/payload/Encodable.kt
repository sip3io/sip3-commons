package io.sip3.commons.domain.payload

import io.netty.buffer.ByteBuf

interface Encodable : Payload {

    fun encode(): ByteBuf
}