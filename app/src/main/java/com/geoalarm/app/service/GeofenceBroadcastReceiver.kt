package com.geoalarm.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.geoalarm.app.GeoAlarmApplication
import com.geoalarm.app.util.Constants
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Receives ENTER/EXIT transitions dispatched by the Play Services Geofencing API. */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e(TAG, "Geofencing error: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }
        val transition = event.geofenceTransition
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER && transition != Geofence.GEOFENCE_TRANSITION_EXIT) return

        val alarmIds = event.triggeringGeofences.orEmpty().mapNotNull { it.requestId.toLongOrNull() }
        if (alarmIds.isEmpty()) return

        val app = context.applicationContext as GeoAlarmApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().dayOfWeek
                alarmIds.forEach { alarmId ->
                    val alarm = app.serviceLocator.getAlarmUseCase()(alarmId) ?: return@forEach
                    val transitionMatches = when (transition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> alarm.transition == com.geoalarm.app.domain.model.GeofenceTransition.ENTER
                        else -> alarm.transition == com.geoalarm.app.domain.model.GeofenceTransition.EXIT
                    }
                    if (transitionMatches && alarm.isActiveOn(today)) {
                        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
                            action = Constants.ACTION_START_RINGING
                            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
                        }
                        ContextCompat.startForegroundService(context, serviceIntent)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
