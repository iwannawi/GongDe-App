/**
 * 应用主题定义
 *
 * 定义功德应用的 Material3 暗色主题配置，
 * 将 Color.kt 中定义的颜色映射到 Material 主题槽位。
 */

package com.gongde.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * 暗色配色方案
 *
 * 基于 darkColorScheme 定义，将自定义颜色映射到 Material3 主题槽位：
 * - primary/secondary/tertiary：按键相关颜色（红色系）
 * - background/surface：背景和卡片颜色（深蓝紫色系）
 * - on* 系列：前景文字颜色（白色）
 */
private val DarkColorScheme = darkColorScheme(
    primary = KeycapRed,          // 主色调：键帽红色
    secondary = KeycapLightRed,   // 次要色：键帽亮红色
    tertiary = GlowRed,           // 第三色：辉光红色
    background = BackgroundDark,  // 背景色：深蓝紫色
    surface = CardBackground,     // 表面色：卡片深蓝灰色
    onPrimary = TextWhite,        // 主色上的文字：白色
    onSecondary = TextWhite,      // 次要色上的文字：白色
    onTertiary = TextWhite,       // 第三色上的文字：白色
    onBackground = TextWhite,     // 背景上的文字：白色
    onSurface = TextWhite         // 表面上的文字：白色
)

/**
 * 功德应用主题 Composable
 *
 * 根据传入的 themeId 动态选择背景渐变色，
 * 保持暗色文字配色不变，仅切换背景色系。
 *
 * @param themeId 主题标识（"deep_purple"/"cyber_blue"/"emerald"/"flame"）
 * @param content 应用内容
 */
@Composable
fun GongDeTheme(
    themeId: String = "deep_purple",
    content: @Composable () -> Unit
) {
    // 根据主题 ID 动态调整背景色（其他颜色保持暗色方案不变）
    val dynamicScheme = DarkColorScheme.copy(
        background = ThemePresets.getGradient(themeId).first()
    )
    MaterialTheme(
        colorScheme = dynamicScheme,
        typography = Typography,
        content = content
    )
}
