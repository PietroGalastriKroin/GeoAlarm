package com.geoalarm.app.domain.repository

import com.geoalarm.app.domain.model.GeoAlarm
import kotlinx.coroutines.flow.Flow

interface GeoAlarmRepository {
    fun observeAlarms(): Flow<List<GeoAlarm>>
    fun observeEnabledAlarms(): Flow<List<GeoAlarm>>
    suspend fun getAlarm(id: Long): GeoAlarm?
    suspend fun getAllEnabledOnce(): List<GeoAlarm>

    /** Inserts or updates [alarm], syncing its geofence registration, and returns its id. */
    suspend fun saveAlarm(alarm: GeoAlarm): Long

    /** Deletes the alarm and removes its geofence registration. */
    suspend fun deleteAlarm(id: Long)

    /** Flips the on/off switch without touching any other field. */
    suspend fun setEnabled(id: Long, enabled: Boolean)

    /** Re-registers geofences for every enabled alarm (e.g. after boot). */
    suspend fun resyncAllGeofences()
}
