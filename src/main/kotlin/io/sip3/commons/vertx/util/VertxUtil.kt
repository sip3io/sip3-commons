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

package io.sip3.commons.vertx.util

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import kotlin.system.exitProcess

fun Vertx.registerLocalCodec() {
    eventBus().unregisterCodec("local")
    eventBus().registerCodec(object : MessageCodec<Any, Any> {
        override fun decodeFromWire(pos: Int, buffer: Buffer?) = throw NotImplementedError()
        override fun encodeToWire(buffer: Buffer?, s: Any?) = throw NotImplementedError()
        override fun transform(s: Any?) = s
        override fun name() = "local"
        override fun systemCodecID(): Byte = -1
    })
}

fun Vertx.closeAndExitProcess(code: Int = -1) {
    close { exitProcess(code) }
}