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
    onClick: () -> Unit
) {
    val bg = energy?.color ?: MaterialTheme.colorScheme.surface
    val textColor = when {
        energy == null -> MaterialTheme.colorScheme.onSurface
        energy == EnergyLevel.WHITE -> Color(0xFF333333)
        else -> Color(0xFF1B1420)
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .then(
                if (isToday) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
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
    }
}
