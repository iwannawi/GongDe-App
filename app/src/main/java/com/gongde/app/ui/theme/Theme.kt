/**
 * 应用主题定义
 *
 * primary 始终为红色系（匹配键帽图片），
 * secondary 随主题切换（由 ThemePreset.accentColor 控制）。
 * 支持 Android 12+ Material You 动态色。
 */

package com.gongde.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * 扩展颜色（Material3 不覆盖的自定义色）
 */
@Immutable
data class ExtendedColors(
    val accent: Color,       // 主题强调色（统计、装饰）
    val gold: Color,         // 金色（功德数字，所有主题共享）
    val mutedGray: Color,    // 辅助灰色
    val cardBg: Color,       // 卡片背景
    val cardBorder: Color,   // 卡片边框
    val bgGradient: List<Color> // 背景渐变色
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        accent = Color(0xFF4FC3F7),
        gold = GoldColor,
        mutedGray = MutedGrayColor,
        cardBg = CardBgColor,
        cardBorder = CardBorderColor,
        bgGradient = ThemePresets.DeepPurple.gradient
    )
}

/**
 * 功德应用主题
 *
 * @param themeId 主题标识（用户自选主题覆盖 Material You）
 * @param darkTheme 是否使用深色主题（默认跟随系统）
 * @param content 应用内容
 */
@Composable
fun GongDeTheme(
    themeId: String = "deep_purple",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val preset = ThemePresets.getPreset(themeId)

    // Android 12+ 支持 Material You 动态色
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && darkTheme -> {
            val dynamic = dynamicDarkColorScheme(LocalContext.current)
            dynamic.copy(
                primary = KeycapRed,
                onPrimary = TextWhite,
                primaryContainer = KeycapDarkRed,
                secondary = preset.accentColor,
                background = preset.gradient.first(),
                surface = Color(0xFF16213E),
                onSurface = TextWhite,
                onBackground = TextWhite
            )
        }
        dynamicColor && !darkTheme -> {
            val dynamic = dynamicLightColorScheme(LocalContext.current)
            dynamic.copy(
                primary = KeycapRed,
                onPrimary = TextWhite,
                secondary = preset.accentColor
            )
        }
        darkTheme -> darkColorScheme(
            primary = KeycapRed,
            onPrimary = TextWhite,
            primaryContainer = KeycapDarkRed,
            secondary = preset.accentColor,
            onSecondary = TextWhite,
            tertiary = GlowRed,
            onTertiary = TextWhite,
            background = preset.gradient.first(),
            onBackground = TextWhite,
            surface = Color(0xFF16213E),
            onSurface = TextWhite
        )
        else -> lightColorScheme(
            primary = KeycapRed,
            onPrimary = TextWhite,
            secondary = preset.accentColor,
            onSecondary = TextWhite
        )
    }

    // 扩展颜色（通过 CompositionLocal 传递）
    val extendedColors = ExtendedColors(
        accent = preset.accentColor,
        gold = GoldColor,
        mutedGray = MutedGrayColor,
        cardBg = CardBgColor,
        cardBorder = CardBorderColor,
        bgGradient = preset.gradient
    )

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * 便捷访问扩展颜色
 */
object GongDeThemeExt {
    val colors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
