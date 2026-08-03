package com.cycleapp.util

import com.cycleapp.data.CycleForecast
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.data.PregnancyChance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

object CycleCalculator {

    private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun parse(iso: String): LocalDate = LocalDate.parse(iso, FORMATTER)
    fun format(date: LocalDate): String = date.format(FORMATTER)

    /** Формат день.месяц.год — используется везде, где дата показывается пользователю */
    fun formatDMY(date: LocalDate): String =
        "%02d.%02d.%04d".format(date.dayOfMonth, date.monthValue, date.year)

    fun formatDMY(iso: String): String = formatDMY(parse(iso))

    /** Группирует отмеченные дни менструации в непрерывные диапазоны (начало-конец) */
    fun computePeriodRanges(periodDays: List<String>): List<Pair<String, String>> {
        val sorted = periodDays.distinct().sorted().map { parse(it) }
        if (sorted.isEmpty()) return emptyList()

        val ranges = mutableListOf<Pair<LocalDate, LocalDate>>()
        var rangeStart = sorted[0]
        var rangeEnd = sorted[0]

        for (i in 1 until sorted.size) {
            val d = sorted[i]
            if (ChronoUnit.DAYS.between(rangeEnd, d) == 1L) {
                rangeEnd = d
            } else {
                ranges.add(rangeStart to rangeEnd)
                rangeStart = d
                rangeEnd = d
            }
        }
        ranges.add(rangeStart to rangeEnd)
        return ranges.map { format(it.first) to format(it.second) }
    }

    /** Начало последнего диапазона менструации, который начался на дату iso или раньше */
    private fun lastPeriodStartOnOrBefore(iso: String, periodDays: List<String>): String? {
        val ranges = computePeriodRanges(periodDays)
        return ranges.map { it.first }.filter { it <= iso }.maxOrNull()
    }

    fun getCycleInfo(
        iso: String,
        periods: List<String>,
        cycleLength: Int,
        periodLength: Int
    ): CycleInfo {
        val ovulationDay = maxOf(1, cycleLength - 14)
        val start = lastPeriodStartOnOrBefore(iso, periods)
            ?: return CycleInfo(null, CyclePhase.UNKNOWN, ovulationDay, PregnancyChance.LOW)

        val diff = ChronoUnit.DAYS.between(parse(start), parse(iso)).toInt()
        val cycleDay = (diff % cycleLength) + 1

        val phase = when {
            cycleDay <= periodLength -> CyclePhase.MENSTRUATION
            cycleDay < ovulationDay - 1 -> CyclePhase.FOLLICULAR
            cycleDay in (ovulationDay - 1)..(ovulationDay + 1) -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }

        val distanceFromOvulation = abs(cycleDay - ovulationDay)
        val chance = when {
            cycleDay <= periodLength -> PregnancyChance.VERY_LOW
            distanceFromOvulation == 0 -> PregnancyChance.PEAK
            distanceFromOvulation <= 1 -> PregnancyChance.HIGH
            distanceFromOvulation <= 3 -> PregnancyChance.MEDIUM
            distanceFromOvulation <= 5 -> PregnancyChance.LOW
            else -> PregnancyChance.VERY_LOW
        }

        return CycleInfo(cycleDay, phase, ovulationDay, chance)
    }

    data class HistoryRow(
        val startIso: String,
        val endIso: String,
        val periodLengthDays: Int,
        val cycleLengthDays: Int?
    )

    /** История циклов для отображения списком: от новых к старым, с длиной цикла до предыдущего начала */
    fun buildHistory(periods: List<String>): List<HistoryRow> {
        val ranges = computePeriodRanges(periods).sortedBy { it.first }
        val rows = mutableListOf<HistoryRow>()
        for (i in ranges.indices) {
            val (start, end) = ranges[i]
            val periodLen = ChronoUnit.DAYS.between(parse(start), parse(end)).toInt() + 1
            val cycleLen = if (i > 0) ChronoUnit.DAYS.between(parse(ranges[i - 1].first), parse(start)).toInt() else null
            rows.add(HistoryRow(start, end, periodLen, cycleLen))
        }
        return rows.reversed()
    }

    data class CycleStats(
        val cycleLengths: List<Int>,
        val averageLength: Double,
        val stdDeviation: Double,
        val isRegular: Boolean,
        val shortestCycle: Int?,
        val longestCycle: Int?,
        val averagePeriodLength: Double?
    )

