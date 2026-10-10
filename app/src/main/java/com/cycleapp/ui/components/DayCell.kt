package com.cycleapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.theme.PhaseColors
import com.cycleapp.ui.theme.cycleColors

/** Как нарисован круг дня. Одни и те же стили используются в календаре и в легенде. */
enum class DayStyle { NONE, PERIOD, PERIOD_PREDICTED, OVULATION, FERTILE }

/**
 * Рисует круг в стиле [style]. [content] — число внутри.
 * Сплошной круг = факт/овуляция, пунктирное кольцо = прогноз, зелёное кольцо = фертильные дни.
 */
@Composable
fun DayCircle(
    style: DayStyle,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = cycleColors()
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outer = size.minDimension / 2f
            // если сегодня — отступаем внутрь, чтобы снаружи осталось место под кольцо-выделение
            val inner = if (isToday) outer - 5.dp.toPx() else outer - 1.dp.toPx()
            val center = this.center

            when (style) {
                DayStyle.PERIOD -> drawCircle(colors.period, radius = inner, center = center)
                DayStyle.OVULATION -> drawCircle(colors.ovulation, radius = inner, center = center)
                DayStyle.PERIOD_PREDICTED -> {
                    drawCircle(colors.periodRing.copy(alpha = 0.12f), radius = inner, center = center)
                    drawCircle(
                        color = colors.periodRing,
                        radius = inner - 1.dp.toPx(),
                        center = center,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                        )
                    )
                }
                DayStyle.FERTILE -> {
                    drawCircle(colors.fertileFill, radius = inner, center = center)
                    drawCircle(
                        color = colors.fertile,
                        radius = inner - 1.dp.toPx(),
                        center = center,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
                DayStyle.NONE -> Unit
            }

            if (isToday) {
                drawCircle(
                    color = accent,
                    radius = outer - 1.5.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.2.dp.toPx())
                )
            }
        }
        content()
    }
}

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
    val colors = cycleColors()

    val style = when {
        isOtherMonth -> DayStyle.NONE
        isPeriod -> DayStyle.PERIOD
        cycleInfo.phase == CyclePhase.MENSTRUATION -> DayStyle.PERIOD_PREDICTED
        PhaseColors.isOvulationDay(cycleInfo) -> DayStyle.OVULATION
        PhaseColors.isFertileWindow(cycleInfo) -> DayStyle.FERTILE
        else -> DayStyle.NONE
    }

    val numberColor = when (style) {
        DayStyle.PERIOD, DayStyle.OVULATION -> colors.onSolid
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DayCircle(style = style, isToday = isToday, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = dayNumber.toString(),
                color = numberColor.copy(alpha = if (isOtherMonth) 0.3f else 1f),
                fontSize = 14.sp,
                fontWeight = if (isToday || style == DayStyle.PERIOD || style == DayStyle.OVULATION) FontWeight.Bold else FontWeight.Medium
            )
        }

        // Строка значков под кругом — фиксированной высоты, чтобы сетка не «прыгала»
        Row(
            modifier = Modifier.height(19.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isOtherMonth) {
                if (energy != null) EnergyMarker(level = energy, size = 18.dp)
                if (hasExtra) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

/** Небольшие образцы для легенды — те же стили, что и в календаре */
@Composable
fun LegendCircle(style: DayStyle, size: Dp = 22.dp) {
    DayCircle(style = style, isToday = false, modifier = Modifier.size(size)) {}
}

