package com.cycleapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.components.AppCard
import com.cycleapp.ui.theme.cycleColors
import com.cycleapp.util.CycleCalculator
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    periods: List<String>,
    days: Map<String, DayEntry>,
    model: CycleCalculator.CycleModel
) {
    val stats = CycleCalculator.computeCycleStats(periods)
    val history = CycleCalculator.buildHistory(periods)

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = "Статистика",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(18.dp))

        // --- Анализ энергии ---
        SectionHeader("Анализ энергии")
        Spacer(Modifier.height(10.dp))

        SectionCard(title = "Уровень энергии — последние 30 дней") {
            EnergyBarChart(days = days)
        }

        Spacer(Modifier.height(16.dp))

        SectionCard(title = "Средние показатели энергии по фазам") {
            EnergyByPhaseSummary(days = days, model = model)
        }

        Spacer(Modifier.height(22.dp))

        // --- Анализ цикла ---
        SectionHeader("Анализ цикла")
        Spacer(Modifier.height(10.dp))

        SectionCard(title = "Стабильность менструального цикла") {
            if (stats.cycleLengths.isEmpty()) {
                Text(
                    "Отметьте минимум 2 начала менструации, чтобы увидеть статистику стабильности.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBlock("Средняя длина", "${stats.averageLength.roundToInt()} дн.")
                    StatBlock("Мин / Макс", "${stats.shortestCycle} / ${stats.longestCycle}")
                    StatBlock(
                        "Регулярность",
                        if (stats.isRegular) "Стабильный" else "Нерегулярный",
                        valueColor = if (stats.isRegular) Color(0xFF4CAF6D) else Color(0xFFF2B705)
                    )
                }
                stats.averagePeriodLength?.let { avgPeriod ->
                    Spacer(Modifier.height(10.dp))
                    StatBlock("Средняя длительность месячных", "${String.format("%.1f", avgPeriod)} дн.")
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Отклонение: ±${String.format("%.1f", stats.stdDeviation)} дн. между циклами",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                CycleLengthChart(lengths = stats.cycleLengths, cycleLengthTarget = model.cycleLength)
            }
        }

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionCard(title = "История циклов") {
                CycleHistoryList(history = history)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun CycleHistoryList(history: List<CycleCalculator.HistoryRow>) {
    Column {
        history.take(6).forEachIndexed { index, row ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${CycleCalculator.formatDMY(row.startIso)} – ${CycleCalculator.formatDMY(row.endIso)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${row.periodLengthDays} дн. месячных",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (row.cycleLengthDays != null) {
                    Text(
                        text = "${row.cycleLengthDays} дн. цикл",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (index < history.take(6).size - 1) {
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    AppCard {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun StatBlock(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Column {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (valueColor == Color.Unspecified) MaterialTheme.colorScheme.onSurface else valueColor
        )
    }
}

/** Столбчатая диаграмма энергии за последние 30 дней */
@Composable
private fun EnergyBarChart(days: Map<String, DayEntry>) {
    val today = LocalDate.now()
    val last30 = (29 downTo 0).map { today.minusDays(it.toLong()) }
    val barColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val outlineColor = cycleColors().markerOutline

    Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val barWidth = size.width / last30.size
        val maxHeight = size.height

        last30.forEachIndexed { index, date ->
            val iso = CycleCalculator.format(date)
            val entry = days[iso]
            val level = entry?.energy
            val heightFraction = when (level) {
                EnergyLevel.RED -> 0.2f
                EnergyLevel.YELLOW -> 0.4f
                EnergyLevel.WHITE -> 0.6f
                EnergyLevel.GREEN -> 0.8f
                EnergyLevel.BLUE -> 1.0f
                null -> 0.06f
            }
            val color = level?.color ?: barColor
            val barHeight = maxHeight * heightFraction
            val topLeft = Offset(index * barWidth + barWidth * 0.15f, maxHeight - barHeight)
            val barSize = Size(barWidth * 0.7f, barHeight)
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = barSize,
                cornerRadius = CornerRadius(4f, 4f)
            )
            if (level != null) {
                drawRoundRect(
                    color = outlineColor,
                    topLeft = topLeft,
                    size = barSize,
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}

/** Линейный график длины последних циклов относительно целевой длины */
@Composable
private fun CycleLengthChart(lengths: List<Int>, cycleLengthTarget: Int) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val recent = lengths.takeLast(12)
    if (recent.isEmpty()) return

    val minVal = (recent.min() - 2).coerceAtLeast(10)
    val maxVal = recent.max() + 2

    Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
        val stepX = if (recent.size > 1) size.width / (recent.size - 1) else size.width
        fun yFor(v: Int): Float {
            val fraction = (v - minVal).toFloat() / (maxVal - minVal).toFloat()
            return size.height - (fraction * size.height)
        }

        // целевая линия
        val targetY = yFor(cycleLengthTarget.coerceIn(minVal, maxVal))
        drawLine(
            color = gridColor,
            start = Offset(0f, targetY),
            end = Offset(size.width, targetY),
            strokeWidth = 2f
        )

        val points = recent.mapIndexed { i, v -> Offset(i * stepX, yFor(v)) }
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        points.forEach { p ->
            drawCircle(color = lineColor, radius = 5f, center = p)
        }
    }
}

@Composable
private fun EnergyByPhaseSummary(
    days: Map<String, DayEntry>,
    model: CycleCalculator.CycleModel
) {
    val phaseScores = mutableMapOf<com.cycleapp.data.CyclePhase, MutableList<Int>>()
    days.forEach { (iso, entry) ->
        val energy = entry.energy ?: return@forEach
        val info = CycleCalculator.getCycleInfo(iso, model)
        val score = when (energy) {
            EnergyLevel.RED -> 1
            EnergyLevel.YELLOW -> 2
            EnergyLevel.WHITE -> 3
            EnergyLevel.GREEN -> 4
            EnergyLevel.BLUE -> 5
        }
        phaseScores.getOrPut(info.phase) { mutableListOf() }.add(score)
    }

    if (phaseScores.isEmpty()) {
        Text(
            "Отмечайте энергию в календаре — здесь появится сравнение по фазам цикла.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val order = listOf(
        com.cycleapp.data.CyclePhase.MENSTRUATION,
        com.cycleapp.data.CyclePhase.FOLLICULAR,
        com.cycleapp.data.CyclePhase.OVULATION,
        com.cycleapp.data.CyclePhase.LUTEAL
    )

    Column {
        order.forEach { phase ->
            val scores = phaseScores[phase]
            val avg = scores?.average() ?: 0.0
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(phase.label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (scores != null) "${String.format("%.1f", avg)} / 5" else "нет данных",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
