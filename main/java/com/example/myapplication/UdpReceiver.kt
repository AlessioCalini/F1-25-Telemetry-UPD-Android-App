package com.example.myapplication

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.DatagramPacket
import java.net.DatagramSocket

class UdpReceiver(
    private val port: Int = 20777
) {
    private var socket: DatagramSocket? = null

    private val _telemetryState = MutableStateFlow(TelemetryState())
    val telemetryState: StateFlow<TelemetryState> = _telemetryState

    private var bestLapTimeMS: Int = Int.MAX_VALUE

    // Array per salvare i nomi dei piloti
    private var driverNames = Array(22) { "" }

    // Array per salvare le posizioni e delta di ogni pilota
    private var driverPositions = IntArray(22) { 0 }
    private var driverDeltas = IntArray(22) { 0 }

    // Array per salvare i last lap di ogni pilota
    private var driverLastLapTimes = IntArray(22) { 0 }

    // Tracciamento best lap e sectors di ogni pilota
    private var driverBestLaps = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS1 = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS2 = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS3 = IntArray(22) { Int.MAX_VALUE }

    // ⭐ NUOVO - Traccia quale pilota ha fatto ogni best sector
    private var bestS1DriverIndex = -1
    private var bestS2DriverIndex = -1
    private var bestS3DriverIndex = -1

    // ⭐ NUOVO - Traccia la sessione corrente per resettare i sectors
    private var currentSessionType: SessionType = SessionType.UNKNOWN

    // ⭐ Best sectors della sessione corrente
    private var sessionBestS1MS = Int.MAX_VALUE
    private var sessionBestS2MS = Int.MAX_VALUE
    private var sessionBestS3MS = Int.MAX_VALUE
    private var sessionBestS1Driver = -1
    private var sessionBestS2Driver = -1
    private var sessionBestS3Driver = -1

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                socket = DatagramSocket(port)
                socket?.broadcast = false

                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                Log.d("UDP", "✅ Listening on port $port")

                while (true) {
                    socket?.receive(packet)

                    val header = parsePacketHeader(packet.data, packet.length)

                    when (header.packetId) {
                        0 -> { // Participants
                            val participants =
                                parsePacketParticipantsData(packet.data, packet.length)

                            // Salva tutti i nomi
                            for (i in 0..21) {
                                val name = participants.participants[i].name
                                if (name.isNotEmpty()) {
                                    driverNames[i] = name
                                }
                            }

                            Log.d(
                                "UDP_PARTICIPANTS",
                                "✅ Loaded driver names - Player: ${driverNames[header.playerCarIndex]}"
                            )
                        }

                        1 -> { // Session Data
                            val sessionData = parsePacketSessionData(packet.data, packet.length)

                            // ⭐ Converti session type
                            val sessionType = when (sessionData.sessionType.toInt() and 0xFF) {
                                0 -> SessionType.UNKNOWN
                                1 -> SessionType.PRACTICE_1
                                2 -> SessionType.PRACTICE_2
                                3 -> SessionType.PRACTICE_3
                                4 -> SessionType.SHORT_PRACTICE
                                5 -> SessionType.QUALIFYING_1
                                6 -> SessionType.QUALIFYING_2
                                7 -> SessionType.QUALIFYING_3
                                8 -> SessionType.SHORT_QUALIFYING
                                9 -> SessionType.ONE_SHOT_QUALIFYING
                                10 -> SessionType.SPRINT_SHOOTOUT_1       // ⭐ NUOVO
                                11 -> SessionType.SPRINT_SHOOTOUT_2       // ⭐ NUOVO
                                12 -> SessionType.SPRINT_SHOOTOUT_3       // ⭐ NUOVO
                                13 -> SessionType.SHORT_SPRINT_SHOOTOUT   // ⭐ NUOVO
                                14 -> SessionType.ONE_SHOT_SPRINT_SHOOTOUT // ⭐ NUOVO
                                15 -> SessionType.RACE
                                16 -> SessionType.RACE_2
                                17 -> SessionType.RACE_3
                                18 -> SessionType.TIME_TRIAL              // ⭐ CORRETTO: 18!
                                else -> SessionType.UNKNOWN
                            }
                            Log.d("UDP_SESSION", "📊 Session Type received: ${sessionData.sessionType.toInt() and 0xFF} -> $sessionType")


                            // ⭐ NUOVO - Reset best sectors se cambia la sessione (Q1→Q2→Q3)
                            if (sessionType != currentSessionType && sessionType != SessionType.UNKNOWN) {
                                currentSessionType = sessionType

                                // Reset best sectors
                                sessionBestS1MS = Int.MAX_VALUE
                                sessionBestS2MS = Int.MAX_VALUE
                                sessionBestS3MS = Int.MAX_VALUE
                                sessionBestS1Driver = -1
                                sessionBestS2Driver = -1
                                sessionBestS3Driver = -1

                                // Aggiorna lo state con reset dei sectors
                                _telemetryState.value = _telemetryState.value.copy(
                                    totalLaps = sessionData.totalLaps.toInt() and 0xFF,
                                    sessionType = sessionType,
                                    trackTemperature = sessionData.trackTemperature.toInt(),  // ⭐ AGGIUNTO
                                    weather = sessionData.weather.toInt() and 0xFF,
                                    safetyCarStatus = sessionData.safetyCarStatus.toInt() and 0xFF,  // ⭐ NUOVO
                                    bestSector1 = "--.---",
                                    bestSector2 = "--.---",
                                    bestSector3 = "--.---"
                                )
                            } else {
                                // Se la sessione non è cambiata, aggiorna solo sessionType
                                _telemetryState.value = _telemetryState.value.copy(
                                    totalLaps = sessionData.totalLaps.toInt() and 0xFF,
                                    sessionType = sessionType,
                                    trackTemperature = sessionData.trackTemperature.toInt(),  // ⭐ AGGIUNTO
                                    weather = sessionData.weather.toInt() and 0xFF,
                                    safetyCarStatus = sessionData.safetyCarStatus.toInt() and 0xFF,  // ⭐ NUOVO
                                )
                            }
                        }

                        2 -> { // Lap Data
                            val lapData = parsePacketLapData(packet.data, packet.length)
                            val myLapData = lapData.lapData[header.playerCarIndex]

                            val lastLapTimeMS = myLapData.lastLapTimeInMS
                            val lastLapTime = formatLapTime(lastLapTimeMS)
                            val currentLap = myLapData.currentLapNum.toInt() and 0xFF
                            val myPos = myLapData.carPosition.toInt() and 0xFF

                            val currentLapTimeMS = myLapData.currentLapTimeInMS
                            val currentLapTime = formatLapTime(currentLapTimeMS)

                            if (lastLapTimeMS > 0 && lastLapTimeMS < bestLapTimeMS) {
                                bestLapTimeMS = lastLapTimeMS
                                Log.d("UDP_LAP_DEBUG", "✅ NEW BEST LAP! $bestLapTimeMS ms")
                            }

                            val deltaToPersonalBest =
                                if (lastLapTimeMS > 0 && bestLapTimeMS != Int.MAX_VALUE) {
                                    val deltaMS = lastLapTimeMS - bestLapTimeMS
                                    val sign = if (deltaMS >= 0) "+" else ""
                                    "$sign${String.format("%.3f", deltaMS / 1000.0)}"
                                } else {
                                    "+0.000"
                                }

                            val bestLapTime = if (bestLapTimeMS != Int.MAX_VALUE) {
                                formatLapTime(bestLapTimeMS)
                            } else {
                                "--:--.---"
                            }

                            // ⭐ Aggiorna solo posizioni e best lap di TUTTI i piloti (NO settori)
                            for (i in 0..21) {
                                val driver = lapData.lapData[i]

                                driverPositions[i] = driver.carPosition.toInt() and 0xFF
                                driverDeltas[i] = driver.deltaToRaceLeaderMSPart.toInt() and 0xFFFF
                                driverLastLapTimes[i] = driver.lastLapTimeInMS

                                val driverLastLap = driver.lastLapTimeInMS
                                if (driverLastLap > 0 && driverLastLap < driverBestLaps[i]) {
                                    driverBestLaps[i] = driverLastLap
                                }
                            }

                            // ⭐ Session best lap (per il delta)
                            val sessionBestMS =
                                driverBestLaps.filter { it != Int.MAX_VALUE }.minOrNull()
                                    ?: Int.MAX_VALUE

                            // ⭐ DEBUG - Verifica chi ha il session best
                            if (sessionBestMS != Int.MAX_VALUE) {
                                val bestDriverIdx = driverBestLaps.indexOf(sessionBestMS)
                            }

                            val sessionBestLapTime = if (sessionBestMS != Int.MAX_VALUE) {
                                formatLapTime(sessionBestMS)
                            } else {
                                "--:--.---"
                            }

                            val deltaToSessionBest =
                                if (bestLapTimeMS != Int.MAX_VALUE && sessionBestMS != Int.MAX_VALUE) {
                                    val deltaMS = bestLapTimeMS - sessionBestMS
                                    val sign = if (deltaMS >= 0) "+" else ""
                                    "$sign${String.format("%.3f", deltaMS / 1000.0)}"
                                } else {
                                    "+0.000"
                                }

                            val myDelta = myLapData.deltaToRaceLeaderMSPart.toInt() and 0xFFFF
                            val nearbyDrivers = findNearbyDrivers(myPos, myDelta)

                            // ⭐ NUOVO - Lap invalid
                            val lapInvalid = myLapData.currentLapInvalid.toInt() and 0xFF == 1

                            _telemetryState.value = _telemetryState.value.copy(
                                lastLapTime = lastLapTime,
                                bestLapTime = bestLapTime,
                                deltaToPersonalBest = deltaToPersonalBest,
                                currentLap = currentLap,
                                pos = myPos,
                                driver1Ahead = nearbyDrivers.driver1Ahead,
                                driver2Ahead = nearbyDrivers.driver2Ahead,
                                driver1Behind = nearbyDrivers.driver1Behind,
                                driver2Behind = nearbyDrivers.driver2Behind,
                                currentLapTime = currentLapTime,
                                sessionBestLapTime = sessionBestLapTime,
                                deltaToSessionBest = deltaToSessionBest,
                                safetyCarDelta = myLapData.safetyCarDelta,
                                ttLapsCompleted = currentLap,  // Traccia giri completati
                                currentLapInvalid = lapInvalid  // ⭐ NUOVO

                            )
                        }

                        3 -> { // Event Packet
                            val eventData = parsePacketEventData(packet.data, packet.length)


                            // ⭐ Reset quando la sessione finisce o inizia
                            when (eventData.eventStringCode) {
                                "SSTA" -> {
                                    resetTelemetry()
                                }

                                "SEND" -> {
                                    resetTelemetry()
                                }

                                "RTMT" -> {
                                    resetTelemetry()
                                }
                            }
                        }

                        4 -> { // Participants
                            val participants =
                                parsePacketParticipantsData(packet.data, packet.length)

                            for (i in 0..21) {
                                driverNames[i] = participants.participants[i].name
                            }

                        }

                        6 -> { // Car Telemetry
                            val telemetry = parsePacketCarTelemetryData(packet.data, packet.length)
                            val myCarData = telemetry.carTelemetryData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                speed = myCarData.speed,
                                rpm = myCarData.engineRPM,
                                gear = myCarData.gear,
                                suggestedGear = telemetry.suggestedGear,
                                throttle = myCarData.throttle,
                                brake = myCarData.brake,
                                clutch = myCarData.clutch / 100f,  // ⭐ NUOVO - Converti da 0-100 a 0.0-1.0
                                drsActivated = myCarData.drs > 0
                            )
                            //Log.d("DRS", "DRS ACTIVATED= ${myCarData.drs}")
                        }

                        7 -> { // Car Status
                            val status = parsePacketCarStatusData(packet.data, packet.length)
                            val myCarStatus = status.carStatusData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                ersStoreEnergy = myCarStatus.ersStoreEnergy,
                                maxRPM = myCarStatus.maxRPM,
                                drsAllowed = (myCarStatus.drsAllowed.toInt() and 0xFF) > 0,
                                ersDeployMode = myCarStatus.ersDeployMode,
                                visualTyreCompound = myCarStatus.visualTyreCompound.toInt() and 0xFF,  // ⭐ NUOVO
                                actualTyreCompound = myCarStatus.actualTyreCompound.toInt() and 0xFF   // ⭐ NUOVO
                            )
                            //Log.d("DRS", "DRS ALLOWED= ${myCarStatus.drsAllowed}")

                        }

                        10 -> { // Car Damage
                            val damage = parsePacketCarDamageData(packet.data, packet.length)
                            val myCarDamage = damage.carDamageData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                tyreWearFL = myCarDamage.tyresWear[2],
                                tyreWearFR = myCarDamage.tyresWear[3],
                                tyreWearRL = myCarDamage.tyresWear[0],
                                tyreWearRR = myCarDamage.tyresWear[1]
                            )
                        }

                        11 -> { // Session History
                            val history = parsePacketSessionHistoryData(packet.data, packet.length)
                            val carIdx = history.carIdx

                            // Processa tutti i giri completati di questo pilota
                            for (i in 0 until history.numLaps) {
                                val lap = history.lapHistoryData[i]

                                // Salta giri non validi
                                if (lap.lapTimeInMS <= 0) continue

                                // ⭐ Sector 1
                                val s1Total = (lap.sector1TimeMinutes * 60000) + lap.sector1TimeInMS
                                if (s1Total > 0 && s1Total < sessionBestS1MS) {
                                    sessionBestS1MS = s1Total
                                    sessionBestS1Driver = carIdx
                                }

                                // ⭐ Sector 2
                                val s2Total = (lap.sector2TimeMinutes * 60000) + lap.sector2TimeInMS
                                if (s2Total > 0 && s2Total < sessionBestS2MS) {
                                    sessionBestS2MS = s2Total
                                    sessionBestS2Driver = carIdx
                                }

                                // ⭐ Sector 3 - CORRETTO!
                                val s3Total = (lap.sector3TimeMinutes * 60000) + lap.sector3TimeInMS
                                if (s3Total > 0 && s3Total < sessionBestS3MS) {
                                    sessionBestS3MS = s3Total
                                    sessionBestS3Driver = carIdx
                                }
                            }

                            // ⭐ Aggiorna lo state con i best sectors corretti
                            val bestSector1 =
                                if (sessionBestS1MS != Int.MAX_VALUE && sessionBestS1Driver >= 0) {
                                    val driverName =
                                        driverNames[sessionBestS1Driver].take(3).uppercase()
                                    "$driverName: ${
                                        String.format(
                                            "%.3f",
                                            sessionBestS1MS / 1000.0
                                        )
                                    }"
                                } else {
                                    "--.---"
                                }

                            val bestSector2 =
                                if (sessionBestS2MS != Int.MAX_VALUE && sessionBestS2Driver >= 0) {
                                    val driverName =
                                        driverNames[sessionBestS2Driver].take(3).uppercase()
                                    "$driverName: ${
                                        String.format(
                                            "%.3f",
                                            sessionBestS2MS / 1000.0
                                        )
                                    }"
                                } else {
                                    "--.---"
                                }

                            val bestSector3 =
                                if (sessionBestS3MS != Int.MAX_VALUE && sessionBestS3Driver >= 0) {
                                    val driverName =
                                        driverNames[sessionBestS3Driver].take(3).uppercase()
                                    "$driverName: ${
                                        String.format(
                                            "%.3f",
                                            sessionBestS3MS / 1000.0
                                        )
                                    }"
                                } else {
                                    "--.---"
                                }

                            _telemetryState.value = _telemetryState.value.copy(
                                bestSector1 = bestSector1,
                                bestSector2 = bestSector2,
                                bestSector3 = bestSector3
                            )
                        }

                        14 -> { // Time Trial Data
                            val ttData = parsePacketTimeTrialData(packet.data, packet.length)

                            // Personal Best
                            val personalBestMS = ttData.personalBestDataSet.lapTimeInMS
                            val personalBestLap = if (personalBestMS > 0) {
                                formatLapTime(personalBestMS)
                            } else {
                                "--:--.---"
                            }

                            // Rival Best
                            val rivalMS = ttData.rivalDataSet.lapTimeInMS
                            val rivalLap = if (rivalMS > 0 && ttData.rivalDataSet.valid == 1) {
                                formatLapTime(rivalMS)
                            } else {
                                "--:--.---"
                            }

                            // Delta
                            val deltaToRival = if (personalBestMS > 0 && rivalMS > 0 && ttData.rivalDataSet.valid == 1) {
                                val deltaMS = personalBestMS - rivalMS
                                val sign = if (deltaMS >= 0) "+" else ""
                                "$sign${String.format("%.3f", deltaMS / 1000.0)}"
                            } else {
                                "+0.000"
                            }

                            // ⭐ Trova chi ha fatto i best sectors (senza nomi)
                            val playerS1 = ttData.playerSessionBestDataSet.sector1TimeInMS
                            val playerS2 = ttData.playerSessionBestDataSet.sector2TimeInMS
                            val playerS3 = ttData.playerSessionBestDataSet.sector3TimeInMS

                            val rivalS1 = if (ttData.rivalDataSet.valid == 1) ttData.rivalDataSet.sector1TimeInMS else Int.MAX_VALUE
                            val rivalS2 = if (ttData.rivalDataSet.valid == 1) ttData.rivalDataSet.sector2TimeInMS else Int.MAX_VALUE
                            val rivalS3 = if (ttData.rivalDataSet.valid == 1) ttData.rivalDataSet.sector3TimeInMS else Int.MAX_VALUE

                            // Determina owner e valore best per ogni settore
                            val (bestS1, bestS1Owner) = if (playerS1 > 0 && rivalS1 > 0) {
                                if (playerS1 <= rivalS1) {
                                    String.format("%.3f", playerS1 / 1000.0) to "YOU"
                                } else {
                                    String.format("%.3f", rivalS1 / 1000.0) to "RIVAL"
                                }
                            } else if (playerS1 > 0) {
                                String.format("%.3f", playerS1 / 1000.0) to "YOU"
                            } else if (rivalS1 > 0) {
                                String.format("%.3f", rivalS1 / 1000.0) to "RIVAL"
                            } else {
                                "--.---" to "YOU"
                            }

                            val (bestS2, bestS2Owner) = if (playerS2 > 0 && rivalS2 > 0) {
                                if (playerS2 <= rivalS2) {
                                    String.format("%.3f", playerS2 / 1000.0) to "YOU"
                                } else {
                                    String.format("%.3f", rivalS2 / 1000.0) to "RIVAL"
                                }
                            } else if (playerS2 > 0) {
                                String.format("%.3f", playerS2 / 1000.0) to "YOU"
                            } else if (rivalS2 > 0) {
                                String.format("%.3f", rivalS2 / 1000.0) to "RIVAL"
                            } else {
                                "--.---" to "YOU"
                            }

                            val (bestS3, bestS3Owner) = if (playerS3 > 0 && rivalS3 > 0) {
                                if (playerS3 <= rivalS3) {
                                    String.format("%.3f", playerS3 / 1000.0) to "YOU"
                                } else {
                                    String.format("%.3f", rivalS3 / 1000.0) to "RIVAL"
                                }
                            } else if (playerS3 > 0) {
                                String.format("%.3f", playerS3 / 1000.0) to "YOU"
                            } else if (rivalS3 > 0) {
                                String.format("%.3f", rivalS3 / 1000.0) to "RIVAL"
                            } else {
                                "--.---" to "YOU"
                            }

                            Log.d("UDP_TIME_TRIAL", "🏁 Best: $personalBestLap | Rival: $rivalLap | S1:$bestS1Owner S2:$bestS2Owner S3:$bestS3Owner")

                            _telemetryState.value = _telemetryState.value.copy(
                                ttPersonalBestLap = personalBestLap,
                                ttRivalLap = rivalLap,
                                ttDeltaToRival = deltaToRival,
                                ttSessionBestSector1 = bestS1,
                                ttSessionBestSector2 = bestS2,
                                ttSessionBestSector3 = bestS3,
                                ttBestS1Owner = bestS1Owner,
                                ttBestS2Owner = bestS2Owner,
                                ttBestS3Owner = bestS3Owner
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UDP", "❌ UDP error: ${e.message}", e)
            }
        }
    }

    // ⭐ Funzione per resettare tutti i dati telemetrici
    private fun resetTelemetry() {
        _telemetryState.value = TelemetryState()
        bestLapTimeMS = Int.MAX_VALUE
        driverNames = Array(22) { "" }
        driverPositions = IntArray(22) { 0 }
        driverDeltas = IntArray(22) { 0 }
        driverLastLapTimes = IntArray(22) { 0 }
        driverBestLaps = IntArray(22) { Int.MAX_VALUE }
        driverBestS1 = IntArray(22) { Int.MAX_VALUE }
        driverBestS2 = IntArray(22) { Int.MAX_VALUE }
        driverBestS3 = IntArray(22) { Int.MAX_VALUE }

        // ⭐ Reset session tracking
        currentSessionType = SessionType.UNKNOWN
        sessionBestS1MS = Int.MAX_VALUE
        sessionBestS2MS = Int.MAX_VALUE
        sessionBestS3MS = Int.MAX_VALUE
        sessionBestS1Driver = -1
        sessionBestS2Driver = -1
        sessionBestS3Driver = -1

        Log.d("UDP_RESET", "✅ Telemetria completamente resettata")
    }

    private fun findNearbyDrivers(myPos: Int, myDelta: Int): NearbyDrivers {
        val driver2PosAhead = myPos - 2
        val driver1PosAhead = myPos - 1
        val driver1PosBehind = myPos + 1
        val driver2PosBehind = myPos + 2

        val idx2Ahead = driverPositions.indexOf(driver2PosAhead)
        val idx1Ahead = driverPositions.indexOf(driver1PosAhead)
        val idx1Behind = driverPositions.indexOf(driver1PosBehind)
        val idx2Behind = driverPositions.indexOf(driver2PosBehind)

        return NearbyDrivers(
            driver1Ahead = if (idx2Ahead >= 0) createDriverInfo(idx2Ahead, myDelta) else DriverInfo(),
            driver2Ahead = if (idx1Ahead >= 0) createDriverInfo(idx1Ahead, myDelta) else DriverInfo(),
            driver1Behind = if (idx1Behind >= 0) createDriverInfo(idx1Behind, myDelta) else DriverInfo(),
            driver2Behind = if (idx2Behind >= 0) createDriverInfo(idx2Behind, myDelta) else DriverInfo()
        )
    }

    private fun createDriverInfo(driverIdx: Int, myDelta: Int): DriverInfo {
        val name = getDriverShortName(driverNames[driverIdx])

        if (name == "---" || driverNames[driverIdx].isEmpty()) {
            return DriverInfo(
                name = "---",
                delta = "-.---",
                lastLapTime = "--:--.---",
                position = 0
            )
        }

        val driverDelta = driverDeltas[driverIdx]
        val deltaToMe = driverDelta - myDelta
        val sign = if (deltaToMe >= 0) "+" else ""

        val driverLastLap = formatLapTime(driverLastLapTimes[driverIdx])

        return DriverInfo(
            name = name,
            delta = "$sign${String.format("%.3f", deltaToMe / 1000.0)}",
            lastLapTime = driverLastLap,
            position = driverPositions[driverIdx]
        )
    }

    private fun getDriverShortName(fullName: String): String {
        if (fullName.isEmpty()) return "---"
        return fullName.take(3).uppercase()
    }


    fun stop() {
        socket?.close()
        socket = null
    }
}

// Data class helper
data class NearbyDrivers(
    val driver1Ahead: DriverInfo,
    val driver2Ahead: DriverInfo,
    val driver1Behind: DriverInfo,
    val driver2Behind: DriverInfo
)

fun formatLapTime(timeInMS: Int): String {
    if (timeInMS <= 0) return "--:--.---"

    val totalSeconds = timeInMS / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = timeInMS % 1000

    return String.format("%d:%02d.%03d", minutes, seconds, milliseconds)
}

