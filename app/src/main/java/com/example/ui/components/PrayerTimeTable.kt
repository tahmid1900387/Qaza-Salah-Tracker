package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerTimeCalculator
import com.example.data.CityConfig
import com.example.data.DateConverter
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun PrayerTimeTable(
    selectedCityName: String,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic real-time updating state for date & clock
    var currentTime by remember { mutableStateOf(Date()) }
    
    // LaunchedEffect to run a real-time ticking clock
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L) // Update every second
        }
    }

    val calendar = Calendar.getInstance()
    calendar.time = currentTime
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val hour24 = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val currentMinutesSinceMidnight = hour24 * 60 + minute

    // Formatted strings for date & time
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(currentTime)
    val formattedTime = timeFormatter.format(currentTime)

    // Calculate prayer times based on the selected city config
    val cityConfig = remember(selectedCityName) {
        PrayerTimeCalculator.getCity(selectedCityName)
    }
    val prayerTimes = remember(cityConfig, dayOfYear) {
        PrayerTimeCalculator.calculatePrayerTimes(cityConfig, dayOfYear)
    }

    // Determine current & next prayer to highlight
    val currentHighlight = remember(prayerTimes, currentMinutesSinceMidnight) {
        val fajr = PrayerTimeCalculator.timeToMinutes(prayerTimes["Fajr"] ?: "")
        val sunrise = PrayerTimeCalculator.timeToMinutes(prayerTimes["Sunrise"] ?: "")
        val dhuhr = PrayerTimeCalculator.timeToMinutes(prayerTimes["Dhuhr"] ?: "")
        val asr = PrayerTimeCalculator.timeToMinutes(prayerTimes["Asr"] ?: "")
        val maghrib = PrayerTimeCalculator.timeToMinutes(prayerTimes["Maghrib"] ?: "")
        val isha = PrayerTimeCalculator.timeToMinutes(prayerTimes["Isha"] ?: "")

        when {
            currentMinutesSinceMidnight < fajr -> "Isha" // Early morning before Fajr, previous active is Isha
            currentMinutesSinceMidnight < sunrise -> "Fajr" // Between Fajr and Sunrise
            currentMinutesSinceMidnight < dhuhr -> "Sunrise" // Between Sunrise and Dhuhr (Duha/Sunrise highlight)
            currentMinutesSinceMidnight < asr -> "Dhuhr" // Between Dhuhr and Asr
            currentMinutesSinceMidnight < maghrib -> "Asr" // Between Asr and Maghrib
            currentMinutesSinceMidnight < isha -> "Maghrib" // Between Maghrib and Isha
            else -> "Isha" // Night after Isha
        }
    }

    // Dropdown state for city selection
    var dropdownExpanded by remember { mutableStateOf(false) }

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prayer_timetable_card"),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header: Date & Time + City Dropdown Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val datePages = remember(formattedDate, currentTime) {
                        listOf(
                            Pair("English", formattedDate),
                            Pair("Islamic", DateConverter.getIslamicDate(calendar)),
                            Pair("Bangla", DateConverter.getBanglaDate(calendar))
                        )
                    }

                    val pagerState = rememberPagerState(pageCount = { 3 })
                    
                    // Automatically scroll dates every 4500ms for an elegant looping animation effect
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(4500L)
                            val nextPage = (pagerState.currentPage + 1) % 3
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val (label, dateText) = datePages[page]
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (label) {
                                        "English" -> "Gregorian Calendar"
                                        "Islamic" -> "Hijri Calendar"
                                        "Bangla" -> "Bangla Calendar"
                                        else -> ""
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Interactive Dropdown Menu for choosing City
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("city_selector_trigger")
                    ) {
                        Text(
                            text = cityConfig.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▼",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        PrayerTimeCalculator.CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${city.name} (GMT${if (city.timezone >= 0) "+" else ""}${city.timezone.toInt()})",
                                        fontWeight = if (city.name == selectedCityName) FontWeight.Bold else FontWeight.Normal,
                                        color = if (city.name == selectedCityName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onCitySelected(city.name)
                                    dropdownExpanded = false
                                },
                                modifier = Modifier.testTag("city_option_${city.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dividers / Subtitle
            Text(
                text = "DAILY SALAH SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Salah Grid/Table with active highlighting
            val salahList = listOf(
                Triple("Fajr", prayerTimes["Fajr"] ?: "--:-- AM", Icons.Default.LightMode),
                Triple("Sunrise", prayerTimes["Sunrise"] ?: "--:-- AM", Icons.Default.LightMode),
                Triple("Dhuhr", prayerTimes["Dhuhr"] ?: "--:-- PM", Icons.Default.LightMode),
                Triple("Asr", prayerTimes["Asr"] ?: "--:-- PM", Icons.Default.LightMode),
                Triple("Maghrib", prayerTimes["Maghrib"] ?: "--:-- PM", Icons.Default.Brightness3),
                Triple("Isha", prayerTimes["Isha"] ?: "--:-- PM", Icons.Default.Brightness3)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                salahList.forEach { (name, time, icon) ->
                    val isHighlighted = name == currentHighlight
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$name Icon",
                                tint = if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = name,
                                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontSize = 14.sp
                            )
                            if (isHighlighted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (name == "Sunrise") "CURRENT" else "ACTIVE",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        val endTime = when (name) {
                            "Fajr" -> prayerTimes["Sunrise"]
                            "Dhuhr" -> prayerTimes["Asr"]
                            "Asr" -> prayerTimes["Maghrib"]
                            "Maghrib" -> prayerTimes["Isha"]
                            "Isha" -> prayerTimes["Fajr"]
                            else -> null
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (endTime != null) "Start: $time" else time,
                                fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontSize = 13.sp
                            )
                            if (endTime != null) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(
                                    text = "Ends: $endTime",
                                    fontWeight = FontWeight.Medium,
                                    color = if (isHighlighted) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    },
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
