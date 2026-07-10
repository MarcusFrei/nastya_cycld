package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.ACCENT_OPTIONS
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.ThemeMode
import com.cycleapp.data.WALLPAPER_PRESETS

@Composable
fun SettingsScreen(
    settings: CycleSettings,
    onCycleLengthChange: (Int) -> Unit,
    onPeriodLengthChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWallpaperChange: (Int) -> Unit,
    onAccentChange: (Int) -> Unit,
    onClearAllData: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = "Настройки",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Длина цикла") {
            Stepper(
                label = "Длина цикла (дней)",
                value = settings.cycleLength,
                onMinus = { onCycleLengthChange((settings.cycleLength - 1).coerceAtLeast(15)) },
                onPlus = { onCycleLengthChange((settings.cycleLength + 1).coerceAtMost(60)) }
            )
            Spacer(Modifier.height(10.dp))
            Stepper(
                label = "Длина менструации (дней)",
                value = settings.periodLength,
                onMinus = { onPeriodLengthChange((settings.periodLength - 1).coerceAtLeast(1)) },
                onPlus = { onPeriodLengthChange((settings.periodLength + 1).coerceAtMost(14)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Тема оформления") {
            Text(
                "Влияет только на цвет текста — фон определяется выбранным пресетом ниже",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOption("Светлая", ThemeMode.LIGHT, settings.themeMode, onThemeModeChange)
                ThemeOption("Тёмная", ThemeMode.DARK, settings.themeMode, onThemeModeChange)
                ThemeOption("Системная", ThemeMode.SYSTEM, settings.themeMode, onThemeModeChange)
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Акцентный цвет") {
            Text(
                "Используется для кнопок, текущего дня и выделений",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ACCENT_OPTIONS.forEach { accent ->
                    val selected = accent.id == settings.accentId
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accent.color)
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                            )
                            .clickable { onAccentChange(accent.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Фон") {
            Text(
                "Не зависит от темы — можно выбрать любой фон при любой теме",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth().height(220.dp)
            ) {
                items(WALLPAPER_PRESETS) { preset ->
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val selected = preset.id == settings.wallpaperId
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(preset.colors))
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                )
                                .clickable { onWallpaperChange(preset.id) }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            preset.name,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Отладка") {
            Text(
                "Полностью удаляет все отметки, настройки и историю. Действие необратимо.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE5484D))
                    .clickable { showClearConfirm = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Очистить все данные", fontSize = 12.sp, color = Color.White)
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Очистить все данные?") },
            text = { Text("Это удалит весь календарь, отметки энергии, настроения, симптомы и настройки без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    onClearAllData()
                }) { Text("Очистить", color = Color(0xFFE5484D)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun Stepper(label: String, value: Int, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton("−", onMinus)
            Text(
                value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            StepButton("+", onPlus)
        }
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    val selected = mode == current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { onSelect(mode) }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
