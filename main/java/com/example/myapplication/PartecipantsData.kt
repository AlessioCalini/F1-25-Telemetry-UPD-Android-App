package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

data class LiveryColour(
    val red: Byte,
    val green: Byte,
    val blue: Byte
)

data class ParticipantData(
    val aiControlled: Byte,
    val driverId: Byte,
    val networkId: Byte,
    val teamId: Byte,
    val myTeam: Byte,
    val raceNumber: Byte,
    val nationality: Byte,
    val name: String,
    val yourTelemetry: Byte,
    val showOnlineNames: Byte,
    val techLevel: Short,
    val platform: Byte,
    val numColours: Byte,
    val liveryColours: Array<LiveryColour>
)

data class PacketParticipantsData(
    val header: PacketHeader,
    val numActiveCars: Byte,
    val participants: Array<ParticipantData>
)

fun parsePacketParticipantsData(data: ByteArray, length: Int): PacketParticipantsData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    val numActiveCars = buffer.get()

    val participants = Array(22) {
        val aiControlled = buffer.get()
        val driverId = buffer.get()
        val networkId = buffer.get()
        val teamId = buffer.get()
        val myTeam = buffer.get()
        val raceNumber = buffer.get()
        val nationality = buffer.get()

        // Nome (32 byte)
        val nameBytes = ByteArray(32)
        buffer.get(nameBytes)
        val name = String(nameBytes, StandardCharsets.UTF_8).trim('\u0000')

        val yourTelemetry = buffer.get()
        val showOnlineNames = buffer.get()
        val techLevel = buffer.short
        val platform = buffer.get()
        val numColours = buffer.get()

        // Livery colours (4 colori)
        val liveryColours = Array(4) {
            LiveryColour(
                red = buffer.get(),
                green = buffer.get(),
                blue = buffer.get()
            )
        }

        ParticipantData(
            aiControlled, driverId, networkId, teamId, myTeam,
            raceNumber, nationality, name, yourTelemetry,
            showOnlineNames, techLevel, platform, numColours,
            liveryColours
        )
    }

    return PacketParticipantsData(header, numActiveCars, participants)
}