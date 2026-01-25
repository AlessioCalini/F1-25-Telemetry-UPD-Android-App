package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class LapHistoryData(
    val lapTimeInMS: Int,
    val sector1TimeInMS: Int,
    val sector1TimeMinutes: Int,
    val sector2TimeInMS: Int,
    val sector2TimeMinutes: Int,
    val sector3TimeInMS: Int,
    val sector3TimeMinutes: Int,
    val lapValidBitFlags: Int
)

data class PacketSessionHistoryData(
    val header: PacketHeader,
    val carIdx: Int,
    val numLaps: Int,
    val numTyreStints: Int,
    val bestLapTimeLapNum: Int,
    val bestSector1LapNum: Int,
    val bestSector2LapNum: Int,
    val bestSector3LapNum: Int,
    val lapHistoryData: Array<LapHistoryData>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PacketSessionHistoryData

        if (header != other.header) return false
        if (carIdx != other.carIdx) return false
        if (!lapHistoryData.contentEquals(other.lapHistoryData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + carIdx
        result = 31 * result + lapHistoryData.contentHashCode()
        return result
    }
}

fun parsePacketSessionHistoryData(data: ByteArray, length: Int): PacketSessionHistoryData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29)

    val carIdx = buffer.get().toInt() and 0xFF
    val numLaps = buffer.get().toInt() and 0xFF
    val numTyreStints = buffer.get().toInt() and 0xFF
    val bestLapTimeLapNum = buffer.get().toInt() and 0xFF
    val bestSector1LapNum = buffer.get().toInt() and 0xFF
    val bestSector2LapNum = buffer.get().toInt() and 0xFF
    val bestSector3LapNum = buffer.get().toInt() and 0xFF

    val lapHistoryData = Array(100) {
        LapHistoryData(
            lapTimeInMS = buffer.int,
            sector1TimeInMS = buffer.short.toInt() and 0xFFFF,
            sector1TimeMinutes = buffer.get().toInt() and 0xFF,
            sector2TimeInMS = buffer.short.toInt() and 0xFFFF,
            sector2TimeMinutes = buffer.get().toInt() and 0xFF,
            sector3TimeInMS = buffer.short.toInt() and 0xFFFF,
            sector3TimeMinutes = buffer.get().toInt() and 0xFF,
            lapValidBitFlags = buffer.get().toInt() and 0xFF
        )
    }

    return PacketSessionHistoryData(
        header = header,
        carIdx = carIdx,
        numLaps = numLaps,
        numTyreStints = numTyreStints,
        bestLapTimeLapNum = bestLapTimeLapNum,
        bestSector1LapNum = bestSector1LapNum,
        bestSector2LapNum = bestSector2LapNum,
        bestSector3LapNum = bestSector3LapNum,
        lapHistoryData = lapHistoryData
    )
}
