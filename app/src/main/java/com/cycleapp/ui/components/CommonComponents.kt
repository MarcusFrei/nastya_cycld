package com.cycleapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cycleapp.data.EnergyLevel
import com.cycleapp.ui.theme.cycleColors

/** Базовая карточка — крупные скругления и мягкая рамка, как на макетах */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}

/**
 * Маркер энергии: круг цвета уровня с ЦИФРОЙ 1–5 и тёмной обводкой.
 * Цифра дублирует цвет (читается и при плохом различении цветов), обводка делает видимым
 * светлые оттенки на светлом фоне.
 */
@Composable
fun EnergyMarker(
    level: EnergyLevel,
    size: Dp,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val colors = cycleColors()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(level.color)
            .border(
                width = if (selected) 3.dp else 1.2.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else colors.markerOutline,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = (size.value * 0.55f).coerceAtLeast(10f)
        Text(
            text = EnergyLevel.scoreOf(level).toString(),
            // lineHeight = fontSize и обрезка полей строки — иначе цифра уезжает вниз в маленьком круге
            style = TextStyle(
                color = Color(0xFF241426),
                fontWeight = FontWeight.Bold,
                fontSize = fontSize.sp,
                lineHeight = fontSize.sp,
                textAlign = TextAlign.Center,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both
                )
            )
        )
    }
}

/** Капля, нарисованная вручную — не зависит от набора иконок */
@Composable
fun DropIcon(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.8f, h * 0.3f, w * 0.95f, h * 0.5f, w * 0.95f, h * 0.65f)
            cubicTo(w * 0.95f, h * 0.85f, w * 0.75f, h, w * 0.5f, h)
            cubicTo(w * 0.25f, h, w * 0.05f, h * 0.85f, w * 0.05f, h * 0.65f)
            cubicTo(w * 0.05f, h * 0.5f, w * 0.2f, h * 0.3f, w * 0.5f, 0f)
            close()
        }
        drawPath(path, color)
    }
}

/** Чип выбора (настроение, симптомы, интенсивность) — выбранный заметно отличается рамкой и заливкой */
@Composable
fun SelectChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: String? = null
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(if (selected) primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(
                BorderStroke(
                    if (selected) 1.8.dp else 1.dp,
                    if (selected) primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        if (leading != null) {
            Text(leading, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Закруглённая «таблетка»-кнопка */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .then(
                if (filled) Modifier.background(primary)
                else Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .border(1.dp, primary.copy(alpha = 0.6f), shape)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}


