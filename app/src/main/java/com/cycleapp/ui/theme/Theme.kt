package com.cycleapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AccentPurple = Color(0xFFC9A0DC)

private val DarkColors = darkColorScheme(
    primary = AccentPurple,
    secondary = Color(0xFFA996AD),
    background = Color(0xFF1B1420),
    surface = Color(0xFF2C2036),
    onBackground = Color(0xFFF2E9F5),
    onSurface = Color(0xFFF2E9F5),
    surfaceVariant = Color(0xFF3D2F47),
    onSurfaceVariant = Color(0xFFA996AD)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF8B5FA8),
    secondary = Color(0xFF6B5670),
    background = Color(0xFFFAF6FB),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF2C2036),
    onSurface = Color(0xFF2C2036),
    surfaceVariant = Color(0xFFE9DEEF),
    onSurfaceVariant = Color(0xFF6B5670)
)

@Composable
fun CycleAppTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
