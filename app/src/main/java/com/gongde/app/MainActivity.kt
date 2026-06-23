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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.InsertChartOutlined
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.navigation.Screen
import com.gongde.app.data.AchievementMetric
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.GongDeThemeExt
import com.gongde.app.ui.theme.ThemePresets
import com.gongde.app.viewmodel.GongDeViewModel
import com.gongde.app.viewmodel.SettingsAction
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
    NavItem("首页", Icons.Outlined.Home, Screen.Home.route),
    NavItem("记录", Icons.Outlined.InsertChartOutlined, Screen.Achievements.route),
    NavItem("设置", Icons.Outlined.Settings, Screen.Settings.route)
)

@Composable
fun GongDeApp(vm: GongDeViewModel) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }
    var keycapOrigin by remember { mutableStateOf<Offset?>(null) }
    val context = LocalContext.current

    LaunchedEffect(state.toastEvents) {
        val events = vm.consumeToastEvents()
        events.forEach { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler(enabled = currentRoute != Screen.Home.route) {
        currentRoute = Screen.Home.route
    }

    val bgColors = colors.bgGradient
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Achievements.route, Screen.Settings.route)
    // Background, content, and floating text layers
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.home_paper_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        if (state.themeId != "morning_mist") {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(bgColors.map { it.copy(alpha = 0.42f) }))
            )
        }

        // Content layer
        Column(modifier = Modifier.systemBarsPadding()) {
            Box(modifier = Modifier.weight(1f)) {
                AppContent(
                    currentRoute = currentRoute,
                    vm = vm,
                    onNavigate = { currentRoute = it },
                    onKeycapOriginChanged = { keycapOrigin = it }
                )
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
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.accent,
                                selectedTextColor = colors.accent,
                                unselectedIconColor = colors.unselected,
                                unselectedTextColor = colors.unselected,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        // Floating text layer
        FloatingTextContainer(
            triggerCount = state.triggerCount,
            origin = keycapOrigin,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AppContent(
    currentRoute: String,
    vm: GongDeViewModel,
    onNavigate: (String) -> Unit,
    onKeycapOriginChanged: (Offset) -> Unit
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
                Screen.Asmr.route -> AsmrRoute(
                    vm = vm,
                    onBack = { onNavigate(Screen.Home.route) },
                    onKeycapOriginChanged = onKeycapOriginChanged
                )
                Screen.Achievements.route -> AchievementsScreen(vm)
                Screen.Settings.route -> SettingsScreen(
                    hapticEnabled = vm.uiState.hapticEnabled,
                    switchType = vm.uiState.switchType.name.lowercase(),
                    themeId = vm.uiState.themeId,
                    onSettingsAction = { vm.handleSettings(it) },
                    onReset = vm::resetMerit
                )
                else -> HomeScreen(vm, onNavigate, onKeycapOriginChanged)
            }
        }
    }
}

@Composable
fun AsmrRoute(
    vm: GongDeViewModel,
    onBack: () -> Unit,
    onKeycapOriginChanged: (Offset) -> Unit
) {
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
        onKeycapOriginChanged = onKeycapOriginChanged,
        onMeritGain = { vm.incrementMerit() },
        onBack = onBack
    )
}

