package com.example.data

import java.util.Calendar

object DateConverter {

    fun getIslamicDate(calendar: Calendar): String {
        // Safe conversion using java.time if API level is 26+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                val hijriDate = java.time.chrono.HijrahDate.now()
                val formatter = java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH)
                return hijriDate.format(formatter) + " AH"
            } catch (e: Exception) {
                // Fallback
            }
        }
        
        // Fallback calculation using Julian Day based Tabular Islamic Calendar
        val jd = getJulianDay(calendar)
        val epoch = 1948440 // JDN for 1 Muharram 1 AH
        val diff = (jd - epoch).toInt()
        
        val totalCycles = diff / 10631
        val cycleRemaining = diff % 10631
        
        var hY = totalCycles * 30 + 1
        var daysLeft = cycleRemaining
        
        // Leap years in 30-year Islamic cycle: 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29
        val leapYears = setOf(2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29)
        
        while (true) {
            val isLeap = leapYears.contains((hY - 1) % 30 + 1)
            val daysInYear = if (isLeap) 355 else 354
            if (daysLeft < daysInYear) break
            daysLeft -= daysInYear
            hY++
        }
        
        var hM = 1
        while (hM <= 12) {
            val daysInMonth = if (hM % 2 != 0) 30 else {
                if (hM == 12 && leapYears.contains((hY - 1) % 30 + 1)) 30 else 29
            }
            if (daysLeft < daysInMonth) break
            daysLeft -= daysInMonth
            hM++
        }
        val hD = daysLeft + 1
        
        val months = listOf(
            "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' ath-Thani",
            "Jumada al-Awwal", "Jumada ath-Thani", "Rajab", "Sha'ban",
            "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
        )
        
        return "$hD ${months.getOrElse(hM - 1) { "Ramadan" }} $hY AH"
    }

    fun getBanglaDate(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 1-indexed
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        
        val monthsBangla = listOf(
            "Baishakh" to "বৈশাখ",
            "Jyeshtha" to "জ্যৈষ্ঠ",
            "Ashadha" to "আষাঢ়",
            "Shravan" to "শ্রাবণ",
            "Bhadra" to "ভাদ্র",
            "Ashwin" to "আশ্বিন",
            "Kartik" to "কার্তিক",
            "Agrahayan" to "অগ্রহায়ণ",
            "Paush" to "পৌষ",
            "Magh" to "মাঘ",
            "Falgun" to "ফাল্গুন",
            "Chaitra" to "চৈত্র"
        )

        // Calculate days from April 14 (which is 1st Baishakh)
        val gregDays = intArrayOf(0, 31, if (isLeapYear) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        
        var currentDayOfYear = 0
        for (m in 1 until month) {
            currentDayOfYear += gregDays[m]
        }
        currentDayOfYear += day
        
        val april14Day = 31 + gregDays[2] + 31 + 14 // Jan + Feb + Mar + 14 days of April
        
        var bYear = year - 593
        var bDayOfYear = 0
        
        if (currentDayOfYear >= april14Day) {
            bDayOfYear = currentDayOfYear - april14Day + 1
        } else {
            bYear -= 1
            val totalGregDays = if (isLeapYear) 366 else 365
            bDayOfYear = totalGregDays - april14Day + currentDayOfYear + 1
        }
        
        // Bangladesh revised calendar rules
        val bMonthDays = intArrayOf(
            31, 31, 31, 31, 31, // Baishakh to Bhadra
            30, 30, 30, 30, 30, // Ashwin to Magh
            if (isLeapYear) 31 else 30, // Falgun
            30 // Chaitra
        )
        
        var bMonthIndex = 0
        var bDay = bDayOfYear
        
        while (bMonthIndex < 12) {
            val mDays = bMonthDays[bMonthIndex]
            if (bDay <= mDays) {
                break
            }
            bDay -= mDays
            bMonthIndex++
        }
        
        val banglaMonthName = monthsBangla.getOrElse(bMonthIndex) { "Baishakh" to "বৈশাখ" }
        
        fun toBanglaDigits(num: Int): String {
            val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
            return num.toString().map { char ->
                if (char.isDigit()) banglaDigits[char - '0'] else char
            }.joinToString("")
        }
        
        val bDayBangla = toBanglaDigits(bDay)
        val bYearBangla = toBanglaDigits(bYear)
        
        return "$bDayBangla ${banglaMonthName.second} $bYearBangla বঙ্গাব্দ"
    }

    private fun getJulianDay(calendar: Calendar): Double {
        var year = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        if (month <= 2) {
            year -= 1
            month += 12
        }
        val a = year / 100
        val b = 2 - a + (a / 4)
        val jd = (365.25 * (year + 4716)).toInt() + (30.6001 * (month + 1)).toInt() + day + b - 1524.5
        return jd
    }
}
