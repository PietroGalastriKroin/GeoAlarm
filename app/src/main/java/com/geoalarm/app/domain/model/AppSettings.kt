package com.geoalarm.app.domain.model

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** ARGB accent color used to seed the Material 3 dynamic color scheme. */
    val accentColorArgb: Long = 0xFF3B6EF6,
)
