/*
 * Copyright 2018-2020 SIP3.IO, Inc.
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
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigUtilTest {

    @Test
    fun `Check 'contains()' method`() {
        val config = JsonObject().apply {
            put("level1", JsonObject().apply {
                put("level2", true)
            })
        }

        assertTrue(ConfigUtil.contains(config, "level1"))
        assertTrue(ConfigUtil.contains(config, "level1.level2"))

        assertFalse(ConfigUtil.contains(config, "levelX"))
        assertFalse(ConfigUtil.contains(config, "level1.level2.level3"))
    }
}