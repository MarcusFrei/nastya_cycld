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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.ThemeMode
import com.cycleapp.data.WALLPAPER_PRESETS
import com.cycleapp.ui.components.AppCard
import com.cycleapp.ui.components.PillButton
import com.cycleapp.ui.theme.presetBackground
import androidx.compose.ui.graphics.luminance
import com.cycleapp.ui.components.SelectChip
import com.cycleapp.util.CycleCalculator
import androidx.compose.foundation.layout.width

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: CycleSettings,
    model: CycleCalculator.CycleModel,
    onOpenNotifications: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onWallpaperChange: (Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAllData: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.35f

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text(
            text = "Настройки",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        // Уведомления вынесены сюда из нижней навигации
        AppCard(onClick = onOpenNotifications) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Уведомления", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Напоминания о месячных и овуляции",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Вместо ручного ввода длины цикла и месячных — автоматический расчёт
        SettingsCard(title = "Как считается прогноз") {
            Text(
                "Длину цикла и месячных вводить не нужно: приложение считает их само по вашим отметкам, " +
                    "а свежие циклы влияют на прогноз сильнее старых.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                EstimateBlock("Цикл", "${model.cycleLength} дн.")
                EstimateBlock("Месячные", "${model.periodLength} дн.")
                EstimateBlock("Погрешность", "±${model.accuracyDays} дн.")
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (model.cyclesUsed == 0)
                    "Пока используются типовые значения (28 и 5 дней). Отметьте начало месячных хотя бы дважды — расчёт подстроится под вас."
                else
                    "Расчёт по последним ${model.cyclesUsed} ${if (model.cyclesUsed == 1) "циклу" else "циклам"}.",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Тема оформления") {
            Text(
                "Цвета кнопок и выделений подбираются под выбранный фон. Тёмная тема сохраняет оттенок фона.",
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

        SettingsCard(title = "Фон") {
            Text(
                "Нежные пастельные цвета. Работают и в светлой, и в тёмной теме.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WALLPAPER_PRESETS.forEach { preset ->
                    Column(
                        modifier = Modifier.width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val selected = preset.id == settings.wallpaperId
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(presetBackground(preset, isDark)))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                )
                                .clickable { onWallpaperChange(preset.id) }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            preset.name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsCard(title = "Журнал: экспорт и импорт") {
            Text(
                "Сохраните все отметки в файл — например, перед переустановкой приложения. " +
                    "Если обновление не ставится, удалите приложение, установите новую версию и загрузите файл обратно.",
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("Экспорт", onExport, Modifier.weight(1f))
                PillButton("Импорт", onImport, Modifier.weight(1f), filled = false)
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
    AppCard {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EstimateBlock(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun ThemeOption(
    label: String,
    mode: ThemeMode,
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    SelectChip(label = label, selected = mode == current, onClick = { onSelect(mode) })
}
