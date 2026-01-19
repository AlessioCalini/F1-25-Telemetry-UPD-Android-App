package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MarshalZone(
    val zoneStart: Float,
    val zoneFlag: Byte
)

data class WeatherForecastSample(
    val sessionType: Byte,
    val timeOffset: Byte,
    val weather: Byte,
    val trackTemperature: Byte,
    val trackTemperatureChange: Byte,
    val airTemperature: Byte,
    val airTemperatureChange: Byte,
    val rainPercentage: Byte
)

data class PacketSessionData(
    val header: PacketHeader,
    val weather: Byte,
    val trackTemperature: Byte,
    val airTemperature: Byte,
    val totalLaps: Byte,
    val trackLength: Short,
    val sessionType: Byte,
    val trackId: Byte,
    val formula: Byte,
    val sessionTimeLeft: Short,
    val sessionDuration: Short,
    val pitSpeedLimit: Byte,
    val gamePaused: Byte,
    val isSpectating: Byte,
    val spectatorCarIndex: Byte,
    val sliProNativeSupport: Byte,
    val numMarshalZones: Byte,
    val marshalZones: Array<MarshalZone>,
    val safetyCarStatus: Byte,
    val networkGame: Byte,
    val numWeatherForecastSamples: Byte,
    val weatherForecastSamples: Array<WeatherForecastSample>,
    val forecastAccuracy: Byte,
    val aiDifficulty: Byte,
    val seasonLinkIdentifier: Int,
    val weekendLinkIdentifier: Int,
    val sessionLinkIdentifier: Int,
    val pitStopWindowIdealLap: Byte,
    val pitStopWindowLatestLap: Byte,
    val pitStopRejoinPosition: Byte,
    val steeringAssist: Byte,
    val brakingAssist: Byte,
    val gearboxAssist: Byte,
    val pitAssist: Byte,
    val pitReleaseAssist: Byte,
    val ersAssist: Byte,
    val drsAssist: Byte,
    val dynamicRacingLine: Byte,
    val dynamicRacingLineType: Byte,
    val gameMode: Byte,
    val ruleSet: Byte,
    val timeOfDay: Int,
    val sessionLength: Byte,
    val speedUnitsLeadPlayer: Byte,
    val temperatureUnitsLeadPlayer: Byte,
    val speedUnitsSecondaryPlayer: Byte,
    val temperatureUnitsSecondaryPlayer: Byte,
    val numSafetyCarPeriods: Byte,
    val numVirtualSafetyCarPeriods: Byte,
    val numRedFlagPeriods: Byte,
    val equalCarPerformance: Byte,
    val recoveryMode: Byte,
    val flashbackLimit: Byte,
    val surfaceType: Byte,
    val lowFuelMode: Byte,
    val raceStarts: Byte,
    val tyreTemperature: Byte,
    val pitLaneTyreSim: Byte,
    val carDamage: Byte,
    val carDamageRate: Byte,
    val collisions: Byte,
    val collisionsOffForFirstLapOnly: Byte,
    val mpUnsafePitRelease: Byte,
    val mpOffForGriefing: Byte,
    val cornerCuttingStringency: Byte,
    val parcFermeRules: Byte,
    val pitStopExperience: Byte,
    val safetyCar: Byte,
    val safetyCarExperience: Byte,
    val formationLap: Byte,
    val formationLapExperience: Byte,
    val redFlags: Byte,
    val affectsLicenceLevelSolo: Byte,
    val affectsLicenceLevelMP: Byte,
    val numSessionsInWeekend: Byte,
    val weekendStructure: ByteArray,
    val sector2LapDistanceStart: Float,
    val sector3LapDistanceStart: Float
)

