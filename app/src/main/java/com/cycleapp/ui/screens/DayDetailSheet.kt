package com.cycleapp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.cycleapp.data.DayEntry
import com.cycleapp.data.EnergyLevel
import com.cycleapp.data.FlowLevel
import com.cycleapp.data.Mood
import com.cycleapp.data.Symptom
import com.cycleapp.ui.components.EnergyMarker
import com.cycleapp.ui.components.SelectChip
import com.cycleapp.util.CycleCalculator
import kotlinx.coroutines.delay

/**
 * Карточка дня. Кнопки «Сохранить» нет — любое изменение сохраняется сразу:
 * энергия, настроение, симптомы и месячные — в момент нажатия, заметка — через полсекунды после ввода
 * и при закрытии. Энергия вынесена наверх, чтобы не нужно было листать.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun DayDetailSheet(
    dateIso: String,
    initialEntry: DayEntry?,
    initialIsPeriod: Boolean,
    model: CycleCalculator.CycleModel,
    onDismiss: () -> Unit,
    onChange: (energy: EnergyLevel?, note: String, isPeriod: Boolean, mood: Mood?, symptoms: Set<Symptom>, flow: FlowLevel?) -> Unit,
    onClear: () -> Unit
) {
    var energy by remember { mutableStateOf(initialEntry?.energy) }
    var isPeriod by remember { mutableStateOf(initialIsPeriod) }
    var note by remember { mutableStateOf(initialEntry?.note ?: "") }
    var mood by remember { mutableStateOf(initialEntry?.mood) }
    var symptoms by remember { mutableStateOf(initialEntry?.symptoms ?: emptySet()) }
    var flow by remember { mutableStateOf(initialEntry?.flow) }

    val date = CycleCalculator.parse(dateIso)
    val info = CycleCalculator.getCycleInfo(dateIso, model)

    fun push() {
        onChange(energy, note, isPeriod, mood, symptoms, if (isPeriod) flow else null)
    }

    // Заметку сохраняем с небольшой задержкой, чтобы не писать на диск при каждой букве
    val originalNote = initialEntry?.note ?: ""
    LaunchedEffect(note) {
        if (note != originalNote) {
            delay(600)
            push()
        }
    }

    fun close() {
        if (note != originalNote) push() // дописанная заметка не потеряется
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { close() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 0.dp, bottom = 16.dp)
        ) {
            // ---------- Заголовок: дата, фаза, очистка, закрыть ----------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = CycleCalculator.formatLong(date).replaceFirstChar { it.uppercase() },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (info.cycleDay != null) "${info.phase.label} · день ${info.cycleDay}" else info.phase.label,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        energy = null; isPeriod = false; note = ""; mood = null; symptoms = emptySet(); flow = null
                        onClear()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Filled.DeleteOutline,
                        contentDescription = "Очистить день",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { close() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ---------- Энергия (самое частое действие — сверху) ----------
            Spacer(Modifier.height(14.dp))
            Text("Энергия", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                EnergyLevel.entries.forEach { level ->
                    val selected = energy == level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                energy = if (selected) null else level
                                push()
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        EnergyMarker(level = level, size = 44.dp, selected = selected)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = level.shortLabel,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            // ---------- Месячные ----------
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Месячные в этот день",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isPeriod,
                        onCheckedChange = {
                            isPeriod = it
                            push()
                        }
                    )
                }
                if (isPeriod) {
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FlowLevel.entries.forEach { level ->
                            SelectChip(
                                label = level.label,
                                selected = flow == level,
                                onClick = {
                                    flow = if (flow == level) null else level
                                    push()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            // ---------- Настроение ----------
            Spacer(Modifier.height(14.dp))
            Text("Настроение", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Mood.entries.forEach { m ->
                    SelectChip(
                        label = m.label,
                        leading = m.emoji,
                        selected = mood == m,
                        onClick = {
                            mood = if (mood == m) null else m
                            push()
                        }
                    )
                }
            }

            // ---------- Симптомы ----------
            Spacer(Modifier.height(14.dp))
            Text("Симптомы", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Symptom.entries.forEach { symptom ->
                    val selected = symptoms.contains(symptom)
                    SelectChip(
                        label = symptom.label,
                        selected = selected,
                        onClick = {
                            symptoms = if (selected) symptoms - symptom else symptoms + symptom
                            push()
                        }
                    )
                }
            }

            // ---------- Заметка ----------
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Заметка") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Всё сохраняется автоматически",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
