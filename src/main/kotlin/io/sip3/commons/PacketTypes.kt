package io.sip3.commons

object PacketTypes {

    // Real-Time Transport Protocol
    const val RTCP: Byte = 1

    // Real-time Transport Control Protocol
    const val RTP: Byte = 2

    // Session Initiation Protocol
    const val SIP: Byte = 3

    // Internet Control Message Protocol
    const val ICMP: Byte = 4

    // Real-Time Transport Protocol Report (Internal SIP3 protocol)
    const val RTPR: Byte = 5

    // Short Message Peer-to-Peer
    const val SMPP: Byte = 6
}