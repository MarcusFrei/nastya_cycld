package com.cycleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class NavItem(val label: String, val icon: ImageVector)

/**
 * Нижняя навигация как на макете: скруглённая панель, две вкладки слева, две справа
 * и круглая кнопка «+» по центру (быстрая отметка сегодняшнего дня).
 */
@Composable
fun BottomNav(
    items: List<NavItem>,          // ровно 4 пункта
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val barShape = RoundedCornerShape(30.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(8.dp, barShape, clip = false)
                .clip(barShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), barShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavButton(items[0], selectedIndex == 0, Modifier.weight(1f)) { onSelect(0) }
            NavButton(items[1], selectedIndex == 1, Modifier.weight(1f)) { onSelect(1) }
            Spacer(Modifier.width(72.dp)) // место под центральную кнопку
            NavButton(items[2], selectedIndex == 2, Modifier.weight(1f)) { onSelect(2) }
            NavButton(items[3], selectedIndex == 3, Modifier.weight(1f)) { onSelect(3) }
        }

        // Центральная кнопка «+» — выступает над панелью
        Box(
            modifier = Modifier
                .padding(bottom = 38.dp)
                .size(58.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(primary, lerp(primary, Color(0xFF6FA8FF), 0.55f))
                    )
                )
                .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Отметить сегодня",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun NavButton(item: NavItem, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .height(68.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(item.icon, contentDescription = item.label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = tint,
            maxLines = 1
        )
    }
}
