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
import androidx.compose.foundation.shape.CircleShape
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Прогноз", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))

        if (forecast.predictedNextPeriodDate != null && forecast.daysUntilPeriod != null) {
            val date = CycleCalculator.parse(forecast.predictedNextPeriodDate)
            val daysLabel = when {
                forecast.daysUntilPeriod <= 0 -> "со дня на день"
                forecast.daysUntilPeriod == 1 -> "завтра"
                else -> "через ${forecast.daysUntilPeriod} дн."
            }
            Text(
                text = "Ожидаемое начало следующих месячных: ${date.dayOfMonth}.${date.monthValue.toString().padStart(2,'0')} ($daysLabel)",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            forecast.weekBeforeEnergyGuess?.let { level ->
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(level.color)
                )
                Spacer(Modifier.width(8.dp))
            }
            Column {
                Text(
                    text = "За неделю до месячных ожидается: ${forecast.weekBeforePhase.label.lowercase()}" +
                        (forecast.weekBeforeEnergyGuess?.let { ", энергия ~«${it.label.lowercase()}»" } ?: ""),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = forecast.note,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Прогноз строится на основе ваших предыдущих отметок и носит приблизительный характер.",
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

