package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class PacketCarStatusData(
    val header: PacketHeader,
    val carStatusData: Array<CarStatusData>
)

fun parsePacketCarStatusData(data: ByteArray, length: Int): PacketCarStatusData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29)

    val carStatusData = Array(22) {
        val tractionControl = buffer.get().toInt() and 0xFF
        val antiLockBrakes = buffer.get().toInt() and 0xFF
        val fuelMix = buffer.get().toInt() and 0xFF
        val frontBrakeBias = buffer.get().toInt() and 0xFF
        val pitLimiterStatus = buffer.get().toInt() and 0xFF
        val fuelInTank = buffer.float
        val fuelCapacity = buffer.float
        val fuelRemainingLaps = buffer.float
        val maxRPM = buffer.short.toInt() and 0xFFFF
        val idleRPM = buffer.short.toInt() and 0xFFFF
        val maxGears = buffer.get().toInt() and 0xFF
        val drsAllowed = buffer.get().toInt() and 0xFF
        val drsActivationDistance = buffer.short.toInt() and 0xFFFF
        val actualTyreCompound = buffer.get().toInt() and 0xFF
        val visualTyreCompound = buffer.get().toInt() and 0xFF
        val tyresAgeLaps = buffer.get().toInt() and 0xFF
        val vehicleFiaFlags = buffer.get().toInt()
        val enginePowerICE = buffer.float
        val enginePowerMGUK = buffer.float
        val ersStoreEnergy = buffer.float
        val ersDeployMode = buffer.get().toInt() and 0xFF
        val ersHarvestedThisLapMGUK = buffer.float
        val ersHarvestedThisLapMGUH = buffer.float
        val ersDeployedThisLap = buffer.float
        val networkPaused = buffer.get().toInt() and 0xFF

        CarStatusData(
            tractionControl = tractionControl,
            antiLockBrakes = antiLockBrakes,
            fuelMix = fuelMix,
            frontBrakeBias = frontBrakeBias,
            pitLimiterStatus = pitLimiterStatus,
            fuelInTank = fuelInTank,
            fuelCapacity = fuelCapacity,
            fuelRemainingLaps = fuelRemainingLaps,
            maxRPM = maxRPM,
            idleRPM = idleRPM,
            maxGears = maxGears,
            drsAllowed = drsAllowed,
            drsActivationDistance = drsActivationDistance,
            actualTyreCompound = actualTyreCompound,
            visualTyreCompound = visualTyreCompound,
            tyresAgeLaps = tyresAgeLaps,
            vehicleFiaFlags = vehicleFiaFlags,
            enginePowerICE = enginePowerICE,
            enginePowerMGUK = enginePowerMGUK,
            ersStoreEnergy = ersStoreEnergy,
            ersDeployMode = ersDeployMode,
            ersHarvestedThisLapMGUK = ersHarvestedThisLapMGUK,
            ersHarvestedThisLapMGUH = ersHarvestedThisLapMGUH,
            ersDeployedThisLap = ersDeployedThisLap,
            networkPaused = networkPaused
        )
    }

    return PacketCarStatusData(
        header = header,
        carStatusData = carStatusData
    )
}