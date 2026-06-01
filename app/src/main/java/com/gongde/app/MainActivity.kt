/**
 * 主界面 Activity
 *
 * 应用入口，负责：
 * - 全局状态管理（功德计数、触发次数）
 * - 底部导航栏（主页 / 成就 / 设置）
 * - 各页面间导航
 * - 动态主题切换
 * - 冥想模式 / ASMR 模式的全屏覆盖
 */
package com.gongde.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.data.AchievementStore
import com.gongde.app.data.HistoryStore
import com.gongde.app.data.MeritStore
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.ThemePresets
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val store = remember { MeritStore(context) }
            // 主题 ID 由 GongDeApp 内部管理，此处作为初始值
            var themeId by remember { mutableStateOf(store.themeId) }

            GongDeTheme(themeId = themeId) {
                GongDeApp(store, onThemeChanged = { themeId = it })
            }
        }
    }
}

/** 底部导航栏标签 */
private enum class NavTab(val label: String, val icon: String) {
    HOME("主页", "⌨"),
    ACHIEVE("成就", "🏆"),
    SETTINGS("设置", "⚙")
}

@Composable
private fun GongDeApp(store: MeritStore, onThemeChanged: (String) -> Unit = {}) {
    val context = LocalContext.current
    val achievementStore = remember { AchievementStore(context) }
    val historyStore = remember { HistoryStore(context) }

    // 功德计数状态
    var totalCount by rememberSaveable { mutableIntStateOf(store.totalCount) }
    var todayCount by rememberSaveable { mutableIntStateOf(store.todayCount) }
    var triggerCount by rememberSaveable { mutableIntStateOf(0) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    // 声音和触觉引擎
    val soundEngine = remember { SoundEngine() }
    val hapticEngine = remember { HapticEngine(context) }

    // 设置状态（集中管理，由 SettingsScreen 通过回调更新）
    var hapticEnabled by remember { mutableStateOf(store.hapticEnabled) }
    var currentSwitchType by remember { mutableStateOf(SwitchType.valueOf(store.switchType.uppercase())) }
    var asmrEnabled by remember { mutableStateOf(store.asmrEnabled) }

    // 设置变更统一回调
    val onSettingsChange: (String, Any) -> Unit = { key, value ->
        when (key) {
            "haptic" -> { hapticEnabled = value as Boolean; store.hapticEnabled = value }
            "switch" -> { currentSwitchType = SwitchType.valueOf((value as String).uppercase()); store.switchType = value }
            "theme" -> { store.themeId = value as String; onThemeChanged(value) }
            "asmr" -> { asmrEnabled = value as Boolean; store.asmrEnabled = value }
        }
    }

    // 引擎资源释放
    DisposableEffect(Unit) {
        onDispose {
            soundEngine.release()
            hapticEngine.release()
        }
    }

    // 启动时清理超过 90 天的历史数据
    LaunchedEffect(Unit) { historyStore.cleanup() }

    // 导航状态
    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var showMeditation by rememberSaveable { mutableStateOf(false) }
    var showAsmr by rememberSaveable { mutableStateOf(false) }

    // 功德增加回调（统一处理：计数 + 历史 + 成就检查 + 提示）
    val onMeritGain: () -> Unit = {
        try {
            val (newTotal, newToday) = store.increment()
            totalCount = newTotal
            todayCount = newToday
            triggerCount++
            historyStore.recordMerit(java.time.LocalDate.now().toString(), 1)
            achievementStore.updateStreak()
            val unlocked = achievementStore.checkAndUnlock(newTotal, newToday)
            for (achievement in unlocked) {
                Toast.makeText(context, "🏆 成就解锁：${achievement.name}", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) { }
    }

    // 获取当前主题的背景渐变色
    val bgColors = ThemePresets.getGradient(store.themeId)

    // 全屏覆盖：冥想模式
    if (showMeditation) {
        MeditationScreen(store = store, onBack = { showMeditation = false })
        return
    }
    // 全屏覆盖：ASMR 模式（复用 GongDeApp 的引擎实例和功德回调）
    if (showAsmr) {
        AsmrScreen(
            store = store,
            soundEngine = soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = hapticEnabled,
            onMeritGain = onMeritGain,
            onBack = { showAsmr = false }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colorStops = bgColors.mapIndexed { i, c ->
                    (i.toFloat() / (bgColors.size - 1)) to c
                }.toTypedArray())
            )
    ) {
        // 背景装饰（法轮 + 科技点阵）
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSubtleMandala(size.width / 2f, size.height * 0.35f)
            drawTechDots()
        }

        // 飘字层
        FloatingTextContainer(
            triggerCount = triggerCount,
            modifier = Modifier.fillMaxSize().padding(top = 80.dp)
        )

        // 主内容区域（不含底部导航）
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp) // 为底部导航栏留空间
        ) {
            when (currentTab) {
                NavTab.HOME -> HomeContent(
                    onMeritGain = onMeritGain,
                    totalCount = totalCount,
                    todayCount = todayCount,
                    showResetDialog = showResetDialog,
                    onDismissReset = { showResetDialog = false },
                    onConfirmReset = {
                        store.reset(); totalCount = 0; todayCount = 0; showResetDialog = false
                    },
                    onShowReset = { showResetDialog = true },
                    onShowMeditation = { showMeditation = true },
                    onShowAsmr = { showAsmr = true },
                    soundEngine = soundEngine,
                    hapticEngine = hapticEngine,
                    hapticEnabled = hapticEnabled,
                    switchType = currentSwitchType,
                    asmrMode = asmrEnabled
                )
                NavTab.ACHIEVE -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 48.dp)
                ) {
                    AchievementScreen(store = achievementStore, historyStore = historyStore)
                    Spacer(Modifier.height(16.dp))
                    // 分享卡片
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ShareCardView(totalCount = totalCount)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ShareButton(totalCount = totalCount)
                    }
                    Spacer(Modifier.height(16.dp))
                    TimelineScreen(historyStore = historyStore)
                }
                NavTab.SETTINGS -> SettingsScreen(
                    hapticEnabled = hapticEnabled,
                    switchType = store.switchType,
                    themeId = store.themeId,
                    onSettingsChange = onSettingsChange,
                    onOpenMeditation = { showMeditation = true },
                    onOpenAsmr = { showAsmr = true }
                )
            }
        }

        // 底部导航栏
        BottomNavBar(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ==================== 主页内容 ====================

@Composable
private fun HomeContent(
    onMeritGain: () -> Unit,
    totalCount: Int,
    todayCount: Int,
    showResetDialog: Boolean,
    onDismissReset: () -> Unit,
    onConfirmReset: () -> Unit,
    onShowReset: () -> Unit,
    onShowMeditation: () -> Unit,
    onShowAsmr: () -> Unit,
    soundEngine: SoundEngine,
    hapticEngine: HapticEngine,
    hapticEnabled: Boolean,
    switchType: SwitchType,
    asmrMode: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.55f))

        // 机械键盘按钮（默认尺寸 260x300dp）
        MechanicalButton(
            modifier = Modifier.size(260.dp, 300.dp),
            soundEngine = soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = hapticEnabled,
            switchType = switchType,
            asmrMode = asmrMode,
            onPressed = onMeritGain
        )

        Spacer(Modifier.height(8.dp))

        // 标题
        Text("解压键盘", color = Color(0x55FFD54F), fontSize = 13.sp, letterSpacing = 3.sp)

        Spacer(Modifier.weight(0.15f))

        // 功德计数面板
        MeritCounter(totalCount = totalCount, todayCount = todayCount)

        // 底部操作栏：清零 + 冥想 + ASMR
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HomeButton("🧘 冥想") { onShowMeditation() }
            HomeButton("清零") { onShowReset() }
            HomeButton("🎧 ASMR") { onShowAsmr() }
        }
    }

    // 重置确认弹窗
    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissReset,
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color(0xFFFFD54F),
            textContentColor = Color(0xFFB0BEC5),
            title = { Text("确认清零") },
            text = { Text("累计功德和今日功德都将归零，确定吗？") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = onConfirmReset) {
                    Text("确定", color = Color(0xFFFFD54F))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = onDismissReset) {
                    Text("取消", color = Color(0xFFB0BEC5))
                }
            }
        )
    }
}

