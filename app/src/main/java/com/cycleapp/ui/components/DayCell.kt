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
import com.cycleapp.data.EnergyLevel

@Composable
fun DayCell(
    dayNumber: Int,
    isToday: Boolean,
    isOtherMonth: Boolean,
    energy: EnergyLevel?,
    isPeriod: Boolean,
    isOvulation: Boolean,
    hasExtra: Boolean,
    onClick: () -> Unit
) {
    val bg = energy?.color ?: MaterialTheme.colorScheme.surface
    // Энергетические цвета достаточно светлые/насыщенные — тёмный текст поверх них читается лучше,
    // а для обычной ячейки (без отметки) используем цвет темы, который уже гарантированно контрастен.
    val textColor = if (energy != null) Color(0xFF241426) else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
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
        if (isPeriod) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5484D))
            )
        } else if (isOvulation) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4D8CF2))
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
