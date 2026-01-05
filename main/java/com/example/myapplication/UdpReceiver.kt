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

    // ⭐ NUOVO - Tracciamento best lap e sectors di ogni pilota
    private var driverBestLaps = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS1 = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS2 = IntArray(22) { Int.MAX_VALUE }
    private var driverBestS3 = IntArray(22) { Int.MAX_VALUE }

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
                                10 -> SessionType.RACE
                                11 -> SessionType.RACE_2
                                12 -> SessionType.RACE_3
                                13 -> SessionType.TIME_TRIAL
                                else -> SessionType.UNKNOWN
                            }

                            _telemetryState.value = _telemetryState.value.copy(
                                totalLaps = sessionData.totalLaps.toInt() and 0xFF,
                                sessionType = sessionType  // ⭐ NUOVO
                            )
                        }
                        2 -> { // Lap Data
                            val lapData = parsePacketLapData(packet.data, packet.length)
                            val myLapData = lapData.lapData[header.playerCarIndex]

                            val lastLapTimeMS = myLapData.lastLapTimeInMS
                            val lastLapTime = formatLapTime(lastLapTimeMS)
                            val currentLap = myLapData.currentLapNum.toInt() and 0xFF
                            val myPos = myLapData.carPosition.toInt() and 0xFF

                            // ⭐ Current lap time (giro in corso)
                            val currentLapTimeMS = myLapData.currentLapTimeInMS
                            val currentLapTime = formatLapTime(currentLapTimeMS)

                            // Aggiorna best lap personale
                            if (lastLapTimeMS > 0 && lastLapTimeMS < bestLapTimeMS) {
                                bestLapTimeMS = lastLapTimeMS
                                Log.d("UDP_LAP_DEBUG", "✅ NEW BEST LAP! $bestLapTimeMS ms")
                            }

                            // Calcola delta rispetto al best lap personale
                            val deltaToPersonalBest = if (lastLapTimeMS > 0 && bestLapTimeMS != Int.MAX_VALUE) {
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

                            // ⭐ Aggiorna best lap e sectors di TUTTI i piloti
                            for (i in 0..21) {
                                val driver = lapData.lapData[i]

                                // Salva posizioni e delta
                                driverPositions[i] = driver.carPosition.toInt() and 0xFF
                                driverDeltas[i] = driver.deltaToRaceLeaderMSPart.toInt() and 0xFFFF
                                driverLastLapTimes[i] = driver.lastLapTimeInMS

                                // ⭐ Aggiorna best lap del pilota
                                val driverLastLap = driver.lastLapTimeInMS
                                if (driverLastLap > 0 && driverLastLap < driverBestLaps[i]) {
                                    driverBestLaps[i] = driverLastLap
                                }

                                // ⭐ Aggiorna best sectors del pilota
                                val s1 = driver.sector1TimeMSPart.toInt() and 0xFFFF
                                val s2 = driver.sector2TimeMSPart.toInt() and 0xFFFF

                                if (s1 > 0 && s1 < driverBestS1[i]) {
                                    driverBestS1[i] = s1
                                }
                                if (s2 > 0 && s2 < driverBestS2[i]) {
                                    driverBestS2[i] = s2
                                }

                                // ⭐ Calcola Sector 3 (se last lap è valido)
                                if (driverLastLap > 0 && s1 > 0 && s2 > 0) {
                                    val s3 = driverLastLap - s1 - s2
                                    if (s3 > 0 && s3 < driverBestS3[i]) {
                                        driverBestS3[i] = s3
                                    }
                                }
                            }

                            // ⭐ Trova session best lap (miglior tempo tra tutti i piloti)
                            val sessionBestMS = driverBestLaps.filter { it != Int.MAX_VALUE }.minOrNull() ?: Int.MAX_VALUE

                            val sessionBestLapTime = if (sessionBestMS != Int.MAX_VALUE) {
                                formatLapTime(sessionBestMS)
                            } else {
                                "--:--.---"
                            }

                            // ⭐ Delta al session best
                            val deltaToSessionBest = if (bestLapTimeMS != Int.MAX_VALUE && sessionBestMS != Int.MAX_VALUE) {
                                val deltaMS = bestLapTimeMS - sessionBestMS
                                val sign = if (deltaMS >= 0) "+" else ""
                                "$sign${String.format("%.3f", deltaMS / 1000.0)}"
                            } else {
                                "+0.000"
                            }

                            // ⭐ Trova best sectors della sessione
                            val bestS1MS = driverBestS1.filter { it != Int.MAX_VALUE }.minOrNull() ?: Int.MAX_VALUE
                            val bestS2MS = driverBestS2.filter { it != Int.MAX_VALUE }.minOrNull() ?: Int.MAX_VALUE
                            val bestS3MS = driverBestS3.filter { it != Int.MAX_VALUE }.minOrNull() ?: Int.MAX_VALUE

                            val bestSector1 = if (bestS1MS != Int.MAX_VALUE) {
                                String.format("%.1f", bestS1MS / 1000.0)
                            } else {
                                "--.-"
                            }

                            val bestSector2 = if (bestS2MS != Int.MAX_VALUE) {
                                String.format("%.1f", bestS2MS / 1000.0)
                            } else {
                                "--.-"
                            }

                            val bestSector3 = if (bestS3MS != Int.MAX_VALUE) {
                                String.format("%.1f", bestS3MS / 1000.0)
                            } else {
                                "--.-"
                            }

                            // Trova piloti vicini (solo per RACE)
                            val myDelta = myLapData.deltaToRaceLeaderMSPart.toInt() and 0xFFFF
                            val nearbyDrivers = findNearbyDrivers(myPos, myDelta)

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
                                bestSector1 = bestSector1,
                                bestSector2 = bestSector2,
                                bestSector3 = bestSector3
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
                            val participants = parsePacketParticipantsData(packet.data, packet.length)

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
                                throttle = myCarData.throttle,
                                brake = myCarData.brake,
                                drsActivated =myCarData.drs>0 // ⭐ NUOVO
                            )
                            //Log.d("DRS", "DRS ACTIVATED= ${myCarData.drs}")
                        }
                        7 -> { // Car Status
                            val status = parsePacketCarStatusData(packet.data, packet.length)
                            val myCarStatus = status.carStatusData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                ersStoreEnergy = myCarStatus.ersStoreEnergy,
                                maxRPM = myCarStatus.maxRPM,
                                drsAllowed = (myCarStatus.drsAllowed.toInt() and 0xFF) > 0,  // ⭐ NUOVO
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
                    }
                }
            } catch (e: Exception) {
                Log.e("UDP", "❌ UDP error: ${e.message}", e)
            }
        }
    }

    // ⭐ Funzione per resettare tutti i dati telemetrici
    private fun resetTelemetry() {
        _telemetryState.value = TelemetryState()  // Reset a valori default
        bestLapTimeMS = Int.MAX_VALUE
        driverNames = Array(22) { "" }
        driverPositions = IntArray(22) { 0 }
        driverDeltas = IntArray(22) { 0 }
        driverLastLapTimes = IntArray(22) { 0 }

        // ⭐ Reset anche best lap e sectors
        driverBestLaps = IntArray(22) { Int.MAX_VALUE }
        driverBestS1 = IntArray(22) { Int.MAX_VALUE }
        driverBestS2 = IntArray(22) { Int.MAX_VALUE }
        driverBestS3 = IntArray(22) { Int.MAX_VALUE }

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

