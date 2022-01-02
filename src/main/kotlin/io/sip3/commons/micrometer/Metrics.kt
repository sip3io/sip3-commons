/*
 * Copyright 2018-2022 SIP3.IO, Corp.
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

package io.sip3.commons.micrometer

import io.micrometer.core.instrument.*
import io.micrometer.core.instrument.Metrics

object Metrics {

    fun counter(name: String, attributes: Map<String, Any> = emptyMap()): Counter {
        return Metrics.counter(name, tagsOf(attributes))
    }

    fun summary(name: String, attributes: Map<String, Any> = emptyMap()): DistributionSummary {
        return Metrics.summary(name, tagsOf(attributes))
    }

    fun timer(name: String, attributes: Map<String, Any> = emptyMap()): Timer {
        return Metrics.timer(name, tagsOf(attributes))
    }

    private fun tagsOf(attributes: Map<String, Any>): List<Tag> {
        val tags = mutableListOf<Tag>()
        attributes.forEach { (k, v) -> tags.add(ImmutableTag(k, v.toString())) }
        return tags
    }
}