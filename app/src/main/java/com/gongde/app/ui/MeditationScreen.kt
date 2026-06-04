/**
 * 专注计时模式
 *
 * 全屏专注计时界面，包含三种状态：
 * - IDLE（待机）：选择专注时长
 * - RUNNING（运行中）：倒计时进行中，每 3 秒自动 +1 功德
 * - FINISHED（完成）：展示专注成果统计
 *
 * 背景使用缓慢的呼吸动画渐变，营造放松氛围。
 */

package com.gongde.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt
import kotlinx.coroutines.delay

// 背景色由主题提供（见 composable 内局部变量）

// ==================== 专注状态 ====================

/**
 * 专注界面状态枚举
 *
 * IDLE    —— 选择专注时长
 * RUNNING —— 倒计时进行中
 * FINISHED —— 专注完成，展示结果
 */
private enum class FocusState {
    IDLE, RUNNING, FINISHED
}

/**
 * 专注模式主界面
 *
 * @param initialTotal 专注开始时的累计功德数
 * @param onMeritInc 每次功德增加的回调（记录历史）
 * @param onSync 退出专注时的同步回调（更新 UI + 检查成就）
 * @param onBack 返回上一页的回调
 */
@Composable
fun FocusScreen(
    initialTotal: Int = 0,
    onMeritInc: () -> Unit = {},
    onSync: () -> Unit = {},
    onBack: () -> Unit
) {
    // 界面状态：待机 / 运行中 / 完成
    var state by rememberSaveable { mutableStateOf(FocusState.IDLE.name) }

    // 选中的专注时长（秒），默认 3 分钟
    var selectedDuration by rememberSaveable { mutableIntStateOf(3 * 60) }

    // 剩余秒数（倒计时用）
    var remainingSeconds by rememberSaveable { mutableIntStateOf(0) }

    // 本次专注获得的功德数
    var meritEarned by rememberSaveable { mutableIntStateOf(0) }

    // 是否暂停
    var isPaused by rememberSaveable { mutableStateOf(false) }

    // 当前时间戳（用于计算已用时间）
    var elapsedSeconds by rememberSaveable { mutableIntStateOf(0) }

    // 功德计时 tick 累积（跨暂停/继续保持，避免丢失 0-2 秒进度）
    var tickCount by rememberSaveable { mutableIntStateOf(0) }

    // 防止 onSync 被多次调用（倒计时结束 + 返回按钮都会触发）
    var hasSynced by rememberSaveable { mutableStateOf(false) }

    // 退出确认对话框
    var showBackConfirm by rememberSaveable { mutableStateOf(false) }

    // 系统返回键：退出确认弹框 → 关闭弹框；专注中 → 触发退出确认
    BackHandler(enabled = showBackConfirm) {
        showBackConfirm = false
    }
    BackHandler(enabled = !showBackConfirm && state != FocusState.IDLE.name) {
        showBackConfirm = true
    }

    // 专注开始时的累计功德快照（用于展示 "累计 → 累计+N"）
    var baseTotal by rememberSaveable { mutableIntStateOf(0) }

    val currentState = FocusState.entries.find { it.name == state } ?: FocusState.IDLE

    // ─── 呼吸动画：背景渐变在深色与稍亮之间缓慢振荡（4 秒一个周期） ───
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathAlpha"
    )

    // 根据呼吸动画值混合背景色
    val bgDark = GongDeThemeExt.colors.surfaceDark
    val bgLight = GongDeThemeExt.colors.dialogBg
    val breathColor = lerpColor(bgDark, bgLight, breathAlpha)

    // ─── 倒计时协程 ───
    // 当状态为 RUNNING 且未暂停时，每秒递减倒计时
    // 每 3 秒自动通过回调增加功德（计入历史）
    LaunchedEffect(state, isPaused) {
        if (currentState == FocusState.RUNNING && !isPaused) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
                elapsedSeconds++
                tickCount++

                // 每 3 秒自动获得 1 功德
                if (tickCount % 3 == 0) {
                    meritEarned++
                    onMeritInc()
                }
            }
            // 倒计时结束，退出前同步并切换到完成状态
            if (!hasSynced) { hasSynced = true; onSync() }
            state = FocusState.FINISHED.name
        }
    }

    // ─── 全屏布局 ───
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(breathColor)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        when (currentState) {
            // ===== 待机状态：选择专注时长 =====
            FocusState.IDLE -> {
                IdleContent(
                    selectedDuration = selectedDuration,
                    onSelectDuration = { selectedDuration = it },
                    onStart = {
                        baseTotal = initialTotal
                        remainingSeconds = selectedDuration
                        meritEarned = 0
                        elapsedSeconds = 0
                        tickCount = 0
                        isPaused = false
                        state = FocusState.RUNNING.name
                    },
                    onBack = onBack
                )
            }

            // ===== 运行状态：倒计时进行中 =====
            FocusState.RUNNING -> {
                RunningContent(
                    remainingSeconds = remainingSeconds,
                    meritEarned = meritEarned,
                    baseTotal = baseTotal,
                    isPaused = isPaused,
                    onTogglePause = { isPaused = !isPaused },
                    onBack = { showBackConfirm = true }
                )
            }

            // ===== 完成状态：展示专注成果 =====
            FocusState.FINISHED -> {
                FinishedContent(
                    totalDuration = selectedDuration,
                    meritEarned = meritEarned,
                    baseTotal = baseTotal,
                    elapsedSeconds = elapsedSeconds,
                    onBack = { if (!hasSynced) { hasSynced = true; onSync() }; onBack() }
                )
            }
        }
    }

    // 退出确认对话框
    if (showBackConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            containerColor = GongDeThemeExt.colors.dialogBg,
            titleContentColor = Color.White,
            textContentColor = GongDeThemeExt.colors.textSecondary,
            title = { Text("退出专注") },
            text = { Text("专注进行中，已获得 $meritEarned 功德。确定退出吗？") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showBackConfirm = false
                    if (!hasSynced) { hasSynced = true; onSync() }
                    onBack()
                }) { Text("确定", color = Color.White) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showBackConfirm = false }) {
                    Text("取消", color = GongDeThemeExt.colors.textSecondary)
                }
            }
        )
    }
}

