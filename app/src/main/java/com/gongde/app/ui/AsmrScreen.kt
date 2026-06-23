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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongde.app.ui.theme.GongDeThemeExt

// 背景色由主题提供（见 bgDark 局部变量）

/**
 * ASMR 模式主界面
 *
 * @param totalCount 累计功德数（从 ViewModel 传入）
 * @param soundEngine 声音引擎（复用 GongDeApp 实例）
 * @param hapticEngine 触觉引擎（复用 GongDeApp 实例）
 * @param hapticEnabled 触觉反馈开关
 * @param switchType 轴体类型（用户偏好）
 * @param onMeritGain 功德增加回调（统一处理历史/成就）
 * @param onBack 返回回调
 */
@Composable
fun AsmrScreen(
    totalCount: Int,
    soundEngine: SoundEngine,
    hapticEngine: HapticEngine,
    hapticEnabled: Boolean = true,
    switchType: SwitchType = SwitchType.BLUE,
    onKeycapOriginChanged: (Offset) -> Unit = {},
    onMeritGain: () -> Unit = {},
    onBack: () -> Unit
) {
    // 本次会话功德数
    var sessionCount by rememberSaveable { mutableIntStateOf(0) }
    val accent = GongDeThemeExt.colors.accent
    val bgDark = GongDeThemeExt.colors.surfaceDark

    // 白噪音（雨声）开关状态
    var rainEnabled by rememberSaveable { mutableStateOf(false) }

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
            .background(bgDark)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== 顶部导航栏 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "返回" 按钮
            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GongDeThemeExt.colors.accent),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = GongDeThemeExt.colors.accent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("返回", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.weight(1f))
        }

        // ===== 页面标题 =====
        Text(
            text = "ASMR 模式",
            color = GongDeThemeExt.colors.textPrimary,
            fontSize = 18.sp,
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
                color = GongDeThemeExt.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(24.dp))
            // 本次功德
            Text(
                text = "本次: $sessionCount",
                color = GongDeThemeExt.colors.textMuted,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.weight(2f))

        // ===== ASMR 按钮（与首页键盘位置一致） =====
        MechanicalButton(
            modifier = Modifier.size(220.dp, 250.dp),
            soundEngine = soundEngine,
            hapticEngine = hapticEngine,
            hapticEnabled = hapticEnabled,
            switchType = switchType,
            asmrMode = true,
            onKeycapOriginChanged = onKeycapOriginChanged,
            onPressed = {
                // 通过统一回调递增功德（含历史记录 + 成就检查）
                onMeritGain()
                sessionCount++
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // ===== 底部控制区 =====
        // 白噪音（雨声）开关
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = 1.dp,
                    color = if (rainEnabled) accent else GongDeThemeExt.colors.cardBorder,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(if (rainEnabled) accent.copy(alpha = 0.1f) else GongDeThemeExt.colors.cardBg)
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (rainEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                    contentDescription = null,
                    tint = if (rainEnabled) accent else GongDeThemeExt.colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (rainEnabled) "白噪音已开启" else "白噪音已关闭",
                    color = if (rainEnabled) accent else GongDeThemeExt.colors.textMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
