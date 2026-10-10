package com.cycleapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleForecast
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.components.AppCard
import com.cycleapp.ui.components.DropIcon
import com.cycleapp.ui.components.EnergyMarker
import com.cycleapp.ui.components.ForecastCard
import com.cycleapp.ui.components.PillButton
import com.cycleapp.ui.components.SectionTitle
import com.cycleapp.ui.theme.PhaseColors
import com.cycleapp.ui.theme.cycleColors
import com.cycleapp.util.CycleCalculator
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max

private val TILE_TEXT = Color(0xFF2B1B33)

/** Главный экран «Цикл» — по мотивам макета: приветствие, карточка дня с графиком, две плитки */
@Composable
fun HomeScreen(
    model: CycleCalculator.CycleModel,
    periods: List<String>,
    days: Map<String, DayEntry>,
    forecast: CycleForecast?,
    onQuickEnergy: (EnergyLevel) -> Unit,
    onEditToday: () -> Unit,
    onStartPeriod: () -> Unit,
    onMarkToday: () -> Unit,
    onEndPeriod: () -> Unit
) {
    val today = model.today
    val todayIso = CycleCalculator.format(today)
    val info = CycleCalculator.getCycleInfo(todayIso, model)
    val todayLogged = periods.contains(todayIso)
    val inPeriod = info.phase == CyclePhase.MENSTRUATION

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Greeting(today)
        Spacer(Modifier.height(14.dp))

        // ---------- Карточка дня: где я сейчас в цикле ----------
        AppCard {
            HeroHeader(model = model, info = info, forecast = forecast)

            if (model.hasData && info.cycleDay != null) {
                Spacer(Modifier.height(12.dp))
                CycleTimeline(model = model, info = info)
                Spacer(Modifier.height(6.dp))
                TimelineLegend()
                Spacer(Modifier.height(10.dp))
                Text(
                    text = info.phase.hint,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            if (!model.hasData) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton("Начались месячные", onStartPeriod, Modifier.weight(1f))
                    EditButton(onEditToday)
                }
            } else if (inPeriod) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!todayLogged) {
                        PillButton("Отметить сегодня", onMarkToday, Modifier.weight(1f), filled = false)
                    }
                    PillButton("Закончились", onEndPeriod, Modifier.weight(1f))
                    EditButton(onEditToday)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PillButton("Начались месячные", onStartPeriod, Modifier.weight(1f))
                    EditButton(onEditToday)
                }
            }
        }

        // ---------- Две плитки: следующие месячные / овуляция ----------
        if (forecast != null) {
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val nextDate = CycleCalculator.parse(forecast.predictedNextPeriodDate)
                val ovDate = CycleCalculator.parse(forecast.predictedOvulationDate)

                InfoTile(
                    modifier = Modifier.weight(1f),
                    title = "Месячные",
                    date = CycleCalculator.formatShort(nextDate),
                    caption = when {
                        forecast.lateDays > 0 -> "задержка ${forecast.lateDays} дн."
                        forecast.daysUntilPeriod == 0 -> "сегодня"
                        forecast.daysUntilPeriod == 1 -> "завтра"
                        else -> "через ${forecast.daysUntilPeriod} дн. (±${forecast.accuracyDays})"
                    },
                    gradient = listOf(Color(0xFFD5F2E6), Color(0xFF9ED9BF)),
                    showDrop = true
                )
                InfoTile(
                    modifier = Modifier.weight(1f),
                    title = "Овуляция",
                    date = CycleCalculator.formatShort(ovDate),
                    caption = when {
                        forecast.daysUntilOvulation <= 0 -> "сегодня"
                        forecast.daysUntilOvulation == 1 -> "завтра"
                        else -> "через ${forecast.daysUntilOvulation} дн."
                    },
                    gradient = listOf(Color(0xFFF1DDFA), Color(0xFFD3A8EE)),
                    showDrop = false
                )
            }
        }

        // ---------- Быстрая отметка энергии ----------
        Spacer(Modifier.height(14.dp))
        EnergyQuickCard(
            current = days[todayIso]?.energy,
            onSelect = onQuickEnergy
        )

        // ---------- Вероятность зачатия ----------
        Spacer(Modifier.height(14.dp))
        ChanceCard(info = info)

        // ---------- Прогноз ----------
        if (forecast != null) {
            Spacer(Modifier.height(14.dp))
            ForecastCard(forecast = forecast)
        }
    }
}

