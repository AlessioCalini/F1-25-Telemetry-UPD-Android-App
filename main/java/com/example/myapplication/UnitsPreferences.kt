package com.example.myapplication

import android.content.Context

object UnitsPreferences {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_SPEED_UNIT = "speed_unit"
    private const val KEY_TYRE_DISPLAY = "tyre_display"
    private const val KEY_TEMP_UNIT = "temp_unit"

    enum class SpeedUnit {
        KMH,  // Kilometers per hour
        MPH   // Miles per hour
    }

    enum class TyreDisplay {
        WEAR,         // Mostra usura (%)
        TEMPERATURE   // Mostra temperatura (°C o °F)
    }

    enum class TempUnit {
        CELSIUS,
        FAHRENHEIT
    }

    // ========== SPEED UNIT ==========

    fun getSpeedUnit(context: Context): SpeedUnit {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unit = prefs.getString(KEY_SPEED_UNIT, "KMH") ?: "KMH"
        return if (unit == "MPH") SpeedUnit.MPH else SpeedUnit.KMH
    }

    fun setSpeedUnit(context: Context, unit: SpeedUnit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SPEED_UNIT, unit.name).apply()
    }

    fun getSpeedUnitLabel(unit: SpeedUnit): String {
        return when (unit) {
            SpeedUnit.KMH -> "KM/H"
            SpeedUnit.MPH -> "MPH"
        }
    }

    fun convertSpeed(speedKmh: Int, toUnit: SpeedUnit): Int {
        return when (toUnit) {
            SpeedUnit.KMH -> speedKmh
            SpeedUnit.MPH -> (speedKmh * 0.621371).toInt()
        }
    }

    // ========== TYRE DISPLAY ==========

    fun getTyreDisplay(context: Context): TyreDisplay {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val display = prefs.getString(KEY_TYRE_DISPLAY, "WEAR") ?: "WEAR"
        return if (display == "TEMPERATURE") TyreDisplay.TEMPERATURE else TyreDisplay.WEAR
    }

    fun setTyreDisplay(context: Context, display: TyreDisplay) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TYRE_DISPLAY, display.name).apply()
    }

    fun getTyreDisplayLabel(display: TyreDisplay): String {
        return when (display) {
            TyreDisplay.WEAR -> "Wear (%)"
            TyreDisplay.TEMPERATURE -> "Temperature"
        }
    }

    // ========== TEMPERATURE UNIT ==========

    fun getTempUnit(context: Context): TempUnit {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unit = prefs.getString(KEY_TEMP_UNIT, "CELSIUS") ?: "CELSIUS"
        return if (unit == "FAHRENHEIT") TempUnit.FAHRENHEIT else TempUnit.CELSIUS
    }

    fun setTempUnit(context: Context, unit: TempUnit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TEMP_UNIT, unit.name).apply()
    }

    fun getTempUnitLabel(unit: TempUnit): String {
        return when (unit) {
            TempUnit.CELSIUS -> "°C"
            TempUnit.FAHRENHEIT -> "°F"
        }
    }

    fun convertTemp(tempCelsius: Int, toUnit: TempUnit): Int {
        return when (toUnit) {
            TempUnit.CELSIUS -> tempCelsius
            TempUnit.FAHRENHEIT -> (tempCelsius * 9 / 5) + 32
        }
    }
}