package com.cycleapp.util

import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.PregnancyChance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.sqrt

object CycleCalculator {

    private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    fun parse(iso: String): LocalDate = LocalDate.parse(iso, FORMATTER)
    fun format(date: LocalDate): String = date.format(FORMATTER)

    /** Ближайшая дата начала менструации на или до указанного дня */
    private fun lastPeriodStartOnOrBefore(iso: String, periods: List<String>): String? {
        return periods.filter { it <= iso }.maxOrNull()
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

        val diff = java.time.temporal.ChronoUnit.DAYS.between(parse(start), parse(iso)).toInt()
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

    data class CycleStats(
        val cycleLengths: List<Int>,
        val averageLength: Double,
        val stdDeviation: Double,
        val isRegular: Boolean,
        val shortestCycle: Int?,
        val longestCycle: Int?
    )

    /** Статистика по регулярности циклов на основе истории отмеченных начал менструаций */
    fun computeCycleStats(periods: List<String>): CycleStats {
        val sorted = periods.sorted()
        if (sorted.size < 2) {
            return CycleStats(emptyList(), 0.0, 0.0, isRegular = false, shortestCycle = null, longestCycle = null)
        }
        val lengths = sorted.zipWithNext { a, b ->
            java.time.temporal.ChronoUnit.DAYS.between(parse(a), parse(b)).toInt()
        }.filter { it in 10..90 } // отсекаем случайные ошибки ввода

        if (lengths.isEmpty()) {
            return CycleStats(emptyList(), 0.0, 0.0, isRegular = false, shortestCycle = null, longestCycle = null)
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
            longestCycle = lengths.maxOrNull()
        )
    }
}
