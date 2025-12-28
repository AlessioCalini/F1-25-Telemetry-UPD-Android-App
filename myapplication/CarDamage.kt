package com.example.myapplication

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class CarDamageData(
    val tyresWear: FloatArray,           // 4 float - Usura gomme (%)
    val tyresDamage: ByteArray,          // 4 byte - Danni gomme (%)
    val brakesDamage: ByteArray,         // 4 byte - Danni freni (%)
    val tyreBlisters: ByteArray,         // 4 byte - Bolle gomme (%)
    val frontLeftWingDamage: Byte,       // Danno ala anteriore sx (%)
    val frontRightWingDamage: Byte,      // Danno ala anteriore dx (%)
    val rearWingDamage: Byte,            // Danno ala posteriore (%)
    val floorDamage: Byte,               // Danno fondo (%)
    val diffuserDamage: Byte,            // Danno diffusore (%)
    val sidepodDamage: Byte,             // Danno sidepod (%)
    val drsFault: Byte,                  // Fault DRS (0=OK, 1=fault)
    val ersFault: Byte,                  // Fault ERS (0=OK, 1=fault)
    val gearBoxDamage: Byte,             // Danno cambio (%)
    val engineDamage: Byte,              // Danno motore (%)
    val engineMGUHWear: Byte,            // Usura MGU-H (%)
    val engineESWear: Byte,              // Usura ES (%)
    val engineCEWear: Byte,              // Usura CE (%)
    val engineICEWear: Byte,             // Usura ICE (%)
    val engineMGUKWear: Byte,            // Usura MGU-K (%)
    val engineTCWear: Byte,              // Usura TC (%)
    val engineBlown: Byte,               // Motore esploso (0=OK, 1=fault)
    val engineSeized: Byte               // Motore grippato (0=OK, 1=fault)
)

data class PacketCarDamageData(
    val header: PacketHeader,
    val carDamageData: Array<CarDamageData>
)

fun parsePacketCarDamageData(data: ByteArray, length: Int): PacketCarDamageData {
    val buffer = ByteBuffer
        .wrap(data, 0, Math.min(length, data.size))
        .order(ByteOrder.LITTLE_ENDIAN)

    val header = parsePacketHeader(data, length)
    buffer.position(29) // Skip header

    val carDamageData = Array(22) {
        // Tyres Wear (4 float)
        val tyresWear = FloatArray(4) { buffer.float }

        // Tyres Damage (4 byte)
        val tyresDamage = ByteArray(4) { buffer.get() }

        // Brakes Damage (4 byte)
        val brakesDamage = ByteArray(4) { buffer.get() }

        // Tyre Blisters (4 byte)
        val tyreBlisters = ByteArray(4) { buffer.get() }

        CarDamageData(
            tyresWear = tyresWear,
            tyresDamage = tyresDamage,
            brakesDamage = brakesDamage,
            tyreBlisters = tyreBlisters,
            frontLeftWingDamage = buffer.get(),
            frontRightWingDamage = buffer.get(),
            rearWingDamage = buffer.get(),
            floorDamage = buffer.get(),
            diffuserDamage = buffer.get(),
            sidepodDamage = buffer.get(),
            drsFault = buffer.get(),
            ersFault = buffer.get(),
            gearBoxDamage = buffer.get(),
            engineDamage = buffer.get(),
            engineMGUHWear = buffer.get(),
            engineESWear = buffer.get(),
            engineCEWear = buffer.get(),
            engineICEWear = buffer.get(),
            engineMGUKWear = buffer.get(),
            engineTCWear = buffer.get(),
            engineBlown = buffer.get(),
            engineSeized = buffer.get()
        )
    }

    return PacketCarDamageData(header, carDamageData)
}