fun parsePacketSessionData(data: ByteArray, length: Int): PacketSessionData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    val weather = buffer.get()
    val trackTemperature = buffer.get()
    val airTemperature = buffer.get()
    val totalLaps = buffer.get()
    val trackLength = buffer.short
    val sessionType = buffer.get()
    val trackId = buffer.get()
    val formula = buffer.get()
    val sessionTimeLeft = buffer.short
    val sessionDuration = buffer.short
    val pitSpeedLimit = buffer.get()
    val gamePaused = buffer.get()
    val isSpectating = buffer.get()
    val spectatorCarIndex = buffer.get()
    val sliProNativeSupport = buffer.get()
    val numMarshalZones = buffer.get()

    // Marshal zones (21 max)
    val marshalZones = Array(21) {
        MarshalZone(
            zoneStart = buffer.float,
            zoneFlag = buffer.get()
        )
    }

    val safetyCarStatus = buffer.get()
    val networkGame = buffer.get()
    val numWeatherForecastSamples = buffer.get()

    // Weather forecast samples (64 max)
    val weatherForecastSamples = Array(64) {
        WeatherForecastSample(
            sessionType = buffer.get(),
            timeOffset = buffer.get(),
            weather = buffer.get(),
            trackTemperature = buffer.get(),
            trackTemperatureChange = buffer.get(),
            airTemperature = buffer.get(),
            airTemperatureChange = buffer.get(),
            rainPercentage = buffer.get()
        )
    }

    val forecastAccuracy = buffer.get()
    val aiDifficulty = buffer.get()
    val seasonLinkIdentifier = buffer.int
    val weekendLinkIdentifier = buffer.int
    val sessionLinkIdentifier = buffer.int
    val pitStopWindowIdealLap = buffer.get()
    val pitStopWindowLatestLap = buffer.get()
    val pitStopRejoinPosition = buffer.get()
    val steeringAssist = buffer.get()
    val brakingAssist = buffer.get()
    val gearboxAssist = buffer.get()
    val pitAssist = buffer.get()
    val pitReleaseAssist = buffer.get()
    val ersAssist = buffer.get()
    val drsAssist = buffer.get()
    val dynamicRacingLine = buffer.get()
    val dynamicRacingLineType = buffer.get()
    val gameMode = buffer.get()
    val ruleSet = buffer.get()
    val timeOfDay = buffer.int
    val sessionLength = buffer.get()
    val speedUnitsLeadPlayer = buffer.get()
    val temperatureUnitsLeadPlayer = buffer.get()
    val speedUnitsSecondaryPlayer = buffer.get()
    val temperatureUnitsSecondaryPlayer = buffer.get()
    val numSafetyCarPeriods = buffer.get()
    val numVirtualSafetyCarPeriods = buffer.get()
    val numRedFlagPeriods = buffer.get()
    val equalCarPerformance = buffer.get()
    val recoveryMode = buffer.get()
    val flashbackLimit = buffer.get()
    val surfaceType = buffer.get()
    val lowFuelMode = buffer.get()
    val raceStarts = buffer.get()
    val tyreTemperature = buffer.get()
    val pitLaneTyreSim = buffer.get()
    val carDamage = buffer.get()
    val carDamageRate = buffer.get()
    val collisions = buffer.get()
    val collisionsOffForFirstLapOnly = buffer.get()
    val mpUnsafePitRelease = buffer.get()
    val mpOffForGriefing = buffer.get()
    val cornerCuttingStringency = buffer.get()
    val parcFermeRules = buffer.get()
    val pitStopExperience = buffer.get()
    val safetyCar = buffer.get()
    val safetyCarExperience = buffer.get()
    val formationLap = buffer.get()
    val formationLapExperience = buffer.get()
    val redFlags = buffer.get()
    val affectsLicenceLevelSolo = buffer.get()
    val affectsLicenceLevelMP = buffer.get()
    val numSessionsInWeekend = buffer.get()

    // Weekend structure (12 sessions max)
    val weekendStructure = ByteArray(12)
    buffer.get(weekendStructure)

    val sector2LapDistanceStart = buffer.float
    val sector3LapDistanceStart = buffer.float

    return PacketSessionData(
        header, weather, trackTemperature, airTemperature, totalLaps,
        trackLength, sessionType, trackId, formula, sessionTimeLeft,
        sessionDuration, pitSpeedLimit, gamePaused, isSpectating,
        spectatorCarIndex, sliProNativeSupport, numMarshalZones,
        marshalZones, safetyCarStatus, networkGame,
        numWeatherForecastSamples, weatherForecastSamples,
        forecastAccuracy, aiDifficulty, seasonLinkIdentifier,
        weekendLinkIdentifier, sessionLinkIdentifier,
        pitStopWindowIdealLap, pitStopWindowLatestLap,
        pitStopRejoinPosition, steeringAssist, brakingAssist,
        gearboxAssist, pitAssist, pitReleaseAssist, ersAssist,
        drsAssist, dynamicRacingLine, dynamicRacingLineType,
        gameMode, ruleSet, timeOfDay, sessionLength,
        speedUnitsLeadPlayer, temperatureUnitsLeadPlayer,
        speedUnitsSecondaryPlayer, temperatureUnitsSecondaryPlayer,
        numSafetyCarPeriods, numVirtualSafetyCarPeriods,
        numRedFlagPeriods, equalCarPerformance, recoveryMode,
        flashbackLimit, surfaceType, lowFuelMode, raceStarts,
        tyreTemperature, pitLaneTyreSim, carDamage, carDamageRate,
        collisions, collisionsOffForFirstLapOnly, mpUnsafePitRelease,
        mpOffForGriefing, cornerCuttingStringency, parcFermeRules,
        pitStopExperience, safetyCar, safetyCarExperience,
        formationLap, formationLapExperience, redFlags,
        affectsLicenceLevelSolo, affectsLicenceLevelMP,
        numSessionsInWeekend, weekendStructure,
        sector2LapDistanceStart, sector3LapDistanceStart
    )
}