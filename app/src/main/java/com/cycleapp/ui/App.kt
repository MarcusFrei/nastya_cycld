package com.cycleapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContentColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cycleapp.data.ACCENT_OPTIONS
import com.cycleapp.data.CycleRepository
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.NotificationSettings
import com.cycleapp.data.ThemeMode
import com.cycleapp.data.WALLPAPER_PRESETS
import com.cycleapp.notifications.NotificationScheduler
import com.cycleapp.ui.screens.CalendarScreen
import com.cycleapp.ui.screens.DayDetailSheet
import com.cycleapp.ui.screens.NotificationsScreen
import com.cycleapp.ui.screens.SettingsScreen
import com.cycleapp.ui.screens.StatisticsScreen
import com.cycleapp.ui.theme.CycleAppTheme
import com.cycleapp.ui.theme.appBackgroundModifier
import com.cycleapp.util.CycleCalculator
import kotlinx.coroutines.launch
import java.time.YearMonth

private enum class Tab { CALENDAR, STATS, NOTIFICATIONS, SETTINGS }

@Composable
fun CycleApp() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(context) }
    val scope = rememberCoroutineScope()

    val periods by repo.periodsFlow.collectAsState(initial = emptyList())
    val days by repo.daysFlow.collectAsState(initial = emptyMap())
    val settings by repo.settingsFlow.collectAsState(initial = CycleSettings())
    val notifSettings by repo.notificationSettingsFlow.collectAsState(initial = NotificationSettings())

    var tab by remember { mutableStateOf(Tab.CALENDAR) }
    var viewMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    var permissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> permissionGranted = granted }

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val useLightText = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val preset = WALLPAPER_PRESETS.find { it.id == settings.wallpaperId } ?: WALLPAPER_PRESETS[0]
    val accent = ACCENT_OPTIONS.find { it.id == settings.accentId }?.color ?: ACCENT_OPTIONS[0].color

    // Пересчитываем прогноз и переустанавливаем уведомления при любом изменении
    // данных цикла или настроек уведомлений.
    LaunchedEffect(periods, settings.cycleLength, settings.periodLength, notifSettings) {
        val forecast = CycleCalculator.buildForecast(periods, days, settings.cycleLength, settings.periodLength)
        NotificationScheduler.scheduleAll(context, forecast, notifSettings, settings.periodLength)
    }

    CycleAppTheme(preset = preset, accent = accent, useLightText = useLightText) {
        // Гарантирует корректный цвет текста/иконок по умолчанию — без этого Text() без явного
        // цвета мог "не видеть" текущую тему из-за прозрачного containerColor у Scaffold.
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .then(appBackgroundModifier()),
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
                            selected = tab == Tab.NOTIFICATIONS,
                            onClick = { tab = Tab.NOTIFICATIONS },
                            icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                            label = { Text("Уведомления") }
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
                        Tab.NOTIFICATIONS -> NotificationsScreen(
                            settings = notifSettings,
                            permissionGranted = permissionGranted,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onChange = { scope.launch { repo.updateNotificationSettings(it) } }
                        )
                        Tab.SETTINGS -> SettingsScreen(
                            settings = settings,
                            onCycleLengthChange = { scope.launch { repo.updateCycleLength(it) } },
                            onPeriodLengthChange = { scope.launch { repo.updatePeriodLength(it) } },
                            onThemeModeChange = { scope.launch { repo.updateThemeMode(it) } },
                            onWallpaperChange = { scope.launch { repo.updateWallpaper(it) } },
                            onAccentChange = { scope.launch { repo.updateAccent(it) } },
                            onClearAllData = { scope.launch { repo.clearAllData() } }
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
                    onSave = { energy, note, isPeriod, mood, symptoms ->
                        scope.launch {
                            repo.saveDay(DayEntry(iso, energy, note, symptoms, mood), days)
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
}