// ==================== 底部导航栏 ====================

@Composable
private fun BottomNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xDD0A0A1A))
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .semantics { contentDescription = tab.label }
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tab.icon,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        color = if (isSelected) Color(0xFFFFD54F) else Color(0x66B0BEC5),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

/** 主页小按钮（冥想/清零/ASMR） */
@Composable
private fun HomeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(20.dp))
            .background(Color(0x08FFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color(0x40B0BEC5), fontSize = 12.sp)
    }
}

// ==================== 背景装饰函数 ====================

/** 法轮图案（极淡） */
private fun DrawScope.drawSubtleMandala(cx: Float, cy: Float) {
    val lines = 12
    val radius = size.width * 0.45f
    val strokeColor = Color(0x08FFFFFF)
    val strokeWidth = 0.8f

    for (i in 0 until lines) {
        val angle = (2.0 * Math.PI * i / lines).toFloat()
        drawLine(strokeColor, Offset(cx, cy),
            Offset(cx + cos(angle) * radius, cy + sin(angle) * radius), strokeWidth)
    }
    for (r in listOf(0.15f, 0.25f, 0.35f, 0.45f)) {
        drawCircle(strokeColor, radius * r, Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth))
    }
    for (i in 0 until lines) {
        val angle = (2.0 * Math.PI * i / lines).toFloat()
        drawCircle(Color(0x0CFFD54F), 3f,
            Offset(cx + cos(angle) * radius * 0.25f, cy + sin(angle) * radius * 0.25f))
    }
}

/** 科技点阵 */
private fun DrawScope.drawTechDots() {
    val spacing = 48f
    val dotColor = Color(0x06FFFFFF)
    val accentColor = Color(0x0AFFD54F)
    var x = 0f
    while (x < size.width) {
        var y = 0f
        while (y < size.height) {
            val isAccent = ((x / spacing).toInt() + (y / spacing).toInt()) % 7 == 0
            drawCircle(if (isAccent) accentColor else dotColor,
                if (isAccent) 1.8f else 1f, Offset(x, y))
            y += spacing
        }
        x += spacing
    }
}
