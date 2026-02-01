package com.example.myapplication

data class CarStatusData(
    val tractionControl: Int,
    val antiLockBrakes: Int,
    val fuelMix: Int,
    val frontBrakeBias: Int,
    val pitLimiterStatus: Int,
    val fuelInTank: Float,
    val fuelCapacity: Float,
    val fuelRemainingLaps: Float,
    val maxRPM: Int,
    val idleRPM: Int,
    val maxGears: Int,
    val drsAllowed: Int,
    val drsActivationDistance: Int,
    val actualTyreCompound: Int,
    val visualTyreCompound: Int,
    val tyresAgeLaps: Int,
    val vehicleFiaFlags: Int,
    val enginePowerICE: Float,
    val enginePowerMGUK: Float,
    val ersStoreEnergy: Float,      // Energia ERS (0-4000000 J)
    val ersDeployMode: Int,
    val ersHarvestedThisLapMGUK: Float,
    val ersHarvestedThisLapMGUH: Float,
    val ersDeployedThisLap: Float,
    val networkPaused: Int
)