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

package io.sip3.commons.domain

import io.vertx.core.json.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SdpSessionTest {

    @Test
    fun `Serialization to JSON`() {
        val sdpSession = SdpSession().apply {
            id = 1000L
            timestamp = System.currentTimeMillis()

            codec = Codec().apply {
                payloadType = 0
                name = "PCMU"
                clockRate = 8000
                ie = 0F
                bpl = 4.3F
            }
            ptime = 30

            callId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com"
        }

        JsonObject.mapFrom(sdpSession).apply {
            assertEquals(5, size())
            assertEquals(sdpSession.id, getLong("id"))
            assertEquals(sdpSession.timestamp, getLong("timestamp"))
            assertEquals(sdpSession.callId, getString("call_id"))
            assertEquals(sdpSession.ptime, getInteger("ptime"))

            val codec = sdpSession.codec
            getJsonObject("codec").apply {
                assertEquals(codec.payloadType, getInteger("payload_type").toByte())
                assertEquals(codec.name, getString("name"))

                assertEquals(codec.clockRate, getInteger("clock_rate"))
                assertEquals(codec.ie, getFloat("ie"))
                assertEquals(codec.bpl, getFloat("bpl"))
            }
        }
    }

    @Test
    fun `Deserialization from JSON`() {
        val jsonObject = JsonObject().apply {
            put("id", 1000L)
            put("timestamp", System.currentTimeMillis())

            put("codec", JsonObject().apply {
                put("payload_type", 0)
                put("name", "PCMU")
                put("clock_rate", 8000)
                put("ie", 0F)
                put("bpl", 4.3F)
            })
            put("ptime", 30)

            put("call_id", "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com")
        }

        jsonObject.mapTo(SdpSession::class.java).apply {
            assertEquals(jsonObject.getLong("id"), id)
            assertEquals(jsonObject.getLong("timestamp"), timestamp)

            jsonObject.getJsonObject("codec").apply {
                assertEquals(getInteger("payload_type").toByte(), codec.payloadType)
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