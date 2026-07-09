package com.cycleapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "cycle_data")

class CycleRepository(private val context: Context) {

    private object Keys {
        val PERIODS = stringPreferencesKey("periods")       // JSON array of ISO dates
        val DAYS = stringPreferencesKey("days")             // JSON object { date: {energy, note} }
        val CYCLE_LENGTH = intPreferencesKey("cycle_length")
        val PERIOD_LENGTH = intPreferencesKey("period_length")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_ID = intPreferencesKey("wallpaper_id")
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
            map[key] = DayEntry(
                date = key,
                energy = EnergyLevel.fromKey(entry.optString("energy", "")),
                note = entry.optString("note", "")
            )
        }
        map
    }

    val settingsFlow: Flow<CycleSettings> = context.dataStore.data.map { prefs ->
        CycleSettings(
            cycleLength = prefs[Keys.CYCLE_LENGTH] ?: 28,
            periodLength = prefs[Keys.PERIOD_LENGTH] ?: 5,
            themeMode = ThemeMode.entries.find { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.SYSTEM,
            wallpaperId = prefs[Keys.WALLPAPER_ID] ?: 0
        )
    }

    suspend fun saveDay(entry: DayEntry, currentDays: Map<String, DayEntry>) {
        val updated = currentDays.toMutableMap()
        if (entry.energy == null && entry.note.isBlank()) {
            updated.remove(entry.date)
        } else {
            updated[entry.date] = entry
        }
        val obj = JSONObject()
        updated.forEach { (date, e) ->
            val inner = JSONObject()
            inner.put("energy", e.energy?.name ?: "")
            inner.put("note", e.note)
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
}
