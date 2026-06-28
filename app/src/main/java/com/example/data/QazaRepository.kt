package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class QazaRepository(private val qazaDao: QazaDao) {

    val prayers: Flow<List<QazaPrayerEntity>> = qazaDao.getPrayersFlow()
    val history: Flow<List<QazaHistoryEntity>> = qazaDao.getHistoryFlow()
    val settings: Flow<QazaSettingsEntity?> = qazaDao.getSettingsFlow()

    suspend fun completePrayer(prayerName: String, dateStr: String, amount: Int = 1): Boolean {
        val prayersList = qazaDao.getPrayers()
        val targetPrayer = prayersList.find { it.name == prayerName } ?: return false

        // We can't complete more than totalMissed
        if (targetPrayer.completed >= targetPrayer.totalMissed && amount > 0) return false

        val newCompleted = (targetPrayer.completed + amount).coerceIn(0, targetPrayer.totalMissed)
        qazaDao.insertPrayers(listOf(targetPrayer.copy(completed = newCompleted)))

        // Log in history
        val log = QazaHistoryEntity(
            date = dateStr,
            prayerName = prayerName,
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        qazaDao.insertHistory(log)

        // Update streak
        updateStreakAfterLog()

        return true
    }

    suspend fun completeMultiplePrayers(prayersToLog: Map<String, Int>, dateStr: String): Boolean {
        val prayersList = qazaDao.getPrayers().toMutableList()
        val updatedPrayers = mutableListOf<QazaPrayerEntity>()
        var logsInserted = 0

        for ((prayerName, amount) in prayersToLog) {
            if (amount <= 0) continue
            val index = prayersList.indexOfFirst { it.name == prayerName }
            if (index != -1) {
                val p = prayersList[index]
                val newCompleted = (p.completed + amount).coerceIn(0, p.totalMissed)
                val updated = p.copy(completed = newCompleted)
                prayersList[index] = updated
                updatedPrayers.add(updated)

                val log = QazaHistoryEntity(
                    date = dateStr,
                    prayerName = prayerName,
                    amount = amount,
                    timestamp = System.currentTimeMillis()
                )
                qazaDao.insertHistory(log)
                logsInserted++
            }
        }

        if (updatedPrayers.isNotEmpty()) {
            qazaDao.insertPrayers(updatedPrayers)
            updateStreakAfterLog()
            return true
        }
        return false
    }

    private suspend fun updateStreakAfterLog() {
        val currentSettings = qazaDao.getSettings() ?: QazaSettingsEntity()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        var currentStreak = currentSettings.streak
        val lastActive = currentSettings.lastActiveDate

        if (lastActive.isEmpty()) {
            currentStreak = 1
        } else if (lastActive == todayStr) {
            // Already logged today, streak remains unchanged
        } else {
            // Check if last active was yesterday
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            if (lastActive == yesterdayStr) {
                currentStreak += 1
            } else {
                currentStreak = 1 // reset streak to 1
            }
        }

        qazaDao.insertSettings(
            currentSettings.copy(
                streak = currentStreak,
                lastActiveDate = todayStr
            )
        )
    }

    suspend fun undoPrayer(historyId: Int): Boolean {
        val log = qazaDao.getHistoryById(historyId) ?: return false
        val prayersList = qazaDao.getPrayers()
        val targetPrayer = prayersList.find { it.name == log.prayerName } ?: return false

        val newCompleted = (targetPrayer.completed - log.amount).coerceAtLeast(0)
        qazaDao.insertPrayers(listOf(targetPrayer.copy(completed = newCompleted)))

        qazaDao.deleteHistoryById(historyId)
        return true
    }

    suspend fun setupOnboarding(years: Int, months: Int, days: Int, dailyGoal: Int): Boolean {
        val totalDays = (years * 365) + (months * 30) + days
        val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val prayersList = prayerNames.map { name ->
            QazaPrayerEntity(name = name, totalMissed = totalDays, completed = 0)
        }
        qazaDao.insertPrayers(prayersList)

        val settingsEntity = QazaSettingsEntity(
            id = 1,
            missedYears = years,
            missedMonths = months,
            missedDays = days,
            dailyGoal = dailyGoal,
            isOnboarded = true,
            streak = 0,
            lastActiveDate = ""
        )
        qazaDao.insertSettings(settingsEntity)
        return true
    }

    suspend fun updateDailyGoal(goal: Int) {
        val currentSettings = qazaDao.getSettings() ?: QazaSettingsEntity()
        qazaDao.insertSettings(currentSettings.copy(dailyGoal = goal))
    }

    suspend fun updateCalculation(years: Int, months: Int, days: Int) {
        val totalDays = (years * 365) + (months * 30) + days
        val currentPrayers = qazaDao.getPrayers()
        
        // If current prayers list is empty (e.g. somehow reset), recreate them
        val updatedPrayers = if (currentPrayers.isEmpty()) {
            listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").map { name ->
                QazaPrayerEntity(name = name, totalMissed = totalDays, completed = 0)
            }
        } else {
            currentPrayers.map { prayer ->
                prayer.copy(totalMissed = totalDays)
            }
        }
        qazaDao.insertPrayers(updatedPrayers)

        val currentSettings = qazaDao.getSettings() ?: QazaSettingsEntity()
        qazaDao.insertSettings(
            currentSettings.copy(
                missedYears = years,
                missedMonths = months,
                missedDays = days,
                isOnboarded = true
            )
        )
    }

    suspend fun resetAll() {
        qazaDao.clearHistory()
        qazaDao.clearPrayers()
        qazaDao.insertSettings(
            QazaSettingsEntity(
                id = 1,
                missedYears = 0,
                missedMonths = 0,
                missedDays = 0,
                dailyGoal = 5,
                isOnboarded = false,
                streak = 0,
                lastActiveDate = ""
            )
        )
    }
}
