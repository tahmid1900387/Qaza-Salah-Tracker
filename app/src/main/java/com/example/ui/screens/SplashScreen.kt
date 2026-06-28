package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QazaSettingsEntity
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    settings: QazaSettingsEntity?,
    onNavigateNext: (isOnboarded: Boolean) -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
        delay(800) // Keep visible for an extra beat
        if (settings != null) {
            onNavigateNext(settings.isOnboarded)
        } else {
            onNavigateNext(false)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Elegant Crescent Moon Icon representing Islamic aesthetics
            Icon(
                imageVector = Icons.Default.Brightness3,
                contentDescription = "Qaza Tracker Crescent Logo",
                tint = Color(0xFFD5A94E),
                modifier = Modifier
                    .size(100.dp)
                    .testTag("splash_logo")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Qaza Tracker",
                color = Color(0xFFFAF8F5),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your gentle companion back to consistency",
                color = Color(0xFFE4ECE7).copy(alpha = 0.7f),
                fontSize = 15.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
