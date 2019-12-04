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