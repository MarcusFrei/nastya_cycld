package com.cycleapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

private val Context.dataStore by preferencesDataStore(name = "cycle_data")

/** Сколько записей добавлено при импорте */
data class ImportResult(val periodDays: Int, val dayEntries: Int)

class CycleRepository(private val context: Context) {

    private object Keys {
        val PERIODS = stringPreferencesKey("periods")       // JSON array of ISO dates
        val DAYS = stringPreferencesKey("days")             // JSON object { date: {energy, note, symptoms, mood, flow} }
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_ID = intPreferencesKey("wallpaper_id")
        val DESIGN_V3_APPLIED = booleanPreferencesKey("design_v3_applied")

        // Старые ключи cycle_length / period_length / accent_id больше не используются.

        val NOTIF_WEEK_BEFORE_PERIOD = booleanPreferencesKey("notif_week_before_period")
        val NOTIF_DAY_BEFORE_PERIOD = booleanPreferencesKey("notif_day_before_period")
        val NOTIF_WEEK_BEFORE_OVULATION = booleanPreferencesKey("notif_week_before_ovulation")
        val NOTIF_EVERY_PERIOD_DAY = booleanPreferencesKey("notif_every_period_day")
        val NOTIF_HOUR = intPreferencesKey("notif_hour")
    }

    // ------------------------------------------------------------ (де)сериализация

    private fun isValidDate(s: String): Boolean = try {
        LocalDate.parse(s); true
    } catch (e: Exception) {
        false
    }

    private fun parsePeriods(raw: String?): List<String> {
        val arr = JSONArray(raw ?: "[]")
        return (0 until arr.length()).map { arr.getString(it) }
    }

    private fun encodePeriods(list: Collection<String>): String {
        val arr = JSONArray()
        list.distinct().sorted().forEach { arr.put(it) }
        return arr.toString()
    }

    private fun parseDays(raw: String?): Map<String, DayEntry> {
        val obj = JSONObject(raw ?: "{}")
        val map = mutableMapOf<String, DayEntry>()
        obj.keys().forEach { key ->
            val entry = obj.optJSONObject(key) ?: return@forEach
            val symptomsArr = entry.optJSONArray("symptoms")
            val symptoms = mutableSetOf<Symptom>()
            if (symptomsArr != null) {
                for (i in 0 until symptomsArr.length()) {
                    Symptom.fromKey(symptomsArr.getString(i))?.let { symptoms.add(it) }
                }
            }
            map[key] = DayEntry(
                date = key,
                energy = EnergyLevel.fromKey(entry.optString("energy", "")),
                note = entry.optString("note", ""),
                symptoms = symptoms,
                mood = Mood.fromKey(entry.optString("mood", "").ifBlank { null }),
                flow = FlowLevel.fromKey(entry.optString("flow", "").ifBlank { null })
            )
        }
        return map
    }

    private fun encodeDays(days: Map<String, DayEntry>): String {
        val obj = JSONObject()
        days.forEach { (date, e) ->
            val inner = JSONObject()
            inner.put("energy", e.energy?.name ?: "")
            inner.put("note", e.note)
            inner.put("mood", e.mood?.name ?: "")
            inner.put("flow", e.flow?.name ?: "")
            val symArr = JSONArray()
            e.symptoms.forEach { symArr.put(it.name) }
            inner.put("symptoms", symArr)
            obj.put(date, inner)
        }
        return obj.toString()
    }

    private fun DayEntry.isEmpty(): Boolean =
        energy == null && note.isBlank() && symptoms.isEmpty() && mood == null && flow == null

    // ------------------------------------------------------------ потоки

    val periodsFlow: Flow<List<String>> = context.dataStore.data.map { parsePeriods(it[Keys.PERIODS]) }

    val daysFlow: Flow<Map<String, DayEntry>> = context.dataStore.data.map { parseDays(it[Keys.DAYS]) }

