package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class TimeTrialDataSet(
    val carIdx: Int,
    val teamId: Int,
    val lapTimeInMS: Int,
    val sector1TimeInMS: Int,
    val sector2TimeInMS: Int,
    val sector3TimeInMS: Int,
    val tractionControl: Int,
    val gearboxAssist: Int,
    val antiLockBrakes: Int,
    val equalCarPerformance: Int,
    val customSetup: Int,
    val valid: Int
)

data class PacketTimeTrialData(
    val header: PacketHeader,
    val playerSessionBestDataSet: TimeTrialDataSet,
    val personalBestDataSet: TimeTrialDataSet,
    val rivalDataSet: TimeTrialDataSet
)

fun parseTimeTrialDataSet(buffer: ByteBuffer): TimeTrialDataSet {
    return TimeTrialDataSet(
        carIdx = buffer.get().toInt() and 0xFF,
        teamId = buffer.get().toInt() and 0xFF,
        lapTimeInMS = buffer.int,
        sector1TimeInMS = buffer.int,
        sector2TimeInMS = buffer.int,
        sector3TimeInMS = buffer.int,
        tractionControl = buffer.get().toInt() and 0xFF,
        gearboxAssist = buffer.get().toInt() and 0xFF,
        antiLockBrakes = buffer.get().toInt() and 0xFF,
        equalCarPerformance = buffer.get().toInt() and 0xFF,
        customSetup = buffer.get().toInt() and 0xFF,
        valid = buffer.get().toInt() and 0xFF
    )
}

fun parsePacketTimeTrialData(data: ByteArray, length: Int): PacketTimeTrialData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    val playerSessionBest = parseTimeTrialDataSet(buffer)
    val personalBest = parseTimeTrialDataSet(buffer)
    val rival = parseTimeTrialDataSet(buffer)

    return PacketTimeTrialData(
        header = header,
        playerSessionBestDataSet = playerSessionBest,
        personalBestDataSet = personalBest,
        rivalDataSet = rival
    )
}