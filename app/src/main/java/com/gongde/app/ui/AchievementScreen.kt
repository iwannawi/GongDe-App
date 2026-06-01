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
import com.gongde.app.data.HistoryStore
import com.gongde.app.ui.theme.CardBgColor
import com.gongde.app.ui.theme.GoldColor

// 颜色从 ui.theme.Color 统一导入
private val GoldGlow = Color(0x33FFD54F)
private val LockedGray = Color(0xFF616161)

/**
 * 成就界面 - 展示所有成就卡片及本周/本月功德统计
 * 使用普通 Column，由外部提供滚动容器
 */
@Composable
fun AchievementScreen(
    store: AchievementStore,
    historyStore: HistoryStore
) {
    val weekTotal = historyStore.getWeekTotal()
    val monthTotal = historyStore.getMonthTotal()
    val achievements = store.allAchievements

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ---- 顶部功德统计 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBadge(label = "本周功德", value = weekTotal, color = GoldColor)
            StatBadge(label = "本月功德", value = monthTotal, color = Color(0xFF4FC3F7))
        }

        // ---- 成就卡片列表 ----
        achievements.forEach { achievement ->
            val unlocked = store.isUnlocked(achievement.id)
            AchievementCard(achievement, unlocked)
        }
    }
}

/**
 * 顶部统计徽章
 */
@Composable
private fun StatBadge(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFFB0BEC5),
            fontSize = 13.sp
        )
    }
}

/**
 * 单个成就卡片
 * - 已解锁：金色边框 + 金色辉光
 * - 未解锁：灰色边框，图标变暗，描述显示 "???"
 */
@Composable
private fun AchievementCard(achievement: Achievement, unlocked: Boolean) {
    val borderColor = if (unlocked) GoldColor else LockedGray
    val descriptionText = if (unlocked) achievement.description else "???"
    val nameAlpha = if (unlocked) 1f else 0.5f
    val iconAlpha = if (unlocked) 1f else 0.35f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (unlocked)
                    Modifier.shadow(8.dp, shape = RoundedCornerShape(12.dp), ambientColor = GoldGlow)
                else Modifier
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(CardBgColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 图标区域 - 已解锁时带金色背景，未解锁时灰色
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (unlocked) GoldColor.copy(alpha = 0.15f)
                    else Color(0xFF424242).copy(alpha = 0.5f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = achievement.icon,
                fontSize = 24.sp,
                modifier = Modifier.then(
                    Modifier.graphicsLayer { alpha = iconAlpha }
                )
            )
        }

        // 名称 + 描述
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.name,
                color = Color.White.copy(alpha = nameAlpha),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = descriptionText,
                color = Color(0xFF90A4AE).copy(alpha = nameAlpha),
                fontSize = 13.sp,
                maxLines = 2
            )
        }
    }
}
