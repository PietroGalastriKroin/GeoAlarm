package com.geoalarm.app.data.location

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.service.LocationTrackingService
import com.geoalarm.app.util.PermissionUtils

/**
 * Decides, per the Android + Google Play services fallback requirement, which location
 * backend keeps geo-alarms armed:
 *  - Play services present  -> low-power [GeofenceManager] (system-level geofencing).
 *  - Play services missing  -> [LocationTrackingService], a foreground service polling
 *    [android.location.LocationManager] directly and evaluating radius crossings itself.
 */
class GeofenceSyncCoordinator(private val context: Context) {

    private val geofenceManager = GeofenceManager(context)

    fun sync(alarm: GeoAlarm) {
        if (!alarm.isEnabled) {
            remove(alarm.id)
            return
        }
        if (PermissionUtils.isPlayServicesAvailable(context)) {
            geofenceManager.registerGeofence(alarm)
        }
        refreshFallbackServiceState()
    }

    fun remove(alarmId: Long) {
        if (PermissionUtils.isPlayServicesAvailable(context)) {
            geofenceManager.unregisterGeofence(alarmId)
        }
        refreshFallbackServiceState()
    }

    fun resyncAll(enabledAlarms: List<GeoAlarm>) {
        if (PermissionUtils.isPlayServicesAvailable(context)) {
            geofenceManager.unregisterAll()
            enabledAlarms.forEach(geofenceManager::registerGeofence)
        }
        refreshFallbackServiceState(hasEnabledAlarmsHint = enabledAlarms.isNotEmpty())
    }

    /** Starts/stops the LocationManager-based fallback service based on current need. */
    private fun refreshFallbackServiceState(hasEnabledAlarmsHint: Boolean? = null) {
        if (PermissionUtils.isPlayServicesAvailable(context)) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
            return
        }
        if (!PermissionUtils.hasAllLocationPermissions(context)) return
        val shouldRun = hasEnabledAlarmsHint ?: true
        if (shouldRun) {
            ContextCompat.startForegroundService(context, Intent(context, LocationTrackingService::class.java))
        }
    }
}
