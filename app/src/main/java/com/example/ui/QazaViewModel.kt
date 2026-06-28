package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QazaDatabase
import com.example.data.QazaHistoryEntity
import com.example.data.QazaPrayerEntity
import com.example.data.QazaRepository
import com.example.data.QazaSettingsEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class QazaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QazaRepository

    init {
        val database = QazaDatabase.getDatabase(application)
        repository = QazaRepository(database.qazaDao())
    }

    val prayers: StateFlow<List<QazaPrayerEntity>> = repository.prayers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val history: StateFlow<List<QazaHistoryEntity>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<QazaSettingsEntity?> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Derived State Flow: Stats
    val stats: StateFlow<QazaStats> = prayers.map { prayerList ->
        val totalMissed = prayerList.sumOf { it.totalMissed }
        val totalCompleted = prayerList.sumOf { it.completed }
        val remaining = prayerList.sumOf { it.remaining }
        val progress = if (totalMissed > 0) totalCompleted.toFloat() / totalMissed else 0f
        QazaStats(
            totalMissed = totalMissed,
            totalCompleted = totalCompleted,
            remaining = remaining,
            progressPercentage = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QazaStats()
    )

    // Derived State Flow: Completed Today
    val completedToday: StateFlow<Int> = history.map { historyList ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        historyList.filter { it.date == todayStr }.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Format remaining days to years, months, days format
    fun getEstimatedRemainingTime(remainingPrayers: Int, dailyGoal: Int): String {
        if (remainingPrayers <= 0 || dailyGoal <= 0) return "Completed! 🎉"
        
        // dailyGoal is in total prayers.
        // There are 5 prayers per missed day.
        // Therefore, if user completes `dailyGoal` prayers per day,
        // they are making up `dailyGoal / 5.0` days of missed prayers per calendar day.
        // Let's calculate total days needed to complete remaining.
        val daysNeeded = Math.ceil(remainingPrayers.toDouble() / dailyGoal).toInt()
        
        val years = daysNeeded / 365
        val remainingDaysAfterYears = daysNeeded % 365
        val months = remainingDaysAfterYears / 30
        val days = remainingDaysAfterYears % 30

        val sb = java.lang.StringBuilder()
        if (years > 0) sb.append("$years ${if (years == 1) "year" else "years"} ")
        if (months > 0) sb.append("$months ${if (months == 1) "month" else "months"} ")
        if (days > 0 || sb.isEmpty()) sb.append("$days ${if (days == 1) "day" else "days"}")

        return sb.toString().trim()
    }

    // UI actions
    fun completePrayer(prayerName: String, date: Date = Date(), amount: Int = 1) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            repository.completePrayer(prayerName, dateStr, amount)
        }
    }

    fun completeMultiplePrayers(prayersToLog: Map<String, Int>, date: Date = Date()) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            repository.completeMultiplePrayers(prayersToLog, dateStr)
        }
    }

    fun undoPrayer(historyId: Int) {
        viewModelScope.launch {
            repository.undoPrayer(historyId)
        }
    }

    fun setupOnboarding(years: Int, months: Int, days: Int, dailyGoal: Int) {
        viewModelScope.launch {
            repository.setupOnboarding(years, months, days, dailyGoal)
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(goal)
        }
    }

    fun updateCalculation(years: Int, months: Int, days: Int) {
        viewModelScope.launch {
            repository.updateCalculation(years, months, days)
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.resetAll()
        }
    }

    // Motivational Quote Generator (Sincere, Hopeful, Islamic and Peaceful)
    fun getMotivationalMessage(streak: Int): String {
        val quotes = listOf(
            "Allah loves the deeds that are consistent, even if they are small. Take it step by step.",
            "Each prayer you make up is a step closer to peace and spiritual light. Be gentle with yourself.",
            "\"And establish prayer... Indeed, good deeds do away with misdeeds.\" (Quran 11:114)",
            "The door of repentance is always open. Your effort to return to consistency is beautiful to Allah.",
            "Do not feel discouraged by the past. The focus is on your sincerity and action today.",
            "\"Indeed, with hardship [will be] ease.\" (Quran 94:6). Keep moving forward at your own comfortable pace.",
            "You are rebuilding your connection with your Creator. Celebrate every single prayer you complete.",
            "Patience and consistency are keys to spiritual success. You are doing great."
        )
        // Select quote based on hash of current day to change daily, or randomly
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }
}

data class QazaStats(
    val totalMissed: Int = 0,
    val totalCompleted: Int = 0,
    val remaining: Int = 0,
    val progressPercentage: Float = 0f
)
