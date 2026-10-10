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
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Весь расчёт цикла строится ТОЛЬКО на отметках пользователя — длина цикла и длина
 * менструации больше не вводятся вручную.
 *
 *  - длина цикла = взвешенное среднее последних 6 циклов (свежие весят больше),
 *    выбросы отсеиваются; пока данных нет — типовые 28 дней;
 *  - длина месячных считается так же по завершённым периодам;
 *  - прошлые циклы разбираются по их ФАКТИЧЕСКОЙ длине, а не по «усреднённой»;
 *  - задержка не «перескакивает» на следующий цикл, а показывается как задержка.
 */
object CycleCalculator {

    private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5
    private const val LUTEAL_LENGTH = 14          // от овуляции до следующих месячных
    private const val MAX_GAP_DAYS = 3L           // отметки с разрывом до 2 дней — один период
    private const val LATE_GRACE_DAYS = 10        // сколько дней считаем «задержкой», а не пропуском отметок
    private const val PROJECTION_DAYS = 190L      // дальше ~6 месяцев вперёд не прогнозируем
    private const val PRIOR_WEIGHT = 0.5          // вес «типового» значения при малом числе данных
    private const val MAX_SAMPLES = 6

    private val MONTHS_GEN = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    private val MONTHS_SHORT = listOf(
        "янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
    )
    private val WEEKDAYS = listOf(
        "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье"
    )

    fun parse(iso: String): LocalDate = LocalDate.parse(iso, FORMATTER)
    fun format(date: LocalDate): String = date.format(FORMATTER)

    /** Формат день.месяц.год */
    fun formatDMY(date: LocalDate): String =
        "%02d.%02d.%04d".format(date.dayOfMonth, date.monthValue, date.year)

    fun formatDMY(iso: String): String = formatDMY(parse(iso))

    /** «15 дек» */
    fun formatShort(date: LocalDate): String = "${date.dayOfMonth} ${MONTHS_SHORT[date.monthValue - 1]}"

    /** «суббота, 10 октября» */
    fun formatLong(date: LocalDate): String =
        "${WEEKDAYS[date.dayOfWeek.value - 1]}, ${date.dayOfMonth} ${MONTHS_GEN[date.monthValue - 1]}"

    fun monthShort(date: LocalDate): String = MONTHS_SHORT[date.monthValue - 1]

    private fun days(from: LocalDate, to: LocalDate): Int = ChronoUnit.DAYS.between(from, to).toInt()

    // ---------------------------------------------------------------- модель

    /** Всё, что приложение «знает» о цикле пользователя. Строится один раз из отметок. */
    data class CycleModel(
        val ranges: List<Pair<LocalDate, LocalDate>>,   // периоды (начало, конец), от старых к новым
        val cycleLength: Int,                           // оценка длины цикла
        val periodLength: Int,                          // оценка длины месячных
        val cyclesUsed: Int,                            // по скольким циклам посчитана оценка
        val stdDev: Double,                             // разброс длины цикла
        val today: LocalDate,
        val nextStart: LocalDate?,                      // ожидаемое начало следующих месячных
        val lateDays: Int                               // >0 — месячные задерживаются
    ) {
        val hasData: Boolean get() = ranges.isNotEmpty()
        val lastStart: LocalDate? get() = ranges.lastOrNull()?.first
        val lastEnd: LocalDate? get() = ranges.lastOrNull()?.second

        /** Погрешность прогноза в днях (±) */
        val accuracyDays: Int
            get() = when {
                cyclesUsed < 2 -> 4
                else -> ceil(stdDev).toInt().coerceIn(1, 7)
            }
    }

