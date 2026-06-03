package com.gongde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.navigation.Screen
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.ThemePresets
import com.gongde.app.viewmodel.GongDeViewModel
import com.gongde.app.viewmodel.SettingsAction
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val vm: GongDeViewModel by viewModels {
        val app = application as GongDeApplication
        GongDeViewModel.Factory(app)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state = vm.uiState

            GongDeTheme(themeId = state.themeId) {
                GongDeApp(vm)
            }
        }
    }
}

/** 底部导航栏项目定义 */
private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val NAV_ITEMS = listOf(
    NavItem("主页", Icons.Default.Home, Screen.Home.route),
    NavItem("成就", Icons.Default.Star, Screen.Achievements.route),
    NavItem("设置", Icons.Default.Settings, Screen.Settings.route)
)

@Composable
fun GongDeApp(vm: GongDeViewModel) {
    val state = vm.uiState
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }

    // 启动闪屏
    var showSplash by rememberSaveable { mutableStateOf(true) }
    val splashAlpha = remember { Animatable(1f) }
    val splashQuote = remember { getRandomFunQuote() }

    if (showSplash) {
        BackHandler { /* 闪屏期间拦截返回键 */ }
        LaunchedEffect(Unit) {
            delay(800L)
            splashAlpha.animateTo(0f, animationSpec = tween(400))
            showSplash = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = splashAlpha.value }
                .background(Color(0xFF0D0D24)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "解压键盘",
                    color = Color(0xFFFFD54F),
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = splashQuote,
                    color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        return
    }

    // 系统返回键：非主页 → 回主页
    BackHandler(enabled = currentRoute != Screen.Home.route) {
        currentRoute = Screen.Home.route
    }

    val bgColors = ThemePresets.getGradient(state.themeId)
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Achievements.route, Screen.Settings.route)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colorStops = bgColors.mapIndexed { i, c ->
                (i.toFloat() / (bgColors.size - 1)) to c
            }.toTypedArray()))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTechDots()
        }

        FloatingTextContainer(
            triggerCount = state.triggerCount,
            modifier = Modifier.fillMaxSize().padding(top = 80.dp)
        )

        // 底部导航栏
        if (showBottomBar) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                NavigationBar(
                    containerColor = Color(0xDD0A0A1A),
                    contentColor = Color(0xFFFFD54F)
                ) {
                    for (item in NAV_ITEMS) {
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentRoute = item.route },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFFFD54F),
                                selectedTextColor = Color(0xFFFFD54F),
                                unselectedIconColor = Color(0x66B0BEC5),
                                unselectedTextColor = Color(0x66B0BEC5),
                                indicatorColor = Color(0x20FFD54F)
                            )
                        )
                    }
                }
            }
        }

        // 页面内容
        AppContent(currentRoute, vm, showBottomBar) { currentRoute = it }
    }
}

@Composable
fun AppContent(
    currentRoute: String,
    vm: GongDeViewModel,
    showBottomBar: Boolean,
    onNavigate: (String) -> Unit
) {
    Box(modifier = Modifier.padding(bottom = if (showBottomBar) 80.dp else 0.dp)) {
        when (currentRoute) {
            Screen.Focus.route -> FocusScreen(
                initialTotal = vm.uiState.totalCount,
                onMeritInc = { vm.incrementStore() },
                onSync = { vm.syncFromStore() },
                onBack = { onNavigate(Screen.Home.route) }
            )
            Screen.Asmr.route -> AsmrRoute(vm) { onNavigate(Screen.Home.route) }
            Screen.Achievements.route -> AchievementsScreen(vm)
            Screen.Settings.route -> SettingsScreen(
                hapticEnabled = vm.uiState.hapticEnabled,
                switchType = vm.uiState.switchType.name.lowercase(),
                themeId = vm.uiState.themeId,
                onSettingsAction = { vm.handleSettings(it) }
            )
            else -> HomeScreen(vm, onNavigate)
        }
    }
}

