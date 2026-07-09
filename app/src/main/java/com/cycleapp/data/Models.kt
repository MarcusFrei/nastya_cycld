package com.cycleapp.data

import androidx.compose.ui.graphics.Color

enum class EnergyLevel(val label: String, val color: Color) {
    RED("Минимум энергии", Color(0xFFE5484D)),
    YELLOW("Ниже нормы", Color(0xFFF2B705)),
    // Тёплый бежевый вместо чистого белого — иначе плохо видно на светлом фоне
    WHITE("Норма", Color(0xFFEFDFC8)),
    GREEN("Выше нормы", Color(0xFF4CAF6D)),
    BLUE("Пик энергии", Color(0xFF4D8CF2));

    companion object {
        fun fromKey(key: String?): EnergyLevel? = entries.find { it.name == key }

        fun scoreOf(level: EnergyLevel): Int = when (level) {
            RED -> 1; YELLOW -> 2; WHITE -> 3; GREEN -> 4; BLUE -> 5
        }

        fun fromScore(score: Double): EnergyLevel = when (score.roundToIntSafe()) {
            1 -> RED; 2 -> YELLOW; 3 -> WHITE; 4 -> GREEN; else -> BLUE
        }
    }
}

private fun Double.roundToIntSafe(): Int = Math.round(this).toInt().coerceIn(1, 5)

data class DayEntry(
    val date: String,           // "yyyy-MM-dd"
    val energy: EnergyLevel? = null,
    val note: String = ""
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Пресет фонового градиента. Первый цвет темнее/насыщеннее — используется как база и для тёмной, и (в осветлённом виде) для светлой темы. */
data class WallpaperPreset(
    val id: Int,
    val name: String,
    val colors: List<Color>
)

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset(0, "Полночь", listOf(Color(0xFF1B1420), Color(0xFF2C2036))),
    WallpaperPreset(1, "Лаванда", listOf(Color(0xFF2D1B4E), Color(0xFF4A2E6B))),
    WallpaperPreset(2, "Пудра", listOf(Color(0xFF3D2530), Color(0xFF5C3644))),
    WallpaperPreset(3, "Карамель", listOf(Color(0xFF3A2E1F), Color(0xFF5C4530))),
    WallpaperPreset(4, "Океан", listOf(Color(0xFF0F2A3D), Color(0xFF1E4D5C))),
    WallpaperPreset(5, "Лес", listOf(Color(0xFF1A2E1F), Color(0xFF2E4D33))),
    WallpaperPreset(6, "Роза", listOf(Color(0xFF3D1F2A), Color(0xFF6B3350)))
)

/** Акцентный (вторичный) цвет — используется для кнопок, выделения текущего дня, активных элементов */
data class AccentOption(val id: Int, val name: String, val color: Color)

val ACCENT_OPTIONS = listOf(
    AccentOption(0, "Фиолетовый", Color(0xFFC9A0DC)),
    AccentOption(1, "Розовый", Color(0xFFF2A6C6)),
    AccentOption(2, "Бежевый", Color(0xFFD9B99B)),
    AccentOption(3, "Пудровый", Color(0xFFE8B4BC)),
    AccentOption(4, "Мятный", Color(0xFF8ED1A0)),
    AccentOption(5, "Голубой", Color(0xFF8FB8E0))
)

data class CycleSettings(
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val wallpaperId: Int = 0,
    val accentId: Int = 0
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
    UNKNOWN("Нет данных", "Отметьте дни менструации, чтобы начать отслеживание.")
}

data class CycleInfo(
    val cycleDay: Int?,
    val phase: CyclePhase,
    val ovulationDay: Int,
    val pregnancyChance: PregnancyChance
)

data class CycleForecast(
    val predictedNextPeriodDate: String?,   // ISO
    val daysUntilPeriod: Int?,
    val weekBeforeDate: String?,            // ISO
    val weekBeforePhase: CyclePhase,
    val weekBeforeEnergyGuess: EnergyLevel?,
    val note: String
)
