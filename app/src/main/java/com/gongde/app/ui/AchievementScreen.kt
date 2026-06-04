package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.data.Achievement
import com.gongde.app.data.AchievementStore
import com.gongde.app.ui.theme.GongDeThemeExt

private val GlowWhite = Color(0x33FFFFFF)

@Composable
fun AchievementScreen(
    store: AchievementStore,
    weekTotal: Int,
    monthTotal: Int
) {
    val achievements = store.allAchievements
    val colors = GongDeThemeExt.colors

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 板块 1：本周/本月功德统计
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(label = "本周功德", value = weekTotal, color = colors.textPrimary)
                StatBadge(label = "本月功德", value = monthTotal, color = colors.accent)
            }
        }

        // 板块 2：成就卡片列表
        SectionCard {
            Text(
                text = "成就",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            achievements.forEach { achievement ->
                val unlocked = store.isUnlocked(achievement.id)
                AchievementCard(achievement, unlocked)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = GongDeThemeExt.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBg)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun StatBadge(label: String, value: Int, color: Color) {
    val colors = GongDeThemeExt.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$value", color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = colors.textSecondary, fontSize = 15.sp)
    }
}

@Composable
private fun AchievementCard(achievement: Achievement, unlocked: Boolean) {
    val colors = GongDeThemeExt.colors
    val borderColor = if (unlocked) colors.accent else colors.barTrack
    val nameAlpha = if (unlocked) 1f else 0.5f
    val iconAlpha = if (unlocked) 1f else 0.35f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (unlocked)
                    Modifier.shadow(8.dp, shape = RoundedCornerShape(12.dp), ambientColor = GlowWhite)
                else Modifier
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(colors.cardBg, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (unlocked) colors.accent.copy(alpha = 0.15f)
                    else colors.barTrack.copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = achievement.icon,
                fontSize = 24.sp,
                modifier = Modifier.graphicsLayer { alpha = iconAlpha }
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.name,
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = achievement.description,
                color = colors.textMuted,
                fontSize = 15.sp,
                maxLines = 2
            )
        }
    }
}
