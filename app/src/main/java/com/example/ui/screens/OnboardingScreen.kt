package com.example.ui.screens

import com.example.ui.components.FrostedGlassCard
import com.example.data.PrayerTimeCalculator
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(
    onComplete: (years: Int, months: Int, days: Int, dailyGoal: Int, userName: String, selectedCity: String) -> Unit,
    getEstimatedRemainingTime: (remainingPrayers: Int, dailyGoal: Int) -> String
) {
    var step by remember { mutableStateOf(1) }
    
    // User name state
    var userName by remember { mutableStateOf("") }
    
    // Selected city state
    var selectedCity by remember { mutableStateOf("Dhaka") }
    
    // Calculation state
    var years by remember { mutableStateOf(0) }
    var months by remember { mutableStateOf(0) }
    var days by remember { mutableStateOf(0) }
    
    // Daily goal state
    var dailyGoal by remember { mutableStateOf(5) } // default 5 prayers (1 complete day per day)

    val totalMissedPrayers = ((years * 365) + (months * 30) + days) * 5

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header / Step Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val isCurrentOrPast = step > index
                    val isCurrent = step == index + 1
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCurrentOrPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            )
                            .padding(horizontal = 4.dp)
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated content step container
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "step_content"
            ) { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentStep) {
                        1 -> StepWelcome(
                            userName = userName,
                            onUserNameChange = { userName = it },
                            selectedCity = selectedCity,
                            onCityChange = { selectedCity = it }
                        )
                        2 -> StepCalculator(
                            years = years,
                            months = months,
                            days = days,
                            onYearsChange = { years = it },
                            onMonthsChange = { months = it },
                            onDaysChange = { days = it }
                        )
                        3 -> StepGoalSelection(
                            totalMissedPrayers = totalMissedPrayers,
                            dailyGoal = dailyGoal,
                            onGoalChange = { dailyGoal = it },
                            getEstimatedRemainingTime = getEstimatedRemainingTime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    Button(
                        onClick = { step-- },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            if (step == 2 && totalMissedPrayers <= 0) {
                                // Graceful block if they entered nothing
                                // Let's auto-give 1 day or prompt them gently
                                days = 1
                            }
                            step++
                        } else {
                            onComplete(years, months, days, dailyGoal, userName, selectedCity)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("btn_next")
                ) {
                    Text(
                        text = if (step == 3) "Begin Journey" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (step == 3) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Icon"
                    )
                }
            }
        }
    }
}

@Composable
fun StepWelcome(
    userName: String,
    onUserNameChange: (String) -> Unit,
    selectedCity: String,
    onCityChange: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            text = "Welcome to Qaza Tracker",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your safe space with zero guilt.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name input card with custom border for visibility and contrast
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "What is your name?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "This will be used to greet you on the dashboard.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = onUserNameChange,
                    placeholder = { Text("Enter your name", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // City selection card with custom border/dropdown
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Edit Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Where are you located?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Select your nearest city to automatically customize your offline daily Salah timetable.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                var onboardingDropdownExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .clickable { onboardingDropdownExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("onboarding_city_selector_trigger")
                    ) {
                        Text(
                            text = selectedCity,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "▼",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = onboardingDropdownExpanded,
                        onDismissRequest = { onboardingDropdownExpanded = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        PrayerTimeCalculator.CITIES.forEach { city ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${city.name} (GMT${if (city.timezone >= 0) "+" else ""}${city.timezone.toInt()})",
                                        fontWeight = if (city.name == selectedCity) FontWeight.Bold else FontWeight.Normal,
                                        color = if (city.name == selectedCity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onCityChange(city.name)
                                    onboardingDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("onboarding_city_option_${city.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Why track Qaza prayers?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Salah is our beautiful connection with our Creator. Over time, life happens and we might miss prayers. Making them up can feel overwhelming.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "This app is built to act as a gentle companion. It helps you track your progress step-by-step, at your own comfortable pace, without any pressure or judgement.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun StepCalculator(
    years: Int,
    months: Int,
    days: Int,
    onYearsChange: (Int) -> Unit,
    onMonthsChange: (Int) -> Unit,
    onDaysChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "How much Salah was missed?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estimate how long you missed praying daily. You can adjust this anytime in Settings.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Year incrementor card
        IncrementorCard(label = "Years", value = years, onValueChange = onYearsChange, testTag = "years")
        Spacer(modifier = Modifier.height(16.dp))
        // Month incrementor card
        IncrementorCard(label = "Months", value = months, onValueChange = onMonthsChange, testTag = "months")
        Spacer(modifier = Modifier.height(16.dp))
        // Day incrementor card
        IncrementorCard(label = "Days", value = days, onValueChange = onDaysChange, testTag = "days")

        Spacer(modifier = Modifier.height(32.dp))

        val totalMissed = ((years * 365) + (months * 30) + days) * 5
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculated Missed Prayers: ",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = "$totalMissed prayers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("total_calculated_missed")
                )
            }
        }
    }
}

@Composable
fun IncrementorCard(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (value > 0) onValueChange(value - 1) },
                    modifier = Modifier.testTag("btn_minus_$testTag")
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                }

                Text(
                    text = value.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("val_$testTag"),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onValueChange(value + 1) },
                    modifier = Modifier.testTag("btn_plus_$testTag")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}

@Composable
fun StepGoalSelection(
    totalMissedPrayers: Int,
    dailyGoal: Int,
    onGoalChange: (Int) -> Unit,
    getEstimatedRemainingTime: (remainingPrayers: Int, dailyGoal: Int) -> String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Set a Gentle Daily Goal",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "How many extra prayers do you want to complete each day? Take it easy and gentle.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Remaining Estimator Card
        FrostedGlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ESTIMATED COMPLETION TIME",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = getEstimatedRemainingTime(totalMissedPrayers, dailyGoal),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("estimated_duration")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "at a rate of $dailyGoal prayers/day",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preset cards
        val presets = listOf(
            Triple(1, "Gentle Pace", "1 extra prayer daily"),
            Triple(5, "Consistent Pace", "5 extra prayers (1 complete day) daily"),
            Triple(10, "Committed Pace", "10 extra prayers (2 complete days) daily")
        )

        presets.forEach { (goal, name, desc) ->
            val isSelected = dailyGoal == goal
            val borderStroke = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onGoalChange(goal) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        if (androidx.compose.foundation.isSystemInDarkTheme()) {
                            Color(0xFF1E293B).copy(alpha = 0.35f)
                        } else {
                            Color.White.copy(alpha = 0.45f)
                        }
                    },
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = borderStroke,
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = desc,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Custom selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom Amount",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (dailyGoal > 1) onGoalChange(dailyGoal - 1) },
                    modifier = Modifier.testTag("btn_minus_custom_goal")
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease Goal")
                }

                Text(
                    text = dailyGoal.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .testTag("val_custom_goal"),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onGoalChange(dailyGoal + 1) },
                    modifier = Modifier.testTag("btn_plus_custom_goal")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Goal")
                }
            }
        }
    }
}
