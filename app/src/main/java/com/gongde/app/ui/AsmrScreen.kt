/**
 * ASMR 沉浸式模式
 *
 * 全屏 ASMR 增强体验界面，包含：
 * - 大尺寸 MechanicalButton（ASMR 音效增强版）
 * - 白噪音（雨声）开关控制
 * - 实时功德计数展示
 *
 * 使用 SoundEngine 的 ASMR 增强音效，配合环境雨声营造沉浸体验。
 */

package com.gongde.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.data.MeritStore
import com.gongde.app.ui.theme.CardBgColor
import com.gongde.app.ui.theme.CardBorderColor
import com.gongde.app.ui.theme.GoldColor
import com.gongde.app.ui.theme.GongDeThemeExt
import com.gongde.app.ui.theme.MutedGrayColor

// 本地专用颜色
private val BgDark = Color(0xFF0D0D1A)

/**
 * ASMR 模式主界面
 *
 * @param store 功德数据存储
 * @param soundEngine 声音引擎（复用 GongDeApp 实例）
 * @param hapticEngine 触觉引擎（复用 GongDeApp 实例）
 * @param hapticEnabled 触觉反馈开关
 * @param onMeritGain 功德增加回调（统一处理历史/成就）
 * @param onBack 返回回调
 */
@Composable
fun AsmrScreen(
    store: MeritStore,
    soundEngine: SoundEngine,
    hapticEngine: HapticEngine,
    hapticEnabled: Boolean = true,
    onMeritGain: () -> Unit = {},
    onBack: () -> Unit
) {
    // 功德计数（累计 + 本次）
    var totalCount by remember { mutableIntStateOf(store.totalCount) }
    var sessionCount by remember { mutableIntStateOf(0) }
    val accent = GongDeThemeExt.colors.accent

    // 白噪音（雨声）开关状态
    var rainEnabled by remember { mutableStateOf(false) }

    // 退出时停止雨声
    DisposableEffect(Unit) {
        onDispose {
            soundEngine.stopRain()
        }
    }

    // ─── 主布局 ───
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== 顶部导航栏 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "返回" 文本按钮
            TextButton(onClick = onBack) {
                Text("返回", color = MutedGrayColor, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // ===== 页面标题 =====
        Text(
            text = "ASMR 模式",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ===== 功德计数展示 =====
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 累计功德
            Text(
                text = "功德: $totalCount",
                color = GoldColor.copy(alpha = 0.8f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(24.dp))
            // 本次功德
            Text(
                text = "本次: $sessionCount",
                color = MutedGrayColor,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // ===== 大尺寸 ASMR 按钮（居中） =====
        // 使用 MechanicalButton，尺寸放大到 fillMaxWidth x 350.dp
        // asmrMode = true 时按钮内部自动使用 ASMR 增强音效
        MechanicalButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            soundEngine = soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = hapticEnabled,
            asmrMode = true,
            onPressed = {
                // 通过统一回调递增功德（含历史记录 + 成就检查）
                onMeritGain()
                totalCount = store.totalCount
                sessionCount++
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // ===== 底部控制区 =====
        // 白噪音（雨声）开关
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(
                    width = 1.dp,
                    color = if (rainEnabled) accent else CardBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .background(if (rainEnabled) accent.copy(alpha = 0.1f) else CardBgColor)
                .clickable {
                    rainEnabled = !rainEnabled
                    if (rainEnabled) {
                        // 开启雨声环境白噪音
                        soundEngine.playRain()
                    } else {
                        // 关闭雨声
                        soundEngine.stopRain()
                    }
                }
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (rainEnabled) "🔊 白噪音 ON" else "🔇 白噪音 OFF",
                color = if (rainEnabled) accent else MutedGrayColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
