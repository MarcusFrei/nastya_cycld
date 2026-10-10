package com.cycleapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cycleapp.data.CycleRepository
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.data.NotificationSettings
import com.cycleapp.data.ThemeMode
import com.cycleapp.data.findPreset
import com.cycleapp.notifications.NotificationScheduler
import com.cycleapp.ui.components.BottomNav
import com.cycleapp.ui.components.NavItem
import com.cycleapp.ui.screens.CalendarScreen
import com.cycleapp.ui.screens.DayDetailSheet
import com.cycleapp.ui.screens.HomeScreen
import com.cycleapp.ui.screens.NotificationsScreen
import com.cycleapp.ui.screens.SettingsScreen
import com.cycleapp.ui.screens.StatisticsScreen
import com.cycleapp.ui.theme.CycleAppTheme
import com.cycleapp.ui.theme.appBackgroundModifier
import com.cycleapp.util.CycleCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth

private enum class Tab { HOME, CALENDAR, STATS, SETTINGS }

private val NAV_ITEMS = listOf(
    NavItem("Цикл", Icons.Filled.Favorite),
    NavItem("Календарь", Icons.Filled.CalendarMonth),
    NavItem("Статистика", Icons.Filled.BarChart),
    NavItem("Настройки", Icons.Filled.Settings)
)

