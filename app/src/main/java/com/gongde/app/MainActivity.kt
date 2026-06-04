package com.gongde.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.navigation.Screen
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.GongDeThemeExt
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
                    color = colors.gold,
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = splashQuote,
                    color = colors.gold.copy(alpha = 0.6f),
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
            val dc = android.graphics.Color.argb(6, 255, 255, 255)
            val ac = android.graphics.Color.argb(10, 255, 213, 79)
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
        FloatingTextContainer(
            triggerCount = state.triggerCount,
            modifier = Modifier.fillMaxSize()
        )
    }

    Column(modifier = Modifier.systemBarsPadding()) {
        Box(modifier = Modifier.weight(1f)) {
            AppContent(currentRoute, vm) { currentRoute = it }
        }

        if (showBottomBar) {
            HorizontalDivider(color = colors.divider, thickness = 1.dp)
            NavigationBar(
                containerColor = colors.navBarBg,
                contentColor = colors.gold,
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
                            selectedIconColor = colors.gold,
                            selectedTextColor = colors.gold,
                            unselectedIconColor = colors.unselected,
                            unselectedTextColor = colors.unselected,
                            indicatorColor = colors.indicator
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AppContent(
    currentRoute: String,
    vm: GongDeViewModel,
    onNavigate: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeButton("🧘 专注", compact = true) { onNavigate(Screen.Focus.route) }
            Spacer(Modifier.width(10.dp))
            HomeButton("清零", compact = true) { vm.showDialog(true) }
            Spacer(Modifier.width(10.dp))
            HomeButton("🎧 ASMR", compact = true) { onNavigate(Screen.Asmr.route) }
        }

        Spacer(Modifier.height(16.dp))

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
        Text("解压键盘", color = colors.gold.copy(alpha = 0.5f), fontSize = 13.sp, letterSpacing = 3.sp)
        Spacer(Modifier.weight(1f))
        MeritCounter(totalCount = state.totalCount, todayCount = state.todayCount)
        Spacer(Modifier.height(16.dp))
    }

    if (state.showResetDialog) {
        BackHandler { vm.showDialog(false) }
        AlertDialog(
            onDismissRequest = { vm.showDialog(false) },
            containerColor = colors.dialogBg,
            titleContentColor = colors.gold,
            textContentColor = colors.textSecondary,
            title = { Text("确认清零") },
            text = { Text("累计功德和今日功德都将归零，确定吗？\n成就和历史记录将保留。") },
            confirmButton = { TextButton(onClick = { vm.resetMerit() }) { Text("确定", color = colors.gold) } },
            dismissButton = { TextButton(onClick = { vm.showDialog(false) }) { Text("取消", color = colors.textSecondary) } }
        )
    }
}

@Composable
fun AchievementsScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    val cardGrad = listOf(colors.bgGradient.getOrElse(0) { Color(0xFF1A0033) }, colors.bgGradient.getOrElse(2) { Color(0xFF2D1055) })

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
        TimelineScreen(
            entries = state.recentDays,
            weekTotal = state.weekTotal,
            monthTotal = state.monthTotal
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun HomeButton(text: String, compact: Boolean = false, onClick: () -> Unit) {
    val colors = GongDeThemeExt.colors
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 14.dp else 20.dp),
        border = BorderStroke(1.dp, colors.cardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.surfaceOverlay,
            contentColor = colors.unselected
        ),
        contentPadding = PaddingValues(
            horizontal = if (compact) 16.dp else 28.dp,
            vertical = if (compact) 8.dp else 14.dp
        )
    ) {
        Text(text, fontSize = if (compact) 12.sp else 14.sp)
    }
}
