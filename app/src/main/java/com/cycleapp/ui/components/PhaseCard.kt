package com.cycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.PregnancyChance

@Composable
fun PhaseCard(info: CycleInfo, cycleLength: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface)
                )
            )
            .padding(16.dp)
    ) {
        Text(
            text = if (info.cycleDay != null) "День цикла ${info.cycleDay} из $cycleLength" else "Сегодня",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = info.phase.label,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = info.phase.hint,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "Вероятность забеременеть сегодня",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = info.pregnancyChance.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pregnancyColor(info.pregnancyChance)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Приблизительная оценка на основе календарного метода. Не является медицинской рекомендацией или методом контрацепции.",
            fontSize = 10.sp,
            lineHeight = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

fun pregnancyColor(chance: PregnancyChance): Color = when (chance) {
    PregnancyChance.VERY_LOW -> Color(0xFF4CAF6D)
    PregnancyChance.LOW -> Color(0xFF8BC98A)
    PregnancyChance.MEDIUM -> Color(0xFFF2B705)
    PregnancyChance.HIGH -> Color(0xFFF28705)
    PregnancyChance.PEAK -> Color(0xFFE5484D)
}
