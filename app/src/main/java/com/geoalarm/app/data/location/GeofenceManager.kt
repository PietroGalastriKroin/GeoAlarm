package com.geoalarm.app.data.location

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.model.GeofenceTransition
import com.geoalarm.app.service.GeofenceBroadcastReceiver
import com.geoalarm.app.util.Constants
import com.geoalarm.app.util.PermissionUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

/** Thin wrapper around the Play Services Geofencing API — the primary, low-power backend. */
class GeofenceManager(private val context: Context) {

    private val client: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = Constants.ACTION_GEOFENCE_EVENT
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    fun registerGeofence(alarm: GeoAlarm) {
        if (!PermissionUtils.hasAllLocationPermissions(context)) {
            Log.w(TAG, "Missing location permission, cannot register geofence ${alarm.id}")
            return
        }
        val transitionType = when (alarm.transition) {
            GeofenceTransition.ENTER -> Geofence.GEOFENCE_TRANSITION_ENTER
            GeofenceTransition.EXIT -> Geofence.GEOFENCE_TRANSITION_EXIT
        }
        val initialTrigger = when (alarm.transition) {
            GeofenceTransition.ENTER -> GeofencingRequest.INITIAL_TRIGGER_ENTER
            GeofenceTransition.EXIT -> GeofencingRequest.INITIAL_TRIGGER_EXIT
        }
        val geofence = Geofence.Builder()
            .setRequestId(alarm.id.toString())
            .setCircularRegion(alarm.latitude, alarm.longitude, alarm.radiusMeters.coerceAtLeast(MIN_RADIUS_METERS))
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionType)
            .setNotificationResponsiveness(Constants.GEOFENCE_NOTIFICATION_RESPONSIVENESS_MS)
            .build()
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(initialTrigger)
            .addGeofence(geofence)
            .build()
        try {
            client.addGeofences(request, geofencePendingIntent)
                .addOnFailureListener { e -> Log.e(TAG, "Failed to add geofence ${alarm.id}", e) }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission when adding geofence", e)
        }
    }

    fun unregisterGeofence(alarmId: Long) {
        client.removeGeofences(listOf(alarmId.toString()))
            .addOnFailureListener { e -> Log.e(TAG, "Failed to remove geofence $alarmId", e) }
    }

    fun unregisterAll() {
        client.removeGeofences(geofencePendingIntent)
    }

    companion object {
        private const val TAG = "GeofenceManager"
        private const val MIN_RADIUS_METERS = 20f
    }
}
