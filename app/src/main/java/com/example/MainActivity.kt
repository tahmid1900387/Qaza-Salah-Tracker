package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QazaViewModel
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.SalahScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    Splash, Onboarding, Main
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: QazaViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()

            // Dynamic Dark/Light Theme selection
            val systemDark = isSystemInDarkTheme()
            var darkThemeOverridden by remember { mutableStateOf<Boolean?>(null) }
            val useDarkTheme = darkThemeOverridden ?: systemDark

            MyApplicationTheme(darkTheme = useDarkTheme) {
                AppNavigator(
                    viewModel = viewModel,
                    isDarkTheme = useDarkTheme,
                    onToggleDarkTheme = { darkThemeOverridden = it }
                )
            }
        }
    }
}

@Composable
fun AppNavigator(
    viewModel: QazaViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    var currentScreen by remember { mutableStateOf(ScreenState.Splash) }

    // Bottom Navigation tab index: 0 = Dashboard, 1 = History, 2 = Settings
    var selectedTab by remember { mutableStateOf(0) }

    // Auto-navigate when settings onboarding status changes reactively
    settings?.let {
        if (!it.isOnboarded && currentScreen == ScreenState.Main) {
            currentScreen = ScreenState.Onboarding
            selectedTab = 0
        } else if (it.isOnboarded && currentScreen == ScreenState.Onboarding) {
            currentScreen = ScreenState.Main
        }
    }

    FrostedGlassBackground(isDarkTheme = isDarkTheme) {
        Crossfade(targetState = currentScreen, label = "app_navigation_crossfade") { screen ->
            when (screen) {
                ScreenState.Splash -> {
                    SplashScreen(
                        settings = settings,
                        onNavigateNext = { isOnboarded ->
                            currentScreen = if (isOnboarded) ScreenState.Main else ScreenState.Onboarding
                        }
                    )
                }
                ScreenState.Onboarding -> {
                    OnboardingScreen(
                        onComplete = { years, months, days, dailyGoal, userName, selectedCity ->
                            viewModel.setupOnboarding(years, months, days, dailyGoal, userName, selectedCity)
                            currentScreen = ScreenState.Main
                        },
                        getEstimatedRemainingTime = { remaining, goal ->
                            viewModel.getEstimatedRemainingTime(remaining, goal)
                        }
                    )
                }
                ScreenState.Main -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.Transparent,
                        bottomBar = {
                            val glassBarBg = if (isDarkTheme) Color(0xFF1E293B).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.75f)
                            val navItemColors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = if (isDarkTheme) Color(0xFF047857).copy(alpha = 0.3f) else Color(0xFFD1FAE5),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                             NavigationBar(
                                containerColor = glassBarBg,
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("main_bottom_nav")
                            ) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    colors = navItemColors,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "Daily Salah",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text("Daily Salah") },
                                    modifier = Modifier.testTag("nav_tab_salah")
                                )

                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    colors = navItemColors,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = "Qaza Tracker",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text("Qaza") },
                                    modifier = Modifier.testTag("nav_tab_qaza")
                                )

                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    colors = navItemColors,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Tasbih Counter",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text("Tasbih") },
                                    modifier = Modifier.testTag("nav_tab_tasbih")
                                )

                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    colors = navItemColors,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text("Settings") },
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> SalahScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { selectedTab = 3 }
                                )
                                1 -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { selectedTab = 3 }
                                )
                                2 -> TasbihScreen(
                                    viewModel = viewModel,
                                    onNavigateToSettings = { selectedTab = 3 }
                                )
                                3 -> SettingsScreen(
                                    viewModel = viewModel,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = onToggleDarkTheme
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
