package com.cycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleForecast
import com.cycleapp.util.CycleCalculator

@Composable
fun ForecastCard(forecast: CycleForecast) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            SectionTitle("Прогноз")
        }
        Spacer(Modifier.height(10.dp))

        val next = CycleCalculator.parse(forecast.predictedNextPeriodDate)
        val whenLabel = when {
            forecast.lateDays > 0 -> "задержка ${forecast.lateDays} дн."
            forecast.daysUntilPeriod == 0 -> "сегодня"
            forecast.daysUntilPeriod == 1 -> "завтра"
            else -> "через ${forecast.daysUntilPeriod} дн."
        }
        Text(
            text = "Следующие месячные: ${CycleCalculator.formatDMY(next)} ($whenLabel)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            forecast.weekBeforeEnergyGuess?.let { level ->
                EnergyMarker(level = level, size = 22.dp)
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = "За неделю до месячных ожидается: ${forecast.weekBeforePhase.label.lowercase()}" +
                    (forecast.weekBeforeEnergyGuess?.let { ", энергия ~«${it.label.lowercase()}»" } ?: ""),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = forecast.note,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = accuracyText(forecast),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
        )
    }
}

private fun accuracyText(f: CycleForecast): String = when {
    f.cyclesUsed == 0 ->
        "Пока в основе прогноза типовой цикл в 28 дней (±${f.accuracyDays} дн.). После двух отмеченных циклов он подстроится под вас."
    f.cyclesUsed < 3 ->
        "Прогноз учитывает ${f.cyclesUsed} ${cyclesWord(f.cyclesUsed)} и станет точнее с каждым новым (сейчас ±${f.accuracyDays} дн.)."
    else ->
        "Прогноз построен по ${f.cyclesUsed} ${cyclesWord(f.cyclesUsed)}, погрешность около ±${f.accuracyDays} дн. Он приблизительный и не заменяет консультацию врача."
}

private fun cyclesWord(n: Int): String = when {
    n % 10 == 1 && n % 100 != 11 -> "цикл"
    n % 10 in 2..4 && n % 100 !in 12..14 -> "цикла"
    else -> "циклов"
}
