package com.cycleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.CycleSettings
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.data.Mood
import com.cycleapp.data.Symptom
import com.cycleapp.util.CycleCalculator

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DayDetailSheet(
    dateIso: String,
    initialEntry: DayEntry?,
    initialIsPeriod: Boolean,
    periods: List<String>,
    settings: CycleSettings,
    onDismiss: () -> Unit,
    onSave: (energy: EnergyLevel?, note: String, isPeriod: Boolean, mood: Mood?, symptoms: Set<Symptom>) -> Unit,
    onClear: () -> Unit
) {
    var selectedEnergy by remember { mutableStateOf(initialEntry?.energy) }
    var isPeriod by remember { mutableStateOf(initialIsPeriod) }
    var note by remember { mutableStateOf(initialEntry?.note ?: "") }
    var selectedMood by remember { mutableStateOf(initialEntry?.mood) }
    var selectedSymptoms by remember { mutableStateOf(initialEntry?.symptoms ?: emptySet()) }

    val date = CycleCalculator.parse(dateIso)
    val info = CycleCalculator.getCycleInfo(dateIso, periods, settings.cycleLength, settings.periodLength)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()) // <-- ДОБАВИТЬ ЭТУ СТРОКУ
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp)
        ) {
            // Заголовок + крестик закрытия — не нужно свайпать дважды
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = CycleCalculator.formatDMY(date),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (info.cycleDay != null) "${info.phase.label} · день ${info.cycleDay}" else info.phase.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

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

            // Блок менструации — центрированный текст, единый отступ 12dp со всех сторон
            Spacer(Modifier.height(18.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Менструация в этот день",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(checked = isPeriod, onCheckedChange = { isPeriod = it })
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Отметьте каждый день менструации — начало и конец определятся автоматически.",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Настроение — карточки одинакового размера с равными отступами по горизонтали и вертикали
            Spacer(Modifier.height(18.dp))
            Text(text = "Настроение", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mood.entries.forEach { mood ->
                    val selected = selectedMood == mood
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { selectedMood = if (selected) null else mood }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(mood.emoji, fontSize = 14.sp)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            mood.label,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Симптомы — та же сетка с равными отступами
            Spacer(Modifier.height(18.dp))
            Text(text = "Симптомы", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Symptom.entries.forEach { symptom ->
                    val selected = selectedSymptoms.contains(symptom)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable {
                                selectedSymptoms = if (selected) selectedSymptoms - symptom else selectedSymptoms + symptom
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Text(
                            symptom.label,
                            fontSize = 12.sp,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка (необязательно)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Кнопка сохранения — крупная, во всю ширину, с иконкой, чтобы было очевидно, что она нажимаема.
            // "Очистить" — второстепенное действие меньшего размера рядом с ней.
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(selectedEnergy, note.trim(), isPeriod, selectedMood, selectedSymptoms)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Сохранить", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onClear(); onDismiss() },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Очистить день", fontSize = 13.sp)
            }
        }
    }
}
