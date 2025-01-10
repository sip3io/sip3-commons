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

import io.netty.buffer.ByteBuf
import java.nio.charset.Charset

fun ByteBuf.writeTlv(tag: Int, value: Any) {
    when (value) {
        is Byte -> {
            writeByte(tag)
            writeShort(4)
            writeByte(value.toInt())
        }
        is Boolean -> {
            writeByte(tag)
            writeShort(4)
            writeBoolean(value)
        }
        is Short -> {
            writeByte(tag)
            writeShort(5)
            writeShort(value.toInt())
        }
        is Int -> {
            writeByte(tag)
            writeShort(7)
            writeInt(value)
        }
        is Float -> {
            writeByte(tag)
            writeShort(7)
            writeFloat(value)
        }
        is Long -> {
            writeByte(tag)
            writeShort(11)
            writeLong(value)
        }
        is String -> {
            writeByte(tag)
            val bytes = value.toByteArray(Charset.defaultCharset())
            writeShort(3 + bytes.size)
            writeBytes(bytes)
        }
        is ByteArray -> {
            writeByte(tag)
            writeShort(3 + value.size)
            writeBytes(value)
        }
        is ByteBuf -> {
            writeByte(tag)
            writeShort(3 + value.capacity())
            writeBytes(value)
        }
        else -> {
            throw IllegalArgumentException("Type of value $value for tag $tag is not supported")
        }
    }
}

fun ByteBuf.getBytes(index: Int, length: Int): ByteArray {
    return ByteArray(length).apply {
        (0 until length).forEach { i ->
            this[i] = getByte(index + i)
        }
    }
}

fun ByteBuf.getBytes(): ByteArray {
    return getBytes(readerIndex(), readableBytes())
}