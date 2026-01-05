package com.example.myapplication

enum class SessionType {
    UNKNOWN,
    PRACTICE_1,
    PRACTICE_2,
    PRACTICE_3,
    SHORT_PRACTICE,
    QUALIFYING_1,
    QUALIFYING_2,
    QUALIFYING_3,
    SHORT_QUALIFYING,
    ONE_SHOT_QUALIFYING,
    RACE,
    RACE_2,
    RACE_3,
    TIME_TRIAL
}


data class TelemetryState(
    val speed: Int = 0,
    val rpm: Int = 0,
    val gear: Int = 0,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val ersStoreEnergy: Float = 0f,
    val maxRPM: Int = 15000,

    // Session info
    val sessionType: SessionType = SessionType.UNKNOWN,  // ⭐ NUOVO

    // LAP DATA
    val lastLapTime: String = "--:--.---",
    val bestLapTime: String = "--:--.---",
    val deltaToPersonalBest: String = "+-.--",
    val currentLap: Int = 0,
    val totalLaps: Int = 0,
    val pos: Int=0,

    // ⭐ NUOVO - Per Qualifica
    val currentLapTime: String = "--:--.---",           // Giro in corso
    val sessionBestLapTime: String = "--:--.---",       // Miglior tempo sessione
    val deltaToSessionBest: String = "+0.000",          // Delta al session best
    val bestSector1: String = "--.-",                   // Miglior S1 sessione
    val bestSector2: String = "--.-",                   // Miglior S2 sessione
    val bestSector3: String = "--.-",                   // Miglior S3 sessione

    //Car Damage - Usura Gomme
    val tyreWearFL: Float = 0f,  // Front Left
    val tyreWearFR: Float = 0f,  // Front Right
    val tyreWearRL: Float = 0f,  // Rear Left
    val tyreWearRR: Float = 0f,   // Rear Right

    // Piloti vicini (2 davanti + 2 dietro)
    val driver1Ahead: DriverInfo = DriverInfo(),   // 2 posizioni avanti
    val driver2Ahead: DriverInfo = DriverInfo(),   // 1 posizione avanti
    val driver1Behind: DriverInfo = DriverInfo(),  // 1 posizione dietro
    val driver2Behind: DriverInfo = DriverInfo(),  // 2 posizioni dietro

    //  DRS
    val drsAllowed: Boolean = false,      // DRS disponibile (zona DRS)
    val drsActivated: Boolean = false     // DRS attivato (pulsante premuto)
)