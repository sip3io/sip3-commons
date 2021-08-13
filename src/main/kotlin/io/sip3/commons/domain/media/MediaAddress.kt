/*
 * Copyright 2018-2021 SIP3.IO, Corp.
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

package io.sip3.commons.domain.media

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import io.sip3.commons.util.MediaUtil

class MediaAddress {

    lateinit var addr: String

    @JsonProperty("rtp_port")
    var rtpPort: Int = 0

    @JsonProperty("rtcp_port")
    var rtcpPort: Int = 0

    @get:JsonIgnore
    val rtpId by lazy {
        MediaUtil.sdpSessionId(addr, rtpPort)
    }

    @get:JsonIgnore
    val rtcpId by lazy {
        MediaUtil.sdpSessionId(addr, rtcpPort)
    }
}