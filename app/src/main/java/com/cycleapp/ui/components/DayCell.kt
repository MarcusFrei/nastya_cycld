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
import com.cycleapp.data.CyclePhase
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
    val periodFill = PhaseColors.fillTint(cycleInfo, isPeriod)
    val fill = energy?.color ?: periodFill
    val isOvulation = PhaseColors.isOvulationDay(cycleInfo)
    val isFertile = PhaseColors.isFertileWindow(cycleInfo)
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(shape)
            .then(if (fill != null) Modifier.background(fill) else Modifier)
            .then(
                if (isToday) Modifier.border(1.7.dp, MaterialTheme.colorScheme.primary, shape)
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            color = when {
                isOtherMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                energy != null -> Color(0xFF302333)
                isPeriod -> Color(0xFF873653)
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = 12.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )

        if (isOvulation || isFertile) {
            Text(
                text = if (isOvulation) "✦" else "·",
                fontSize = if (isOvulation) 10.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOvulation) PhaseColors.ovulationMarker else PhaseColors.fertileMarker,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 3.dp, top = 0.dp)
            )
        }

        if (isPeriod || cycleInfo.phase == CyclePhase.MENSTRUATION) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .then(
                        if (isPeriod) Modifier.background(PhaseColors.menstruationActual)
                        else Modifier.border(1.dp, PhaseColors.menstruationPredicted, CircleShape)
                    )
            )
        }

        if (hasExtra) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (energy != null) Color(0xFF302333).copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
            )
        }
    }
}
