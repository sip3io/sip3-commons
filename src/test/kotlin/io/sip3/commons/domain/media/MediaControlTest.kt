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
        val mediaControl = MediaControl().apply {
            timestamp = System.currentTimeMillis()

            callId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com"

            sdpSession = SdpSession().apply {
                src = MediaAddress().apply {
                    addr = "127.0.0.1"
                    rtpPort = 1000
                    rtcpPort = 1001
                }

                dst = MediaAddress().apply {
                    addr = "127.0.0.2"
                    rtpPort = 2000
                    rtcpPort = 2001
                }

                codecs = mutableListOf(Codec().apply {
                    payloadTypes = listOf(0)
                    name = "PCMU"
                    clockRate = 8000
                    ie = 0F
                    bpl = 4.3F
                })

                ptime = 30
            }

            recording = Recording()
        }

        JsonObject.mapFrom(mediaControl).apply {
            assertEquals(4, size())
            assertEquals(mediaControl.timestamp, getLong("timestamp"))

            assertEquals(mediaControl.callId, getString("call_id"))

            getJsonObject("sdp_session").apply {
                val srcJsonObject = getJsonObject("src")
                val src = mediaControl.sdpSession.src
                assertEquals(src.addr, srcJsonObject.getString("addr"))
                assertEquals(src.rtpPort, srcJsonObject.getInteger("rtp_port"))
                assertEquals(src.rtcpPort, srcJsonObject.getInteger("rtcp_port"))

                val dstJsonObject = getJsonObject("dst")
                val dst = mediaControl.sdpSession.dst
                assertEquals(dst.addr, dstJsonObject.getString("addr"))
                assertEquals(dst.rtpPort, dstJsonObject.getInteger("rtp_port"))
                assertEquals(dst.rtcpPort, dstJsonObject.getInteger("rtcp_port"))

                val codec = mediaControl.sdpSession.codecs.first()
                getJsonArray("codecs").getJsonObject(0).apply {
                    assertEquals(codec.name, getString("name"))

                    assertEquals(codec.payloadTypes.size, getJsonArray("payload_types").size())
                    assertEquals(codec.payloadTypes.first(), getJsonArray("payload_types").getInteger(0))

                    assertEquals(codec.clockRate, getInteger("clock_rate"))
                    assertEquals(codec.ie, getFloat("ie"))
                    assertEquals(codec.bpl, getFloat("bpl"))
                }

                assertEquals(mediaControl.sdpSession.ptime, getInteger("ptime"))
            }

            assertEquals(mediaControl.recording?.mode, getJsonObject("recording").getInteger("mode").toByte())
        }
    }

    @Test
    fun `Deserialization from JSON`() {
        val jsonObject = JsonObject().apply {
            put("timestamp", System.currentTimeMillis())

            put("call_id", "f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com")

            put("sdp_session", JsonObject().apply {
                put("src", JsonObject().apply {
                    put("addr", "127.0.0.1")
                    put("rtp_port", 1000)
                    put("rtcp_port", 1001)
                })
                put("dst", JsonObject().apply {
                    put("addr", "127.0.0.2")
                    put("rtp_port", 2000)
                    put("rtcp_port", 2001)
                })

                put("codecs", listOf(JsonObject().apply {
                    put("payload_types", listOf(0))
                    put("name", "PCMU")
                    put("clock_rate", 8000)
                    put("ie", 0F)
                    put("bpl", 4.3F)
                }))

                put("ptime", 30)
            })

            put("recording", JsonObject().apply {
                put("mode", 1)
            })
        }

        jsonObject.mapTo(MediaControl::class.java).apply {
            assertEquals(jsonObject.getLong("timestamp"), timestamp)

            assertEquals(jsonObject.getString("call_id"), callId)

            val sdpSessionJson = jsonObject.getJsonObject("sdp_session")

            val src = sdpSessionJson.getJsonObject("src")
            assertEquals(src.getString("addr"), sdpSession.src.addr)
            assertEquals(src.getInteger("rtp_port"), sdpSession.src.rtpPort)
            assertEquals(src.getInteger("rtcp_port"), sdpSession.src.rtcpPort)

            val dst = sdpSessionJson.getJsonObject("dst")
            assertEquals(dst.getString("addr"), sdpSession.dst.addr)
            assertEquals(dst.getInteger("rtp_port"), sdpSession.dst.rtpPort)
            assertEquals(dst.getInteger("rtcp_port"), sdpSession.dst.rtcpPort)

            sdpSessionJson.getJsonArray("codecs").getJsonObject(0).apply {
                val codec = sdpSession.codecs.first()
                assertEquals(getJsonArray("payload_types").getInteger(0), codec.payloadTypes.first())
                assertEquals(getString("name"), codec.name)
                assertEquals(getInteger("clock_rate"), codec.clockRate)
                assertEquals(getFloat("ie"), codec.ie)
                assertEquals(getFloat("bpl"), codec.bpl)
            }
            assertEquals(sdpSessionJson.getInteger("ptime"), sdpSession.ptime)

            assertEquals(jsonObject.getJsonObject("recording").getInteger("mode"), recording!!.mode.toInt())
        }
    }
}