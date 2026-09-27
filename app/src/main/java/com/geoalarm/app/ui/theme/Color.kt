package com.geoalarm.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/** Accent colors offered in the settings picker. */
val AccentColorSwatches = listOf(
    Color(0xFF3B6EF6), // blue (default)
    Color(0xFF2E7D32), // green
    Color(0xFF8E24AA), // purple
    Color(0xFFE64A19), // orange
    Color(0xFFD81B60), // pink
    Color(0xFF00838F), // teal
    Color(0xFFC62828), // red
    Color(0xFF5D4037), // brown
)

private fun Color.toHsl(): FloatArray {
    val hsl = FloatArray(3)
    android.graphics.Color.colorToHSV(toArgb(), hsl)
    return hsl
}

private fun hslToColor(hsv: FloatArray): Color = Color(android.graphics.Color.HSVToColor(hsv))

/** Shifts the seed's lightness (value channel) to derive a lighter/darker tonal variant. */
fun Color.tone(valueMultiplier: Float, saturationMultiplier: Float = 1f): Color {
    val hsv = toHsl()
    hsv[1] = (hsv[1] * saturationMultiplier).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * valueMultiplier).coerceIn(0f, 1f)
    return hslToColor(hsv)
}
