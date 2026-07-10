package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.components.DayCell
import com.cycleapp.ui.components.ForecastCard
import com.cycleapp.ui.components.PhaseCard
import com.cycleapp.util.CycleCalculator
import java.time.LocalDate
import java.time.YearMonth

private val RU_MONTHS = listOf(
    "Январь","Февраль","Март","Апрель","Май","Июнь",
    "Июль","Август","Сентябрь","Октябрь","Ноябрь","Декабрь"
)
private val RU_WEEKDAYS = listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")

private const val FIXED_ROWS = 6
private const val FIXED_CELLS = FIXED_ROWS * 7

@Composable
fun CalendarScreen(
    viewMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    periods: List<String>,
    days: Map<String, DayEntry>,
    settings: CycleSettings,
    onDayClick: (String) -> Unit
) {
    val today = LocalDate.now()
    val todayIso = CycleCalculator.format(today)
    val todayInfo = CycleCalculator.getCycleInfo(todayIso, periods, settings.cycleLength, settings.periodLength)
    val forecast = CycleCalculator.buildForecast(periods, days, settings.cycleLength, settings.periodLength)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Карточка фазы — компактная сводка на сегодня
        PhaseCard(info = todayInfo, cycleLength = settings.cycleLength)

        Spacer(Modifier.height(14.dp))

        // Календарь — сразу после карточки фазы, до прогноза
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(viewMonth.minusMonths(1)) }) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Предыдущий месяц",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "${RU_MONTHS[viewMonth.monthValue - 1]} ${viewMonth.year}",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { onMonthChange(viewMonth.plusMonths(1)) }) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Следующий месяц",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            RU_WEEKDAYS.forEach {
                Text(
                    text = it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        val cells = buildMonthCells(viewMonth)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height((FIXED_ROWS * 52).dp),
        ) {
            items(cells) { cell ->
                val iso = CycleCalculator.format(cell.date)
                val entry = days[iso]
                val info = CycleCalculator.getCycleInfo(iso, periods, settings.cycleLength, settings.periodLength)
                DayCell(
                    dayNumber = cell.date.dayOfMonth,
                    isToday = iso == todayIso,
                    isOtherMonth = cell.otherMonth,
                    energy = entry?.energy,
                    isPeriod = periods.contains(iso),
                    isOvulation = info.cycleDay != null && info.cycleDay == info.ovulationDay,
                    hasExtra = entry != null && (entry.symptoms.isNotEmpty() || entry.mood != null || entry.note.isNotBlank()),
                    onClick = { onDayClick(iso) }
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        LegendList()

        // Прогноз — теперь после календаря, внизу экрана
        if (forecast != null) {
            Spacer(Modifier.height(18.dp))
            ForecastCard(forecast = forecast)
        }

        Spacer(Modifier.height(24.dp))
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

/** Компактный список-легенда: одна строка на цвет, без "прыгающих" чипов */
@Composable
private fun LegendList() {
    val items = listOf(
        EnergyLevel.RED, EnergyLevel.YELLOW, EnergyLevel.WHITE, EnergyLevel.GREEN, EnergyLevel.BLUE
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        items.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(level.color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = level.label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
