package com.cycleapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cycleapp.data.CycleRepository
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.ThemeMode
import com.cycleapp.ui.screens.CalendarScreen
import com.cycleapp.ui.screens.DayDetailSheet
import com.cycleapp.ui.screens.SettingsScreen
import com.cycleapp.ui.screens.StatisticsScreen
import com.cycleapp.ui.theme.CycleAppTheme
import com.cycleapp.ui.theme.WallpaperBackgroundModifier
import kotlinx.coroutines.launch
import java.time.YearMonth

private enum class Tab { CALENDAR, STATS, SETTINGS }

@Composable
fun CycleApp() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(context) }
    val scope = rememberCoroutineScope()

    val periods by repo.periodsFlow.collectAsState(initial = emptyList())
    val days by repo.daysFlow.collectAsState(initial = emptyMap())
    val settings by repo.settingsFlow.collectAsState(initial = CycleSettings())

    var tab by remember { mutableStateOf(Tab.CALENDAR) }
    var viewMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    CycleAppTheme(darkTheme = isDark) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .then(WallpaperBackgroundModifier(settings.wallpaperId, isDark)),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        selected = tab == Tab.CALENDAR,
                        onClick = { tab = Tab.CALENDAR },
                        icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        label = { Text("Календарь") }
                    )
                    NavigationBarItem(
                        selected = tab == Tab.STATS,
                        onClick = { tab = Tab.STATS },
                        icon = { Icon(Icons.Filled.BarChart, contentDescription = null) },
                        label = { Text("Статистика") }
                    )
                    NavigationBarItem(
                        selected = tab == Tab.SETTINGS,
                        onClick = { tab = Tab.SETTINGS },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                        label = { Text("Настройки") }
                    )
                }
            }
        ) { padding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                when (tab) {
                    Tab.CALENDAR -> CalendarScreen(
                        viewMonth = viewMonth,
                        onMonthChange = { viewMonth = it },
                        periods = periods,
                        days = days,
                        settings = settings,
                        onDayClick = { selectedDate = it }
                    )
                    Tab.STATS -> StatisticsScreen(periods = periods, days = days, settings = settings)
                    Tab.SETTINGS -> SettingsScreen(
                        settings = settings,
                        onCycleLengthChange = { scope.launch { repo.updateCycleLength(it) } },
                        onPeriodLengthChange = { scope.launch { repo.updatePeriodLength(it) } },
                        onThemeModeChange = { scope.launch { repo.updateThemeMode(it) } },
                        onWallpaperChange = { scope.launch { repo.updateWallpaper(it) } }
                    )
                }
            }
        }

        selectedDate?.let { iso ->
            DayDetailSheet(
                dateIso = iso,
                initialEntry = days[iso],
                initialIsPeriod = periods.contains(iso),
                periods = periods,
                settings = settings,
                onDismiss = { selectedDate = null },
                onSave = { energy, note, isPeriod ->
                    scope.launch {
                        repo.saveDay(DayEntry(iso, energy, note), days)
                        repo.togglePeriod(iso, isPeriod, periods)
                    }
                },
                onClear = {
                    scope.launch {
                        repo.saveDay(DayEntry(iso, null, ""), days)
                        repo.togglePeriod(iso, false, periods)
                    }
                }
            )
        }
    }
}
