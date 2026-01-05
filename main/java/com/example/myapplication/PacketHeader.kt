package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PacketHeader(
    val packetFormat: Int,
    val gameMajorVersion: Int,
    val gameMinorVersion: Int,
    val packetVersion: Int,
    val packetType: Int,
    val packetId: Int,
    val sessionUID: Long,
    val sessionTime: Float,
    val frameIdentifier: Long,
    val playerCarIndex: Int,
    val secondaryPlayerCarIndex: Int
)

fun parsePacketHeader(data: ByteArray, length: Int): PacketHeader {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val packetFormat = buffer.short.toInt() and 0xFFFF       // uint16 - bytes 0-1
    val gameMajor = buffer.get().toInt() and 0xFF            // uint8 - byte 2
    val gameMinor = buffer.get().toInt() and 0xFF            // uint8 - byte 3
    val packetVersion = buffer.get().toInt() and 0xFF        // uint8 - byte 4
    val packetType = buffer.get().toInt() and 0xFF           // uint8 - byte 5
    val packetId = buffer.get().toInt() and 0xFF             // uint8 - byte 6

    val sessionUID = buffer.long                             // uint64 - bytes 7-14
    val sessionTime = buffer.float                           // float - bytes 15-18
    val frameIdentifier = buffer.int.toLong() and 0xFFFFFFFFL // uint32 - bytes 19-22

    // Salta 4 bytes (seconda copia del frameIdentifier)
    buffer.int

    val playerCarIndex = buffer.get().toInt() and 0xFF       // uint8 - byte 27
    val secondaryPlayerCarIndex = buffer.get().toInt() and 0xFF // uint8 - byte 28

    return PacketHeader(
        packetFormat = packetFormat,
        gameMajorVersion = gameMajor,
        gameMinorVersion = gameMinor,
        packetVersion = packetVersion,
        packetType = packetType,
        packetId = packetId,
        sessionUID = sessionUID,
        sessionTime = sessionTime,
        frameIdentifier = frameIdentifier,
        playerCarIndex = playerCarIndex,
        secondaryPlayerCarIndex = secondaryPlayerCarIndex
    )
}