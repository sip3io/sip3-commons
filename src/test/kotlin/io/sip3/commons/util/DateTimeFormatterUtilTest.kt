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

package io.sip3.commons.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.Timestamp
import java.time.format.DateTimeFormatter

class DateTimeFormatterUtilTest {

    @Test
    fun `Format milliseconds using different patterns`() {
        val millis = 1440298800000
        val timestamp = Timestamp(millis)

        var formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        assertEquals("20150823", formatter.format(millis))
        assertEquals("20150823", formatter.format(timestamp))

        formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:00:00")
        assertEquals("20150823 03:00:00", formatter.format(millis))
        assertEquals("20150823 03:00:00", formatter.format(timestamp))
    }
}