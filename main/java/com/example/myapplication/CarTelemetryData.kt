package com.example.myapplication

data class CarTelemetryData(
    val speed: Int,              // km/h
    val throttle: Float,         // 0.0 - 1.0
    val steer: Float,            // -1.0 (full left) to 1.0 (full right)
    val brake: Float,            // 0.0 - 1.0
    val clutch: Int,             // 0 - 100
    val gear: Int,               // -1 (R), 0 (N), 1-8
    val engineRPM: Int,          // RPM
    val drs: Int,                // 0 = off, 1 = on
    val revLightsPercent: Int,   // 0 - 100
    val revLightsBitValue: Int,  // bit 0 = leftmost LED, bit 14 = rightmost LED
    val brakesTemperature: IntArray,        // [RL, RR, FL, FR] in celsius
    val tyresSurfaceTemperature: IntArray,  // [RL, RR, FL, FR] in celsius
    val tyresInnerTemperature: IntArray,    // [RL, RR, FL, FR] in celsius
    val engineTemperature: Int,  // celsius
    val tyresPressure: FloatArray           // [RL, RR, FL, FR] in PSI
)