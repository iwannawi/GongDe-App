package com.gongde.app

import android.os.Bundle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gongde.app.ui.*
import com.gongde.app.ui.theme.GongDeTheme
import com.gongde.app.ui.theme.ThemePresets
import com.gongde.app.viewmodel.GongDeViewModel
import com.gongde.app.viewmodel.SettingsAction
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: GongDeViewModel = viewModel()
            val state = vm.uiState

            GongDeTheme(themeId = state.themeId) {
                GongDeApp(vm)
            }
        }
    }
}

private enum class NavTab(val label: String, val icon: String) {
    HOME("主页", "⌨"), ACHIEVE("成就", "🏆"), SETTINGS("设置", "⚙")
}

@Composable
private fun GongDeApp(vm: GongDeViewModel) {
    val state = vm.uiState
    var currentTab by rememberSaveable { mutableStateOf(NavTab.HOME) }
    var showMeditation by rememberSaveable { mutableStateOf(false) }
    var showAsmr by rememberSaveable { mutableStateOf(false) }

    // 全屏覆盖
    if (showMeditation) {
        MeditationScreen(store = vm.uiState.let { _ ->
            // MeditationScreen needs direct store access for increment
            com.gongde.app.data.MeritStore(LocalContext.current)
        }, onBack = { showMeditation = false })
        return
    }
    if (showAsmr) {
        AsmrScreen(
            store = com.gongde.app.data.MeritStore(LocalContext.current),
            soundEngine = vm.soundEngine,
            hapticEngine = HapticEngine(LocalContext.current),
            hapticEnabled = state.hapticEnabled,
            onMeritGain = { vm.incrementMerit() },
            onBack = { showAsmr = false }
        )
        return
    }

    val bgColors = ThemePresets.getGradient(state.themeId)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colorStops = bgColors.mapIndexed { i, c ->
                (i.toFloat() / (bgColors.size - 1)) to c
            }.toTypedArray()))
    ) {
        key(bgColors) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSubtleMandala(size.width / 2f, size.height * 0.35f)
                drawTechDots()
            }
        }

        FloatingTextContainer(
            triggerCount = state.triggerCount,
            modifier = Modifier.fillMaxSize().padding(top = 80.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)
        ) {
            when (currentTab) {
                NavTab.HOME -> HomeContent(
                    onMeritGain = { vm.incrementMerit() },
                    totalCount = state.totalCount,
                    todayCount = state.todayCount,
                    showResetDialog = state.showResetDialog,
                    onDismissReset = { vm.showDialog(false) },
                    onConfirmReset = { vm.resetMerit() },
                    onShowReset = { vm.showDialog(true) },
                    onShowMeditation = { showMeditation = true },
                    onShowAsmr = { showAsmr = true },
                    soundEngine = vm.soundEngine,
                    hapticEngine = HapticEngine(LocalContext.current),
                    hapticEnabled = state.hapticEnabled,
                    switchType = state.switchType,
                    asmrMode = state.asmrEnabled
                )
                NavTab.ACHIEVE -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 48.dp)
                ) {
                    AchievementScreen(store = vm.achievementStore, historyStore = vm.historyStore)
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ShareCardView(totalCount = state.totalCount)
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        ShareButton(totalCount = state.totalCount)
                    }
                    Spacer(Modifier.height(16.dp))
                    TimelineScreen(historyStore = vm.historyStore)
                }
                NavTab.SETTINGS -> SettingsScreen(
                    hapticEnabled = state.hapticEnabled,
                    switchType = vm.uiState.let { com.gongde.app.data.MeritStore(LocalContext.current).switchType },
                    themeId = state.themeId,
                    onSettingsAction = { vm.handleSettings(it) }
                )
            }
        }

        key("navbar") {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ==================== HomeContent / BottomNavBar / HomeButton ====================

@Composable
private fun HomeContent(
    onMeritGain: () -> Unit, totalCount: Int, todayCount: Int,
    showResetDialog: Boolean, onDismissReset: () -> Unit, onConfirmReset: () -> Unit,
    onShowReset: () -> Unit, onShowMeditation: () -> Unit, onShowAsmr: () -> Unit,
    soundEngine: SoundEngine, hapticEngine: HapticEngine,
    hapticEnabled: Boolean, switchType: SwitchType, asmrMode: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.55f))
        MechanicalButton(
            modifier = Modifier.size(260.dp, 300.dp),
            soundEngine = soundEngine, hapticEngine = hapticEngine,
            hapticEnabled = hapticEnabled, switchType = switchType,
            asmrMode = asmrMode, onPressed = onMeritGain
        )
        Spacer(Modifier.height(8.dp))
        Text("解压键盘", color = Color(0x55FFD54F), fontSize = 13.sp, letterSpacing = 3.sp)
        Spacer(Modifier.weight(0.15f))
        MeritCounter(totalCount = totalCount, todayCount = todayCount)
        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HomeButton("🧘 冥想") { onShowMeditation() }
            HomeButton("清零") { onShowReset() }
            HomeButton("🎧 ASMR") { onShowAsmr() }
        }
    }
    if (showResetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissReset,
            containerColor = Color(0xFF1A1A2E),
            titleContentColor = Color(0xFFFFD54F),
            textContentColor = Color(0xFFB0BEC5),
            title = { Text("确认清零") },
            text = { Text("累计功德和今日功德都将归零，确定吗？") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = onConfirmReset) { Text("确定", color = Color(0xFFFFD54F)) } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = onDismissReset) { Text("取消", color = Color(0xFFB0BEC5)) } }
        )
    }
}

@Composable
private fun BottomNavBar(currentTab: NavTab, onTabSelected: (NavTab) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().background(Color(0xDD0A0A1A)).padding(vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            NavTab.entries.forEach { tab ->
                val isSelected = tab == currentTab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { contentDescription = tab.label }
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(tab.icon, fontSize = 24.sp, modifier = Modifier.padding(bottom = 2.dp))
                    Text(tab.label, fontSize = 12.sp,
                        color = if (isSelected) Color(0xFFFFD54F) else Color(0x66B0BEC5),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun HomeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(20.dp))
            .background(Color(0x08FFFFFF), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color(0x40B0BEC5), fontSize = 13.sp) }
}

// ==================== 背景装饰 ====================

private fun DrawScope.drawSubtleMandala(cx: Float, cy: Float) {
    val lines = 12; val radius = size.width * 0.45f; val sc = Color(0x08FFFFFF); val sw = 0.8f
    for (i in 0 until lines) { val a = (2.0 * Math.PI * i / lines).toFloat(); drawLine(sc, Offset(cx, cy), Offset(cx + cos(a) * radius, cy + sin(a) * radius), sw) }
    for (r in listOf(0.15f, 0.25f, 0.35f, 0.45f)) { drawCircle(sc, radius * r, Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(sw)) }
    for (i in 0 until lines) { val a = (2.0 * Math.PI * i / lines).toFloat(); drawCircle(Color(0x0CFFD54F), 3f, Offset(cx + cos(a) * radius * 0.25f, cy + sin(a) * radius * 0.25f)) }
}

private fun DrawScope.drawTechDots() {
    val sp = 48f; val dc = Color(0x06FFFFFF); val ac = Color(0x0AFFD54F)
    var x = 0f; while (x < size.width) { var y = 0f; while (y < size.height) { drawCircle(if (((x / sp).toInt() + (y / sp).toInt()) % 7 == 0) ac else dc, if (((x / sp).toInt() + (y / sp).toInt()) % 7 == 0) 1.8f else 1f, Offset(x, y)); y += sp }; x += sp }
}
