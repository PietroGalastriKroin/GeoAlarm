package com.geoalarm.app.ui.alarmedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.model.GeofenceTransition
import com.geoalarm.app.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

sealed interface AlarmEditEvent {
    data object Saved : AlarmEditEvent
    data object Deleted : AlarmEditEvent
}

class AlarmEditViewModel(
    private val serviceLocator: ServiceLocator,
    private val alarmId: Long,
) : ViewModel() {

    val isNew: Boolean = alarmId == Screen.NEW_ALARM_ID

    private val _state = MutableStateFlow(
        GeoAlarm(title = "", message = "", latitude = 0.0, longitude = 0.0, radiusMeters = 200f),
    )
    val state: StateFlow<GeoAlarm> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AlarmEditEvent>()
    val events: SharedFlow<AlarmEditEvent> = _events.asSharedFlow()

    init {
        if (!isNew) {
            viewModelScope.launch {
                serviceLocator.getAlarmUseCase()(alarmId)?.let { _state.value = it }
            }
        }
    }

    fun update(transform: (GeoAlarm) -> GeoAlarm) {
        _state.value = transform(_state.value)
    }

    fun setLocation(latitude: Double, longitude: Double) = update { it.copy(latitude = latitude, longitude = longitude) }

    fun toggleDay(day: DayOfWeek) = update {
        val days = it.activeDays.toMutableSet()
        if (day in days) days.remove(day) else days.add(day)
        it.copy(activeDays = days)
    }

    fun setTransition(transition: GeofenceTransition) = update { it.copy(transition = transition) }

    fun save() {
        viewModelScope.launch {
            serviceLocator.saveAlarmUseCase()(_state.value)
            _events.emit(AlarmEditEvent.Saved)
        }
    }

    fun delete() {
        if (isNew) return
        viewModelScope.launch {
            serviceLocator.deleteAlarmUseCase()(alarmId)
            _events.emit(AlarmEditEvent.Deleted)
        }
    }
}
