package com.gongde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.navigation.Screen
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.GongDeThemeExt
import com.gongde.app.ui.theme.ThemePresets
import com.gongde.app.viewmodel.GongDeViewModel
import com.gongde.app.viewmodel.SettingsAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val NAV_ITEMS = listOf(
    NavItem("主页", Icons.Rounded.Home, Screen.Home.route),
    NavItem("成就", Icons.Rounded.Star, Screen.Achievements.route),
    NavItem("设置", Icons.Rounded.Tune, Screen.Settings.route)
)

@Composable
fun GongDeApp(vm: GongDeViewModel) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }
    val context = LocalContext.current

    LaunchedEffect(state.toastEvents) {
        val events = vm.consumeToastEvents()
        events.forEach { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showSplash by rememberSaveable { mutableStateOf(true) }
    val splashAlpha = remember { Animatable(1f) }
    val splashQuote = remember { getRandomFunQuote() }

    if (showSplash) {
        BackHandler { }
        LaunchedEffect(Unit) {
            delay(800L)
            splashAlpha.animateTo(0f, animationSpec = tween(400))
            showSplash = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = splashAlpha.value }
                .background(colors.surfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "解压键盘",
                    color = colors.textPrimary,
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = splashQuote,
                    color = colors.textMuted,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        }
        return
    }

    BackHandler(enabled = currentRoute != Screen.Home.route) {
        currentRoute = Screen.Home.route
    }

    val bgColors = colors.bgGradient
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Achievements.route, Screen.Settings.route)
    // 根据背景亮度决定圆点颜色
    val bgLuminance = bgColors.first().let { 0.299f * it.red + 0.587f * it.green + 0.114f * it.blue }
    val isLightBg = bgLuminance > 0.5f

    // 背景层 + 内容层 + 浮动文字层（最顶层）
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景：渐变 + 科技圆点
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = bgColors.mapIndexed { i, c ->
                    (i.toFloat() / (bgColors.size - 1)) to c
                }.toTypedArray()))
        ) {
            Canvas(modifier = Modifier.fillMaxSize().drawWithCache {
                val bitmap = android.graphics.Bitmap.createBitmap(
                    size.width.toInt().coerceAtLeast(1),
                    size.height.toInt().coerceAtLeast(1),
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                val sp = 48f
                val dc = if (isLightBg) android.graphics.Color.argb(8, 0, 0, 0) else android.graphics.Color.argb(6, 255, 255, 255)
                val ac = if (isLightBg) android.graphics.Color.argb(12, colors.accent.red.toInt(), colors.accent.green.toInt(), colors.accent.blue.toInt()) else android.graphics.Color.argb(10, 255, 213, 79)
                val paint = android.graphics.Paint().apply { isAntiAlias = true }
                var x = 0f
                while (x < bitmap.width) {
                    var y = 0f
                    while (y < bitmap.height) {
                        val isAccent = ((x / sp).toInt() + (y / sp).toInt()) % 7 == 0
                        paint.color = if (isAccent) ac else dc
                        canvas.drawCircle(x, y, if (isAccent) 1.8f else 1f, paint)
                        y += sp
                    }
                    x += sp
                }
                val imageBitmap = bitmap.asImageBitmap()
                onDrawBehind {
                    drawImage(imageBitmap)
                }
            }) {}
        }

        // 内容层
        Column(modifier = Modifier.systemBarsPadding()) {
            Box(modifier = Modifier.weight(1f)) {
                AppContent(currentRoute, vm) { currentRoute = it }
            }

            if (showBottomBar) {
                HorizontalDivider(color = colors.divider, thickness = 1.dp)
                NavigationBar(
                    containerColor = colors.navBarBg,
                    contentColor = colors.textPrimary,
                    tonalElevation = 0.dp
                ) {
                    for (item in NAV_ITEMS) {
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentRoute = item.route },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.textPrimary,
                                selectedTextColor = colors.textPrimary,
                                unselectedIconColor = colors.unselected,
                                unselectedTextColor = colors.unselected,
                                indicatorColor = colors.indicator
                            )
                        )
                    }
                }
            }
        }

        // 浮动文字层（最顶层，覆盖一切）
        FloatingTextContainer(
            triggerCount = state.triggerCount,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AppContent(
    currentRoute: String,
    vm: GongDeViewModel,
    onNavigate: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                    fadeOut(animationSpec = tween(150))
            },
            label = "page_transition"
        ) { route ->
            when (route) {
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
}

@Composable
fun AsmrRoute(vm: GongDeViewModel, onBack: () -> Unit) {
    val state = vm.uiState
    val context = LocalContext.current
    val hapticEngine = remember { HapticEngine(context) }
    DisposableEffect(Unit) {
        onDispose { hapticEngine.release() }
    }
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

@Composable
fun HomeScreen(vm: GongDeViewModel, onNavigate: (String) -> Unit) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    val context = LocalContext.current
    val hapticEngine = remember { HapticEngine(context) }
    DisposableEffect(Unit) {
        onDispose { hapticEngine.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 信息区（顶部）：功德计数
        MeritCounter(totalCount = state.totalCount, todayCount = state.todayCount)

        // 功能按钮（紧挨计数区）
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeButton("专注", compact = true, modifier = Modifier.weight(1f), icon = Icons.Rounded.PlayArrow) { onNavigate(Screen.Focus.route) }
            Spacer(Modifier.width(10.dp))
            HomeButton("归零", compact = true, modifier = Modifier.weight(1f), icon = Icons.Rounded.Refresh) { vm.showDialog(true) }
            Spacer(Modifier.width(10.dp))
            HomeButton("ASMR", compact = true, modifier = Modifier.weight(1f), icon = Icons.Rounded.MusicNote) { onNavigate(Screen.Asmr.route) }
        }

        Spacer(Modifier.weight(2f))

        // 操作区：键盘中心位于下半部分中心
        MechanicalButton(
            modifier = Modifier.size(220.dp, 250.dp),
            soundEngine = vm.soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = state.hapticEnabled,
            switchType = state.switchType,
            asmrMode = false,
            onPressed = { vm.incrementMerit() }
        )
        Spacer(Modifier.height(2.dp))
        Text("解压键盘", color = colors.textMuted, fontSize = 15.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(20.dp))
    }

    if (state.showResetDialog) {
        BackHandler { vm.showDialog(false) }
        AlertDialog(
            onDismissRequest = { vm.showDialog(false) },
            containerColor = colors.dialogBg,
            titleContentColor = colors.textPrimary,
            textContentColor = colors.textSecondary,
            title = { Text("确认归零", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary) },
            text = { Text("累计功德和今日功德都将归零，确定吗？\n成就和历史记录将保留。", fontSize = 15.sp, color = colors.textSecondary) },
            confirmButton = { TextButton(onClick = { vm.resetMerit() }) { Text("确定", color = colors.accent, fontSize = 15.sp, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { vm.showDialog(false) }) { Text("取消", color = colors.textSecondary, fontSize = 15.sp) } }
        )
    }
}

@Composable
fun AchievementsScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AchievementScreen(store = vm.achievementStore, weekTotal = state.weekTotal, monthTotal = state.monthTotal)

        // 板块：分享
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBg)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("分享", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.accent.copy(alpha = 0.1f))
                    .padding(vertical = 8.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
            ShareButton(todayCount = state.todayCount)
        }

        // 板块：功德日历
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBg)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            TimelineScreen(
                entries = state.recentDays,
                weekTotal = state.weekTotal,
                monthTotal = state.monthTotal
            )
        }
    }
}

@Composable
fun HomeButton(text: String, compact: Boolean = false, modifier: Modifier = Modifier, icon: ImageVector? = null, onClick: () -> Unit) {
    val colors = GongDeThemeExt.colors
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    OutlinedButton(
        onClick = {
            scope.launch {
                scale.animateTo(0.92f, tween(60, easing = FastOutSlowInEasing))
                scale.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
            }
            onClick()
        },
        modifier = modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        shape = RoundedCornerShape(if (compact) 14.dp else 20.dp),
        border = BorderStroke(1.dp, colors.cardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surfaceOverlay,
            contentColor = colors.textPrimary
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 12.dp else 28.dp,
            vertical = if (compact) 8.dp else 14.dp
        )
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 15.sp)
    }
}
