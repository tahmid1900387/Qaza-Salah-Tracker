package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun FrostedGlassBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseBackgroundColor = if (isDarkTheme) {
        Color(0xFF0A0F0D) // Deep night emerald-tinted black
    } else {
        Color(0xFFF1F5F2) // Light green-tinted background
    }

    val blob1Colors = if (isDarkTheme) {
        listOf(Color(0xFF047857).copy(alpha = 0.35f), Color.Transparent)
    } else {
        listOf(Color(0xFFA7F3D0).copy(alpha = 0.5f), Color.Transparent)
    }

    val blob2Colors = if (isDarkTheme) {
        listOf(Color(0xFF0D9488).copy(alpha = 0.25f), Color.Transparent)
    } else {
        listOf(Color(0xFFCCFBF1).copy(alpha = 0.5f), Color.Transparent)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBackgroundColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Blob 1: Top Left (equivalent to top-[-10%] left-[-10%] w-[60%] h-[40%])
            drawCircle(
                brush = Brush.radialGradient(
                    colors = blob1Colors,
                    center = Offset(x = size.width * -0.1f, y = size.height * -0.1f),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(x = size.width * -0.1f, y = size.height * -0.1f)
            )

            // Blob 2: Bottom Right (equivalent to bottom-[10%] right-[-10%] w-[50%] h-[50%])
            drawCircle(
                brush = Brush.radialGradient(
                    colors = blob2Colors,
                    center = Offset(x = size.width * 1.1f, y = size.height * 0.9f),
                    radius = size.width * 1.0f
                ),
                radius = size.width * 1.0f,
                center = Offset(x = size.width * 1.1f, y = size.height * 0.9f)
            )
        }
        content()
    }
}
