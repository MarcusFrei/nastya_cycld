package com.cycleapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.cycleapp.data.WallpaperPreset

private val DARK_BASE = Color(0xFF131016)

/** Цвета фона-градиента для превью и для самого экрана с учётом темы */
fun presetBackground(preset: WallpaperPreset, dark: Boolean): List<Color> =
    if (!dark) preset.colors
    else listOf(lerp(DARK_BASE, preset.accent, 0.08f), lerp(DARK_BASE, preset.accent, 0.17f))

/**
 * Единая палитра строится ТОЛЬКО из выбранного фона:
 *  - светлая тема — сам пастельный фон, светлые матовые карточки, акцент фона;
 *  - тёмная тема — тот же оттенок, но глубокий и приглушённый, акцент светлее.
 * Отдельного выбора акцентного цвета нет.
 */
fun buildColorScheme(preset: WallpaperPreset, dark: Boolean): ColorScheme {
    val accent: Color
    val background: Color
    val surface: Color
    val surfaceVariant: Color
    val outline: Color
    val onColor: Color
    val onVariant: Color

    if (!dark) {
        val baseSurface = preset.colors[1]
        accent = preset.accent
        background = preset.colors[0]
        surface = lerp(baseSurface, Color.White, 0.65f)
        surfaceVariant = lerp(baseSurface, Color.White, 0.2f)
        outline = lerp(baseSurface, Color.Black, 0.25f)
        onColor = Color(0xFF241726)
        onVariant = Color(0xFF4A3B52)
    } else {
        accent = lerp(preset.accent, Color.White, 0.3f)
        background = lerp(DARK_BASE, preset.accent, 0.08f)
        surface = lerp(DARK_BASE, preset.accent, 0.17f)
        surfaceVariant = lerp(surface, Color.White, 0.1f)
        outline = lerp(surface, Color.White, 0.3f)
        onColor = Color(0xFFF7F2F7)
        onVariant = Color(0xFFDCCEE3)
    }

    // Текст на кнопках: тёмный или белый — какой контрастнее на цвете акцента
    val onAccent = if (accent.luminance() > 0.22f) Color(0xFF241426) else Color.White

    return darkColorScheme(
        primary = accent,
        onPrimary = onAccent,
        secondary = lerp(accent, if (dark) Color.White else Color.Black, 0.2f),
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
fun CycleAppTheme(preset: WallpaperPreset, dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = buildColorScheme(preset, dark),
        typography = MaterialTheme.typography,
        content = content
    )
}
