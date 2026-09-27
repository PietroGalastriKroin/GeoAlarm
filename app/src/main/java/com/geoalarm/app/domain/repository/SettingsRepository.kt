package com.geoalarm.app.domain.repository

import com.geoalarm.app.domain.model.AppSettings
import com.geoalarm.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentColor(argb: Long)
}
