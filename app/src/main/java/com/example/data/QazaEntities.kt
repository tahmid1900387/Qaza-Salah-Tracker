package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qaza_prayers")
data class QazaPrayerEntity(
    @PrimaryKey val name: String, // "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"
    val totalMissed: Int,
    val completed: Int
) {
    val remaining: Int get() = (totalMissed - completed).coerceAtLeast(0)
    val progress: Float get() = if (totalMissed > 0) completed.toFloat() / totalMissed else 0f
}

@Entity(tableName = "qaza_history")
data class QazaHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "yyyy-MM-dd"
    val prayerName: String, // "Fajr", "Dhuhr", "Asr", "Maghrib", "Isha"
    val amount: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "qaza_settings")
data class QazaSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val missedYears: Int = 0,
    val missedMonths: Int = 0,
    val missedDays: Int = 0,
    val dailyGoal: Int = 5, // daily goal in total prayers, e.g., 5 prayers a day
    val isOnboarded: Boolean = false,
    val streak: Int = 0,
    val lastActiveDate: String = "" // "yyyy-MM-dd"
)
