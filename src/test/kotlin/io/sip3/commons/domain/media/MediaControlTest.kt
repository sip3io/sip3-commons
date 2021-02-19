/*
 * Copyright 2018-2021 SIP3.IO, Inc.
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

import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MediaControlTest {

    @Test
    fun `Serialization to JSON`() {
        val sdpSession = MediaControl().apply {
            timestamp = System.currentTimeMillis()

            address = "127.0.0.1"
            rtpPort = 1000
            rtcpPort = 1001

            codecs = mutableListOf(Codec().apply {
                payloadTypes = listOf(0)
                name = "PCMU"
                clockRate = 8000
                ie = 0F
                bpl = 4.3F
            })
            ptime = 30

            callId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com"
        }

        JsonObject.mapFrom(sdpSession).apply {
            assertEquals(7, size())
            assertEquals(sdpSession.timestamp, getLong("timestamp"))

            assertEquals(sdpSession.address, getString("address"))
            assertEquals(sdpSession.rtpPort, getInteger("rtp_port"))
            assertEquals(sdpSession.rtcpPort, getInteger("rtcp_port"))

            val codec = sdpSession.codecs.first()
            getJsonArray("codecs").getJsonObject(0).apply {
                assertEquals(codec.name, getString("name"))

                assertEquals(codec.payloadTypes.size, getJsonArray("payload_types").size())
                assertEquals(codec.payloadTypes.first(), getJsonArray("payload_types").getInteger(0))

                assertEquals(codec.clockRate, getInteger("clock_rate"))
                assertEquals(codec.ie, getFloat("ie"))
                assertEquals(codec.bpl, getFloat("bpl"))
            }

            assertEquals(sdpSession.ptime, getInteger("ptime"))
            assertEquals(sdpSession.callId, getString("call_id"))
        }
    }

    @Test
    fun `Deserialization from JSON`() {
        val jsonObject = JsonObject().apply {
            put("timestamp", System.currentTimeMillis())

            put("codecs", listOf(JsonObject().apply {
                put("payload_types", listOf(0))
                put("name", "PCMU")
                put("clock_rate", 8000)
                put("ie", 0F)
                put("bpl", 4.3F)
            }))
            put("ptime", 30)

            put("call_id", "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com")
        }

        jsonObject.mapTo(MediaControl::class.java).apply {
            assertEquals(jsonObject.getLong("timestamp"), timestamp)

            jsonObject.getJsonArray("codecs").getJsonObject(0).apply {
                val codec = codecs.first()
                assertEquals(getJsonArray("payload_types").getInteger(0), codec.payloadTypes.first())
                assertEquals(getString("name"), codec.name)
                assertEquals(getInteger("clock_rate"), codec.clockRate)
                assertEquals(getFloat("ie"), codec.ie)
                assertEquals(getFloat("bpl"), codec.bpl)
            }
            assertEquals(jsonObject.getInteger("ptime"), ptime)

            assertEquals(jsonObject.getString("call_id"), callId)
        }
    }
}