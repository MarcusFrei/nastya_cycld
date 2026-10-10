package com.cycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.PregnancyChance

@Composable
fun PhaseCard(info: CycleInfo, cycleLength: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = phaseSymbol(info.phase.label),
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = if (info.cycleDay != null) "ДЕНЬ ${info.cycleDay} ИЗ $cycleLength" else "ВАШ ЦИКЛ",
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = info.phase.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = info.phase.hint,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.size(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(pregnancyColor(info.pregnancyChance)))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Фертильность: ${info.pregnancyChance.label.lowercase()}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun phaseSymbol(label: String): String = when {
    label.contains("Менстру", ignoreCase = true) -> "✿"
    label.contains("Фоллик", ignoreCase = true) -> "🌱"
    label.contains("Овуляц", ignoreCase = true) -> "✦"
    else -> "☾"
}

fun pregnancyColor(chance: PregnancyChance): Color = when (chance) {
    PregnancyChance.VERY_LOW -> Color(0xFF4CAF6D)
    PregnancyChance.LOW -> Color(0xFF8BC98A)
    PregnancyChance.MEDIUM -> Color(0xFFF2B705)
    PregnancyChance.HIGH -> Color(0xFFF28705)
    PregnancyChance.PEAK -> Color(0xFFE5484D)
}
