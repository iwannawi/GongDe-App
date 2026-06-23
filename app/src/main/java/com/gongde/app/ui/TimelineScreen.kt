package com.gongde.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DefaultVisibleDays = 5

private val DateFmt = DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINESE)

@Composable
fun TimelineScreen(entries: List<Pair<String, Int>>, weekTotal: Int, monthTotal: Int) {
    val colors = GongDeThemeExt.colors
    val today = LocalDate.now()
    val maxCount = (entries.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.cardBg, RoundedCornerShape(8.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryValue("近 7 天", weekTotal)
            SummaryValue("近 30 天", monthTotal)
        }

        entries.take(DefaultVisibleDays).forEach { TimelineRow(it, today, maxCount) }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entries.drop(DefaultVisibleDays).forEach { TimelineRow(it, today, maxCount) }
            }
        }
        if (entries.size > DefaultVisibleDays) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (expanded) "收起" else "查看 30 天", color = colors.accent, fontSize = 13.sp)
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: Int) {
    val colors = GongDeThemeExt.colors
    Column {
        Text(value.toString(), color = colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, color = colors.textMuted, fontSize = 12.sp)
    }
}

@Composable
private fun TimelineRow(entry: Pair<String, Int>, today: LocalDate, maxCount: Int) {
    val colors = GongDeThemeExt.colors
    val date = LocalDate.parse(entry.first)
    val label = when (date) {
        today -> "今天"
        today.minusDays(1) -> "昨天"
        else -> date.format(DateFmt)
    }
    val progress = (entry.second.toFloat() / maxCount).coerceIn(0f, 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(label, color = colors.textMuted, fontSize = 13.sp, modifier = Modifier.width(68.dp))
        Text(
            entry.second.toString(),
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(42.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .height(5.dp)
                .background(colors.barTrack, RoundedCornerShape(3.dp))
        ) {
            if (progress > 0f) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Color(0xFF607D8B), RoundedCornerShape(3.dp))
                )
            }
        }
    }
}
