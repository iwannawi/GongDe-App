package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.data.Achievement
import com.gongde.app.data.AchievementMetric
import com.gongde.app.ui.theme.GongDeThemeExt

@Composable
fun AchievementScreen(
    achievements: List<Achievement>,
    completedAchievementIds: Set<String>,
    totalCount: Int,
    todayCount: Int,
    streak: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("成就进度")
        achievements.forEach { achievement ->
            val current = when (achievement.metric) {
                AchievementMetric.TOTAL -> totalCount
                AchievementMetric.TODAY -> todayCount
                AchievementMetric.STREAK -> streak
            }
            AchievementCard(
                achievement = achievement,
                current = current.coerceAtMost(achievement.target),
                completed = achievement.id in completedAchievementIds
            )
        }
    }
}

@Composable
fun SectionHeading(text: String) {
    Text(
        text = text,
        color = GongDeThemeExt.colors.textPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AchievementCard(achievement: Achievement, current: Int, completed: Boolean) {
    val colors = GongDeThemeExt.colors
    val progress = if (achievement.target == 0) 0f else current.toFloat() / achievement.target
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardBg, RoundedCornerShape(8.dp))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (completed) colors.accent.copy(alpha = 0.12f) else colors.barTrack.copy(alpha = 0.45f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (completed) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                contentDescription = null,
                tint = if (completed) colors.accent else colors.textMuted,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(Modifier.weight(1f)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = achievement.name,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (completed) "已完成" else "$current / ${achievement.target}",
                    color = if (completed) colors.accent else colors.textMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(achievement.description, color = colors.textMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = if (completed) colors.accent else Color(0xFF607D8B),
                trackColor = colors.barTrack,
                drawStopIndicator = {}
            )
        }
    }
}