// ==================== IDLE 状态内容 ====================

/**
 * 待机状态：选择专注时长并开始
 *
 * @param selectedDuration 当前选中的时长（秒）
 * @param onSelectDuration 选择时长的回调
 * @param onStart 开始专注的回调
 * @param onBack 返回的回调
 */
@Composable
private fun IdleContent(
    selectedDuration: Int,
    onSelectDuration: (Int) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    // 可选的专注时长（分钟）
    val durations = listOf(3, 5, 10, 15, 20)
    val bgDark = GongDeThemeExt.colors.surfaceDark

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "返回" 文本按钮
            // "返回" 按钮（增大触摸区域）
            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("← 返回", color = GongDeThemeExt.colors.textMuted, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 页面标题
        Text(
            text = "专注模式",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 副标题说明
        Text(
            text = "放松身心，每3秒自动攒功德",
            color = GongDeThemeExt.colors.textMuted,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 时长选择网格（两行排列）
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 第一行：3、5、10 分钟
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                durations.take(3).forEach { minutes ->
                    DurationCard(
                        minutes = minutes,
                        selected = selectedDuration == minutes * 60,
                        onClick = { onSelectDuration(minutes * 60) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // 第二行：15、20 分钟
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                durations.drop(3).forEach { minutes ->
                    DurationCard(
                        minutes = minutes,
                        selected = selectedDuration == minutes * 60,
                        onClick = { onSelectDuration(minutes * 60) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // "开始" 按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GongDeThemeExt.colors.accent)
                .clickable { onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "开始",
                color = bgDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * 时长选项卡片
 *
 * @param minutes 分钟数
 * @param selected 是否被选中
 * @param onClick 点击回调
 */
@Composable
private fun DurationCard(
    minutes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.border(2.dp, GongDeThemeExt.colors.accent, RoundedCornerShape(12.dp))
                else Modifier.border(1.dp, GongDeThemeExt.colors.cardBorder, RoundedCornerShape(12.dp))
            )
            .background(if (selected) GongDeThemeExt.colors.accent.copy(alpha = 0.08f) else GongDeThemeExt.colors.cardBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$minutes",
                color = if (selected) GongDeThemeExt.colors.accent else Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "分钟",
                color = if (selected) GongDeThemeExt.colors.accent else GongDeThemeExt.colors.textMuted,
                fontSize = 15.sp
            )
        }
    }
}

// ==================== RUNNING 状态内容 ====================

/**
 * 运行状态：倒计时进行中
 *
 * @param remainingSeconds 剩余秒数
 * @param meritEarned 本次获得的功德数
 * @param baseTotal 专注开始时的累计功德
 * @param isPaused 是否暂停中
 * @param onTogglePause 暂停/继续切换回调
 * @param onBack 返回的回调
 */
@Composable
private fun RunningContent(
    remainingSeconds: Int,
    meritEarned: Int,
    baseTotal: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部导航栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "返回" 按钮（增大触摸区域）
            TextButton(
                onClick = onBack,
                modifier = Modifier.padding(8.dp)
            ) {
                Text("← 返回", color = GongDeThemeExt.colors.textMuted, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(60.dp))

        // 倒计时数字（大号白色）
        Text(
            text = formatTime(remainingSeconds),
            color = Color.White,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // "暂停" / "继续" 切换按钮
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Color.White, RoundedCornerShape(24.dp))
                .background(Color.Transparent)
                .clickable { onTogglePause() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPaused) "继续" else "暂停",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 本次功德统计
        Text(
            text = "本次功德: $meritEarned",
            color = Color.White,
            fontSize = 15.sp,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "累计: $baseTotal → ${baseTotal + meritEarned}",
            color = GongDeThemeExt.colors.textMuted,
            fontSize = 15.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

// ==================== FINISHED 状态内容 ====================

/**
 * 完成状态：展示专注成果
 *
 * @param totalDuration 总专注时长（秒）
 * @param meritEarned 本次获得的功德数
 * @param baseTotal 专注开始时的累计功德
 * @param elapsedSeconds 实际经过的秒数
 * @param onBack 返回的回调
 */
@Composable
private fun FinishedContent(
    totalDuration: Int,
    meritEarned: Int,
    baseTotal: Int,
    elapsedSeconds: Int,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 完成标题
        Text(
            text = "专注完成",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 功德统计
        Text(
            text = "本次获得 $meritEarned 功德",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 累计功德变化
        Text(
            text = "累计: $baseTotal → ${baseTotal + meritEarned}",
            color = Color.White,
            fontSize = 18.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 时长统计（格式化为 MM:SS）
        Text(
            text = "专注时长 ${formatTime(elapsedSeconds)}",
            color = GongDeThemeExt.colors.textMuted,
            fontSize = 15.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // "返回" 按钮
        Box(
            modifier = Modifier
                .width(160.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                .background(Color.Transparent)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "返回",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================== 工具函数 ====================

/**
 * 将秒数格式化为 MM:SS 字符串
 *
 * @param totalSeconds 总秒数
 * @return 格式化后的时间字符串
 */
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * 在两个颜色之间线性插值（用于呼吸动画背景色过渡）
 *
 * @param startColor 起始颜色
 * @param endColor 终止颜色
 * @param fraction 插值比例（0~1）
 * @return 插值后的颜色
 */
private fun lerpColor(startColor: Color, endColor: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = startColor.red + (endColor.red - startColor.red) * f,
        green = startColor.green + (endColor.green - startColor.green) * f,
        blue = startColor.blue + (endColor.blue - startColor.blue) * f,
        alpha = startColor.alpha + (endColor.alpha - startColor.alpha) * f
    )
}