@Composable
fun CycleApp() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(context) }
    val scope = rememberCoroutineScope()

    val periods by repo.periodsFlow.collectAsState(initial = emptyList())
    val days by repo.daysFlow.collectAsState(initial = emptyMap())
    val settings by repo.settingsFlow.collectAsState(initial = CycleSettings())
    val notifSettings by repo.notificationSettingsFlow.collectAsState(initial = NotificationSettings())

    var tab by remember { mutableStateOf(Tab.HOME) }
    var showNotifications by remember { mutableStateOf(false) }
    var viewMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    // Применяем новое оформление один раз (для тех, кто обновился со старой версии)
    LaunchedEffect(Unit) { repo.applyDesignV3Once() }

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
    val dark = when (settings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    // Все цвета (в том числе акцент) берутся из выбранного фона; неизвестный фон → «Лепесток»
    val preset = findPreset(settings.wallpaperId)

    // ---- Экспорт / импорт журнала ----
    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = repo.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                        ?: throw IllegalStateException("не удалось открыть файл")
                }
                toast("Журнал сохранён в файл")
            } catch (e: Exception) {
                toast("Не удалось сохранить: ${e.message}")
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { String(it.readBytes(), Charsets.UTF_8) }
                        ?: throw IllegalStateException("не удалось открыть файл")
                }
                pendingImportJson = text
            } catch (e: Exception) {
                toast("Не удалось прочитать файл: ${e.message}")
            }
        }
    }

    // Единая модель цикла: длина цикла и месячных считаются автоматически по отметкам.
    val today = LocalDate.now()
    val model = remember(periods, today) { CycleCalculator.buildModel(periods, today) }
    val forecast = remember(model, days) { CycleCalculator.buildForecast(model, days) }

    // Переустанавливаем уведомления при любом изменении данных цикла или настроек уведомлений
    LaunchedEffect(model, notifSettings) {
        NotificationScheduler.scheduleAll(context, CycleCalculator.buildForecast(model, emptyMap()), notifSettings, model.periodLength)
    }

    // Кнопка «назад»: из подраздела — в настройки, с любой вкладки — на главную
    BackHandler(enabled = tab != Tab.HOME) {
        if (tab == Tab.SETTINGS && showNotifications) showNotifications = false else tab = Tab.HOME
    }

    val todayIso = CycleCalculator.format(today)

    CycleAppTheme(preset = preset, dark = dark) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .then(appBackgroundModifier()),
                containerColor = Color.Transparent,
                bottomBar = {
                    BottomNav(
                        items = NAV_ITEMS,
                        selectedIndex = tab.ordinal,
                        onSelect = {
                            tab = Tab.entries[it]
                            showNotifications = false
                        },
                        onFabClick = { selectedDate = todayIso }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(
                            model = model,
                            periods = periods,
                            days = days,
                            forecast = forecast,
                            onQuickEnergy = { level -> scope.launch { quickEnergy(repo, todayIso, level, days) } },
                            onEditToday = { selectedDate = todayIso },
                            onStartPeriod = { scope.launch { repo.addPeriodDays(listOf(todayIso)) } },
                            onMarkToday = { scope.launch { repo.addPeriodDays(listOf(todayIso)) } },
                            onEndPeriod = {
                                scope.launch {
                                    val toAdd = CycleCalculator.daysToCloseCurrentPeriod(model, periods.contains(todayIso))
                                    repo.addPeriodDays(toAdd)
                                }
                            }
                        )
                        Tab.CALENDAR -> CalendarScreen(
                            viewMonth = viewMonth,
                            onMonthChange = { viewMonth = it },
                            model = model,
                            periods = periods,
                            days = days,
                            onDayClick = { selectedDate = it }
                        )
                        Tab.STATS -> StatisticsScreen(periods = periods, days = days, model = model)
                        Tab.SETTINGS -> {
                            if (showNotifications) {
                                NotificationsScreen(
                                    settings = notifSettings,
                                    permissionGranted = permissionGranted,
                                    onRequestPermission = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    onChange = { scope.launch { repo.updateNotificationSettings(it) } },
                                    onBack = { showNotifications = false }
                                )
                            } else {
                                SettingsScreen(
                                    settings = settings,
                                    model = model,
                                    onOpenNotifications = { showNotifications = true },
                                    onThemeModeChange = { scope.launch { repo.updateThemeMode(it) } },
                                    onWallpaperChange = { scope.launch { repo.updateWallpaper(it) } },
                                    onExport = { exportLauncher.launch("cycle-journal-$todayIso.json") },
                                    onImport = { importLauncher.launch(arrayOf("*/*")) },
                                    onClearAllData = { scope.launch { repo.clearAllData() } }
                                )
                            }
                        }
                    }
                }
            }

            selectedDate?.let { iso ->
                DayDetailSheet(
                    dateIso = iso,
                    initialEntry = days[iso],
                    initialIsPeriod = periods.contains(iso),
                    model = model,
                    onDismiss = { selectedDate = null },
                    // автосохранение: вызывается при каждом изменении в карточке дня
                    onChange = { energy, note, isPeriod, mood, symptoms, flow ->
                        scope.launch {
                            repo.saveDay(DayEntry(iso, energy, note, symptoms, mood, flow))
                            repo.setPeriod(iso, isPeriod)
                        }
                    },
                    onClear = {
                        scope.launch {
                            repo.saveDay(DayEntry(iso))
                            repo.setPeriod(iso, false)
                        }
                    }
                )
            }

            // Выбор способа импорта
            pendingImportJson?.let { json ->
                fun runImport(replace: Boolean) {
                    pendingImportJson = null
                    scope.launch {
                        try {
                            val r = repo.importJson(json, replace)
                            toast("Загружено: ${r.periodDays} дн. месячных, ${r.dayEntries} записей")
                        } catch (e: Exception) {
                            toast(e.message ?: "Не удалось загрузить журнал")
                        }
                    }
                }
                AlertDialog(
                    onDismissRequest = { pendingImportJson = null },
                    title = { Text("Загрузить журнал") },
                    text = {
                        Text(
                            "«Объединить» добавит записи из файла к текущим. " +
                                "«Заменить» удалит текущие данные и оставит только содержимое файла."
                        )
                    },
                    confirmButton = { TextButton(onClick = { runImport(false) }) { Text("Объединить") } },
                    dismissButton = {
                        Row {
                            TextButton(onClick = { pendingImportJson = null }) { Text("Отмена") }
                            TextButton(onClick = { runImport(true) }) { Text("Заменить") }
                        }
                    }
                )
            }
        }
    }
}

/** Быстрая отметка энергии на сегодня с главного экрана; повторное нажатие снимает отметку */
private suspend fun quickEnergy(
    repo: CycleRepository,
    iso: String,
    level: EnergyLevel,
    days: Map<String, DayEntry>
) {
    val existing = days[iso] ?: DayEntry(date = iso)
    val newLevel = if (existing.energy == level) null else level
    repo.saveDay(existing.copy(energy = newLevel))
}
