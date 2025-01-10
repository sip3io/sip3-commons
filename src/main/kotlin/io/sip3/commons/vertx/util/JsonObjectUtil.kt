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

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun JsonObject.containsKebabCase(): Boolean {
    forEach { (k, v) ->
        if (k.contains("-")) {
            return true
        }

        when (v) {
            is JsonObject -> if (v.containsKebabCase()) return true
            is JsonArray -> if (v.containsKebabCase()) return true
        }
    }
    return false
}

private fun JsonArray.containsKebabCase(): Boolean {
    forEach { v ->
        when(v) {
            is JsonObject -> return v.containsKebabCase()
            is JsonArray -> return v.containsKebabCase()
        }
    }

    return false
}

fun JsonObject.toSnakeCase(): JsonObject {
    return JsonObject().apply {

        this@toSnakeCase.forEach { (k, v) ->
            val convertedKey = k.replace("-", "_")
            if (convertedKey != k && this@toSnakeCase.containsKey(convertedKey)) {
                return@forEach
            }

            val convertedValue = when (v) {
                is JsonObject -> v.toSnakeCase()
                is JsonArray -> v.toSnakeCase()
                else -> v
            }

            put(convertedKey, convertedValue)
        }
    }
}

private fun JsonArray.toSnakeCase(): JsonArray {
    return JsonArray().apply {
        this@toSnakeCase.forEach { v ->
            when (v) {
                is JsonObject -> add(v.toSnakeCase())
                is JsonArray -> add(v.toSnakeCase())
                else -> add(v)
            }
        }
    }
}