    /** Объединяет отмеченные дни в периоды; разрыв до 2 дней внутри периода не разбивает его */
    fun mergedRanges(periodDays: List<String>): List<Pair<LocalDate, LocalDate>> {
        val sorted = periodDays.distinct().sorted().map { parse(it) }
        if (sorted.isEmpty()) return emptyList()

        val result = mutableListOf<Pair<LocalDate, LocalDate>>()
        var start = sorted[0]
        var end = sorted[0]
        for (i in 1 until sorted.size) {
            val d = sorted[i]
            if (ChronoUnit.DAYS.between(end, d) <= MAX_GAP_DAYS) {
                end = d
            } else {
                result.add(start to end)
                start = d
                end = d
            }
        }
        result.add(start to end)
        return result
    }

    /** То же в виде ISO-строк (для статистики и истории) */
    fun computePeriodRanges(periodDays: List<String>): List<Pair<String, String>> =
        mergedRanges(periodDays).map { format(it.first) to format(it.second) }

    private fun validCycleLengths(starts: List<LocalDate>): List<Int> =
        starts.zipWithNext { a, b -> days(a, b) }.filter { it in 15..90 } // отсекаем ошибки ввода

    /** Взвешенное среднее: чем свежее значение, тем больше вес; плюс слабая «подушка» из типового значения */
    private fun weightedEstimate(values: List<Int>, prior: Int): Double {
        if (values.isEmpty()) return prior.toDouble()
        var sum = prior * PRIOR_WEIGHT
        var weights = PRIOR_WEIGHT
        values.forEachIndexed { i, v ->
            val w = (i + 1).toDouble()
            sum += v * w
            weights += w
        }
        return sum / weights
    }

    /** Убирает явные выбросы (например, пропущенный цикл), когда данных достаточно */
    private fun withoutOutliers(values: List<Int>): List<Int> {
        if (values.size < 4) return values
        val median = values.sorted()[values.size / 2]
        return values.filter { abs(it - median) <= 7 }.ifEmpty { values }
    }

    private fun stdDeviation(values: List<Int>): Double {
        if (values.size < 2) return 0.0
        val avg = values.average()
        return sqrt(values.sumOf { (it - avg) * (it - avg) } / values.size)
    }

    fun ovulationDay(cycleLength: Int, periodLength: Int): Int =
        (cycleLength - LUTEAL_LENGTH).coerceAtLeast(periodLength + 2)

    fun buildModel(periods: List<String>, today: LocalDate = LocalDate.now()): CycleModel {
        val ranges = mergedRanges(periods)

        // Длина цикла
        val lengths = validCycleLengths(ranges.map { it.first }).takeLast(MAX_SAMPLES)
        val cycleLength = weightedEstimate(withoutOutliers(lengths), DEFAULT_CYCLE_LENGTH)
            .roundToInt().coerceIn(15, 90)

        // Длина месячных: текущий (ещё идущий) период и одиночные отметки старта не учитываем
        val lastIsOngoing = ranges.isNotEmpty() && !ranges.last().second.isBefore(today.minusDays(1))
        val completed = if (lastIsOngoing) ranges.dropLast(1) else ranges
        val periodLens = completed
            .map { days(it.first, it.second) + 1 }
            .filter { it >= 2 }
            .takeLast(MAX_SAMPLES)
        val periodLength = weightedEstimate(periodLens, DEFAULT_PERIOD_LENGTH).roundToInt().coerceIn(2, 10)

        // Ожидаемое начало следующих месячных
        var nextStart: LocalDate? = null
        var lateDays = 0
        if (ranges.isNotEmpty()) {
            val last = ranges.last().first
            val base = last.plusDays(cycleLength.toLong())
            if (!base.isBefore(today)) {
                nextStart = base
            } else {
                val overdue = days(base, today)
                if (overdue <= LATE_GRACE_DAYS) {
                    // задержка: ждём «со дня на день»
                    lateDays = overdue
                    nextStart = today.plusDays(1)
                } else {
                    // отметки, судя по всему, пропущены — берём ближайшую будущую дату по ритму цикла
                    val k = days(last, today) / cycleLength + 1
                    nextStart = last.plusDays((k * cycleLength).toLong())
                }
            }
        }

        return CycleModel(
            ranges = ranges,
            cycleLength = cycleLength,
            periodLength = periodLength,
            cyclesUsed = lengths.size,
            stdDev = stdDeviation(lengths),
            today = today,
            nextStart = nextStart,
            lateDays = lateDays
        )
    }

