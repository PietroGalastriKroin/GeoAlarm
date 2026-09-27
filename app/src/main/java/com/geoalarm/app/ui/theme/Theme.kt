package com.geoalarm.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.geoalarm.app.domain.model.ThemeMode

/**
 * Builds a full Material 3 [androidx.compose.material3.ColorScheme] from a single accent
 * seed color, so every token in the app (buttons, chips, containers, ...) reacts to the
 * user's chosen accent — no dependency on Android 12's wallpaper-based dynamic color.
 */
private fun buildColorScheme(seed: Color, dark: Boolean): androidx.compose.material3.ColorScheme {
    val onSeed = Color.White
    return if (dark) {
        darkColorScheme(
            primary = seed.tone(1.1f),
            onPrimary = onSeed,
            primaryContainer = seed.tone(0.55f, 0.7f),
            onPrimaryContainer = seed.tone(1.6f),
            secondary = seed.tone(0.85f, 0.6f),
            onSecondary = onSeed,
            tertiary = seed.tone(1.3f, 0.5f),
            background = Color(0xFF121318),
            surface = Color(0xFF1C1D24),
            surfaceVariant = Color(0xFF2A2B33),
        )
    } else {
        lightColorScheme(
            primary = seed,
            onPrimary = onSeed,
            primaryContainer = seed.tone(1.7f, 0.35f),
            onPrimaryContainer = seed.tone(0.4f),
            secondary = seed.tone(1.1f, 0.5f),
            onSecondary = onSeed,
            tertiary = seed.tone(0.7f, 0.6f),
            background = Color(0xFFFBFBFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFE8E9F0),
        )
    }
}

private val GeoAlarmTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
)

@Composable
fun GeoAlarmTheme(
    themeMode: ThemeMode,
    accentColor: Color,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = buildColorScheme(accentColor, useDark)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GeoAlarmTypography,
        content = content,
    )
}
