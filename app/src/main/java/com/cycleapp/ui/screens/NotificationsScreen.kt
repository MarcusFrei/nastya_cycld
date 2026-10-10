package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.NotificationSettings
import com.cycleapp.ui.components.AppCard

@Composable
fun NotificationsScreen(
    settings: NotificationSettings,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onChange: (NotificationSettings) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Уведомления",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(12.dp))

        if (!permissionGranted) {
            NotifCard {
                Text(
                    "Разрешение на уведомления не выдано",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Без него напоминания не будут показываться, даже если включены ниже.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onRequestPermission)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Разрешить уведомления", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        NotifCard {
            Text("Напоминания", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))

            ToggleRow(
                title = "За неделю до месячных",
                subtitle = "Однократное напоминание за 7 дней",
                checked = settings.weekBeforePeriod,
                onCheckedChange = { onChange(settings.copy(weekBeforePeriod = it)) }
            )
            ToggleRow(
                title = "За день до месячных",
                subtitle = "Однократное напоминание за 1 день",
                checked = settings.dayBeforePeriod,
                onCheckedChange = { onChange(settings.copy(dayBeforePeriod = it)) }
            )
            ToggleRow(
                title = "За неделю до овуляции",
                subtitle = "Однократное напоминание за 7 дней до овуляции",
                checked = settings.weekBeforeOvulation,
                onCheckedChange = { onChange(settings.copy(weekBeforeOvulation = it)) }
            )
            ToggleRow(
                title = "Каждый день менструации",
                subtitle = "Напоминание в каждый из дней менструации",
                checked = settings.everyPeriodDay,
                onCheckedChange = { onChange(settings.copy(everyPeriodDay = it)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        NotifCard {
            Text("Время напоминаний", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Час напоминания", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HourStepButton("−") {
                        onChange(settings.copy(reminderHour = (settings.reminderHour - 1 + 24) % 24))
                    }
                    Text(
                        String.format("%02d:00", settings.reminderHour),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HourStepButton("+") {
                        onChange(settings.copy(reminderHour = (settings.reminderHour + 1) % 24))
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Уведомления рассчитываются на основе прогноза цикла и обновляются автоматически при изменении данных.",
            fontSize = 11.sp,
            lineHeight = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotifCard(content: @Composable () -> Unit) {
    AppCard { content() }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HourStepButton(symbol: String, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
    }
}
