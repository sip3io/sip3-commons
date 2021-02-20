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

package io.sip3.commons.vertx.util

import io.sip3.commons.vertx.util.EventBusUtil.USE_LOCAL_CODEC
import io.vertx.core.AsyncResult
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.impl.EventBusImpl
import io.vertx.kotlin.core.eventbus.deliveryOptionsOf

object EventBusUtil {

    val USE_LOCAL_CODEC = deliveryOptionsOf(codecName = "local", localOnly = true)
}

fun <T> EventBus.localRequest(address: String, message: Any, options: DeliveryOptions? = null, replyHandler: ((AsyncResult<Message<T>>) -> Unit)) {
    options?.apply {
        codecName = "local"
        isLocalOnly = true
    }
    request(address, message, options ?: USE_LOCAL_CODEC, replyHandler)
}

fun EventBus.localSend(address: String, message: Any, options: DeliveryOptions? = null) {
    options?.apply {
        codecName = "local"
        isLocalOnly = true
    }
    send(address, message, options ?: USE_LOCAL_CODEC)
}

fun EventBus.localPublish(address: String, message: Any, options: DeliveryOptions? = null) {
    options?.apply {
        codecName = "local"
        isLocalOnly = true
    }
    publish(address, message, options ?: USE_LOCAL_CODEC)
}

@Suppress("UNCHECKED_CAST")
fun EventBus.endpoints(): Set<String> {
    return (this as? EventBusImpl)?.let { eventBus ->
        // Read protected `handlerMap` field
        val handlerMapField = EventBusImpl::class.java
            .getDeclaredField("handlerMap")
        handlerMapField.isAccessible = true

        (handlerMapField.get(eventBus) as? Map<String, Any>)?.keys
    } ?: emptySet()
}