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

import io.sip3.commons.vertx.test.VertxTest
import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonObjectUtilTest : VertxTest() {

    private val JSON_OBJECT = JsonObject(
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
                      {
                        "c_1_1_1": "some-value_1",
                        "c_1_1_2": {
                          "c-1-1-2_1": 1,
                          "c-1-1-2_2": "2"
                        }
                      },
                      {"c_1_1_3": "some-value_3"},
                      {"c_1_1_4": "some-value_4"}
                    ]
                  },
                  "d-1": {
                    "d-1-1": "value1",
                    "d-1_1": "value2",
                    "d_1-1": "value3",
                    "d_1_1": "value4"
                  },
                  "e_1": {
                    "e_1_1_1": [
                      { 
                        "e_1_1_1": {
                          "e_1_1_1_1": "value"
                         }
                      },
                      {"e_1_1_2": "some-value_2"}
                    ]
                  }
                }
            """.trimIndent()
    )

    @Test
    fun `Check 'containsKebabCase()' method`() {
        assertTrue(JSON_OBJECT.containsKebabCase())
        assertTrue(JSON_OBJECT.getJsonObject("a_1").containsKebabCase())
        assertTrue(JSON_OBJECT.getJsonObject("b-1").containsKebabCase())
        assertTrue(JSON_OBJECT.getJsonObject("c-1").containsKebabCase())
        assertTrue(JSON_OBJECT.getJsonObject("d-1").containsKebabCase())

        assertFalse(JSON_OBJECT.getJsonObject("a_1").getJsonObject("a-1-1").containsKebabCase())
        assertFalse(JSON_OBJECT.getJsonObject("e_1").containsKebabCase())
    }

    @Test
    fun `Check 'toSnakeCase()' method`() {
        val inSnakeCase = JSON_OBJECT.toSnakeCase()
        assertEquals(8, inSnakeCase.fieldNames().size)

        assertEquals(true, inSnakeCase.getBoolean("z_1"))
        assertEquals("string", inSnakeCase.getString("z_2"))
        assertEquals(123, inSnakeCase.getInteger("z_3"))

        assertTrue(inSnakeCase.getValue("a_1") is JsonObject)
        assertTrue(inSnakeCase.getValue("b_1") is JsonObject)
        assertFalse(inSnakeCase.containsKey("b-1"))

        assertEquals(1, inSnakeCase.getJsonObject("c_1")
            .getJsonArray("c_1_1")
            .getJsonObject(0)
            .getJsonObject("c_1_1_2")
            .getInteger("c_1_1_2_1")
        )

        assertEquals(1, inSnakeCase.getJsonObject("d_1").fieldNames().size)
        assertEquals("value4", inSnakeCase.getJsonObject("d_1").getString("d_1_1"))
        assertEquals(JSON_OBJECT.getJsonObject("e_1"), inSnakeCase.getJsonObject("e_1"))
    }
}