@Composable
private fun Greeting(today: LocalDate) {
    val hour = LocalTime.now().hour
    val greeting = when (hour) {
        in 5..11 -> "Доброе утро"
        in 12..17 -> "Добрый день"
        in 18..22 -> "Добрый вечер"
        else -> "Доброй ночи"
    }
    Column {
        Text(
            text = greeting,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CycleCalculator.formatLong(today).replaceFirstChar { it.uppercase() },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun HeroHeader(model: CycleCalculator.CycleModel, info: CycleInfo, forecast: CycleForecast?) {
    val colors = cycleColors()
    val title: String
    val subtitle: String
    when {
        !model.hasData -> {
            title = "Начнём отслеживание"
            subtitle = info.phase.hint
        }
        info.phase == CyclePhase.MENSTRUATION -> {
            title = "Месячные · день ${info.cycleDay}"
            subtitle = untilText(info, model, forecast)
        }
        model.lateDays > 0 -> {
            title = "Задержка ${model.lateDays} дн."
            subtitle = "День цикла ${info.cycleDay} из ${info.cycleLength}"
        }
        else -> {
            title = info.phase.label
            subtitle = untilText(info, model, forecast)
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.period.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            DropIcon(color = colors.period, size = 22.dp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** «День 2 из 28 · до месячных 27 дн.» */
private fun untilText(info: CycleInfo, model: CycleCalculator.CycleModel, forecast: CycleForecast?): String {
    val day = "День ${info.cycleDay} из ${info.cycleLength}"
    val until = when {
        forecast == null -> ""
        model.lateDays > 0 -> ""
        forecast.daysUntilPeriod == 0 -> " · месячные сегодня"
        forecast.daysUntilPeriod == 1 -> " · месячные завтра"
        else -> " · до месячных ${forecast.daysUntilPeriod} дн."
    }
    return day + until
}

/**
 * Шкала цикла: по одной полоске на каждый день. Видно, где месячные, фертильные дни и овуляция,
 * сколько дней прошло (яркие) и сколько осталось (приглушённые). Сегодняшний день — высокая полоска с рамкой.
 */
@Composable
private fun CycleTimeline(model: CycleCalculator.CycleModel, info: CycleInfo) {
    val colors = cycleColors()
    val today = info.cycleDay ?: return
    val cycleLen = info.cycleLength
    val ovDay = info.ovulationDay
    val lastStart = model.lastStart
    val lastEnd = model.lastEnd
    val markedLen = if (lastStart != null && lastEnd != null) {
        ChronoUnit.DAYS.between(lastStart, lastEnd).toInt() + 1
    } else 0
    val periodLen = max(model.periodLength, markedLen)
    val neutral = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    val lateColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val todayRing = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = Modifier.fillMaxWidth().height(34.dp)) {
        val total = max(cycleLen, today)          // при задержке шкала удлиняется
        val gap = 1.5.dp.toPx()
        val sliceW = (size.width - gap * (total - 1)) / total
        val corner = minOf(sliceW / 2f, 3.dp.toPx())

        for (d in 1..total) {
            val base = when {
                d > cycleLen -> lateColor
                d <= periodLen -> colors.period
                d == ovDay -> colors.ovulation
                abs(d - ovDay) <= 3 -> colors.fertile
                else -> neutral
            }
            val color = if (d <= today) base else base.copy(alpha = base.alpha * 0.5f)
            val isToday = d == today
            val h = if (isToday) size.height else size.height * 0.55f
            val topLeft = Offset((d - 1) * (sliceW + gap), (size.height - h) / 2f)
            drawRoundRect(color, topLeft, Size(sliceW, h), CornerRadius(corner, corner))
            if (isToday) {
                drawRoundRect(
                    color = todayRing,
                    topLeft = topLeft,
                    size = Size(sliceW, h),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun TimelineLegend() {
    val colors = cycleColors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(colors.period, "Месячные")
        LegendDot(colors.fertile, "Фертильные дни")
        LegendDot(colors.ovulation, "Овуляция")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun EditButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Edit,
            contentDescription = "Редактировать сегодняшний день",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Цветная плитка (мятная / лиловая) — текст всегда тёмный, поэтому читается при любой теме */
@Composable
private fun InfoTile(
    modifier: Modifier,
    title: String,
    date: String,
    caption: String,
    gradient: List<Color>,
    showDrop: Boolean
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(gradient))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.White.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                if (showDrop) DropIcon(color = Color(0xFFD6336C), size = 14.dp)
                else Text("✦", fontSize = 13.sp, color = Color(0xFF7B2FA0))
            }
            Spacer(Modifier.width(8.dp))
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TILE_TEXT, maxLines = 1)
        }
        Spacer(Modifier.height(8.dp))
        Text(text = date, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TILE_TEXT)
        Text(text = caption, fontSize = 12.sp, color = TILE_TEXT.copy(alpha = 0.85f), maxLines = 1)
    }
}

/** Отметить энергию на сегодня одним нажатием; цифры 1–5 дублируют цвет */
@Composable
private fun EnergyQuickCard(current: EnergyLevel?, onSelect: (EnergyLevel) -> Unit) {
    AppCard {
        SectionTitle("Как энергия сегодня?")
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            EnergyLevel.entries.forEach { level ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(level) }
                        .padding(vertical = 4.dp)
                ) {
                    EnergyMarker(level = level, size = 42.dp, selected = current == level)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = level.shortLabel,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = if (current == level) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (current != null) "Отмечено: ${current.label}. Нажмите ещё раз, чтобы снять отметку."
            else "1 — минимум сил, 5 — пик энергии. Отметки собираются в статистику.",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChanceCard(info: CycleInfo) {
    AppCard {
        SectionTitle("Вероятность забеременеть сегодня")
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(PhaseColors.chanceColor(info.pregnancyChance))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = info.pregnancyChance.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Приблизительная оценка на основе календарного метода. Не является медицинской рекомендацией или методом контрацепции.",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
