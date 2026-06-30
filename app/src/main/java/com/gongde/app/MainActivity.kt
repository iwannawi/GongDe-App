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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    val iconRes: Int,
    val route: String
)

private val NAV_ITEMS = listOf(
    NavItem("解压", R.drawable.concept_nav_home_mask, Screen.Home.route),
    NavItem("图鉴", R.drawable.concept_nav_cube_mask, Screen.Collection.route),
    NavItem("记录", R.drawable.concept_nav_bars_mask, Screen.Records.route),
    NavItem("设置", R.drawable.concept_nav_gear_mask, Screen.Settings.route)
)

private data class EmotionCard(
    val title: String,
    val prompt: String,
    val event: String
)

private data class KeycapGalleryItem(
    val title: String,
    val subtitle: String,
    val asset: Int,
    val status: String,
    val unlocked: Boolean
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
                                Image(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = item.label,
                                    colorFilter = ColorFilter.tint(if (selected) colors.accent else colors.unselected),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .semantics { contentDescription = item.label }
                                )
                            },
                            label = { Text(item.label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
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
        val compactHeight = maxHeight < 900.dp
        val veryCompactHeight = maxHeight < 700.dp
        val keySize = minOf(
            maxWidth * if (compactHeight) 0.38f else 0.44f,
            when {
                veryCompactHeight -> 132.dp
                compactHeight -> 150.dp
                else -> 172.dp
            }
        )
        val hintSize = if (veryCompactHeight) 14.sp else 16.sp
        val sectionGap = if (veryCompactHeight) 6.dp else 7.dp

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
                    .height(keySize * 0.98f),
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
            HandHintIcon(
                modifier = Modifier.size(if (compactHeight) 20.dp else 24.dp),
                color = colors.textMuted
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
                    compact = compactHeight,
                    modifier = Modifier.weight(1f)
                )
                RewardCard(
                    completed = state.dailyGoalCompleted,
                    compact = compactHeight,
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
            drawCircle(Color(0x128FA0AA), size.width * 0.2f, Offset(size.width * 0.64f, size.height * 0.46f))
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
private fun HandHintIcon(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.46f, h * 0.18f), Offset(w * 0.46f, h * 0.66f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.58f, h * 0.32f), Offset(w * 0.58f, h * 0.67f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.69f, h * 0.42f), Offset(w * 0.69f, h * 0.7f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.34f, h * 0.48f), Offset(w * 0.46f, h * 0.64f), stroke.width, StrokeCap.Round)
        val palm = Path().apply {
            moveTo(w * 0.34f, h * 0.48f)
            quadraticTo(w * 0.18f, h * 0.52f, w * 0.28f, h * 0.72f)
            quadraticTo(w * 0.38f, h * 0.9f, w * 0.61f, h * 0.88f)
            quadraticTo(w * 0.78f, h * 0.86f, w * 0.74f, h * 0.68f)
        }
        drawPath(palm, color, style = stroke)
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
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.52f))
                    .padding(horizontal = if (compact) 8.dp else 9.dp, vertical = if (compact) 4.dp else 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("每日 0 点重置", color = colors.textSecondary, fontSize = 12.sp)
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
        Spacer(Modifier.height(if (compact) 2.dp else 4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", color = colors.textMuted, fontSize = 12.sp)
            Text("50", color = colors.textMuted, fontSize = 12.sp)
            Text(todayGoal.toString(), color = colors.textMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun RewardIcon(completed: Boolean, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.concept_gift_icon),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
@Composable
private fun PressureCard(progress: Float, roundPressure: Int, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 116.dp else 120.dp), compact = compact) {
        Text("压力槽", color = colors.textPrimary, fontSize = if (compact) 18.sp else 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(if (compact) 1.dp else 3.dp))
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(if (compact) 72.dp else 84.dp)) {
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
                Text("${(progress * 100).toInt()}%", color = colors.accent, fontSize = if (compact) 23.sp else 26.sp, fontWeight = FontWeight.Bold)
                Text(if (roundPressure < 40) "中等压力" else "高压释放", color = colors.textMuted, fontSize = 12.sp)
            }
        }
        Text("点击释放压力吧", color = colors.textMuted, fontSize = if (compact) 12.sp else 13.sp)
    }
}

