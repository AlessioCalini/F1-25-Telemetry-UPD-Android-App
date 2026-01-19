package com.example.myapplication

data class DriverInfo(
    val name: String = "",
    val delta: String = "",
    val lastLapTime: String = "",     // ⭐ NUOVO: Last lap del pilota
    val position: Int = 0
)