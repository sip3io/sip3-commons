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

package io.sip3.commons.vertx.util

import io.sip3.commons.vertx.test.VertxTest
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonObjectUtilTest : VertxTest() {

    @Test
    fun `Check 'toSnakeCase()' method`() {
        val jsonObject = JsonObject(
            """
                {
                  "z-1": true,
                  "z_2": "string",
                  "z_3": 123,
                  "a_1": {
                    "a-1-1": {
                      "a_1_1_1": "string-1_1"
                    }
                  },
                  "b-1": {
                    "b-1-1": {
                      "b_1-1_1": "string-1_2"
                    }
                  },
                  "c-1": {
                    "c_1_1": [
                      {"c-1-1-1": "some-value_1"},
                      {"c-1-1-2": "some-value_2"},
                      {
                        "c-1-1-3": "some-value_3",
                        "c-1-1-4": {
                          "c-1-1-4_1": 1,
                          "c-1-1-4_2": "2"
                        }
                      }
                    ]
                  },
                  "d-1": {
                    "d-1-1": "value1",
                    "d-1_1": "value2",
                    "d_1-1": "value3",
                    "d_1_1": "value4"
                  }
                }
            """.trimIndent()
        )

        val inSnakeCase = jsonObject.toSnakeCase()
        assertEquals(7, inSnakeCase.fieldNames().size)

        assertEquals(true, inSnakeCase.getBoolean("z_1"))
        assertEquals("string", inSnakeCase.getString("z_2"))
        assertEquals(123, inSnakeCase.getInteger("z_3"))

        assertTrue(inSnakeCase.getValue("a_1") is JsonObject)
        assertTrue(inSnakeCase.getValue("b_1") is JsonObject)
        assertFalse(inSnakeCase.containsKey("b-1"))

        assertEquals(1, inSnakeCase.getJsonObject("c_1")
            .getJsonArray("c_1_1")
            .getJsonObject(2)
            .getJsonObject("c_1_1_4")
            .getInteger("c_1_1_4_1")
        )

        assertEquals(1, inSnakeCase.getJsonObject("d_1").fieldNames().size)
        assertEquals("value4", inSnakeCase.getJsonObject("d_1").getString("d_1_1"))
    }
}