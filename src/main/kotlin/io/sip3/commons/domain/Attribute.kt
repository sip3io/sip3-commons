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

package io.sip3.commons.domain

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "Attribute")
class Attribute {

    companion object {

        const val TYPE_STRING = "string"
        const val TYPE_NUMBER = "number"
        const val TYPE_BOOLEAN = "boolean"
    }

    @Schema(
        required = true,
        title = "Name",
        example = "sip.caller"
    )
    lateinit var name: String

    @Schema(
        required = true,
        title = "Type",
        allowableValues = [TYPE_STRING, TYPE_NUMBER, TYPE_BOOLEAN],
        example = TYPE_STRING
    )
    lateinit var type: String

    @Schema(
        required = false,
        title = "Options",
        example = "[\"1001\",\"1002\"]"
    )
    var options: MutableSet<String>? = null
}
