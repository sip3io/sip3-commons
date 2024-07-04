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

import io.vertx.core.json.JsonObject
import io.vertx.core.net.JksOptions
import io.vertx.kotlin.core.http.webSocketClientOptionsOf
import io.vertx.kotlin.core.net.netClientOptionsOf
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class NetOptionsUtilTest {

    @Test
    fun `Validate init ssl for NetClientOptions`() {
        val config = JsonObject().apply {
            put("key_store", "key_store")
            put("key_store_password", "iddqd")
        }

        val options = netClientOptionsOf().initSsl(config)
        assertTrue(options.isSsl)
        assertTrue(options.sslOptions.keyCertOptions is JksOptions)
        (options.sslOptions.keyCertOptions as JksOptions).let { jksOptions ->
            assertEquals(config.getString("key_store"), jksOptions.path)
            assertEquals(config.getString("key_store_password"), jksOptions.password)
        }
    }

    @Test
    fun `Validate init ssl for WebSocketClientOptions`() {
        val config = JsonObject().apply {
            put("key_store", "key_store")
            put("key_store_password", "iddqd")
        }

        val options = webSocketClientOptionsOf().initSsl(config)
        assertTrue(options.isSsl)
        assertTrue(options.sslOptions.keyCertOptions is JksOptions)
        (options.sslOptions.keyCertOptions as JksOptions).let { jksOptions ->
            assertEquals(config.getString("key_store"), jksOptions.path)
            assertEquals(config.getString("key_store_password"), jksOptions.password)
        }
    }
}