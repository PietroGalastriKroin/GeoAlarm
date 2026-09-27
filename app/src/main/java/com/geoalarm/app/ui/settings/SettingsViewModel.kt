package com.geoalarm.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.domain.model.AppSettings
import com.geoalarm.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val serviceLocator: ServiceLocator) : ViewModel() {

    val settings: StateFlow<AppSettings> = serviceLocator.settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { serviceLocator.settingsRepository.setThemeMode(mode) }
    }

    fun setAccentColor(argb: Long) {
        viewModelScope.launch { serviceLocator.settingsRepository.setAccentColor(argb) }
    }
}