    val settingsFlow: Flow<CycleSettings> = context.dataStore.data.map { prefs ->
        CycleSettings(
            themeMode = ThemeMode.entries.find { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.LIGHT,
            wallpaperId = prefs[Keys.WALLPAPER_ID] ?: DEFAULT_WALLPAPER_ID
        )
    }

    val notificationSettingsFlow: Flow<NotificationSettings> = context.dataStore.data.map { prefs ->
        NotificationSettings(
            weekBeforePeriod = prefs[Keys.NOTIF_WEEK_BEFORE_PERIOD] ?: false,
            dayBeforePeriod = prefs[Keys.NOTIF_DAY_BEFORE_PERIOD] ?: false,
            weekBeforeOvulation = prefs[Keys.NOTIF_WEEK_BEFORE_OVULATION] ?: false,
            everyPeriodDay = prefs[Keys.NOTIF_EVERY_PERIOD_DAY] ?: false,
            reminderHour = prefs[Keys.NOTIF_HOUR] ?: 9
        )
    }

    /**
     * Один раз переключает тех, кто обновился со старой версии, на светлую тему.
     * Старые тёмные фоны больше не существуют — неизвестный id фона автоматически заменяется на «Лепесток».
     */
    suspend fun applyDesignV3Once() {
        context.dataStore.edit { prefs ->
            if (prefs[Keys.DESIGN_V3_APPLIED] != true) {
                prefs[Keys.THEME_MODE] = ThemeMode.LIGHT.name
                prefs[Keys.WALLPAPER_ID] = DEFAULT_WALLPAPER_ID
                prefs[Keys.DESIGN_V3_APPLIED] = true
            }
        }
    }

    // ------------------------------------------------------------ изменения
    // Все изменения читают актуальные данные ВНУТРИ транзакции DataStore, поэтому
    // быстрые подряд идущие сохранения (автосохранение в карточке дня) не затирают друг друга.

    suspend fun saveDay(entry: DayEntry) {
        context.dataStore.edit { prefs ->
            val map = parseDays(prefs[Keys.DAYS]).toMutableMap()
            if (entry.isEmpty()) map.remove(entry.date) else map[entry.date] = entry
            prefs[Keys.DAYS] = encodeDays(map)
        }
    }

    suspend fun setPeriod(date: String, isPeriod: Boolean) {
        context.dataStore.edit { prefs ->
            val set = parsePeriods(prefs[Keys.PERIODS]).toMutableSet()
            if (isPeriod) set.add(date) else set.remove(date)
            prefs[Keys.PERIODS] = encodePeriods(set)
        }
    }

    /** Добавляет сразу несколько дней менструации (кнопки «Начались» / «Закончились») */
    suspend fun addPeriodDays(dates: Collection<String>) {
        context.dataStore.edit { prefs ->
            val set = parsePeriods(prefs[Keys.PERIODS]).toMutableSet()
            set.addAll(dates)
            prefs[Keys.PERIODS] = encodePeriods(set)
        }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateWallpaper(id: Int) {
        context.dataStore.edit { it[Keys.WALLPAPER_ID] = id }
    }

    suspend fun updateNotificationSettings(settings: NotificationSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIF_WEEK_BEFORE_PERIOD] = settings.weekBeforePeriod
            prefs[Keys.NOTIF_DAY_BEFORE_PERIOD] = settings.dayBeforePeriod
            prefs[Keys.NOTIF_WEEK_BEFORE_OVULATION] = settings.weekBeforeOvulation
            prefs[Keys.NOTIF_EVERY_PERIOD_DAY] = settings.everyPeriodDay
            prefs[Keys.NOTIF_HOUR] = settings.reminderHour
        }
    }

    /** Полная очистка всех данных приложения */
    suspend fun clearAllData() {
        context.dataStore.edit {
            it.clear()
            it[Keys.DESIGN_V3_APPLIED] = true // чтобы после очистки не перетёрлось оформление
        }
    }

    // ------------------------------------------------------------ экспорт / импорт журнала

