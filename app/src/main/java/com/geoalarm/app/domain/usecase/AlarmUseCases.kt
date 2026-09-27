package com.geoalarm.app.domain.usecase

import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.repository.GeoAlarmRepository
import kotlinx.coroutines.flow.Flow

/** Bundle of small, single-purpose use cases wrapping [GeoAlarmRepository]. */
class ObserveAlarmsUseCase(private val repository: GeoAlarmRepository) {
    operator fun invoke(): Flow<List<GeoAlarm>> = repository.observeAlarms()
}

class GetAlarmUseCase(private val repository: GeoAlarmRepository) {
    suspend operator fun invoke(id: Long): GeoAlarm? = repository.getAlarm(id)
}

class SaveAlarmUseCase(private val repository: GeoAlarmRepository) {
    suspend operator fun invoke(alarm: GeoAlarm): Long = repository.saveAlarm(alarm)
}

class DeleteAlarmUseCase(private val repository: GeoAlarmRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteAlarm(id)
}

class ToggleAlarmUseCase(private val repository: GeoAlarmRepository) {
    suspend operator fun invoke(id: Long, enabled: Boolean) = repository.setEnabled(id, enabled)
}

class ResyncGeofencesUseCase(private val repository: GeoAlarmRepository) {
    suspend operator fun invoke() = repository.resyncAllGeofences()
}
