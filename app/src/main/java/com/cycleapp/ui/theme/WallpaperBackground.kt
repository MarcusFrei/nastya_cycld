package com.cycleapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp

/**
 * Мягкий вертикальный градиент фона на основе текущей цветовой схемы (которая уже учитывает
 * выбранный пресет и тему). Работает одинаково для светлой и тёмной темы.
 */
@Composable
fun appBackgroundModifier(): Modifier {
    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    return Modifier.background(
        Brush.verticalGradient(listOf(bg, lerp(bg, surface, 0.55f)))
    )
}
