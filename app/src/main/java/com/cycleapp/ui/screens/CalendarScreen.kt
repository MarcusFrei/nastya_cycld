package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.components.AppCard
import com.cycleapp.ui.components.DayCell
import com.cycleapp.ui.components.DayStyle
import com.cycleapp.ui.components.EnergyMarker
import com.cycleapp.ui.components.LegendCircle
import com.cycleapp.ui.components.SectionTitle
import com.cycleapp.util.CycleCalculator
import java.time.LocalDate
import java.time.YearMonth

private val RU_MONTHS = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)
private val RU_WEEKDAYS = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

private const val FIXED_ROWS = 6
private const val FIXED_CELLS = FIXED_ROWS * 7

@Composable
fun CalendarScreen(
    viewMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    model: CycleCalculator.CycleModel,
    periods: List<String>,
    days: Map<String, DayEntry>,
    onDayClick: (String) -> Unit
) {
    val todayIso = CycleCalculator.format(model.today)
    val periodSet = periods.toSet()

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            text = "Календарь",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(12.dp))

        AppCard(
            modifier = Modifier.pointerInput(viewMonth) {
                // свайп влево/вправо — следующий/предыдущий месяц
                var total = 0f
                detectHorizontalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = {
                        if (total > 110f) onMonthChange(viewMonth.minusMonths(1))
                        else if (total < -110f) onMonthChange(viewMonth.plusMonths(1))
                    },
                    onDragCancel = { total = 0f },
                    onHorizontalDrag = { _, dragAmount -> total += dragAmount }
                )
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(viewMonth.minusMonths(1)) }) {
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = "Предыдущий месяц",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${RU_MONTHS[viewMonth.monthValue - 1]} ${viewMonth.year}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onMonthChange(viewMonth.plusMonths(1)) }) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Следующий месяц",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                RU_WEEKDAYS.forEach {
                    Text(
                        text = it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // Сетка 6×7 без прокрутки внутри — высота фиксирована, месяц не «прыгает»
            val cells = buildMonthCells(viewMonth)
            for (week in 0 until FIXED_ROWS) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (dayIdx in 0 until 7) {
                        val cell = cells[week * 7 + dayIdx]
                        val iso = CycleCalculator.format(cell.date)
                        val entry = days[iso]
                        Box(modifier = Modifier.weight(1f)) {
                            DayCell(
                                dayNumber = cell.date.dayOfMonth,
                                isToday = iso == todayIso,
                                isOtherMonth = cell.otherMonth,
                                energy = entry?.energy,
                                isPeriod = periodSet.contains(iso),
                                cycleInfo = CycleCalculator.getCycleInfo(cell.date, model),
                                hasExtra = entry != null &&
                                    (entry.symptoms.isNotEmpty() || entry.mood != null || entry.note.isNotBlank() || entry.flow != null),
                                onClick = { onDayClick(iso) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "Нажмите на день, чтобы отметить месячные, энергию, настроение и симптомы. Свайп — смена месяца.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(14.dp))
        Legend()
    }
}

private data class MonthCell(val date: LocalDate, val otherMonth: Boolean)

private fun buildMonthCells(month: YearMonth): List<MonthCell> {
    val first = month.atDay(1)
    val startWeekday = first.dayOfWeek.value // Monday=1..Sunday=7
    val cells = mutableListOf<MonthCell>()

    for (i in (startWeekday - 1) downTo 1) {
        cells.add(MonthCell(first.minusDays(i.toLong()), otherMonth = true))
    }
    for (d in 1..month.lengthOfMonth()) {
        cells.add(MonthCell(month.atDay(d), otherMonth = false))
    }
    var next = month.atEndOfMonth().plusDays(1)
    while (cells.size < FIXED_CELLS) {
        cells.add(MonthCell(next, otherMonth = true))
        next = next.plusDays(1)
    }
    return cells
}

/**
 * Легенда рисуется теми же кругами, что и сам календарь, — значки совпадают 1 в 1.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend() {
    AppCard {
        SectionTitle("Что означают значки")
        Spacer(Modifier.height(12.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LegendItem("Месячные (отмечены)") { LegendCircle(DayStyle.PERIOD) }
            LegendItem("Месячные (прогноз)") { LegendCircle(DayStyle.PERIOD_PREDICTED) }
            LegendItem("Фертильные дни") { LegendCircle(DayStyle.FERTILE) }
            LegendItem("Овуляция") { LegendCircle(DayStyle.OVULATION) }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "Энергия — маркер с цифрой под числом",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        EnergyLevel.entries.forEach { level ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnergyMarker(level = level, size = 22.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = level.label,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "Есть заметка, настроение или симптомы",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun LegendItem(label: String, icon: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(8.dp))
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}
