package com.cycleapp.data

import androidx.compose.ui.graphics.Color

enum class EnergyLevel(val label: String, val color: Color) {
    RED("Минимум энергии", Color(0xFFE5484D)),
    YELLOW("Ниже нормы", Color(0xFFF2B705)),
    WHITE("Норма", Color(0xFFF7F4F2)),
    GREEN("Выше нормы", Color(0xFF4CAF6D)),
    BLUE("Пик энергии", Color(0xFF4D8CF2));

    companion object {
        fun fromKey(key: String?): EnergyLevel? = entries.find { it.name == key }
    }
}

data class DayEntry(
    val date: String,           // "yyyy-MM-dd"
    val energy: EnergyLevel? = null,
    val note: String = ""
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class WallpaperPreset(
    val id: Int,
    val name: String,
    val colors: List<Color>
)

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset(0, "Полночь", listOf(Color(0xFF1B1420), Color(0xFF2C2036))),
    WallpaperPreset(1, "Лаванда", listOf(Color(0xFF2D1B4E), Color(0xFF4A2E6B))),
    WallpaperPreset(2, "Закат", listOf(Color(0xFF3A1B2E), Color(0xFF6B2E4A))),
    WallpaperPreset(3, "Океан", listOf(Color(0xFF0F2A3D), Color(0xFF1E4D5C))),
    WallpaperPreset(4, "Лес", listOf(Color(0xFF1A2E1F), Color(0xFF2E4D33))),
    WallpaperPreset(5, "Роза", listOf(Color(0xFF3D1F2A), Color(0xFF6B3350)))
)

data class CycleSettings(
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val wallpaperId: Int = 0
)

enum class PregnancyChance(val label: String) {
    VERY_LOW("Очень низкая"),
    LOW("Низкая"),
    MEDIUM("Средняя"),
    HIGH("Высокая"),
    PEAK("Пиковая")
}

enum class CyclePhase(val label: String, val hint: String) {
    MENSTRUATION("Менструация", "Тело восстанавливается — энергии обычно меньше. Хорошее время для отдыха."),
    FOLLICULAR("Фолликулярная фаза", "Энергия постепенно растёт. Хорошее время для новых начинаний."),
    OVULATION("Овуляция", "Пик энергии и коммуникабельности у многих в этот период."),
    LUTEAL("Лютеиновая фаза", "Энергия постепенно снижается, ближе к концу возможна усталость."),
    UNKNOWN("Нет данных", "Отметьте день начала менструации, чтобы начать отслеживание.")
}

data class CycleInfo(
    val cycleDay: Int?,
    val phase: CyclePhase,
    val ovulationDay: Int,
    val pregnancyChance: PregnancyChance
)
