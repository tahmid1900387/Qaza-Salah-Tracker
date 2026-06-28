package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    secondary = GoldDarkSecondary,
    tertiary = TealDarkTertiary,
    background = NightGreenBackground,
    surface = CardDarkSurface,
    onPrimary = NightGreenBackground,
    onSecondary = NightGreenBackground,
    onBackground = TextLightBone,
    onSurface = TextLightBone
)

private val LightColorScheme = lightColorScheme(
    primary = SageGreenPrimary,
    secondary = SandGoldSecondary,
    tertiary = DeepTealTertiary,
    background = WarmCreamBackground,
    surface = SoftWhiteSurface,
    onPrimary = SoftWhiteSurface,
    onSecondary = TextDeepCharcoal,
    onBackground = TextDeepCharcoal,
    onSurface = TextDeepCharcoal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
