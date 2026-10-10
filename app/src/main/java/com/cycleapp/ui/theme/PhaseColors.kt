package com.cycleapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.PregnancyChance

/**
 * Цвета календаря. Правила, чтобы значки читались на любом фоне:
 *  - ФАКТ менструации — сплошной насыщенный круг с белой цифрой;
 *  - ПРОГНОЗ менструации — пунктирное кольцо того же цвета;
 *  - ОВУЛЯЦИЯ — сплошной бирюзовый круг;
 *  - ФЕРТИЛЬНЫЕ дни — зелёное кольцо с мягкой заливкой;
 *  - ЭНЕРГИЯ — отдельный круглый маркер с цифрой 1–5 ПОД числом (не заливает ячейку),
 *    всегда с тёмной обводкой — даже «бежевый» (норма) виден на светлом фоне.
 */
@Immutable
data class CycleColors(
    val period: Color,
    val periodRing: Color,
    val ovulation: Color,
    val fertile: Color,
    val fertileFill: Color,
    val markerOutline: Color,
    val onSolid: Color
)

@Composable
fun cycleColors(): CycleColors {
    val darkBackground = MaterialTheme.colorScheme.background.luminance() < 0.35f
    return if (darkBackground) {
        CycleColors(
            period = Color(0xFFE0457F),
            periodRing = Color(0xFFFF8FB8),
            ovulation = Color(0xFF22B2CF),
            fertile = Color(0xFF6BD08F),
            fertileFill = Color(0x336BD08F),
            markerOutline = Color(0xCCFFFFFF),
            onSolid = Color.White
        )
    } else {
        CycleColors(
            period = Color(0xFFD6336C),
            periodRing = Color(0xFFD6336C),
            ovulation = Color(0xFF138CA6),
            fertile = Color(0xFF2F9E5E),
            fertileFill = Color(0xFFCDEFDB),
            markerOutline = Color(0xFF241426).copy(alpha = 0.55f),
            onSolid = Color.White
        )
    }
}

object PhaseColors {
    fun isOvulationDay(info: CycleInfo): Boolean =
        info.cycleDay != null && info.cycleDay == info.ovulationDay

    /** Фертильное окно, но не сам день овуляции (у него отдельный маркер) */
    fun isFertileWindow(info: CycleInfo): Boolean =
        !isOvulationDay(info) &&
            (info.pregnancyChance == PregnancyChance.HIGH || info.pregnancyChance == PregnancyChance.MEDIUM)

    fun chanceColor(chance: PregnancyChance): Color = when (chance) {
        PregnancyChance.UNKNOWN -> Color(0xFF9A8FA0)
        PregnancyChance.VERY_LOW -> Color(0xFF2F9E5E)
        PregnancyChance.LOW -> Color(0xFF6BAA55)
        PregnancyChance.MEDIUM -> Color(0xFFD99100)
        PregnancyChance.HIGH -> Color(0xFFE06A00)
        PregnancyChance.PEAK -> Color(0xFFD6336C)
    }
}
