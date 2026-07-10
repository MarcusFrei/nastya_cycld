package com.cycleapp.data

import androidx.compose.ui.graphics.Color

enum class EnergyLevel(val label: String, val color: Color) {
    RED("Минимум энергии", Color(0xFFE5484D)),
    YELLOW("Ниже нормы", Color(0xFFF2B705)),
    WHITE("Норма", Color(0xFFEFDFC8)), // тёплый бежевый вместо чистого белого — виден на любом фоне
    GREEN("Выше нормы", Color(0xFF4CAF6D)),
    BLUE("Пик энергии", Color(0xFF4D8CF2));

    companion object {
        fun fromKey(key: String?): EnergyLevel? = entries.find { it.name == key }
        fun scoreOf(level: EnergyLevel): Int = when (level) {
            RED -> 1; YELLOW -> 2; WHITE -> 3; GREEN -> 4; BLUE -> 5
        }
        fun fromScore(score: Double): EnergyLevel = when (Math.round(score).toInt().coerceIn(1, 5)) {
            1 -> RED; 2 -> YELLOW; 3 -> WHITE; 4 -> GREEN; else -> BLUE
        }
    }
}

enum class Symptom(val label: String) {
    CRAMPS("Спазмы"),
    HEADACHE("Головная боль"),
    BLOATING("Вздутие"),
    FATIGUE("Усталость"),
    ACNE("Высыпания"),
    BACK_PAIN("Боль в пояснице"),
    NAUSEA("Тошнота"),
    TENDER_BREASTS("Чувствительность груди"),
    INSOMNIA("Бессонница"),
    CRAVINGS("Тяга к еде");

    companion object {
        fun fromKey(key: String): Symptom? = entries.find { it.name == key }
    }
}

enum class Mood(val label: String, val emoji: String) {
    GREAT("Отлично", "😄"),
    GOOD("Хорошо", "🙂"),
    NEUTRAL("Нейтрально", "😐"),
    LOW("Подавленно", "😔"),
    IRRITABLE("Раздражительно", "😠"),
    ANXIOUS("Тревожно", "😰");

    companion object {
        fun fromKey(key: String?): Mood? = entries.find { it.name == key }
    }
}

data class DayEntry(
    val date: String,           // "yyyy-MM-dd"
    val energy: EnergyLevel? = null,
    val note: String = "",
    val symptoms: Set<Symptom> = emptySet(),
    val mood: Mood? = null
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** Пресет фона. Не зависит от темы — тема влияет только на цвет текста. */
data class WallpaperPreset(
    val id: Int,
    val name: String,
    val colors: List<Color>
)

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset(0, "Полночь", listOf(Color(0xFF1B1420), Color(0xFF2C2036))),
    WallpaperPreset(1, "Лаванда", listOf(Color(0xFF2D1B4E), Color(0xFF4A2E6B))),
    WallpaperPreset(2, "Пудра тёмная", listOf(Color(0xFF3D2530), Color(0xFF5C3644))),
    WallpaperPreset(3, "Карамель тёмная", listOf(Color(0xFF3A2E1F), Color(0xFF5C4530))),
    WallpaperPreset(4, "Океан", listOf(Color(0xFF0F2A3D), Color(0xFF1E4D5C))),
    WallpaperPreset(5, "Лес", listOf(Color(0xFF1A2E1F), Color(0xFF2E4D33))),
    WallpaperPreset(6, "Роза тёмная", listOf(Color(0xFF3D1F2A), Color(0xFF6B3350))),
    WallpaperPreset(7, "Графит", listOf(Color(0xFF232326), Color(0xFF343438))),
    WallpaperPreset(8, "Ваниль", listOf(Color(0xFFFDF6EC), Color(0xFFF5E9D6))),
    WallpaperPreset(9, "Мята", listOf(Color(0xFFEAF7F0), Color(0xFFD8F0E3))),
    WallpaperPreset(10, "Небо", listOf(Color(0xFFEAF3FB), Color(0xFFD7E9F8))),
    WallpaperPreset(11, "Персик", listOf(Color(0xFFFDEDE3), Color(0xFFFBDDC9))),
    WallpaperPreset(12, "Сирень", listOf(Color(0xFFF3EAFB), Color(0xFFE6D6F5))),
    WallpaperPreset(13, "Пудра светлая", listOf(Color(0xFFFCEAEF), Color(0xFFF7D9E3)))
)

/** Акцентный цвет — используется для кнопок, выделения текущего дня, активных элементов */
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

data class NotificationSettings(
    val weekBeforePeriod: Boolean = false,
    val dayBeforePeriod: Boolean = false,
    val weekBeforeOvulation: Boolean = false,
    val everyPeriodDay: Boolean = false,
    val reminderHour: Int = 9
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
    val predictedOvulationDate: String?,    // ISO
    val weekBeforeDate: String?,            // ISO
    val weekBeforePhase: CyclePhase,
    val weekBeforeEnergyGuess: EnergyLevel?,
    val note: String
)
