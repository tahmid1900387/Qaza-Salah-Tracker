package com.example.data

import java.util.Calendar
import kotlin.math.*

data class CityConfig(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: Double
)

object PrayerTimeCalculator {
    val CITIES = listOf(
        CityConfig("Mecca", 21.4225, 39.8262, 3.0),
        CityConfig("Dhaka", 23.8103, 90.4125, 6.0),
        CityConfig("London", 51.5074, -0.1278, 1.0),
        CityConfig("New York", 40.7128, -74.0060, -4.0),
        CityConfig("Cairo", 30.0444, 31.2357, 3.0),
        CityConfig("Jakarta", -6.2088, 106.8456, 7.0),
        CityConfig("Kuala Lumpur", 3.1390, 101.6869, 8.0),
        CityConfig("Istanbul", 41.0082, 28.9784, 3.0),
        CityConfig("Karachi", 24.8607, 67.0011, 5.0),
        CityConfig("Sydney", -33.8688, 151.2093, 10.0),
        CityConfig("Los Angeles", 34.0522, -118.2437, -7.0),
        CityConfig("Dubai", 25.2048, 55.2708, 4.0)
    )

    fun getCity(name: String): CityConfig {
        return CITIES.find { it.name.lowercase() == name.lowercase() } ?: CITIES[1] // Default to Dhaka
    }

    // Mathematical calculations for 5 prayer times + Sunrise
    fun calculatePrayerTimes(city: CityConfig, dayOfYear: Int): Map<String, String> {
        val latRad = Math.toRadians(city.latitude)
        
        // 1. Declination of Sun
        val declination = 23.45 * sin(Math.toRadians(360.0 * (284 + dayOfYear) / 365.0))
        val declRad = Math.toRadians(declination)
        
        // 2. Equation of Time
        val b = Math.toRadians(360.0 * (dayOfYear - 81) / 364.0)
        val eot = 9.87 * sin(2.0 * b) - 7.53 * cos(b) - 1.5 * sin(b) // in minutes
        
        // 3. Midday (Dhuhr) in local time
        val dhuhrDec = 12.0 + city.timezone - (city.longitude / 15.0) - (eot / 60.0)
        
        // Helper to convert decimal hour to standard String format (12-hour AM/PM)
        fun formatHour(decimalHour: Double): String {
            var normHour = (decimalHour + 24.0) % 24.0
            val hours = normHour.toInt()
            var mins = ((normHour - hours) * 60.0).roundToInt()
            var finalHour = hours
            if (mins >= 60) {
                finalHour += 1
                mins -= 60
            }
            finalHour = (finalHour + 24) % 24
            
            val period = if (finalHour >= 12) "PM" else "AM"
            val displayHour = when {
                finalHour == 0 -> 12
                finalHour > 12 -> finalHour - 12
                else -> finalHour
            }
            return String.format("%02d:%02d %s", displayHour, mins, period)
        }

        // 4. Sunrise and Sunset (Maghrib)
        val cosH = (sin(Math.toRadians(-0.833)) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val hAngle = if (cosH in -1.0..1.0) Math.toDegrees(acos(cosH)) else 90.0
        
        val sunriseDec = dhuhrDec - (hAngle / 15.0)
        val maghribDec = dhuhrDec + (hAngle / 15.0) // Sunset is Maghrib
        
        // 5. Fajr (-18 degrees angle)
        val cosHFajr = (sin(Math.toRadians(-18.0)) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val hFajr = if (cosHFajr in -1.0..1.0) Math.toDegrees(acos(cosHFajr)) else 115.0
        val fajrDec = dhuhrDec - (hFajr / 15.0)
        
        // 6. Isha (-18 degrees angle)
        val cosHIsha = (sin(Math.toRadians(-18.0)) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val hIsha = if (cosHIsha in -1.0..1.0) Math.toDegrees(acos(cosHIsha)) else 115.0
        val ishaDec = dhuhrDec + (hIsha / 15.0)
        
        // 7. Asr (Standard shadow ratio = 1)
        val shadowAngle = atan(1.0 + tan(abs(latRad - declRad)))
        val cosHAsr = (sin(shadowAngle) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        val hAsr = if (cosHAsr in -1.0..1.0) Math.toDegrees(acos(cosHAsr)) else 45.0
        val asrDec = dhuhrDec + (hAsr / 15.0)
        
        return mapOf(
            "Fajr" to formatHour(fajrDec),
            "Sunrise" to formatHour(sunriseDec),
            "Dhuhr" to formatHour(dhuhrDec),
            "Asr" to formatHour(asrDec),
            "Maghrib" to formatHour(maghribDec),
            "Isha" to formatHour(ishaDec)
        )
    }

    // Helper to calculate minutes from midnight for time comparisons
    fun timeToMinutes(timeStr: String): Int {
        try {
            val parts = timeStr.trim().split(" ")
            if (parts.size < 2) return 0
            val hm = parts[0].split(":")
            if (hm.size < 2) return 0
            var h = hm[0].toInt()
            val m = hm[1].toInt()
            val isPm = parts[1].uppercase() == "PM"
            if (isPm && h != 12) h += 12
            if (!isPm && h == 12) h = 0
            return h * 60 + m
        } catch (e: Exception) {
            return 0
        }
    }
}
