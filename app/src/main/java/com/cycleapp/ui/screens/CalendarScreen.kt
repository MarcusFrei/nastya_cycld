package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.cycleapp.ui.theme.PhaseColors
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    viewMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    periods: List<String>,
    days: Map<String, DayEntry>,
    settings: CycleSettings,
    onDayClick: (String) -> Unit
) {
    val todayIso = CycleCalculator.format(LocalDate.now())
    val todayInfo = CycleCalculator.getCycleInfo(todayIso, periods, settings.cycleLength, settings.periodLength)
    val forecast = CycleCalculator.buildForecast(periods, days, settings.cycleLength, settings.periodLength)
    val todayEntry = days[todayIso]

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Мой цикл",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Самочувствие и календарь в одном месте",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 3.dp, bottom = 16.dp)
        )

        PhaseCard(info = todayInfo, cycleLength = settings.cycleLength)

        if (todayEntry?.energy == null) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .clickable { onDayClick(todayIso) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.EditCalendar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 11.dp)) {
                    Text("Как ваша энергия сегодня?", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Отметка займёт несколько секунд", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChange(viewMonth.minusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Предыдущий месяц", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${RU_MONTHS[viewMonth.monthValue - 1]} ${viewMonth.year}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text("Нажмите на день для отметки", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onMonthChange(viewMonth.plusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Следующий месяц", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RU_WEEKDAYS.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (index > 4) MaterialTheme.colorScheme.primary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val cells = buildMonthCells(viewMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth().height((FIXED_ROWS * 47).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
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
                        cycleInfo = info,
                        hasExtra = entry != null && (entry.symptoms.isNotEmpty() || entry.mood != null || entry.note.isNotBlank()),
                        onClick = { onDayClick(iso) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("ЭНЕРГИЯ", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 5.dp, bottom = 7.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnergyLevel.entries.forEach { level ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(level.color).border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(level.label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f)).padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(PhaseColors.menstruationActual))
                    Spacer(Modifier.width(5.dp))
                    Text("Месячные", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌱", fontSize = 11.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("Фертильное окно", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("★", fontSize = 11.sp, color = PhaseColors.ovulationMarker)
                    Spacer(Modifier.width(4.dp))
                    Text("Овуляция", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (forecast != null) {
            Spacer(Modifier.height(14.dp))
            ForecastCard(forecast = forecast)
        }
        Spacer(Modifier.height(22.dp))
    }
}

private data class MonthCell(val date: LocalDate, val otherMonth: Boolean)

private fun buildMonthCells(month: YearMonth): List<MonthCell> {
    val first = month.atDay(1)
    val startWeekday = first.dayOfWeek.value
    val cells = mutableListOf<MonthCell>()
    for (i in (startWeekday - 1) downTo 1) cells.add(MonthCell(first.minusDays(i.toLong()), true))
    for (d in 1..month.lengthOfMonth()) cells.add(MonthCell(month.atDay(d), false))
    var next = month.atEndOfMonth().plusDays(1)
    while (cells.size < FIXED_CELLS) {
        cells.add(MonthCell(next, true))
        next = next.plusDays(1)
    }
    return cells
}
