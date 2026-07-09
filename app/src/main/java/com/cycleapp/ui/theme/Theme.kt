package com.cycleapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.cycleapp.data.WallpaperPreset

/**
 * Строит полную цветовую схему на основе выбранного пресета фона и акцентного цвета.
 * Благодаря этому карточки, кнопки и фон согласованно меняются вместе с пресетом —
 * а не остаются фиолетовыми при выборе другого фона.
 * Текст всегда высокого контраста: светлый на тёмном фоне, тёмный на светлом.
 */
fun buildColorScheme(preset: WallpaperPreset, accent: Color, dark: Boolean): ColorScheme {
    val baseDark = preset.colors[0]
    val surfaceDark = preset.colors[1]

    return if (dark) {
        darkColorScheme(
            primary = accent,
            onPrimary = Color(0xFF241426),
            secondary = lerp(accent, Color.White, 0.25f),
            background = baseDark,
            onBackground = Color(0xFFF7F2F7),
            surface = surfaceDark,
            onSurface = Color(0xFFF7F2F7),
            surfaceVariant = lerp(surfaceDark, Color.White, 0.14f),
            onSurfaceVariant = Color(0xFFDCCEE3),
            outline = lerp(surfaceDark, Color.White, 0.3f)
        )
    } else {
        val bgLight = lerp(baseDark, Color.White, 0.94f)
        val surfVariantLight = lerp(surfaceDark, Color.White, 0.82f)
        lightColorScheme(
            primary = lerp(accent, Color.Black, 0.08f),
            onPrimary = Color.White,
            secondary = lerp(accent, Color.Black, 0.2f),
            background = bgLight,
            onBackground = Color(0xFF2A1F30),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2A1F30),
            surfaceVariant = surfVariantLight,
            onSurfaceVariant = Color(0xFF6B5670),
            outline = Color(0xFFCBBFD1)
        )
    }
}

@Composable
fun CycleAppTheme(preset: WallpaperPreset, accent: Color, darkTheme: Boolean, content: @Composable () -> Unit) {
    val scheme = buildColorScheme(preset, accent, darkTheme)
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
