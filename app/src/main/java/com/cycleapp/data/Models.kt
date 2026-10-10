package com.cycleapp.data

import androidx.compose.ui.graphics.Color

enum class EnergyLevel(val label: String, val shortLabel: String, val color: Color) {
    RED("Минимум энергии", "Минимум", Color(0xFFE5484D)),
    YELLOW("Ниже нормы", "Низкая", Color(0xFFF5B700)),
    WHITE("Норма", "Норма", Color(0xFFF3E6D2)), // тёплый кремовый; на календаре всегда с контрастной обводкой
    GREEN("Выше нормы", "Хорошая", Color(0xFF4CAF6D)),
    BLUE("Пик энергии", "Пик", Color(0xFF4D8CF2));

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

enum class FlowLevel(val label: String) {
    LIGHT("Слабые"),
    MEDIUM("Средние"),
    HEAVY("Обильные");

    companion object {
        fun fromKey(key: String?): FlowLevel? = entries.find { it.name == key }
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
    val mood: Mood? = null,
    val flow: FlowLevel? = null
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * Пресет оформления. Все фоны — только пастельные (розовый, бежевый, голубой, зелёный, сиреневый).
 * Акцентный цвет (кнопки, выделения, текущий день) жёстко привязан к фону и отдельно не выбирается.
 * Тёмная тема строится автоматически из того же пресета: тот же оттенок, но глубокий и приглушённый.
 */
data class WallpaperPreset(
    val id: Int,
    val name: String,
    val colors: List<Color>,   // верх и низ градиента в светлой теме
    val accent: Color          // акцент этого фона (светлая тема)
)

const val DEFAULT_WALLPAPER_ID = 14

val WALLPAPER_PRESETS = listOf(
    WallpaperPreset(14, "Лепесток", listOf(Color(0xFFFAEDF3), Color(0xFFF1DAF0)), Color(0xFFA64CC2)),
    WallpaperPreset(13, "Пудра", listOf(Color(0xFFFCEAEF), Color(0xFFF7D9E3)), Color(0xFFCF4B7C)),
    WallpaperPreset(11, "Персик", listOf(Color(0xFFFDEDE3), Color(0xFFFBDDC9)), Color(0xFFC8622A)),
    WallpaperPreset(8, "Ваниль", listOf(Color(0xFFFDF6EC), Color(0xFFF5E9D6)), Color(0xFFA9774A)),
    WallpaperPreset(15, "Лён", listOf(Color(0xFFF5F0E8), Color(0xFFE8DFCF)), Color(0xFF8A7050)),
    WallpaperPreset(9, "Мята", listOf(Color(0xFFEAF7F0), Color(0xFFD8F0E3)), Color(0xFF2E9A68)),
    WallpaperPreset(16, "Фисташка", listOf(Color(0xFFF1F6E4), Color(0xFFE1EDC9)), Color(0xFF6A942C)),
    WallpaperPreset(10, "Небо", listOf(Color(0xFFEAF3FB), Color(0xFFD7E9F8)), Color(0xFF3B7FC4)),
    WallpaperPreset(17, "Лёд", listOf(Color(0xFFE9F6F8), Color(0xFFD2ECF1)), Color(0xFF248FA4)),
    WallpaperPreset(12, "Сирень", listOf(Color(0xFFF3EAFB), Color(0xFFE6D6F5)), Color(0xFF8650C6))
)

fun findPreset(id: Int): WallpaperPreset =
    WALLPAPER_PRESETS.find { it.id == id } ?: WALLPAPER_PRESETS.first()

/**
 * Настройки оформления: тема (светлая / тёмная / системная) и фон.
 * Длина цикла и менструации здесь не хранятся — они вычисляются автоматически.
 */
data class CycleSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val wallpaperId: Int = DEFAULT_WALLPAPER_ID
)

data class NotificationSettings(
    val weekBeforePeriod: Boolean = false,
    val dayBeforePeriod: Boolean = false,
    val weekBeforeOvulation: Boolean = false,
    val everyPeriodDay: Boolean = false,
    val reminderHour: Int = 9
)

enum class PregnancyChance(val label: String) {
    UNKNOWN("Нет данных"),
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
    UNKNOWN("Нет данных", "Нажмите «Начались месячные» в первый день — и приложение начнёт считать цикл само.")
}

data class CycleInfo(
    val cycleDay: Int?,
    val phase: CyclePhase,
    val ovulationDay: Int,
    val pregnancyChance: PregnancyChance,
    val cycleLength: Int
)

data class CycleForecast(
    val predictedNextPeriodDate: String,     // ISO
    val daysUntilPeriod: Int,                // 0 — сегодня/со дня на день
    val lateDays: Int,                       // >0 — задержка относительно прогноза
    val predictedOvulationDate: String,      // ISO
    val daysUntilOvulation: Int,
    val weekBeforeDate: String,              // ISO
    val weekBeforePhase: CyclePhase,
    val weekBeforeEnergyGuess: EnergyLevel?,
    val accuracyDays: Int,                   // ± дней
    val cyclesUsed: Int,                     // по скольким циклам считается прогноз
    val note: String
)
