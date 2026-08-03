package com.cycleapp.ui.theme

import androidx.compose.ui.graphics.Color
import com.cycleapp.data.CycleInfo
import com.cycleapp.data.CyclePhase
import com.cycleapp.data.PregnancyChance

/**
 * Минимальная цветовая индикация на календаре — только для "критических" дней:
 * менструация (факт/прогноз). Фолликулярная и лютеиновая фазы больше НЕ подсвечиваются
 * заливкой/рамкой — это перегружало календарь и ухудшало читаемость цифр на тёмном фоне.
 * Овуляция и окно фертильности показываются маленькими значками (звезда / росток),
 * а не цветом всей ячейки — так же, как это сделано в референсе, который прислал пользователь.
 */
object PhaseColors {
    val menstruationActual = Color(0xFFD1477E)
    val menstruationPredicted = Color(0xFFF0AFC7)
    val menstruationTint = Color(0xFFF8DCE6)
    val menstruationTintLight = Color(0xFFFBEAF0)

    val ovulationMarker = Color(0xFF2FA9C3)
    val fertileMarker = Color(0xFF5FBF7C)

    /** Мягкая заливка ячейки — только для менструации (факт или прогноз), иначе null */
    fun fillTint(info: CycleInfo, isPeriod: Boolean): Color? = when {
        isPeriod -> menstruationTint
        info.phase == CyclePhase.MENSTRUATION -> menstruationTintLight
        else -> null
    }

    fun isOvulationDay(info: CycleInfo): Boolean =
        info.cycleDay != null && info.cycleDay == info.ovulationDay

    /** Фертильное окно, но не сам день овуляции (у него отдельный маркер) */
    fun isFertileWindow(info: CycleInfo): Boolean =
        !isOvulationDay(info) && (info.pregnancyChance == PregnancyChance.HIGH || info.pregnancyChance == PregnancyChance.MEDIUM)
}
