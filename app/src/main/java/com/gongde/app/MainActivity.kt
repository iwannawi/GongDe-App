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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.R
import com.gongde.app.navigation.Screen
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.GongDeThemeExt
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
    NavItem("解压", Icons.Rounded.Workspaces, Screen.Home.route),
    NavItem("图鉴", Icons.Rounded.Inventory2, Screen.Collection.route),
    NavItem("记录", Icons.Rounded.BarChart, Screen.Records.route),
    NavItem("设置", Icons.Outlined.Settings, Screen.Settings.route)
)

private data class EmotionCard(
    val title: String,
    val prompt: String,
    val event: String
)

private val EMOTION_CARDS = listOf(
    EmotionCard("会议后遗症", "把没说出口的话敲散一点", "暴击：连击 10 次后压力释放翻倍"),
    EmotionCard("作业堆叠", "先完成一轮，再继续写", "稳住节奏：不断连击会点亮压力槽"),
    EmotionCard("通勤暴击", "把路上的烦躁留在键帽上", "情绪弹幕：飘字更密集"),
    EmotionCard("Deadline 压顶", "先释放 30 秒，再处理正事", "短局挑战：30 秒内看最高连击")
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
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Collection.route,
        Screen.Records.route,
        Screen.Settings.route
    )
    // Background, content, and floating text layers
    Box(modifier = Modifier.fillMaxSize()) {
        ConceptBackground(bgColors)

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
                Screen.Collection.route -> CollectionScreen(vm)
                Screen.Records.route, Screen.Achievements.route -> RecordsScreen(vm)
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
@Suppress("UNUSED_PARAMETER")
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

    var emotionIndex by rememberSaveable { mutableIntStateOf(state.emotionCardIndex) }
    var roundRunning by rememberSaveable { mutableStateOf(false) }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(30) }
    var roundPressure by rememberSaveable { mutableIntStateOf(0) }
    var combo by rememberSaveable { mutableIntStateOf(0) }
    var bestCombo by rememberSaveable { mutableIntStateOf(0) }
    var showRoundResult by rememberSaveable { mutableStateOf(false) }
    var lastPressAt by remember { mutableLongStateOf(0L) }

    val emotionCard = EMOTION_CARDS[emotionIndex % EMOTION_CARDS.size]
    val todayPressureProgress = (state.todayCount.toFloat() / state.todayGoal).coerceIn(0f, 1f)
    val roundProgress = (roundPressure.toFloat() / 60f).coerceIn(0f, 1f)
    val statusText = if (roundRunning) {
        "连击 $combo · 剩余 ${remainingSeconds}s"
    } else {
        "抽一张情绪签，开始一轮"
    }
    val hintTransition = rememberInfiniteTransition(label = "first_press_hint")
    val hintPulse by hintTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100), repeatMode = RepeatMode.Reverse),
        label = "hint_pulse"
    )
    val startRound = {
        roundRunning = true
        remainingSeconds = 30
        roundPressure = 0
        combo = 0
        bestCombo = 0
        showRoundResult = false
        lastPressAt = 0L
    }

    LaunchedEffect(state.emotionCardIndex) {
        emotionIndex = state.emotionCardIndex
    }

    LaunchedEffect(roundRunning, remainingSeconds) {
        if (roundRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds -= 1
        } else if (roundRunning && remainingSeconds == 0) {
            roundRunning = false
            showRoundResult = true
            vm.completeReliefRound(bestCombo)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        val compactHeight = maxHeight < 720.dp
        val veryCompactHeight = maxHeight < 620.dp
        val keySize = minOf(
            maxWidth * if (compactHeight) 0.46f else 0.52f,
            when {
                veryCompactHeight -> 156.dp
                compactHeight -> 176.dp
                else -> 200.dp
            }
        )
        val hintSize = if (veryCompactHeight) 14.sp else 16.sp
        val sectionGap = if (veryCompactHeight) 8.dp else 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(sectionGap)
        ) {
            Spacer(Modifier.height(if (compactHeight) 2.dp else 4.dp))
            TaskProgressCard(
                todayCount = state.todayCount,
                todayGoal = state.todayGoal,
                completed = state.dailyGoalCompleted,
                progress = todayPressureProgress,
                compact = compactHeight
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                PressureCard(progress = roundProgress, roundPressure = roundPressure, compact = compactHeight, modifier = Modifier.weight(1f))
                ComboCard(combo = combo, bestCombo = bestCombo, compact = compactHeight, modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keySize * 1.05f),
                contentAlignment = Alignment.Center
            ) {

                MechanicalButton(
                    modifier = Modifier
                        .size(width = keySize * 1.18f, height = keySize)
                        .graphicsLayer {
                            val scale = if (state.showFirstPressHint) 1f + hintPulse * 0.012f else 1f
                            scaleX = scale
                            scaleY = scale
                        },
                    soundEngine = vm.soundEngine,
                    hapticEngine = hapticEngine,
                    hapticEnabled = state.hapticEnabled,
                    switchType = state.switchType,
                    onKeycapOriginChanged = onKeycapOriginChanged,
                    onPressed = {
                        if (!roundRunning) startRound()
                        val now = System.currentTimeMillis()
                        combo = if (now - lastPressAt <= 1000L) combo + 1 else 1
                        bestCombo = maxOf(bestCombo, combo)
                        lastPressAt = now
                        val gain = if (combo >= 10 && combo % 10 == 0) 3 else 1
                        roundPressure = (roundPressure + gain).coerceAtMost(60)
                        vm.incrementMerit()
                    }
                )
            }
            Text(
                if (state.showFirstPressHint && !roundRunning) "点击键帽，释放压力" else statusText,
                color = colors.textPrimary,
                fontSize = hintSize,
                fontWeight = FontWeight.Medium
            )
            EmotionTicketCard(
                emotionCard = emotionCard,
                enabled = !roundRunning,
                onDraw = {
                    if (!roundRunning) {
                        val nextIndex = (emotionIndex + 1) % EMOTION_CARDS.size
                        emotionIndex = nextIndex
                        vm.setEmotionCard(nextIndex)
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ChallengeCard(
                    running = roundRunning,
                    remainingSeconds = remainingSeconds,
                    onClick = { startRound() },
                    modifier = Modifier.weight(1f)
                )
                RewardCard(
                    completed = state.dailyGoalCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
            KeycapCollectionPreview(
                totalCount = state.totalCount,
                dailyRewardCompleted = state.dailyGoalCompleted
            )
            Spacer(Modifier.height(if (veryCompactHeight) 8.dp else 10.dp))
        }
    }

    if (showRoundResult) {
        AlertDialog(
            onDismissRequest = { showRoundResult = false },
            containerColor = colors.dialogBg,
            title = { Text("本轮解压完成", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("释放压力：$roundPressure 点", color = colors.textSecondary)
                    Text("最高连击：$bestCombo", color = colors.textSecondary)
                    Text("今日奖励进度：${state.todayCount} / ${state.todayGoal}", color = colors.textMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoundResult = false; startRound() }) {
                    Text("再来一轮", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoundResult = false }) {
                    Text("收下奖励", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun ConceptBackground(bgColors: List<Color>) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.46f), size.width * 0.46f, Offset(size.width * 0.12f, size.height * 0.08f))
            drawCircle(Color(0x1A8D9AA6), size.width * 0.42f, Offset(size.width * 0.92f, size.height * 0.88f))
            drawCircle(Color(0x10D83A31), size.width * 0.22f, Offset(size.width * 0.62f, size.height * 0.45f))
            repeat(7) { index ->
                val y = size.height * (0.16f + index * 0.105f)
                drawLine(
                    color = Color.White.copy(alpha = 0.23f),
                    start = Offset(size.width * 0.08f, y),
                    end = Offset(size.width * 0.92f, y + size.height * 0.018f),
                    strokeWidth = 1f
                )
            }
        }
    }
}

@Composable
private fun ConceptCard(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = GongDeThemeExt.colors
    val radius = if (compact) 16.dp else 18.dp
    Column(
        modifier = modifier
            .shadow(7.dp, RoundedCornerShape(radius), ambientColor = Color(0x1415191D), spotColor = Color(0x2015191D))
            .clip(RoundedCornerShape(radius))
            .background(colors.cardBg)
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(radius))
            .padding(if (compact) 10.dp else 12.dp),
        content = content
    )
}

@Composable
private fun TaskProgressCard(
    todayCount: Int,
    todayGoal: Int,
    completed: Boolean,
    progress: Float,
    compact: Boolean
) {
    val colors = GongDeThemeExt.colors
    val titleSize = if (compact) 19.sp else 20.sp
    val countSize = if (compact) 42.sp else 50.sp
    val bodySize = if (compact) 14.sp else 15.sp
    ConceptCard(Modifier.fillMaxWidth(), compact = compact) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(23.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accent)
            )
            Spacer(Modifier.width(10.dp))
            Text("今日解压任务", color = colors.textPrimary, fontSize = titleSize, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            if (!compact) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.52f))
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("每日 0 点重置", color = colors.textSecondary, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(if (compact) 8.dp else 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        todayCount.toString(),
                        color = colors.accent,
                        fontSize = countSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = countSize
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("/$todayGoal 次", color = colors.textPrimary, fontSize = if (compact) 19.sp else 22.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (completed) "今日奖励已可领取" else "还差 ${(todayGoal - todayCount).coerceAtLeast(0)} 次，领今日奖励",
                    color = colors.textSecondary,
                    fontSize = bodySize,
                    maxLines = 1
                )
            }
            RewardIcon(completed = completed, modifier = Modifier.size(if (compact) 52.dp else 60.dp))
        }
        Spacer(Modifier.height(if (compact) 8.dp else 10.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = if (completed) Color(0xFF2E8B70) else colors.accent,
            trackColor = colors.barTrack.copy(alpha = 0.55f),
            drawStopIndicator = {}
        )
        if (!compact) {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("0", color = colors.textMuted, fontSize = 12.sp)
                Text("50", color = colors.textMuted, fontSize = 12.sp)
                Text(todayGoal.toString(), color = colors.textMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RewardIcon(completed: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.concept_gift_icon),
        contentDescription = null,
        modifier = modifier.size(72.dp),
        contentScale = ContentScale.Fit
    )
}
@Composable
private fun PressureCard(progress: Float, roundPressure: Int, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 108.dp else 120.dp), compact = compact) {
        Text("压力槽", color = colors.textPrimary, fontSize = if (compact) 18.sp else 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(if (compact) 1.dp else 3.dp))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(if (compact) 76.dp else 84.dp)) {
                drawArc(
                    color = colors.barTrack.copy(alpha = 0.7f),
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx())
                )
                drawArc(
                    color = colors.accent,
                    startAngle = 160f,
                    sweepAngle = 220f * progress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx())
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress * 100).toInt()}%", color = colors.accent, fontSize = if (compact) 24.sp else 26.sp, fontWeight = FontWeight.Bold)
                Text(if (roundPressure < 40) "中等压力" else "高压释放", color = colors.textMuted, fontSize = 12.sp)
            }
        }
        if (!compact) Text("点击释放压力吧", color = colors.textMuted, fontSize = 13.sp)
    }
}

