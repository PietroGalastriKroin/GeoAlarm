package com.geoalarm.app.data.repository

import com.geoalarm.app.data.local.GeoAlarmDao
import com.geoalarm.app.data.local.toDomain
import com.geoalarm.app.data.local.toEntity
import com.geoalarm.app.data.location.GeofenceSyncCoordinator
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.repository.GeoAlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GeoAlarmRepositoryImpl(
    private val dao: GeoAlarmDao,
    private val geofenceSync: GeofenceSyncCoordinator,
) : GeoAlarmRepository {

    override fun observeAlarms(): Flow<List<GeoAlarm>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeEnabledAlarms(): Flow<List<GeoAlarm>> =
        dao.observeEnabled().map { list -> list.map { it.toDomain() } }

    override suspend fun getAlarm(id: Long): GeoAlarm? = dao.getById(id)?.toDomain()

    override suspend fun getAllEnabledOnce(): List<GeoAlarm> =
        dao.getAllEnabledOnce().map { it.toDomain() }

    override suspend fun saveAlarm(alarm: GeoAlarm): Long {
        val id = dao.upsert(alarm.toEntity())
        val saved = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
        geofenceSync.sync(saved)
        return saved.id
    }

    override suspend fun deleteAlarm(id: Long) {
        dao.deleteById(id)
        geofenceSync.remove(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.setEnabled(id, enabled)
        val alarm = dao.getById(id)?.toDomain() ?: return
        if (enabled) geofenceSync.sync(alarm) else geofenceSync.remove(id)
    }

    override suspend fun resyncAllGeofences() {
        geofenceSync.resyncAll(getAllEnabledOnce())
    }
}