@Composable
fun HomeScreen(
    vm: GongDeViewModel,
    onNavigate: (String) -> Unit,
    onKeycapOriginChanged: (Offset) -> Unit
) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    val context = LocalContext.current
    val hapticEngine = remember { HapticEngine(context) }
    DisposableEffect(Unit) {
        onDispose { hapticEngine.release() }
    }

    val nextAchievement = vm.achievementStore.allAchievements
        .filter { it.metric == AchievementMetric.TOTAL && it.target > state.totalCount }
        .minByOrNull { it.target }
    val nextMessage = nextAchievement?.let {
        "距离${it.name}还差 ${it.target - state.totalCount} 次"
    } ?: "累计成就已全部完成"
    val progress = (state.todayCount.toFloat() / state.todayGoal).coerceIn(0f, 1f)
    val hintTransition = rememberInfiniteTransition(label = "first_press_hint")
    val hintPulse by hintTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), repeatMode = RepeatMode.Reverse),
        label = "hint_pulse"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        val compactHeight = maxHeight < 720.dp
        val veryCompactHeight = maxHeight < 620.dp
        val keySize = minOf(
            maxWidth * if (compactHeight) 0.78f else 0.82f,
            when {
                veryCompactHeight -> 252.dp
                compactHeight -> 292.dp
                else -> 320.dp
            }
        )
        val titleSize = if (veryCompactHeight) 15.sp else 17.sp
        val countSize = if (veryCompactHeight) 32.sp else 38.sp
        val hintSize = if (veryCompactHeight) 16.sp else 20.sp
        val modeSpacing = if (veryCompactHeight) 0.dp else 2.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(if (compactHeight) 8.dp else 12.dp))
            Column(Modifier.fillMaxWidth()) {
                Text("今日功德", color = colors.textPrimary, fontSize = titleSize, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${state.todayCount}", color = colors.textPrimary, fontSize = countSize, fontWeight = FontWeight.Bold)
                    Text(" / ${state.todayGoal}", color = colors.textMuted, fontSize = 17.sp, modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(Modifier.height(if (veryCompactHeight) 8.dp else 10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (state.dailyGoalCompleted) Color(0xFF2E8B70) else colors.accent,
                    trackColor = colors.barTrack,
                    drawStopIndicator = {}
                )
                Spacer(Modifier.height(if (veryCompactHeight) 8.dp else 10.dp))
                Text(nextMessage, color = colors.textMuted, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.size(keySize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0x244A5057), Color.Transparent)
                        ),
                        topLeft = Offset(size.width * 0.23f, size.height * 0.735f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.54f, size.height * 0.07f)
                    )
                    drawOval(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color(0x144A5057), Color.Transparent)
                        ),
                        topLeft = Offset(size.width * 0.18f, size.height * 0.765f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.055f)
                    )
                }
                MechanicalButton(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = if (state.showFirstPressHint) 1f + hintPulse * 0.012f else 1f
                            scaleX = scale
                            scaleY = scale
                        },
                    soundEngine = vm.soundEngine,
                    hapticEngine = hapticEngine,
                    hapticEnabled = state.hapticEnabled,
                    switchType = state.switchType,
                    asmrMode = false,
                    onKeycapOriginChanged = onKeycapOriginChanged,
                    onPressed = { vm.incrementMerit() }
                )
            }
            Spacer(Modifier.height(if (veryCompactHeight) 4.dp else 8.dp))
            Text(
                if (state.showFirstPressHint) "按一下，放松一秒" else "轻敲键帽，让压力停一下",
                color = colors.textPrimary,
                fontSize = hintSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(if (veryCompactHeight) 10.dp else 18.dp))
            Column(
                modifier = Modifier.width(160.dp),
                verticalArrangement = Arrangement.spacedBy(modeSpacing)
            ) {
                ModeButton("专注", Icons.Outlined.MyLocation, Modifier.fillMaxWidth()) {
                    vm.trackModeOpen("focus")
                    onNavigate(Screen.Focus.route)
                }
                ModeButton("ASMR", Icons.Outlined.Waves, Modifier.fillMaxWidth()) {
                    vm.trackModeOpen("asmr")
                    onNavigate(Screen.Asmr.route)
                }
            }
            Spacer(Modifier.height(if (veryCompactHeight) 4.dp else 8.dp))
        }
    }
}

@Composable
fun AchievementsScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("近期记录")
            TimelineScreen(
                entries = state.recentDays,
                weekTotal = state.weekTotal,
                monthTotal = state.monthTotal
            )
        }

        AchievementScreen(
            achievements = vm.achievementStore.allAchievements,
            completedAchievementIds = state.completedAchievementIds,
            totalCount = state.totalCount,
            todayCount = state.todayCount,
            streak = state.streak
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("分享今日")
            ShareButton(
                todayCount = state.todayCount,
                onShareStarted = vm::trackShareStarted,
                onShareCompleted = vm::trackShareCompleted
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ModeButton(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = GongDeThemeExt.colors
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    TextButton(
        onClick = {
            scope.launch {
                scale.animateTo(0.92f, tween(60, easing = FastOutSlowInEasing))
                scale.animateTo(1f, tween(120, easing = FastOutSlowInEasing))
            }
            onClick()
        },
        modifier = modifier.graphicsLayer(scaleX = scale.value, scaleY = scale.value),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textSecondary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}
