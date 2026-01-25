package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class LapData(
    val lastLapTimeInMS: Int,
    val currentLapTimeInMS: Int,
    val sector1TimeMSPart: Short,
    val sector1TimeMinutesPart: Byte,
    val sector2TimeMSPart: Short,
    val sector2TimeMinutesPart: Byte,
    val deltaToCarInFrontMSPart: Short,
    val deltaToCarInFrontMinutesPart: Byte,
    val deltaToRaceLeaderMSPart: Short,
    val deltaToRaceLeaderMinutesPart: Byte,
    val lapDistance: Float,
    val totalDistance: Float,
    val safetyCarDelta: Float,
    val carPosition: Byte,
    val currentLapNum: Byte,
    val pitStatus: Byte,
    val numPitStops: Byte,
    val sector: Byte,
    val currentLapInvalid: Byte,
    val penalties: Byte,
    val totalWarnings: Byte,
    val cornerCuttingWarnings: Byte,
    val numUnservedDriveThroughPens: Byte,
    val numUnservedStopGoPens: Byte,
    val gridPosition: Byte,
    val driverStatus: Byte,
    val resultStatus: Byte,
    val pitLaneTimerActive: Byte,
    val pitLaneTimeInLaneInMS: Short,
    val pitStopTimerInMS: Short,
    val pitStopShouldServePen: Byte,
    val speedTrapFastestSpeed: Float,
    val speedTrapFastestLap: Byte
)

data class PacketLapData(
    val header: PacketHeader,
    val lapData: Array<LapData>,
    val timeTrialPBCarIdx: Byte,
    val timeTrialRivalCarIdx: Byte
)

fun parsePacketLapData(data: ByteArray, length: Int): PacketLapData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    val lapData = Array(22) {
        LapData(
            lastLapTimeInMS = buffer.int,
            currentLapTimeInMS = buffer.int,
            sector1TimeMSPart = buffer.short,
            sector1TimeMinutesPart = buffer.get(),
            sector2TimeMSPart = buffer.short,
            sector2TimeMinutesPart = buffer.get(),
            deltaToCarInFrontMSPart = buffer.short,
            deltaToCarInFrontMinutesPart = buffer.get(),
            deltaToRaceLeaderMSPart = buffer.short,
            deltaToRaceLeaderMinutesPart = buffer.get(),
            lapDistance = buffer.float,
            totalDistance = buffer.float,
            safetyCarDelta = buffer.float,
            carPosition = buffer.get(),
            currentLapNum = buffer.get(),
            pitStatus = buffer.get(),
            numPitStops = buffer.get(),
            sector = buffer.get(),
            currentLapInvalid = buffer.get(),
            penalties = buffer.get(),
            totalWarnings = buffer.get(),
            cornerCuttingWarnings = buffer.get(),
            numUnservedDriveThroughPens = buffer.get(),
            numUnservedStopGoPens = buffer.get(),
            gridPosition = buffer.get(),
            driverStatus = buffer.get(),
            resultStatus = buffer.get(),
            pitLaneTimerActive = buffer.get(),
            pitLaneTimeInLaneInMS = buffer.short,
            pitStopTimerInMS = buffer.short,
            pitStopShouldServePen = buffer.get(),
            speedTrapFastestSpeed = buffer.float,
            speedTrapFastestLap = buffer.get()
        )
    }

    val timeTrialPBCarIdx = buffer.get()
    val timeTrialRivalCarIdx = buffer.get()

    return PacketLapData(header, lapData, timeTrialPBCarIdx, timeTrialRivalCarIdx)
}