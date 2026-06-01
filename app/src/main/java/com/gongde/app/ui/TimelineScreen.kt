package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.data.HistoryStore
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeThemeExt
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// 主题颜色（Gold/Blue 从 theme 导入，其余为本地专用）
private val TodayBg = Color(0x18FFFFFF)
private val Muted = Color(0xFF78909C)
private val BarGreen = Color(0xFF66BB6A)

// 日期格式器：输出 "MM月DD日"
private val DateFmt = DateTimeFormatter.ofPattern("MM月dd日", Locale.CHINESE)

/**
 * 时间线界面 - 展示最近30天的每日功德记录
 * 使用普通 Column，由外部提供滚动容器
 */
@Composable
fun TimelineScreen(historyStore: HistoryStore) {
    val today = LocalDate.now()
    val entries = historyStore.getRecentDays(30)
    val accent = GongDeThemeExt.colors.accent
    // 动态基准值：取最近 30 天最大值（至少为 10 避免除零）
    val maxCount = (entries.maxOfOrNull { it.second } ?: 10).coerceAtLeast(10)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题
        Text(
            text = "功德时间线",
            color = GoldColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // ---- 顶部统计 ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TopStat("本周功德", historyStore.getWeekTotal(), GoldColor)
            TopStat("本月功德", historyStore.getMonthTotal(), accent)
        }

        // ---- 每日时间线条目 ----
        entries.forEach { (dateStr, count) ->
            val date = LocalDate.parse(dateStr)
            val isToday = date == today
            val isYesterday = date == today.minusDays(1)
            val label = when {
                isToday    -> "今天"
                isYesterday -> "昨天"
                else       -> date.format(DateFmt)
            }
            TimelineRow(label, count, isToday, maxCount)
        }
    }
}

/**
 * 顶部统计文字
 */
@Composable
private fun TopStat(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Muted,
            fontSize = 13.sp
        )
    }
}

/**
 * 单行时间线条目
 *
 * [label]  日期标签（"今天"/"昨天"/"MM月DD日"）
 * [count]  当天功德数
 * [today]  是否为今天，是则高亮背景
 */
@Composable
private fun TimelineRow(label: String, count: Int, today: Boolean, maxCount: Int) {
    val progress = (count.toFloat() / maxCount).coerceIn(0f, 1f)

    // 根据功德数混合颜色：低数偏绿，高数偏金
    val barColor = lerp(BarGreen, GoldColor, progress)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (today)
                    Modifier.background(TodayBg, RoundedCornerShape(8.dp))
                else Modifier
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 左侧日期
        Text(
            text = label,
            color = if (today) Color.White else Muted,
            fontSize = 14.sp,
            fontWeight = if (today) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.width(72.dp)
        )

        // 中间功德数
        Text(
            text = "$count",
            color = GoldColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )

        // 右侧进度条
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF333333))
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
