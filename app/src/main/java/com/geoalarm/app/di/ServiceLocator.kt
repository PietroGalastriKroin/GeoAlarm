package com.geoalarm.app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.geoalarm.app.data.audio.AlarmSoundController
import com.geoalarm.app.data.local.AppDatabase
import com.geoalarm.app.data.location.GeofenceSyncCoordinator
import com.geoalarm.app.data.repository.GeoAlarmRepositoryImpl
import com.geoalarm.app.data.repository.SettingsRepositoryImpl
import com.geoalarm.app.domain.repository.GeoAlarmRepository
import com.geoalarm.app.domain.repository.SettingsRepository
import com.geoalarm.app.domain.usecase.DeleteAlarmUseCase
import com.geoalarm.app.domain.usecase.GetAlarmUseCase
import com.geoalarm.app.domain.usecase.ObserveAlarmsUseCase
import com.geoalarm.app.domain.usecase.ResyncGeofencesUseCase
import com.geoalarm.app.domain.usecase.SaveAlarmUseCase
import com.geoalarm.app.domain.usecase.ToggleAlarmUseCase

private val Context.settingsDataStore by preferencesDataStore(name = "geoalarm_settings")

/**
 * Minimal hand-rolled dependency container. Kept deliberately simple (no reflection-based
 * DI framework) so the whole wiring is readable in one file; every dependency below is a
 * cheap, stateless wrapper safe to hold for the process lifetime.
 */
class ServiceLocator(context: Context) {

    private val appContext = context.applicationContext

    private val database by lazy { AppDatabase.getInstance(appContext) }
    private val geofenceSyncCoordinator by lazy { GeofenceSyncCoordinator(appContext) }

    val geoAlarmRepository: GeoAlarmRepository by lazy {
        GeoAlarmRepositoryImpl(database.geoAlarmDao(), geofenceSyncCoordinator)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(appContext.settingsDataStore)
    }

    fun newAlarmSoundController(): AlarmSoundController = AlarmSoundController(appContext)

    // Use cases are stateless and cheap; constructed fresh per call site.
    fun observeAlarmsUseCase() = ObserveAlarmsUseCase(geoAlarmRepository)
    fun getAlarmUseCase() = GetAlarmUseCase(geoAlarmRepository)
    fun saveAlarmUseCase() = SaveAlarmUseCase(geoAlarmRepository)
    fun deleteAlarmUseCase() = DeleteAlarmUseCase(geoAlarmRepository)
    fun toggleAlarmUseCase() = ToggleAlarmUseCase(geoAlarmRepository)
    fun resyncGeofencesUseCase() = ResyncGeofencesUseCase(geoAlarmRepository)

    companion object {
        @Volatile private var instance: ServiceLocator? = null

        fun getInstance(context: Context): ServiceLocator =
            instance ?: synchronized(this) {
                instance ?: ServiceLocator(context).also { instance = it }
            }
    }
}
