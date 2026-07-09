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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.util.CycleCalculator
import java.time.format.TextStyle
import java.util.Locale

private val RU_MONTHS_GEN = listOf(
    "января","февраля","марта","апреля","мая","июня",
    "июля","августа","сентября","октября","ноября","декабря"
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DayDetailSheet(
    dateIso: String,
    initialEntry: DayEntry?,
    initialIsPeriod: Boolean,
    periods: List<String>,
    settings: CycleSettings,
    onDismiss: () -> Unit,
    onSave: (energy: EnergyLevel?, note: String, isPeriod: Boolean) -> Unit,
    onClear: () -> Unit
) {
    var selectedEnergy by remember { mutableStateOf(initialEntry?.energy) }
    var isPeriod by remember { mutableStateOf(initialIsPeriod) }
    var note by remember { mutableStateOf(initialEntry?.note ?: "") }

    val date = CycleCalculator.parse(dateIso)
    val info = CycleCalculator.getCycleInfo(dateIso, periods, settings.cycleLength, settings.periodLength)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                text = "${date.dayOfMonth} ${RU_MONTHS_GEN[date.monthValue - 1]} ${date.year}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (info.cycleDay != null) "${info.phase.label} · день ${info.cycleDay}" else info.phase.label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))
            Text(text = "Уровень энергии", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                EnergyLevel.entries.forEach { level ->
                    val selected = selectedEnergy == level
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(level.color)
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedEnergy = if (selectedEnergy == level) null else level
                            }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Менструация в этот день")
                    Switch(checked = isPeriod, onCheckedChange = { isPeriod = it })
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Отметьте каждый день менструации — начало и конец определятся автоматически.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { onClear(); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text("Очистить") }
                Button(
                    onClick = { onSave(selectedEnergy, note.trim(), isPeriod); onDismiss() },
                    modifier = Modifier.weight(1f)
                ) { Text("Сохранить") }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