    // ---------------------------------------------------------------- фаза дня

    fun getCycleInfo(iso: String, model: CycleModel): CycleInfo = getCycleInfo(parse(iso), model)

    fun getCycleInfo(date: LocalDate, model: CycleModel): CycleInfo {
        val ranges = model.ranges
        val next = model.nextStart
        val unknown = CycleInfo(
            cycleDay = null,
            phase = CyclePhase.UNKNOWN,
            ovulationDay = ovulationDay(model.cycleLength, model.periodLength),
            pregnancyChance = PregnancyChance.UNKNOWN,
            cycleLength = model.cycleLength
        )
        if (ranges.isEmpty() || next == null) return unknown

        val idx = ranges.indexOfLast { !it.first.isAfter(date) }
        if (idx < 0) return unknown

        val start = ranges[idx].first
        val rangeLen = days(start, ranges[idx].second) + 1

        val cycleLen: Int
        val periodLen: Int
        val cycleDay: Int

        if (idx < ranges.lastIndex) {
            // Завершённый цикл — берём его фактические длины
            cycleLen = days(start, ranges[idx + 1].first)
            periodLen = rangeLen
            cycleDay = days(start, date) + 1
        } else {
            val diff = days(start, date)
            cycleLen = model.cycleLength
            val inCurrentCycle = date.isBefore(next) || (model.lateDays > 0 && !date.isAfter(model.today))
            if (inCurrentCycle && diff < cycleLen + LATE_GRACE_DAYS) {
                periodLen = max(model.periodLength, rangeLen)
                cycleDay = diff + 1
            } else if (!date.isBefore(next)) {
                // Прогноз вперёд от ожидаемого начала следующих месячных
                if (date.isAfter(model.today.plusDays(PROJECTION_DAYS))) return unknown
                periodLen = model.periodLength
                cycleDay = days(next, date) % cycleLen + 1
            } else {
                // Давно пропущенные отметки — выстраиваем по ритму цикла
                periodLen = model.periodLength
                cycleDay = diff % cycleLen + 1
            }
        }

        val ovDay = ovulationDay(cycleLen, periodLen)
        val phase = when {
            cycleDay <= periodLen -> CyclePhase.MENSTRUATION
            cycleDay < ovDay - 1 -> CyclePhase.FOLLICULAR
            cycleDay in (ovDay - 1)..(ovDay + 1) -> CyclePhase.OVULATION
            else -> CyclePhase.LUTEAL
        }

        val distance = abs(cycleDay - ovDay)
        val chance = when {
            cycleDay <= periodLen -> PregnancyChance.VERY_LOW
            distance == 0 -> PregnancyChance.PEAK
            distance <= 1 -> PregnancyChance.HIGH
            distance <= 3 -> PregnancyChance.MEDIUM
            distance <= 5 -> PregnancyChance.LOW
            else -> PregnancyChance.VERY_LOW
        }

        return CycleInfo(cycleDay, phase, ovDay, chance, cycleLen)
    }

    // ---------------------------------------------------------------- история и статистика

    data class HistoryRow(
        val startIso: String,
        val endIso: String,
        val periodLengthDays: Int,
        val cycleLengthDays: Int?
    )

