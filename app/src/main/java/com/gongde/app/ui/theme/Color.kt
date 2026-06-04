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
    /** 晨雾白 - 柔和灰白背景 */
    val MorningMist = ThemePreset(
        name = "晨雾白",
        gradient = listOf(
            Color(0xFFE8E8ED), Color(0xFFE2E2E8),
            Color(0xFFDCDCE3), Color(0xFFE5E5EB)
        ),
        accentColor = Color(0xFF5C6BC0),
        isLight = true
    )

    /** 薄荷绿 - 柔和浅绿背景 */
    val MintFresh = ThemePreset(
        name = "薄荷绿",
        gradient = listOf(
            Color(0xFFD5E8D8), Color(0xFFCEE4D2),
            Color(0xFFC8DECC), Color(0xFFD2E6D5)
        ),
        accentColor = Color(0xFF26A69A),
        isLight = true
    )

    /** 天空蓝 - 柔和浅蓝背景 */
    val SkyBlue = ThemePreset(
        name = "天空蓝",
        gradient = listOf(
            Color(0xFFD0E0F0), Color(0xFFCAD9EB),
            Color(0xFFC4D3E6), Color(0xFFCDDAE9)
        ),
        accentColor = Color(0xFF42A5F5),
        isLight = true
    )

    /** 日落橙 - 柔和暖色背景 */
    val Sunset = ThemePreset(
        name = "日落橙",
        gradient = listOf(
            Color(0xFFEDD9C4), Color(0xFFE8D3BB),
            Color(0xFFE3CDB2), Color(0xFFEBD6BF)
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
