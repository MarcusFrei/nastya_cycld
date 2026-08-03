package com.cycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.theme.PhaseColors

@Composable
fun DayCell(
    dayNumber: Int,
    isToday: Boolean,
    isOtherMonth: Boolean,
    energy: EnergyLevel?,
    isPeriod: Boolean,
    cycleInfo: CycleInfo,
    hasExtra: Boolean,
    onClick: () -> Unit
) {
    // Заливка: отмеченная энергия — главный приоритет (это данные пользователя).
    // Если энергии нет — лёгкая подсветка только для менструации (факт/прогноз).
    // Фолликулярная и лютеиновая фазы больше не подсвечиваются заливкой — только текст сверху.
    val periodFill = PhaseColors.fillTint(cycleInfo, isPeriod)
    val bg = energy?.color ?: periodFill ?: MaterialTheme.colorScheme.surface
    val textColor = if (energy != null) Color(0xFF241426) else MaterialTheme.colorScheme.onSurface

    val isOvulation = PhaseColors.isOvulationDay(cycleInfo)
    val isFertile = PhaseColors.isFertileWindow(cycleInfo)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (isToday) 2.2.dp else 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            color = textColor.copy(alpha = if (isOtherMonth) 0.35f else 1f),
            fontSize = 13.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )

        // Маленький значок вместо заливки всей ячейки — овуляция или фертильное окно
        if (isOvulation) {
            Text(
                text = "★",
                fontSize = 9.sp,
                color = PhaseColors.ovulationMarker,
                modifier = Modifier.align(Alignment.TopStart).padding(3.dp)
            )
        } else if (isFertile) {
            Text(
                text = "🌱",
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.TopStart).padding(2.dp)
            )
        }

        // Точка снизу — факт менструации (насыщенная) или прогноз (светлая)
        if (isPeriod) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(PhaseColors.menstruationActual)
            )
        } else if (cycleInfo.phase == com.cycleapp.data.CyclePhase.MENSTRUATION) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .border(1.dp, PhaseColors.menstruationPredicted, CircleShape)
            )
        }

        if (hasExtra) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.45f))
            )
        }
    }
}