    /** История для списка и графика: от новых к старым */
    fun buildHistory(periods: List<String>): List<HistoryRow> {
        val ranges = mergedRanges(periods)
        val rows = mutableListOf<HistoryRow>()
        for (i in ranges.indices) {
            val (start, end) = ranges[i]
            val periodLen = days(start, end) + 1
            val cycleLen = if (i > 0) days(ranges[i - 1].first, start) else null
            rows.add(HistoryRow(format(start), format(end), periodLen, cycleLen))
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

    fun computeCycleStats(periodDays: List<String>): CycleStats {
        val ranges = mergedRanges(periodDays)
        val periodLens = ranges.map { days(it.first, it.second) + 1 }
        val avgPeriodLen = if (periodLens.isNotEmpty()) periodLens.average() else null

        val lengths = validCycleLengths(ranges.map { it.first })
        if (lengths.isEmpty()) {
            return CycleStats(emptyList(), 0.0, 0.0, false, null, null, avgPeriodLen)
        }
        val std = stdDeviation(lengths)
        return CycleStats(
            cycleLengths = lengths,
            averageLength = lengths.average(),
            stdDeviation = std,
            isRegular = std <= 3.0,
            shortestCycle = lengths.minOrNull(),
            longestCycle = lengths.maxOrNull(),
            averagePeriodLength = avgPeriodLen
        )
    }

    // ---------------------------------------------------------------- прогноз

    fun buildForecast(model: CycleModel, days: Map<String, DayEntry>): CycleForecast? {
        val next = model.nextStart ?: return null
        val last = model.lastStart ?: return null
        val today = model.today

        val ovDay = ovulationDay(model.cycleLength, model.periodLength)
        val currentOvulation = last.plusDays((ovDay - 1).toLong())
        val ovulation = if (!currentOvulation.isBefore(today)) {
            currentOvulation
        } else {
            next.plusDays((ovDay - 1).toLong())
        }

        val weekBefore = next.minusDays(7)
        val weekBeforeInfo = getCycleInfo(weekBefore, model)

        // Средний исторический уровень энергии в лютеиновой фазе
        val lutealScores = days.entries.mapNotNull { (iso, entry) ->
            val energy = entry.energy ?: return@mapNotNull null
            val info = getCycleInfo(iso, model)
            if (info.phase == CyclePhase.LUTEAL) EnergyLevel.scoreOf(energy) else null
        }
        val energyGuess = if (lutealScores.isNotEmpty()) EnergyLevel.fromScore(lutealScores.average()) else null

        val daysUntil = if (model.lateDays > 0) 0 else days(today, next)
        val note = when {
            model.lateDays > 0 ->
                "Месячные задерживаются на ${model.lateDays} дн. относительно прогноза. Как только они начнутся — нажмите «Начались месячные», и прогноз пересчитается."
            daysUntil in 0..7 ->
                "Скоро месячные. Обычно на этом отрезке — поздняя лютеиновая фаза: возможны раздражительность, перепады настроения, тяга к еде и снижение энергии (ПМС)."
            else ->
                "За неделю до месячных (~${formatDMY(weekBefore)}) обычно наступает поздняя лютеиновая фаза — возможны раздражительность и снижение энергии."
        }

        return CycleForecast(
            predictedNextPeriodDate = format(next),
            daysUntilPeriod = daysUntil,
            lateDays = model.lateDays,
            predictedOvulationDate = format(ovulation),
            daysUntilOvulation = days(today, ovulation),
            weekBeforeDate = format(weekBefore),
            weekBeforePhase = weekBeforeInfo.phase,
            weekBeforeEnergyGuess = energyGuess,
            accuracyDays = model.accuracyDays,
            cyclesUsed = model.cyclesUsed,
            note = note
        )
    }

    // ---------------------------------------------------------------- быстрые действия

    /** Дни, которые нужно добавить, чтобы закрыть текущий период: от начала до последнего дня (включительно) */
    fun daysToCloseCurrentPeriod(model: CycleModel, todayLogged: Boolean): List<String> {
        val start = model.lastStart ?: return emptyList()
        val endCandidate = if (todayLogged) model.today else model.today.minusDays(1)
        val end = if (endCandidate.isBefore(start)) start else endCandidate
        val result = mutableListOf<String>()
        var d = start
        while (!d.isAfter(end)) {
            result.add(format(d))
            d = d.plusDays(1)
        }
        return result
    }
}
