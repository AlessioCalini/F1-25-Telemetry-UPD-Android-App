package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class CarTelemetryData(
    val speed: Int,
    val throttle: Float,
    val steer: Float,
    val brake: Float,
    val clutch: Int,
    val gear: Int,
    val engineRPM: Int,
    val drs: Int,
    val revLightsPercent: Int,
    val revLightsBitValue: Int,
    val brakesTemperature: IntArray,
    val tyresSurfaceTemperature: IntArray,
    val tyresInnerTemperature: IntArray,
    val engineTemperature: Int,
    val tyresPressure: FloatArray,
    val surfaceType: IntArray  // ⭐ Ora viene letto
)

data class PacketCarTelemetryData(
    val header: PacketHeader,
    val carTelemetryData: Array<CarTelemetryData>,
    val mfdPanelIndex: Int,
    val mfdPanelIndexSecondaryPlayer: Int,
    val suggestedGear: Int  // ⭐ Questo è nel packet, non in CarTelemetryData
)

fun parsePacketCarTelemetryData(data: ByteArray, length: Int): PacketCarTelemetryData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    // Parse header
    val header = parsePacketHeader(data, length)

    // Salta l'header (29 bytes)
    buffer.position(29)

    // Parse telemetry data per tutte le 22 auto
    val carTelemetryData = Array(22) {
        val speed = buffer.short.toInt() and 0xFFFF           // uint16
        val throttle = buffer.float                           // float
        val steer = buffer.float                              // float
        val brake = buffer.float                              // float
        val clutch = buffer.get().toInt() and 0xFF            // uint8
        val gear = buffer.get().toInt()                       // int8
        val engineRPM = buffer.short.toInt() and 0xFFFF       // uint16
        val drs = buffer.get().toInt() and 0xFF               // uint8
        val revLightsPercent = buffer.get().toInt() and 0xFF  // uint8
        val revLightsBitValue = buffer.short.toInt() and 0xFFFF // uint16

        // Brake temperatures [RL, RR, FL, FR]
        val brakesTemperature = IntArray(4) {
            buffer.short.toInt() and 0xFFFF  // uint16
        }

        // Tyres surface temperature [RL, RR, FL, FR]
        val tyresSurfaceTemperature = IntArray(4) {
            buffer.get().toInt() and 0xFF    // uint8
        }

        // Tyres inner temperature [RL, RR, FL, FR]
        val tyresInnerTemperature = IntArray(4) {
            buffer.get().toInt() and 0xFF    // uint8
        }

        val engineTemperature = buffer.short.toInt() and 0xFFFF // uint16

        // Tyres pressure [RL, RR, FL, FR]
        val tyresPressure = FloatArray(4) {
            buffer.float  // float
        }

        // ⭐ Surface type for each wheel [RL, RR, FL, FR] - ORA VIENE LETTO
        val surfaceType = IntArray(4) {
            buffer.get().toInt() and 0xFF    // uint8
        }

        CarTelemetryData(
            speed = speed,
            throttle = throttle,
            steer = steer,
            brake = brake,
            clutch = clutch,
            gear = gear,
            engineRPM = engineRPM,
            drs = drs,
            revLightsPercent = revLightsPercent,
            revLightsBitValue = revLightsBitValue,
            brakesTemperature = brakesTemperature,
            tyresSurfaceTemperature = tyresSurfaceTemperature,
            tyresInnerTemperature = tyresInnerTemperature,
            engineTemperature = engineTemperature,
            tyresPressure = tyresPressure,
            surfaceType = surfaceType  // ⭐ Aggiunto
        )
    }

    // ⭐ Dopo aver letto tutti i 22 car data, leggi i campi finali del packet
    val mfdPanelIndex = buffer.get().toInt() and 0xFF                    // uint8
    val mfdPanelIndexSecondaryPlayer = buffer.get().toInt() and 0xFF     // uint8
    val suggestedGear = buffer.get().toInt()                             // int8 (può essere negativo? No, 0-8)

    return PacketCarTelemetryData(
        header = header,
        carTelemetryData = carTelemetryData,
        mfdPanelIndex = mfdPanelIndex,
        mfdPanelIndexSecondaryPlayer = mfdPanelIndexSecondaryPlayer,
        suggestedGear = suggestedGear  // ⭐ Questo è il suggestedGear globale del packet
    )
}