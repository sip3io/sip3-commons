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

package io.sip3.commons

enum class SipMethods {

    // RFC 3261
    INVITE,
    REGISTER,
    ACK,
    CANCEL,
    BYE,
    OPTIONS,

    // RFC 3262
    PRACK,

    // RFC 3428
    MESSAGE,

    // RFC 6665
    SUBSCRIBE,
    NOTIFY,

    // RFC 3903
    PUBLISH,

    // RFC 3311
    UPDATE,

    // RFC 3515
    REFER,

    // RFC 2976
    INFO
}