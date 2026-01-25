package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

// Event types (primi 4 byte dopo header)
// SSTA = Session Started
// SEND = Session Ended
// FTLP = Fastest Lap
// RTMT = Retirement
// DRSE = DRS enabled
// DRSD = DRS disabled
// TMPT = Team mate in pits
// CHQF = Chequered flag
// RCWN = Race Winner
// PENA = Penalty Issued
// SPTP = Speed Trap Triggered
// STLG = Start lights
// LGOT = Lights out
// DTSV = Drive through served
// SGSV = Stop go served
// FLBK = Flashback
// BUTN = Button status
// OVTK = Overtake
// SCAR = Safety Car
// COLL = Collision

data class PacketEventData(
    val header: PacketHeader,
    val eventStringCode: String  // 4 caratteri (es. "SEND", "SSTA", "CHQF")
    // EventDataDetails non ci serve per gli eventi di reset
)

fun parsePacketEventData(data: ByteArray, length: Int): PacketEventData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    // Leggi i 4 byte dell'event code
    val eventCodeBytes = ByteArray(4)
    buffer.get(eventCodeBytes)
    val eventStringCode = String(eventCodeBytes, StandardCharsets.UTF_8).trim('\u0000')

    return PacketEventData(header, eventStringCode)
}