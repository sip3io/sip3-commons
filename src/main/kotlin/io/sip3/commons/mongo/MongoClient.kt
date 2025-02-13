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

package io.sip3.commons.mongo

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient

object MongoClient {

    fun createShared(vertx: Vertx, config: JsonObject): MongoClient {
        // Let's do properties remapping because Vert.x MongoDB client options are such a disaster in terms of grouping and naming conventions.
        val uri = config.getString("uri") ?: throw IllegalArgumentException("uri")
        val db = config.getString("db") ?: throw IllegalArgumentException("db")

        return MongoClient.createShared(vertx, JsonObject().apply {
            // URI and database
            put("connection_string", uri)
            put("db_name", db)

            // Always use object_id
            put("useObjectId", config.getBoolean("use_object_id") ?: true)

            // Auth
            config.getJsonObject("auth")?.let { auth ->
                auth.getString("user")?.let {
                    put("username", it)
                }
                auth.getString("source")?.let {
                    put("authSource", it)
                }
                auth.getString("password")?.let {
                    put("password", it)
                }
            }

            // SSL
            config.getJsonObject("ssl")?.let { ssl ->
                ssl.getBoolean("enabled")?.let {
                    put("ssl", it)
                }
                ssl.getString("ca_path")?.let {
                    put("caPath", it)
                }
                ssl.getString("cert_path")?.let {
                    put("certPath", it)
                }
                ssl.getString("key_path")?.let {
                    put("keyPath", it)
                }
                ssl.getBoolean("trust_all")?.let {
                    put("trustAll", it)
                }
            }
        }, "$uri:$db")
    }
}