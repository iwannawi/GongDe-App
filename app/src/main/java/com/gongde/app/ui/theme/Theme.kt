package com.gongde.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Immutable
data class ExtendedColors(
    val accent: Color,
    val gold: Color,
    val mutedGray: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val bgGradient: List<Color>,
    val surfaceDark: Color,
    val surfaceOverlay: Color,
    val dialogBg: Color,
    val navBarBg: Color,
    val divider: Color,
    val unselected: Color,
    val indicator: Color,
    val barTrack: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        accent = Color(0xFF4FC3F7),
        gold = GoldColor,
        mutedGray = MutedGrayColor,
        cardBg = CardBgColor,
        cardBorder = CardBorderColor,
        bgGradient = ThemePresets.DeepPurple.gradient,
        surfaceDark = Color(0xFF0D0D24),
        surfaceOverlay = Color(0x08FFFFFF),
        dialogBg = Color(0xFF1A1A2E),
        navBarBg = Color(0xE60A0A1A),
        divider = Color(0x30FFFFFF),
        unselected = Color(0x66B0BEC5),
        indicator = Color(0x20FFD54F),
        barTrack = Color(0xFF333333),
        textPrimary = Color.White,
        textSecondary = Color(0xFFB0BEC5),
        textMuted = Color(0xFF78909C)
    )
}

@Composable
fun GongDeTheme(
    themeId: String = "deep_purple",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val preset = ThemePresets.getPreset(themeId)

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
        else -> darkColorScheme(
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
    }

    val extendedColors = ExtendedColors(
        accent = preset.accentColor,
        gold = GoldColor,
        mutedGray = MutedGrayColor,
        cardBg = CardBgColor,
        cardBorder = CardBorderColor,
        bgGradient = preset.gradient,
        surfaceDark = preset.gradient.getOrElse(2) { Color(0xFF0D0D24) },
        surfaceOverlay = Color(0x08FFFFFF),
        dialogBg = Color(0xFF1A1A2E),
        navBarBg = Color(0xE60A0A1A),
        divider = Color(0x30FFFFFF),
        unselected = Color(0x66B0BEC5),
        indicator = Color(0x20FFD54F),
        barTrack = Color(0xFF333333),
        textPrimary = TextWhite,
        textSecondary = TextSecondary,
        textMuted = Color(0xFF78909C)
    )

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object GongDeThemeExt {
    val colors: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}
