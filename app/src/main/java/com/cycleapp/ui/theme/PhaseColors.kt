package com.cycleapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.PregnancyChance

/**
 * Цвета для отображения фазы цикла прямо на ячейках календаря — отдельная палитра
 * от цветов энергии, чтобы не путаться: менструация, фолликулярная фаза, окно фертильности,
 * день овуляции, лютеиновая фаза. Прогнозируемые (ещё не наступившие / не отмеченные) дни
 * менструации показываются более светлым оттенком, чем фактически отмеченные.
 */
object PhaseColors {
    val menstruationActual = Color(0xFFD1477E)
    val menstruationPredicted = Color(0xFFF0AFC7)
    val menstruationTint = Color(0xFFF8DCE6)

    val follicular = Color(0xFF4FAE7C)
    val follicularTint = Color(0xFFDCF0E4)

    val ovulation = Color(0xFF2FA9C3)
    val ovulationTint = Color(0xFFCDEEF4)

    val fertile = Color(0xFF6FC7D6)
    val fertileTint = Color(0xFFDCF3F6)

    val luteal = Color(0xFF9B7FC7)
    val lutealTint = Color(0xFFEBE1F7)

    /** Насыщенный цвет для рамки ячейки — виден всегда, даже поверх отмеченной энергии */
    fun borderColor(info: CycleInfo, isPeriod: Boolean): Color? = when {
        info.cycleDay == null -> null
        isPeriod -> menstruationActual
        info.phase == CyclePhase.MENSTRUATION -> menstruationPredicted
        info.pregnancyChance == PregnancyChance.PEAK -> ovulation
        info.pregnancyChance == PregnancyChance.HIGH || info.pregnancyChance == PregnancyChance.MEDIUM -> fertile
        info.phase == CyclePhase.FOLLICULAR -> follicular
        info.phase == CyclePhase.LUTEAL -> luteal
        else -> null
    }

    /** Мягкая заливка ячейки — используется, когда энергия за день не отмечена */
    fun fillTint(info: CycleInfo, isPeriod: Boolean): Color? = when {
        info.cycleDay == null -> null
        isPeriod -> menstruationTint
        info.phase == CyclePhase.MENSTRUATION -> menstruationTint.copy(alpha = 0.6f)
        info.pregnancyChance == PregnancyChance.PEAK -> ovulationTint
        info.pregnancyChance == PregnancyChance.HIGH || info.pregnancyChance == PregnancyChance.MEDIUM -> fertileTint
        info.phase == CyclePhase.FOLLICULAR -> follicularTint
        info.phase == CyclePhase.LUTEAL -> lutealTint
        else -> null
    }
}
