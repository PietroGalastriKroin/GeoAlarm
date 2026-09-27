package com.geoalarm.app.ui.trigger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.util.HaversineUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlarmTriggerViewModel(
    serviceLocator: ServiceLocator,
    private val alarmId: Long,
) : ViewModel() {

    private val _alarm = MutableStateFlow<GeoAlarm?>(null)
    val alarm: StateFlow<GeoAlarm?> = _alarm.asStateFlow()

    private val _distanceMeters = MutableStateFlow<Double?>(null)
    val distanceMeters: StateFlow<Double?> = _distanceMeters.asStateFlow()

    init {
        viewModelScope.launch {
            _alarm.value = serviceLocator.getAlarmUseCase()(alarmId)
        }
    }

    fun onLocationUpdate(latitude: Double, longitude: Double) {
        val current = _alarm.value ?: return
        _distanceMeters.value = HaversineUtil.distanceMeters(current.latitude, current.longitude, latitude, longitude)
    }
}
