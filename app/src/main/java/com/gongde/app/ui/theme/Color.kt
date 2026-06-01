/**
 * 应用颜色定义
 *
 * 定义功德应用全局使用的颜色常量。
 * 采用深色主题配色方案，以深蓝/深紫为背景，
 * 红色系为按键主题色，金色为功德高亮色。
 */

package com.gongde.app.ui.theme

import androidx.compose.ui.graphics.Color

/** 深色背景色 - 深蓝紫色调，营造神秘宇宙感 */
val BackgroundDark = Color(0xFF1A1A2E)

/** 键帽主色 - 鲜艳的红色，用于按键主题元素 */
val KeycapRed = Color(0xFFE53935)

/** 键帽暗红色 - 用于按键阴影或深色变体 */
val KeycapDarkRed = Color(0xFFB71C1C)

/** 键帽亮红色 - 用于按键高亮或浅色变体 */
val KeycapLightRed = Color(0xFFEF5350)

/** 基底灰色 - 用于中性背景元素 */
val BaseGray = Color(0xFF424242)

/** 深灰色 - 用于较深的中性背景元素 */
val BaseDarkGray = Color(0xFF2A2A2A)

/** 辉光红色 - 用于强调发光效果 */
val GlowRed = Color(0xFFFF5252)

/** 白色文字 - 主要文字颜色，用于深色背景上的文本 */
val TextWhite = Color(0xFFFFFFFF)

/** 金色文字 - 用于功德相关数值和重要标题的高亮显示 */
val TextGold = Color(0xFFFFD54F)

/** 次要文字色 - 灰蓝色，用于辅助说明文字 */
val TextSecondary = Color(0xFFB0BEC5)

/** 卡片背景色 - 深蓝灰色，用于信息卡片的底色 */
val CardBackground = Color(0xFF16213E)

// ==================== 共享 UI 颜色 ====================

/** 金色高亮 - 用于功德数字、标题、选中状态 */
val GoldColor = Color(0xFFFFD54F)

/** 柔和灰色 - 用于辅助文字、水印 */
val MutedGrayColor = Color(0x77B0BEC5)

/** 通用卡片背景色 */
val CardBgColor = Color(0x15FFFFFF)

/** 通用卡片边框色 */
val CardBorderColor = Color(0x20FFFFFF)

/** 蓝色强调色 - 用于统计、选中激活态 */
val AccentBlueColor = Color(0xFF4FC3F7)

// ==================== 主题色板 ====================

/**
 * 四套主题配色方案
 * 每套包含：背景渐变色（上/中/下）、强调色、飘字色
 */
object ThemePresets {
    /** 深空紫（默认） */
    val DeepPurple = listOf(
        Color(0xFF050510), Color(0xFF0A0A1E), Color(0xFF0D0D24), Color(0xFF08081A)
    )
    /** 赛博蓝 */
    val CyberBlue = listOf(
        Color(0xFF0A1628), Color(0xFF0D1E37), Color(0xFF102640), Color(0xFF0B1A30)
    )
    /** 翡翠绿 */
    val Emerald = listOf(
        Color(0xFF0A1A12), Color(0xFF0D2418), Color(0xFF102E1E), Color(0xFF0B1E14)
    )
    /** 烈焰红 */
    val Flame = listOf(
        Color(0xFF1A0A0A), Color(0xFF240D0D), Color(0xFF2E1010), Color(0xFF1E0B0B)
    )

    /** 根据主题 ID 获取背景渐变色列表 */
    fun getGradient(themeId: String): List<Color> = when (themeId) {
        "cyber_blue" -> CyberBlue
        "emerald" -> Emerald
        "flame" -> Flame
        else -> DeepPurple
    }

    /** 主题显示名称 */
    fun getDisplayName(themeId: String): String = when (themeId) {
        "cyber_blue" -> "赛博蓝"
        "emerald" -> "翡翠绿"
        "flame" -> "烈焰红"
        else -> "深空紫"
    }

    /** 所有主题 ID 列表 */
    val allThemeIds = listOf("deep_purple", "cyber_blue", "emerald", "flame")
}
