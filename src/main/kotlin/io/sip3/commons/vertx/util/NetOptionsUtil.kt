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

package io.sip3.commons.vertx.util

import io.vertx.core.json.JsonObject
import io.vertx.core.net.JksOptions
import io.vertx.core.net.TCPSSLOptions

fun TCPSSLOptions.initSsl(config: JsonObject): TCPSSLOptions {
    isSsl = true
    keyCertOptions = JksOptions().apply {
        path = config.getString("key_store")
        password = config.getString("key_store_password")
    }

    return this
}