    /** Весь журнал одним JSON-файлом: месячные, отметки по дням, оформление и уведомления */
    suspend fun exportJson(): String {
        val prefs = context.dataStore.data.first()
        val root = JSONObject()
        root.put("app", "CycleApp")
        root.put("format", 1)
        root.put("exportedAt", LocalDateTime.now().toString())
        root.put("periods", JSONArray(parsePeriods(prefs[Keys.PERIODS]).distinct().sorted()))
        root.put("days", JSONObject(encodeDays(parseDays(prefs[Keys.DAYS]))))

        val appearance = JSONObject()
        appearance.put("themeMode", prefs[Keys.THEME_MODE] ?: ThemeMode.LIGHT.name)
        appearance.put("wallpaperId", prefs[Keys.WALLPAPER_ID] ?: DEFAULT_WALLPAPER_ID)
        root.put("appearance", appearance)

        val notif = JSONObject()
        notif.put("weekBeforePeriod", prefs[Keys.NOTIF_WEEK_BEFORE_PERIOD] ?: false)
        notif.put("dayBeforePeriod", prefs[Keys.NOTIF_DAY_BEFORE_PERIOD] ?: false)
        notif.put("weekBeforeOvulation", prefs[Keys.NOTIF_WEEK_BEFORE_OVULATION] ?: false)
        notif.put("everyPeriodDay", prefs[Keys.NOTIF_EVERY_PERIOD_DAY] ?: false)
        notif.put("reminderHour", prefs[Keys.NOTIF_HOUR] ?: 9)
        root.put("notifications", notif)

        return root.toString(2)
    }

    /**
     * Загружает журнал из файла. replace = true — заменить текущие данные,
     * false — объединить (при совпадении дат побеждает файл).
     * Бросает IllegalArgumentException, если файл не похож на журнал.
     */
    suspend fun importJson(json: String, replace: Boolean): ImportResult {
        val root = try {
            JSONObject(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("Файл повреждён или это не журнал")
        }
        if (!root.has("periods") && !root.has("days")) {
            throw IllegalArgumentException("В файле нет данных журнала")
        }

        val importedPeriods = parsePeriods(root.optJSONArray("periods")?.toString()).filter { isValidDate(it) }
        val importedDays = parseDays(root.optJSONObject("days")?.toString())
            .filterKeys { isValidDate(it) }
            .filterValues { !it.isEmpty() }

        context.dataStore.edit { prefs ->
            val periods = if (replace) mutableSetOf<String>() else parsePeriods(prefs[Keys.PERIODS]).toMutableSet()
            periods.addAll(importedPeriods)
            prefs[Keys.PERIODS] = encodePeriods(periods)

            val days = if (replace) mutableMapOf<String, DayEntry>() else parseDays(prefs[Keys.DAYS]).toMutableMap()
            days.putAll(importedDays)
            prefs[Keys.DAYS] = encodeDays(days)

            if (replace) {
                root.optJSONObject("appearance")?.let { a ->
                    ThemeMode.entries.find { it.name == a.optString("themeMode") }?.let { prefs[Keys.THEME_MODE] = it.name }
                    if (a.has("wallpaperId")) prefs[Keys.WALLPAPER_ID] = findPreset(a.optInt("wallpaperId")).id
                }
                root.optJSONObject("notifications")?.let { n ->
                    prefs[Keys.NOTIF_WEEK_BEFORE_PERIOD] = n.optBoolean("weekBeforePeriod", false)
                    prefs[Keys.NOTIF_DAY_BEFORE_PERIOD] = n.optBoolean("dayBeforePeriod", false)
                    prefs[Keys.NOTIF_WEEK_BEFORE_OVULATION] = n.optBoolean("weekBeforeOvulation", false)
                    prefs[Keys.NOTIF_EVERY_PERIOD_DAY] = n.optBoolean("everyPeriodDay", false)
                    prefs[Keys.NOTIF_HOUR] = n.optInt("reminderHour", 9).coerceIn(0, 23)
                }
            }
            prefs[Keys.DESIGN_V3_APPLIED] = true
        }
        return ImportResult(importedPeriods.distinct().size, importedDays.size)
    }
}