@Composable
private fun ComboCard(combo: Int, bestCombo: Int, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 116.dp else 120.dp), compact = compact) {
        Text("连击", color = colors.textPrimary, fontSize = if (compact) 18.sp else 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(if (compact) 2.dp else 6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                combo.toString(),
                color = colors.textPrimary,
                fontSize = if (compact) 34.sp else 42.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compact) 34.sp else 42.sp
            )
            Spacer(Modifier.width(8.dp))
            Text("连击", color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = colors.accent, modifier = Modifier.size(if (compact) 21.dp else 23.dp))
        }
        Spacer(Modifier.height(if (compact) 4.dp else 0.dp).weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.barTrack.copy(alpha = 0.46f))
                .padding(vertical = if (compact) 4.dp else 5.dp),
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
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 84.dp else 94.dp), compact = compact) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("开始一轮", color = colors.textPrimary, fontSize = if (compact) 17.sp else 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(if (compact) 2.dp else 3.dp))
                Text(if (running) "剩余 ${remainingSeconds}s" else "30 秒点击挑战", color = colors.textSecondary, fontSize = 13.sp, maxLines = 1)
                Spacer(Modifier.height(if (compact) 3.dp else 4.dp))
                Row(
                    modifier = Modifier
                        .height(if (compact) 28.dp else 30.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.58f))
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (running) Icons.Rounded.RestartAlt else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (running) "重新开始" else "开始挑战",
                        color = colors.accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }
            Image(
                painter = painterResource(R.drawable.concept_clock_icon),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 50.dp else 58.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Composable
private fun RewardCard(completed: Boolean, compact: Boolean, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(if (compact) 84.dp else 94.dp), compact = compact) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("今日奖励", color = colors.textPrimary, fontSize = if (compact) 17.sp else 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(Color(0xFFD83A31)))
                }
                Spacer(Modifier.height(if (compact) 4.dp else 6.dp))
                Text(if (completed) "红晶碎片 x 20" else "完成任务可领取", color = colors.textSecondary, fontSize = 13.sp)
            }
            Image(
                painter = painterResource(R.drawable.concept_chest_icon),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 52.dp else 62.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
@Composable
private fun KeycapCollectionPreview(totalCount: Int, dailyRewardCompleted: Boolean) {
    val colors = GongDeThemeExt.colors
    val assets = listOf(
        R.drawable.concept_key_transparent,
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
            Text("${(totalCount / 40).coerceAtMost(12).coerceAtLeast(1)} / 36", color = colors.textMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), verticalAlignment = Alignment.CenterVertically) {
            assets.forEachIndexed { index, resId ->
                Box(
                    Modifier
                        .size(44.dp),
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
    val unlockedCount = (state.totalCount / 40).coerceAtMost(12).coerceAtLeast(1)
    val keycaps = listOf(
        KeycapGalleryItem("红色本命", "概念图同款透明红键帽", R.drawable.concept_key_transparent, "已拥有", true),
        KeycapGalleryItem(
            "清透轴体",
            "完成 20 次连击后点亮",
            R.drawable.concept_collection_clear_switch,
            "${(state.totalCount / 50).coerceAtMost(20)} / 20",
            state.totalCount >= 1000
        ),
        KeycapGalleryItem("松弛熊猫", "连续使用解锁", R.drawable.concept_collection_panda_key, "进行中", state.streak >= 3),
        KeycapGalleryItem("夜间黑键", "高连击挑战奖励", R.drawable.concept_collection_black_key, "未解锁", state.bestComboAllTime >= 60),
        KeycapGalleryItem(
            "今日贴纸",
            "完成今日任务后解锁",
            R.drawable.concept_collection_pink_key,
            if (state.dailyGoalCompleted) "已解锁" else "未解锁",
            state.dailyGoalCompleted
        ),
        KeycapGalleryItem("隐藏键帽", "后续奖励保留位", R.drawable.concept_collection_lock_key, "锁定", false)
    )
    val emotionCards = listOf(
        Triple("会议后遗症", "今天适合把压力敲碎一点", true),
        Triple("作业堆叠", "先敲一轮，再继续写", state.totalCount >= 30),
        Triple("通勤暴击", "把路上的烦躁留在键帽上", state.totalCount >= 60)
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("键帽图鉴")
            KeycapGallerySummary(
                totalCount = state.totalCount,
                unlockedCount = unlockedCount,
                dailyRewardCompleted = state.dailyGoalCompleted
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("键帽收藏")
            keycaps.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { item ->
                        KeycapTile(item = item, modifier = Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeading("情绪签收藏")
            emotionCards.forEach { card ->
                EmotionCollectionCard(
                    title = card.first,
                    subtitle = card.second,
                    unlocked = card.third
                )
            }
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
private fun KeycapGallerySummary(
    totalCount: Int,
    unlockedCount: Int,
    dailyRewardCompleted: Boolean
) {
    val colors = GongDeThemeExt.colors
    ConceptCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.concept_key_transparent),
                contentDescription = null,
                modifier = Modifier.size(74.dp),
                contentScale = ContentScale.Inside
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("红色本命键帽", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text("累计点击和每日奖励会解锁更多键帽", color = colors.textSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (unlockedCount / 36f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = colors.accent,
                    trackColor = colors.barTrack.copy(alpha = 0.55f),
                    drawStopIndicator = {}
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("$unlockedCount / 36", color = colors.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(if (dailyRewardCompleted) "今日已领取" else "今日待领取", color = colors.textMuted, fontSize = 12.sp)
                Spacer(Modifier.height(5.dp))
                Text("$totalCount 次", color = colors.textMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun KeycapTile(item: KeycapGalleryItem, modifier: Modifier = Modifier) {
    val colors = GongDeThemeExt.colors
    ConceptCard(modifier.height(158.dp), compact = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (item.unlocked) colors.accent.copy(alpha = 0.08f) else colors.barTrack.copy(alpha = 0.42f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(item.asset),
                contentDescription = null,
                modifier = Modifier.size(76.dp).graphicsLayer { alpha = if (item.unlocked) 1f else 0.62f },
                contentScale = ContentScale.Inside
            )
        }
        Spacer(Modifier.height(7.dp))
        Text(item.title, color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(item.subtitle, color = colors.textMuted, fontSize = 11.sp, maxLines = 1)
        Spacer(Modifier.weight(1f))
        Text(
            item.status,
            color = if (item.unlocked) colors.accent else colors.textMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmotionCollectionCard(title: String, subtitle: String, unlocked: Boolean) {
    val colors = GongDeThemeExt.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x1015191D), spotColor = Color(0x1815191D))
            .clip(RoundedCornerShape(18.dp))
            .background(colors.cardBg)
            .border(1.dp, Color.White.copy(alpha = 0.82f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.concept_ticket_icon),
            contentDescription = null,
            modifier = Modifier.size(42.dp).graphicsLayer { alpha = if (unlocked) 1f else 0.48f },
            contentScale = ContentScale.Fit
        )
        Column(Modifier.weight(1f)) {
            Text(title, color = colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = colors.textMuted, fontSize = 13.sp)
        }
        Text(if (unlocked) "已解锁" else "进行中", color = colors.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}








