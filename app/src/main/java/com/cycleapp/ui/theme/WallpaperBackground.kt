package com.cycleapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.cycleapp.data.WALLPAPER_PRESETS

@Composable
fun WallpaperBackgroundModifier(wallpaperId: Int, darkTheme: Boolean): Modifier {
    if (!darkTheme) {
        // В светлой теме фон всегда мягкий и нейтральный — пресеты влияют только на тёмную тему
        return Modifier.background(MaterialTheme.colorScheme.background)
    }
    val preset = WALLPAPER_PRESETS.find { it.id == wallpaperId } ?: WALLPAPER_PRESETS[0]
    return Modifier.background(
        Brush.verticalGradient(colors = preset.colors)
    )
}
