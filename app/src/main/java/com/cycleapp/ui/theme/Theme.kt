package com.cycleapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.cycleapp.data.WallpaperPreset

/**
 * Строит цветовую схему на основе выбранного пресета фона и акцентного цвета.
 * Фон/поверхности ВСЕГДА берутся из пресета и не зависят от темы — по требованию:
 * тема (светлая/тёмная) влияет ТОЛЬКО на цвет текста и иконок, чтобы можно было
 * свободно выбирать любой фон независимо от текущей темы.
 */
fun buildColorScheme(preset: WallpaperPreset, accent: Color, useLightText: Boolean): ColorScheme {
    val background = preset.colors[0]
    val surface = preset.colors[1]

    val onColor = if (useLightText) Color(0xFFF7F2F7) else Color(0xFF241726)
    val onVariant = if (useLightText) Color(0xFFDCCEE3) else Color(0xFF4A3B52)
    val outline = if (useLightText) lerp(surface, Color.White, 0.35f) else lerp(surface, Color.Black, 0.25f)
    val surfaceVariant = if (useLightText) lerp(surface, Color.White, 0.12f) else lerp(surface, Color.Black, 0.06f)

    // darkColorScheme используется как база в обоих случаях — он не задаёт автоматических
    // тёмных цветов сам по себе, все значения ниже переопределены явно.
    return darkColorScheme(
        primary = accent,
        onPrimary = if (useLightText) Color(0xFF241426) else Color.White,
        secondary = lerp(accent, if (useLightText) Color.White else Color.Black, 0.2f),
        background = background,
        onBackground = onColor,
        surface = surface,
        onSurface = onColor,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onVariant,
        outline = outline
    )
}

@Composable
fun CycleAppTheme(preset: WallpaperPreset, accent: Color, useLightText: Boolean, content: @Composable () -> Unit) {
    val scheme = buildColorScheme(preset, accent, useLightText)
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
