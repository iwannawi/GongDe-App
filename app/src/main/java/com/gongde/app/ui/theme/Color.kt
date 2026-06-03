/**
 * 应用颜色定义
 *
 * 核心设计理念：键帽图片为红色+白色，因此 primary 始终为红色系。
 * 每套主题通过 accentColor 实现差异化视觉，背景渐变配合变化。
 */

package com.gongde.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 键帽固定色（所有主题共享） ====================

val KeycapRed = Color(0xFFE53935)
val KeycapDarkRed = Color(0xFFB71C1C)
val KeycapLightRed = Color(0xFFEF5350)
val GlowRed = Color(0xFFFF5252)

// ==================== 通用文字色 ====================

val TextWhite = Color(0xFFFFFFFF)
val TextGold = Color(0xFFFFD54F)
val TextSecondary = Color(0xFFB0BEC5)

// ==================== 共享 UI 颜色 ====================

val GoldColor = Color(0xFFFFD54F)
val MutedGrayColor = Color(0x77B0BEC5)
val CardBgColor = Color(0x15FFFFFF)
val CardBorderColor = Color(0x20FFFFFF)

// ==================== 主题预设 ====================

/**
 * 主题预设数据类
 *
 * @param name 显示名称
 * @param gradient 背景渐变色（4 个色值，从上到下）
 * @param accentColor 主题强调色（用于统计、次要高亮、装饰）
 */
data class ThemePreset(
    val name: String,
    val gradient: List<Color>,
    val accentColor: Color
)

object ThemePresets {
    /** 深空紫（默认）- 蓝紫色调，科技感 */
    val DeepPurple = ThemePreset(
        name = "深空紫",
        gradient = listOf(
            Color(0xFF050510), Color(0xFF0A0A1E),
            Color(0xFF0D0D24), Color(0xFF08081A)
        ),
        accentColor = Color(0xFF4FC3F7) // 浅蓝
    )

    /** 赛博蓝 - 深蓝色调，赛博朋克风 */
    val CyberBlue = ThemePreset(
        name = "赛博蓝",
        gradient = listOf(
            Color(0xFF0A1628), Color(0xFF0D1E37),
            Color(0xFF102640), Color(0xFF0B1A30)
        ),
        accentColor = Color(0xFF00E5FF) // 青色
    )

    /** 翡翠绿 - 深绿色调，自然清新 */
    val Emerald = ThemePreset(
        name = "翡翠绿",
        gradient = listOf(
            Color(0xFF0A1A12), Color(0xFF0D2418),
            Color(0xFF102E1E), Color(0xFF0B1E14)
        ),
        accentColor = Color(0xFF69F0AE) // 翠绿
    )

    /** 烈焰红 - 深红色调，热烈奔放 */
    val Flame = ThemePreset(
        name = "烈焰红",
        gradient = listOf(
            Color(0xFF1A0A0A), Color(0xFF240D0D),
            Color(0xFF2E1010), Color(0xFF1E0B0B)
        ),
        accentColor = Color(0xFFFF8A65) // 暖橙
    )

    private val presets = mapOf(
        "deep_purple" to DeepPurple,
        "cyber_blue" to CyberBlue,
        "emerald" to Emerald,
        "flame" to Flame
    )

    val allThemeIds = presets.keys.toList()

    fun getPreset(themeId: String): ThemePreset = presets[themeId] ?: DeepPurple
    fun getGradient(themeId: String): List<Color> = getPreset(themeId).gradient
    fun getAccent(themeId: String): Color = getPreset(themeId).accentColor
    fun getDisplayName(themeId: String): String = getPreset(themeId).name
}
