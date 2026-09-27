package com.geoalarm.app.ui.alarmlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.domain.model.GeoAlarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmListViewModel(private val serviceLocator: ServiceLocator) : ViewModel() {

    val alarms: StateFlow<List<GeoAlarm>> = serviceLocator.observeAlarmsUseCase()()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEnabled(alarmId: Long, enabled: Boolean) {
        viewModelScope.launch { serviceLocator.toggleAlarmUseCase()(alarmId, enabled) }
    }

    fun delete(alarmId: Long) {
        viewModelScope.launch { serviceLocator.deleteAlarmUseCase()(alarmId) }
    }
}
