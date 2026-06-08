package com.gongde.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BarGreen = Color(0xFF66BB6A)
private val DateFmt = DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINESE)

@Composable
fun TimelineScreen(
    entries: List<Pair<String, Int>>,
    weekTotal: Int,
    monthTotal: Int
) {
    val today = LocalDate.now()
    val colors = GongDeThemeExt.colors
    val accent = colors.accent
    val todayBg = colors.cardBorder.copy(alpha = 0.09f)
    val maxCount = (entries.maxOfOrNull { it.second } ?: 10).coerceAtLeast(10)

    var expanded by remember { mutableStateOf(false) }
    val visibleEntries = if (expanded) entries else entries.take(7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accent.copy(alpha = 0.1f))
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "功德日历",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TopStat("本周功德", weekTotal, colors.textPrimary, colors.textMuted)
            TopStat("本月功德", monthTotal, accent, colors.textMuted)
        }

        for (entry in visibleEntries) {
            val dateStr = entry.first
            val count = entry.second
            val date = LocalDate.parse(dateStr)
            val isToday = date == today
            val isYesterday = date == today.minusDays(1)
            val label = when {
                isToday    -> "今天"
                isYesterday -> "昨天"
                else       -> date.format(DateFmt)
            }
            TimelineRow(label, count, isToday, maxCount, todayBg, colors.textPrimary, colors.textMuted, colors.barTrack)
        }

        // 展开/收起按钮
        if (entries.size > 7) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (expanded) "收起 ▲" else "展开更多 ▼",
                    color = accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TopStat(label: String, value: Int, color: Color, mutedColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = mutedColor, fontSize = 15.sp)
    }
}

@Composable
private fun TimelineRow(
    label: String, count: Int, today: Boolean, maxCount: Int,
    todayBg: Color, textPrimary: Color, textMuted: Color, barTrack: Color
) {
    val progress = (count.toFloat() / maxCount).coerceIn(0f, 1f)
    val barColor = lerp(BarGreen, textPrimary, progress)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (today) Modifier.background(todayBg, RoundedCornerShape(8.dp))
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            color = if (today) textPrimary else textMuted,
            fontSize = 15.sp,
            fontWeight = if (today) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.width(72.dp)
        )

        Text(
            text = "$count",
            color = textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(barTrack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress.coerceAtLeast(0.02f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}
