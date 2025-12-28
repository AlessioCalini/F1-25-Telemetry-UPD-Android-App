package com.example.myapplication

data class TelemetryState(
    val speed: Int = 0,
    val rpm: Int = 0,
    val gear: Int = 0,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val ersStoreEnergy: Float = 0f,
    val maxRPM: Int = 15000,

    // LAP DATA
    val lastLapTime: String = "--:--.---",
    val bestLapTime: String = "--:--.---",
    val deltaToPersonalBest: String = "+-.--",
    val currentLap: Int = 0,
    val totalLaps: Int = 0,
    val pos: Int=0,

    //Car Damage - Usura Gomme
    val tyreWearFL: Float = 0f,  // Front Left
    val tyreWearFR: Float = 0f,  // Front Right
    val tyreWearRL: Float = 0f,  // Rear Left
    val tyreWearRR: Float = 0f   // Rear Right
)