@Composable
private fun ComboCard(combo: Int, bestCombo: Int, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 108.dp else 120.dp), compact = compact) {
        Text("连击", color = colors.textPrimary, fontSize = if (compact) 18.sp else 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(combo.toString(), color = colors.textPrimary, fontSize = if (compact) 36.sp else 42.sp, fontWeight = FontWeight.Bold, lineHeight = 42.sp)
            Spacer(Modifier.width(8.dp))
            Text("连击", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = colors.accent, modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.barTrack.copy(alpha = 0.46f))
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("最高 $bestCombo 连击", color = colors.textMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun EmotionTicketCard(
    emotionCard: EmotionCard,
    enabled: Boolean,
    onDraw: () -> Unit
) {
    val colors = GongDeThemeExt.colors
    ConceptCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.concept_ticket_icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("抽一张情绪签", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("${emotionCard.title} · ${emotionCard.prompt}", color = colors.textSecondary, fontSize = 13.sp, maxLines = 1)
            }
            Button(
                onClick = onDraw,
                enabled = enabled,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("抽一张", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChallengeCard(
    running: Boolean,
    remainingSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(94.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("开始一轮", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(if (running) "剩余 ${remainingSeconds}s" else "30 秒点击挑战", color = colors.textSecondary, fontSize = 13.sp, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 3.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(if (running) Icons.Rounded.RestartAlt else Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(if (running) "重新开始" else "开始挑战", fontSize = 12.sp)
                }
            }
            Image(
                painter = painterResource(R.drawable.concept_clock_icon),
                contentDescription = null,
                modifier = Modifier.size(58.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Composable
private fun RewardCard(completed: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(94.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("今日奖励", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFD83A31)))
                }
                Spacer(Modifier.height(6.dp))
                Text(if (completed) "红晶碎片 x 20" else "完成任务可领取", color = colors.textSecondary, fontSize = 13.sp)
            }
            Image(
                painter = painterResource(R.drawable.concept_chest_icon),
                contentDescription = null,
                modifier = Modifier.size(62.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Composable
private fun KeycapCollectionPreview(totalCount: Int, dailyRewardCompleted: Boolean) {
    val colors = GongDeThemeExt.colors
    val assets = listOf(
        R.drawable.concept_collection_red_key,
        R.drawable.concept_collection_clear_switch,
        R.drawable.concept_collection_panda_key,
        R.drawable.concept_collection_black_key,
        R.drawable.concept_collection_pink_key,
        R.drawable.concept_collection_lock_key
    )
    ConceptCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("键帽收藏", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("${(totalCount / 40).coerceAtMost(12)} / 36", color = colors.textMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            assets.forEachIndexed { index, resId ->
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (index == 0) colors.accent.copy(alpha = 0.08f) else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (index == 0) colors.accent else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
@Composable
private fun InfoPill(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    Column(
        modifier = modifier
            .background(colors.cardBg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(label, color = colors.textMuted, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecordsScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("今日复盘")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InfoPill("完成轮数", state.roundsToday.toString(), Modifier.weight(1f))
                InfoPill("今日最高连击", state.bestComboToday.toString(), Modifier.weight(1f))
            }
            InfoPill("历史最高连击", state.bestComboAllTime.toString(), Modifier.fillMaxWidth())
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("近期记录")
            TimelineScreen(
                entries = state.recentDays,
                weekTotal = state.weekTotal,
                monthTotal = state.monthTotal
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("分享今日")
            ShareButton(
                todayCount = state.todayCount,
                roundsToday = state.roundsToday,
                bestComboToday = state.bestComboToday,
                emotionTitle = EMOTION_CARDS[state.emotionCardIndex % EMOTION_CARDS.size].title,
                onShareStarted = vm::trackShareStarted,
                onShareCompleted = vm::trackShareCompleted
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun CollectionScreen(vm: GongDeViewModel) {
    val state = vm.uiState
    val colors = GongDeThemeExt.colors
    val emotionCards = remember {
        listOf(
            "会议后遗症" to "今天适合把压力敲碎一点",
            "作业堆叠" to "先敲一轮，再继续写",
            "通勤暴击" to "把路上的烦躁留在键帽上"
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("情绪签图鉴")
            emotionCards.forEachIndexed { index, card ->
                CollectionRow(
                    title = card.first,
                    subtitle = card.second,
                    value = if (index == 0) "已解锁" else "进行中"
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("键帽收藏")
            CollectionRow("红色本命键帽", "概念图同款透明红键帽", "已拥有")
            CollectionRow("青轴碎片", "通过连击和每日奖励获得", "${(state.totalCount / 50).coerceAtMost(20)} / 20")
            CollectionRow("今日限定贴纸", "完成今日解压任务后解锁", if (state.dailyGoalCompleted) "已解锁" else "未解锁")
        }

        AchievementScreen(
            achievements = vm.achievementStore.allAchievements,
            completedAchievementIds = state.completedAchievementIds,
            totalCount = state.totalCount,
            todayCount = state.todayCount,
            streak = state.streak
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CollectionRow(title: String, subtitle: String, value: String) {
    val colors = GongDeThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.cardBg, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = colors.textMuted, fontSize = 13.sp)
        }
        Text(value, color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}








