package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.CityConfig
import com.example.data.DateConverter
import com.example.data.PrayerTimeCalculator
import com.example.ui.QazaViewModel
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.PrayerTimeTable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalahScreen(
    viewModel: QazaViewModel,
    onNavigateToSettings: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val selectedCity = settings?.selectedCity ?: "Dhaka"

    val calendar = Calendar.getInstance()

    // Calculate Sahri / Iftar for the selected city
    val cityConfig = remember(selectedCity) {
        PrayerTimeCalculator.getCity(selectedCity)
    }
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val prayerTimes = remember(cityConfig, dayOfYear) {
        PrayerTimeCalculator.calculatePrayerTimes(cityConfig, dayOfYear)
    }

    // Sahri ends 5 minutes before Fajr
    val sahriTime = remember(prayerTimes) {
        val fajrStr = prayerTimes["Fajr"] ?: ""
        try {
            if (fajrStr.isNotEmpty()) {
                val timePart = fajrStr.split(" ")[0]
                val periodPart = fajrStr.split(" ")[1]
                val parts = timePart.split(":")
                var hour = parts[0].toInt()
                var min = parts[1].toInt()
                
                // Subtract 5 mins
                min -= 5
                if (min < 0) {
                    min += 60
                    hour -= 1
                    if (hour <= 0) hour = 12
                }
                String.format("%02d:%02d %s", hour, min, periodPart)
            } else {
                "--:-- AM"
            }
        } catch (e: Exception) {
            fajrStr // Fallback to raw Fajr
        }
    }

    val iftarTime = prayerTimes["Maghrib"] ?: "--:-- PM"

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DAILY SALAH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Timetable & Calendar",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("salah_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Location-wise Precise Prayer Time list card (moved to the top!)
            Text(
                text = "LOCATION & TIMETABLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
            )

            val selectedCity = settings?.selectedCity ?: "Dhaka"
            PrayerTimeTable(
                selectedCityName = selectedCity,
                onCitySelected = { city ->
                    viewModel.updateSelectedCity(city)
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Sahri & Iftari Timings Card (Side-by-Side Highlight)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sahri Card
                FrostedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("sahri_time_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Sahri",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "SEHRI ENDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sahriTime,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Iftar Card
                FrostedGlassCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("iftar_time_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Iftar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "IFTAR STARTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = iftarTime,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Today's Prayer Checklist
            val dateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            LaunchedEffect(dateStr) {
                viewModel.loadStatusesForDate(dateStr)
            }
            val todayStatuses by viewModel.todayPrayerStatuses.collectAsState()

            Text(
                text = "TODAY'S PRAYER CHECKLIST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
            )

            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val completedCount = todayStatuses.values.count { it == "ADA" }
                    val missedCount = todayStatuses.values.count { it == "QAZA" }
                    val progress = completedCount.toFloat() / 5f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$completedCount / 5 Completed",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )

                    if (missedCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• $missedCount missed prayer(s) automatically added to Qaza Tracker backlog.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Individual prayer rows
            val prayerNamesList = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
            prayerNamesList.forEach { prayerName ->
                val status = todayStatuses[prayerName] ?: "NONE"
                val time = prayerTimes[prayerName] ?: "--:-- AM/PM"

                TodayPrayerChecklistItem(
                    prayerName = prayerName,
                    time = time,
                    status = status,
                    onStatusChange = { newStatus ->
                        viewModel.updateTodayPrayerStatus(prayerName, dateStr, newStatus)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TodayPrayerChecklistItem(
    prayerName: String,
    time: String,
    status: String,
    onStatusChange: (String) -> Unit
) {
    val isPrayed = status == "ADA"
    
    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStatusChange(if (isPrayed) "NONE" else "ADA") }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPrayed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Prayed",
                    tint = if (isPrayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(26.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = prayerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isPrayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPrayed) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isPrayed) "Prayed" else "Not Prayed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPrayed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