// ==================== AsmrRoute ====================

@Composable
fun AsmrRoute(vm: GongDeViewModel, onBack: () -> Unit) {
    val state = vm.uiState
    val hapticEngine = HapticEngine(LocalContext.current)
    AsmrScreen(
        totalCount = state.totalCount,
        soundEngine = vm.soundEngine,
        hapticEngine = hapticEngine,
        hapticEnabled = state.hapticEnabled,
        switchType = state.switchType,
        onMeritGain = { vm.incrementMerit() },
        onBack = onBack
    )
}

// ==================== HomeScreen ====================

@Composable
fun HomeScreen(vm: GongDeViewModel, onNavigate: (String) -> Unit) {
    val state = vm.uiState
    val hapticEngine = HapticEngine(LocalContext.current)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        MechanicalButton(
            modifier = Modifier.size(220.dp, 250.dp),
            soundEngine = vm.soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = state.hapticEnabled,
            switchType = state.switchType,
            asmrMode = false,
            onPressed = { vm.incrementMerit() }
        )
        Spacer(Modifier.height(4.dp))
        Text("解压键盘", color = Color(0x80FFD54F), fontSize = 13.sp, letterSpacing = 3.sp)
        Spacer(Modifier.weight(1f))
        MeritCounter(totalCount = state.totalCount, todayCount = state.todayCount)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeButton("🧘 专注") { onNavigate(Screen.Focus.route) }
            HomeButton("清零") { vm.showDialog(true) }
            HomeButton("🎧 ASMR") { onNavigate(Screen.Asmr.route) }
        }
        Spacer(Modifier.height(12.dp))
    }

    if (state.showResetDialog) {
        BackHandler { vm.showDialog(false) }
        AlertDialog(
            onDismissRequest = { vm.showDialog(false) },
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color(0xFFFFD54F),
            textContentColor = Color(0xFFB0BEC5),
            title = { Text("确认清零") },
            text = { Text("累计功德和今日功德都将归零，确定吗？\n成就和历史记录将保留。") },
            confirmButton = { TextButton(onClick = { vm.resetMerit() }) { Text("确定", color = Color(0xFFFFD54F)) } },
            dismissButton = { TextButton(onClick = { vm.showDialog(false) }) { Text("取消", color = Color(0xFFB0BEC5)) } }
        )
    }
}

// ==================== AchievementsScreen ====================

@Composable
fun AchievementsScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    val bgColors = ThemePresets.getGradient(state.themeId)
    val cardGrad = listOf(bgColors.getOrElse(0) { Color(0xFF1A0033) }, bgColors.getOrElse(2) { Color(0xFF2D1055) })

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        AchievementScreen(store = vm.achievementStore, weekTotal = state.weekTotal, monthTotal = state.monthTotal)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ShareCardView(totalCount = state.totalCount, cardGradient = cardGrad)
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            ShareButton(totalCount = state.totalCount, cardGradient = cardGrad)
        }
        Spacer(Modifier.height(16.dp))
        TimelineScreen(historyStore = vm.historyStore)
        Spacer(Modifier.height(16.dp))
    }
}

// ==================== HomeButton ====================

@Composable
fun HomeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .semantics(mergeDescendants = true) { contentDescription = text }
            .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(20.dp))
            .background(Color(0x08FFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color(0x60B0BEC5), fontSize = 14.sp) }
}

// ==================== 背景装饰 ====================

private fun DrawScope.drawTechDots() {
    val sp = 48f; val dc = Color(0x06FFFFFF); val ac = Color(0x0AFFD54F)
    var x = 0f; while (x < size.width) { var y = 0f; while (y < size.height) { drawCircle(if (((x / sp).toInt() + (y / sp).toInt()) % 7 == 0) ac else dc, if (((x / sp).toInt() + (y / sp).toInt()) % 7 == 0) 1.8f else 1f, Offset(x, y)); y += sp }; x += sp }
}