    /** Статистика по регулярности циклов на основе истории отмеченных диапазонов менструации */
    fun computeCycleStats(periodDays: List<String>): CycleStats {
        val ranges = computePeriodRanges(periodDays)
        val starts = ranges.map { it.first }.sorted()

        val periodLens = ranges.map {
            ChronoUnit.DAYS.between(parse(it.first), parse(it.second)).toInt() + 1
        }
        val avgPeriodLen = if (periodLens.isNotEmpty()) periodLens.average() else null

        if (starts.size < 2) {
            return CycleStats(emptyList(), 0.0, 0.0, isRegular = false, shortestCycle = null, longestCycle = null, averagePeriodLength = avgPeriodLen)
        }
        val lengths = starts.zipWithNext { a, b ->
            ChronoUnit.DAYS.between(parse(a), parse(b)).toInt()
        }.filter { it in 10..90 } // отсекаем случайные ошибки ввода

        if (lengths.isEmpty()) {
            return CycleStats(emptyList(), 0.0, 0.0, isRegular = false, shortestCycle = null, longestCycle = null, averagePeriodLength = avgPeriodLen)
        }

        val avg = lengths.average()
        val variance = lengths.sumOf { (it - avg) * (it - avg) } / lengths.size
        val std = sqrt(variance)

        return CycleStats(
            cycleLengths = lengths,
            averageLength = avg,
            stdDeviation = std,
            isRegular = std <= 3.0,
            shortestCycle = lengths.minOrNull(),
            longestCycle = lengths.maxOrNull(),
            averagePeriodLength = avgPeriodLen
        )
    }

    /** Прогноз: когда ожидаются следующие месячные и каким примерно будет самочувствие за неделю до них */
    fun buildForecast(
        periods: List<String>,
        days: Map<String, DayEntry>,
        cycleLength: Int,
        periodLength: Int
    ): CycleForecast? {
        val ranges = computePeriodRanges(periods)
        if (ranges.isEmpty()) return null

        val lastStart = parse(ranges.last().first)
        val stats = computeCycleStats(periods)
        val avgLen = if (stats.averageLength > 0) stats.averageLength else cycleLength.toDouble()

        // Ищем следующее предсказанное начало цикла, которое ещё не наступило
        var predicted = lastStart
        var guard = 0
        while (!predicted.isAfter(LocalDate.now()) && guard < 24) {
            predicted = predicted.plusDays(avgLen.roundToLong())
            guard++
        }

        val today = LocalDate.now()
        val daysUntil = ChronoUnit.DAYS.between(today, predicted).toInt()
        val weekBefore = predicted.minusDays(7)
        val weekBeforeIso = format(weekBefore)
        val weekBeforeInfo = getCycleInfo(weekBeforeIso, periods, cycleLength, periodLength)

        val ovulationDay = maxOf(1, cycleLength - 14)
        val predictedOvulation = predicted.minusDays((cycleLength - ovulationDay).toLong())

        // Средний исторический уровень энергии в лютеиновой фазе (по всем прошлым циклам)
        val lutealScores = days.entries.mapNotNull { (iso, entry) ->
            val energy = entry.energy ?: return@mapNotNull null
            val info = getCycleInfo(iso, periods, cycleLength, periodLength)
            if (info.phase == CyclePhase.LUTEAL) EnergyLevel.scoreOf(energy) else null
        }
        val energyGuess = if (lutealScores.isNotEmpty()) EnergyLevel.fromScore(lutealScores.average()) else null

        val note = if (daysUntil in 0..7) {
            "Через ${if (daysUntil == 0) "0" else daysUntil} дн. ожидаются месячные. Обычно на этом отрезке — поздняя лютеиновая фаза: возможны раздражительность, перепады настроения, тяга к еде и снижение энергии (ПМС)."
        } else {
            "За неделю до предполагаемого начала месячных (~${formatDMY(weekBefore)}) обычно наступает поздняя лютеиновая фаза — возможны раздражительность и снижение энергии."
        }

        return CycleForecast(
            predictedNextPeriodDate = format(predicted),
            daysUntilPeriod = daysUntil,
            predictedOvulationDate = format(predictedOvulation),
            weekBeforeDate = weekBeforeIso,
            weekBeforePhase = weekBeforeInfo.phase,
            weekBeforeEnergyGuess = energyGuess,
            note = note
        )
    }
}
