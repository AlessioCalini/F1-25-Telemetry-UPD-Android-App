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

    // ⭐ Tracciamento del best lap
    private var bestLapTimeMS: Int = Int.MAX_VALUE

    // ⭐ Tracciamento sessione per reset
    private var currentSessionUID: Long = 0

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

                            _telemetryState.value = _telemetryState.value.copy(
                                totalLaps = sessionData.totalLaps.toInt() and 0xFF
                            )
                        }
                        2 -> { // Lap Data
                            val lapData = parsePacketLapData(packet.data, packet.length)
                            val myLapData = lapData.lapData[header.playerCarIndex]

                            val lastLapTimeMS = myLapData.lastLapTimeInMS
                            val lastLapTime = formatLapTime(lastLapTimeMS)
                            val currentLap = myLapData.currentLapNum.toInt() and 0xFF
                            val pos = myLapData.carPosition.toInt() and 0xFF

                            // ⭐ RESET best lap SOLO se siamo al giro 1 E last lap è ancora 0
                            if (currentLap == 1 && lastLapTimeMS == 0) {
                                bestLapTimeMS = Int.MAX_VALUE
                            }

                            // Aggiorna best lap se è valido e migliore
                            if (lastLapTimeMS > 0 && lastLapTimeMS < bestLapTimeMS) {
                                bestLapTimeMS = lastLapTimeMS
                            }

                            // Calcola delta rispetto al best lap
                            val deltaToPersonalBest = if (lastLapTimeMS > 0 && bestLapTimeMS != Int.MAX_VALUE) {
                                val deltaMS = lastLapTimeMS - bestLapTimeMS
                                val sign = if (deltaMS >= 0) "+" else ""
                                val deltaStr = "$sign${String.format("%.3f", deltaMS / 1000.0)}"


                                deltaStr
                            } else {
                                "+0.000"
                            }

                            val bestLapTime = if (bestLapTimeMS != Int.MAX_VALUE) {
                                formatLapTime(bestLapTimeMS)
                            } else {
                                "--:--.---"
                            }

                            _telemetryState.value = _telemetryState.value.copy(
                                lastLapTime = lastLapTime,
                                bestLapTime = bestLapTime,
                                deltaToPersonalBest = deltaToPersonalBest,
                                currentLap = currentLap,
                                pos = pos
                            )
                        }
                        6 -> { // Car Telemetry
                            val telemetry = parsePacketCarTelemetryData(packet.data, packet.length)
                            val myCarData = telemetry.carTelemetryData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                speed = myCarData.speed,
                                rpm = myCarData.engineRPM,
                                gear = myCarData.gear,
                                throttle = myCarData.throttle,
                                brake = myCarData.brake
                            )
                        }
                        7 -> { // Car Status
                            val status = parsePacketCarStatusData(packet.data, packet.length)
                            val myCarStatus = status.carStatusData[header.playerCarIndex]

                            _telemetryState.value = _telemetryState.value.copy(
                                ersStoreEnergy = myCarStatus.ersStoreEnergy,
                                maxRPM = myCarStatus.maxRPM
                            )
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

    fun stop() {
        socket?.close()
        socket = null
    }
}

fun formatLapTime(timeInMS: Int): String {
    if (timeInMS <= 0) return "--:--.---"

    val totalSeconds = timeInMS / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val milliseconds = timeInMS % 1000

    return String.format("%d:%02d.%03d", minutes, seconds, milliseconds)
}