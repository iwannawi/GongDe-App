package com.gongde.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 键帽固定色（所有主题共享） ====================

val KeycapRed = Color(0xFFE53935)
val KeycapDarkRed = Color(0xFFB71C1C)
val KeycapLightRed = Color(0xFFEF5350)
val GlowRed = Color(0xFFFF5252)

// ==================== 主题预设 ====================

data class ThemePreset(
    val name: String,
    val gradient: List<Color>,
    val accentColor: Color,
    val isLight: Boolean = false
)

object ThemePresets {
    /** 晨雾白 - 冷调中性背景 */
    val MorningMist = ThemePreset(
        name = "晨雾白",
        gradient = listOf(
            Color(0xFFF7F8FA), Color(0xFFF2F4F6),
            Color(0xFFEDF0F2), Color(0xFFF5F6F8)
        ),
        accentColor = Color(0xFF007A9E),
        isLight = true
    )

    /** 薄荷绿 - 低饱和冷绿背景 */
    val MintFresh = ThemePreset(
        name = "薄荷绿",
        gradient = listOf(
            Color(0xFFF4F8F6), Color(0xFFEEF5F1),
            Color(0xFFE8F1ED), Color(0xFFF2F7F4)
        ),
        accentColor = Color(0xFF26A69A),
        isLight = true
    )

    /** 天空蓝 - 柔和浅蓝背景 */
    val SkyBlue = ThemePreset(
        name = "天空蓝",
        gradient = listOf(
            Color(0xFFF4F7FA), Color(0xFFEDF3F8),
            Color(0xFFE8EFF5), Color(0xFFF1F5F8)
        ),
        accentColor = Color(0xFF42A5F5),
        isLight = true
    )

    /** 暖灰 - 克制的暖中性背景 */
    val Sunset = ThemePreset(
        name = "暖灰",
        gradient = listOf(
            Color(0xFFF7F5F4), Color(0xFFF2EFED),
            Color(0xFFEDEAE8), Color(0xFFF5F2F0)
        ),
        accentColor = Color(0xFFFF7043),
        isLight = true
    )

    private val presets = mapOf(
        "morning_mist" to MorningMist,
        "mint_fresh" to MintFresh,
        "sky_blue" to SkyBlue,
        "sunset" to Sunset
    )

    val allThemeIds = presets.keys.toList()

    fun getPreset(themeId: String): ThemePreset = presets[themeId] ?: MorningMist
    fun getGradient(themeId: String): List<Color> = getPreset(themeId).gradient
    fun getAccent(themeId: String): Color = getPreset(themeId).accentColor
    fun getDisplayName(themeId: String): String = getPreset(themeId).name
}
