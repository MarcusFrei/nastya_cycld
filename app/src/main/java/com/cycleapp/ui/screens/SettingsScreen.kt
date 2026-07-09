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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.ThemeMode
import com.cycleapp.data.WALLPAPER_PRESETS

@Composable
fun SettingsScreen(
    settings: CycleSettings,
    onCycleLengthChange: (Int) -> Unit,
    onPeriodLengthChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWallpaperChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(text = "Настройки", fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOption("Светлая", ThemeMode.LIGHT, settings.themeMode, onThemeModeChange)
                ThemeOption("Тёмная", ThemeMode.DARK, settings.themeMode, onThemeModeChange)
                ThemeOption("Системная", ThemeMode.SYSTEM, settings.themeMode, onThemeModeChange)
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Фон (для тёмной темы)") {
            Text(
                "Применяется в тёмном режиме — выберите фоновый градиент",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                items(WALLPAPER_PRESETS) { preset ->
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val selected = preset.id == settings.wallpaperId
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(preset.colors))
                                .then(
                                    if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                    else Modifier
                                )
                                .clickable { onWallpaperChange(preset.id) }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(preset.name, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
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
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
        Text(label, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepButton("−", onMinus)
            Text(
                value.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
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
        Text(symbol, fontSize = 16.sp)
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
