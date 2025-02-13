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
import io.netty.buffer.Unpooled
import java.nio.charset.Charset

fun Any.readByteArray(path: String): ByteArray {
    return this.javaClass.getResource(path)!!
        .readText(Charset.defaultCharset())
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun Any.readByteBuf(path: String): ByteBuf {
    return readByteArray(path)
        .let { Unpooled.wrappedBuffer(it) }
}