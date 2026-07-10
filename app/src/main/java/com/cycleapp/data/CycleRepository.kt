package com.cycleapp.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "cycle_data")

class CycleRepository(private val context: Context) {

    private object Keys {
        val PERIODS = stringPreferencesKey("periods")       // JSON array of ISO dates
        val DAYS = stringPreferencesKey("days")             // JSON object { date: {energy, note, symptoms, mood} }
        val CYCLE_LENGTH = intPreferencesKey("cycle_length")
        val PERIOD_LENGTH = intPreferencesKey("period_length")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_ID = intPreferencesKey("wallpaper_id")
        val ACCENT_ID = intPreferencesKey("accent_id")

        val NOTIF_WEEK_BEFORE_PERIOD = booleanPreferencesKey("notif_week_before_period")
        val NOTIF_DAY_BEFORE_PERIOD = booleanPreferencesKey("notif_day_before_period")
        val NOTIF_WEEK_BEFORE_OVULATION = booleanPreferencesKey("notif_week_before_ovulation")
        val NOTIF_EVERY_PERIOD_DAY = booleanPreferencesKey("notif_every_period_day")
        val NOTIF_HOUR = intPreferencesKey("notif_hour")
    }

    val periodsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.PERIODS] ?: "[]"
        val arr = JSONArray(raw)
        (0 until arr.length()).map { arr.getString(it) }
    }

    val daysFlow: Flow<Map<String, DayEntry>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.DAYS] ?: "{}"
        val obj = JSONObject(raw)
        val map = mutableMapOf<String, DayEntry>()
        obj.keys().forEach { key ->
            val entry = obj.getJSONObject(key)
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
                mood = Mood.fromKey(entry.optString("mood", "").ifBlank { null })
            )
        }
        map
    }

    val settingsFlow: Flow<CycleSettings> = context.dataStore.data.map { prefs ->
        CycleSettings(
            cycleLength = prefs[Keys.CYCLE_LENGTH] ?: 28,
            periodLength = prefs[Keys.PERIOD_LENGTH] ?: 5,
            themeMode = ThemeMode.entries.find { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM,
            wallpaperId = prefs[Keys.WALLPAPER_ID] ?: 0,
            accentId = prefs[Keys.ACCENT_ID] ?: 0
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

    suspend fun saveDay(entry: DayEntry, currentDays: Map<String, DayEntry>) {
        val updated = currentDays.toMutableMap()
        val isEmpty = entry.energy == null && entry.note.isBlank() && entry.symptoms.isEmpty() && entry.mood == null
        if (isEmpty) {
            updated.remove(entry.date)
        } else {
            updated[entry.date] = entry
        }
        val obj = JSONObject()
        updated.forEach { (date, e) ->
            val inner = JSONObject()
            inner.put("energy", e.energy?.name ?: "")
            inner.put("note", e.note)
            inner.put("mood", e.mood?.name ?: "")
            val symArr = JSONArray()
            e.symptoms.forEach { symArr.put(it.name) }
            inner.put("symptoms", symArr)
            obj.put(date, inner)
        }
        context.dataStore.edit { it[Keys.DAYS] = obj.toString() }
    }

    suspend fun togglePeriod(date: String, isPeriod: Boolean, currentPeriods: List<String>) {
        val updated = currentPeriods.toMutableList()
        if (isPeriod && !updated.contains(date)) updated.add(date)
        if (!isPeriod && updated.contains(date)) updated.remove(date)
        val arr = JSONArray()
        updated.sorted().forEach { arr.put(it) }
        context.dataStore.edit { it[Keys.PERIODS] = arr.toString() }
    }

    suspend fun updateCycleLength(value: Int) {
        context.dataStore.edit { it[Keys.CYCLE_LENGTH] = value }
    }

    suspend fun updatePeriodLength(value: Int) {
        context.dataStore.edit { it[Keys.PERIOD_LENGTH] = value }
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateWallpaper(id: Int) {
        context.dataStore.edit { it[Keys.WALLPAPER_ID] = id }
    }

    suspend fun updateAccent(id: Int) {
        context.dataStore.edit { it[Keys.ACCENT_ID] = id }
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

    /** Полная очистка всех данных приложения (отладочная функция в настройках) */
    suspend fun clearAllData() {
        context.dataStore.edit { it.clear() }
    }
}
