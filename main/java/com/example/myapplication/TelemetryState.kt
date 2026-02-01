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
    SPRINT_SHOOTOUT_1,           // ⭐ NUOVO
    SPRINT_SHOOTOUT_2,           // ⭐ NUOVO
    SPRINT_SHOOTOUT_3,           // ⭐ NUOVO
    SHORT_SPRINT_SHOOTOUT,       // ⭐ NUOVO
    ONE_SHOT_SPRINT_SHOOTOUT,    // ⭐ NUOVO
    RACE,
    RACE_2,
    RACE_3,
    TIME_TRIAL;
}


data class TelemetryState(
    val speed: Int = 0,
    val rpm: Int = 0,
    val gear: Int = 0,
    val throttle: Float = 0f,
    val brake: Float = 0f,
    val clutch: Float = 0f,  // Assicurati che ci sia
    val suggestedGear: Int = 0,
    val ersStoreEnergy: Float = 0f,
    val ersDeployMode: Int = 0,  // 0=none, 1=medium, 2=hotlap, 3=overtake
    val maxRPM: Int = 15000,

    // Session info
    val sessionType: SessionType = SessionType.UNKNOWN,
    val safetyCarStatus: Int = 0,  // 0=no, 1=full, 2=virtual, 3=formation


    // LAP DATA
    val lastLapTime: String = "--:--.---",
    val bestLapTime: String = "--:--.---",
    val deltaToPersonalBest: String = "+-.--",
    val safetyCarDelta: Float = 0f,
    val currentLap: Int = 0,
    val totalLaps: Int = 0,
    val pos: Int=0,

    // ⭐ NUOVO - Per Qualifica
    val currentLapTime: String = "--:--.---",           // Giro in corso
    val sessionBestLapTime: String = "--:--.---",       // Miglior tempo sessione
    val deltaToSessionBest: String = "+0.000",          // Delta al session best
    val bestSector1: String = "--.---",  // Da "--.-" a "--.---"
    val bestSector2: String = "--.---",  // Da "--.-" a "--.---"
    val bestSector3: String = "--.---",  // Da "--.-" a "--.---"

    //Car Damage - Usura Gomme
    val tyreWearFL: Float = 0f,  // Front Left
    val tyreWearFR: Float = 0f,  // Front Right
    val tyreWearRL: Float = 0f,  // Rear Left
    val tyreWearRR: Float = 0f,   // Rear Right
    val visualTyreCompound: Int = 0,  //  Compound delle gomme
    val actualTyreCompound: Int = 0,  // Compound reale

    // ⭐ AGGIUNGI se mancano
    val tyreFLTemp: Int = 0,
    val tyreFRTemp: Int = 0,
    val tyreRLTemp: Int = 0,
    val tyreRRTemp: Int = 0,

    // Piloti vicini (2 davanti + 2 dietro)
    val driver1Ahead: DriverInfo = DriverInfo(),   // 2 posizioni avanti
    val driver2Ahead: DriverInfo = DriverInfo(),   // 1 posizione avanti
    val driver1Behind: DriverInfo = DriverInfo(),  // 1 posizione dietro
    val driver2Behind: DriverInfo = DriverInfo(),  // 2 posizioni dietro

    //  DRS
    val drsAllowed: Boolean = false,      // DRS disponibile (zona DRS)
    val drsActivated: Boolean = false,     // DRS attivato (pulsante premuto)

    // Weather
    val trackTemperature: Int = 0,
    val weather: Int = 0,  // 0=clear, 1=light cloud, 2=overcast, 3=light rain, 4=heavy rain, etc.

    // Time Trial data
    val ttPersonalBestLap: String = "--:--.---",
    val ttRivalLap: String = "--:--.---",
    val ttDeltaToRival: String = "+0.000",
    val ttSessionBestSector1: String = "--.---",
    val ttSessionBestSector2: String = "--.---",
    val ttSessionBestSector3: String = "--.---",
    val ttLapsCompleted: Int = 0,
    val ttBestS1Owner: String = "YOU",             // ⭐ NUOVO - Chi ha fatto S1
    val ttBestS2Owner: String = "YOU",             // ⭐ NUOVO - Chi ha fatto S2
    val ttBestS3Owner: String = "YOU",             // ⭐ NUOVO - Chi ha fatto S3
    val currentLapInvalid: Boolean = false,        // ⭐ NUOVO - Giro corrente invalido
)