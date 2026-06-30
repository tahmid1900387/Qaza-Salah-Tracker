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

    suspend fun setupOnboarding(years: Int, months: Int, days: Int, dailyGoal: Int, userName: String, selectedCity: String = "Dhaka"): Boolean {
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
            lastActiveDate = "",
            userName = userName.trim().ifEmpty { "User" },
            selectedCity = selectedCity
        )
        qazaDao.insertSettings(settingsEntity)
        return true
    }

    suspend fun updateQazaBacklog(prayerName: String, delta: Int) {
        val prayersList = qazaDao.getPrayers()
        val targetPrayer = prayersList.find { it.name == prayerName } ?: return
        val newTotal = (targetPrayer.totalMissed + delta).coerceAtLeast(0)
        qazaDao.insertPrayers(listOf(targetPrayer.copy(totalMissed = newTotal)))
    }

    suspend fun updateUserName(name: String) {
        val currentSettings = qazaDao.getSettings() ?: QazaSettingsEntity()
        qazaDao.insertSettings(currentSettings.copy(userName = name.trim().ifEmpty { "User" }))
    }

    suspend fun updateSelectedCity(city: String) {
        val currentSettings = qazaDao.getSettings() ?: QazaSettingsEntity()
        qazaDao.insertSettings(currentSettings.copy(selectedCity = city))
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
        qazaDao.clearTasbihs()
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

    // Tasbih Operations
    val tasbihList: Flow<List<TasbihEntity>> = qazaDao.getTasbihListFlow()

    suspend fun addTasbih(name: String, target: Int, initialCount: Int = 0) {
        val tasbih = TasbihEntity(name = name, target = target, count = initialCount, lastUpdated = System.currentTimeMillis())
        qazaDao.insertTasbih(tasbih)
    }

    suspend fun incrementTasbih(id: Int, amount: Int = 1) {
        val current = qazaDao.getTasbihById(id) ?: return
        // No absolute limit to target if they want to count beyond target, but standard behavior keeps it capped or allows overflow.
        // Capping at target can be frustrating if they accidentally click or want to overflow, but user asked "fixed his target then every time he reads he can count and see how many read and how many left". Let's cap at target or support overflow up to target. Let's make it count up to target or even past it, let's coerce to target for the "left" calculation but allow the counter to go beyond, or cap it at target. Let's cap at target as it is a "fixed target" tracker.
        val newCount = (current.count + amount).coerceIn(0, current.target)
        qazaDao.insertTasbih(current.copy(count = newCount, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun decrementTasbih(id: Int, amount: Int = 1) {
        val current = qazaDao.getTasbihById(id) ?: return
        val newCount = (current.count - amount).coerceAtLeast(0)
        qazaDao.insertTasbih(current.copy(count = newCount, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun resetTasbih(id: Int) {
        val current = qazaDao.getTasbihById(id) ?: return
        qazaDao.insertTasbih(current.copy(count = 0, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun updateTasbihTarget(id: Int, target: Int) {
        val current = qazaDao.getTasbihById(id) ?: return
        qazaDao.insertTasbih(current.copy(target = target, lastUpdated = System.currentTimeMillis()))
    }

    suspend fun deleteTasbih(id: Int) {
        val current = qazaDao.getTasbihById(id) ?: return
        qazaDao.deleteTasbih(current)
    }
}
