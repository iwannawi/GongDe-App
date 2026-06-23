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

val TextDark = Color(0xFF15191D)
val TextDarkSecondary = Color(0xFF33383E)
val TextDarkMuted = Color(0xFF7C838E)

@Immutable
data class ExtendedColors(
    val accent: Color,
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
        accent = Color(0xFF007A9E),
        mutedGray = TextDarkMuted,
        cardBg = Color(0xF0F0F0F0),
        cardBorder = Color(0xFFD0D0D0),
        bgGradient = ThemePresets.MorningMist.gradient,
        surfaceDark = Color(0xFFF1F3F5),
        surfaceOverlay = Color(0x15000000),
        dialogBg = Color(0xFFFFFFFF),
        navBarBg = Color(0xDDFFFFFF),
        divider = Color(0xFFE0E0E0),
        unselected = Color(0xFF9E9E9E),
        indicator = Color(0x335C6BC0),
        barTrack = Color(0xFFCCCCCC),
        textPrimary = TextDark,
        textSecondary = TextDarkSecondary,
        textMuted = TextDarkMuted
    )
}

@Composable
fun GongDeTheme(
    themeId: String = "morning_mist",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val preset = ThemePresets.getPreset(themeId)
    val isLight = preset.isLight

    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && !isLight -> {
            val dynamic = dynamicDarkColorScheme(LocalContext.current)
            dynamic.copy(
                primary = KeycapRed,
                onPrimary = Color.White,
                secondary = preset.accentColor,
                background = preset.gradient.first(),
                surface = preset.gradient.getOrElse(2) { Color(0xFF16213E) },
            )
        }
        dynamicColor && isLight -> {
            val dynamic = dynamicLightColorScheme(LocalContext.current)
            dynamic.copy(
                primary = KeycapRed,
                onPrimary = Color.White,
                secondary = preset.accentColor,
                background = preset.gradient.first(),
                surface = Color.White,
            )
        }
        isLight -> lightColorScheme(
            primary = KeycapRed,
            onPrimary = Color.White,
            secondary = preset.accentColor,
            background = preset.gradient.first(),
            onBackground = TextDark,
            surface = Color.White,
            onSurface = TextDark
        )
        else -> darkColorScheme(
            primary = KeycapRed,
            onPrimary = Color.White,
            secondary = preset.accentColor,
            background = preset.gradient.first(),
            onBackground = Color.White,
            surface = Color(0xFF16213E),
            onSurface = Color.White
        )
    }

    val extendedColors = if (isLight) {
        ExtendedColors(
            accent = preset.accentColor,
            mutedGray = TextDarkMuted,
            cardBg = Color(0xF7FFFFFF),
            cardBorder = Color(0xFFD9DEE3),
            bgGradient = preset.gradient,
            surfaceDark = preset.gradient.getOrElse(2) { Color(0xFFE9ECEF) },
            surfaceOverlay = Color(0x15000000),
            dialogBg = Color.White,
            navBarBg = Color(0xDDFFFFFF),
            divider = Color(0xFFE1E5E9),
            unselected = Color(0xFF4A4A4A),
            indicator = preset.accentColor.copy(alpha = 0.2f),
            barTrack = Color(0xFFDDE2E6),
            textPrimary = TextDark,
            textSecondary = TextDarkSecondary,
            textMuted = TextDarkMuted
        )
    } else {
        ExtendedColors(
            accent = preset.accentColor,
            mutedGray = Color(0xCCB0BEC5),
            cardBg = Color(0x20FFFFFF),
            cardBorder = Color(0x30FFFFFF),
            bgGradient = preset.gradient,
            surfaceDark = preset.gradient.getOrElse(2) { Color(0xFF0D0D24) },
            surfaceOverlay = Color(0x15FFFFFF),
            dialogBg = Color(0xFF1A1A2E),
            navBarBg = Color(0xE60A0A1A),
            divider = Color(0x50FFFFFF),
            unselected = Color(0xCCB0BEC5),
            indicator = preset.accentColor.copy(alpha = 0.2f),
            barTrack = Color(0xFF333333),
            textPrimary = Color.White,
            textSecondary = Color(0xFFE0E0E0),
            textMuted = Color(0xFFB0BEC5)
        )
    }

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
