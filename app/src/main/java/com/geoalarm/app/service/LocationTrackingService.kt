package com.geoalarm.app.service

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.geoalarm.app.GeoAlarmApplication
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.model.GeofenceTransition
import com.geoalarm.app.domain.util.HaversineUtil
import com.geoalarm.app.util.Constants
import com.geoalarm.app.util.NotificationHelper
import com.geoalarm.app.util.PermissionUtils
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Native [LocationManager] fallback used only when Google Play services / the Geofencing
 * API are unavailable on the device. Polls location directly and evaluates radius
 * crossings itself, entirely offline — no network requests are made.
 */
class LocationTrackingService : LifecycleService() {

    private lateinit var locationManager: LocationManager
    private val insideState = mutableMapOf<Long, Boolean>()

    private val listener = LocationListener { location -> onLocation(location) }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(Constants.NOTIFICATION_ID_TRACKING, NotificationHelper.buildTrackingNotification(this, 0))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        if (!PermissionUtils.hasAllLocationPermissions(this)) {
            stopSelf()
            return
        }
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return
        }
        try {
            locationManager.requestLocationUpdates(
                provider,
                Constants.FALLBACK_LOCATION_INTERVAL_MS,
                Constants.FALLBACK_LOCATION_MIN_DISTANCE_M,
                listener,
            )
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun onLocation(location: Location) {
        lifecycleScope.launch {
            val enabledAlarms = app().serviceLocator.geoAlarmRepository.getAllEnabledOnce()
            val today = LocalDate.now().dayOfWeek
            enabledAlarms.forEach { alarm -> evaluate(alarm, location, today) }
            updateTrackingNotification(enabledAlarms.size)
        }
    }

    private fun evaluate(alarm: GeoAlarm, location: Location, today: java.time.DayOfWeek) {
        val isInside = HaversineUtil.isWithinRadius(
            alarm.latitude, alarm.longitude, location.latitude, location.longitude, alarm.radiusMeters,
        )
        val wasInside = insideState[alarm.id]
        insideState[alarm.id] = isInside
        if (wasInside == null) return // first fix for this alarm: establish baseline, don't fire yet

        val justEntered = !wasInside && isInside
        val justExited = wasInside && !isInside
        val shouldFire = when (alarm.transition) {
            GeofenceTransition.ENTER -> justEntered
            GeofenceTransition.EXIT -> justExited
        }
        if (shouldFire && alarm.isActiveOn(today)) {
            val serviceIntent = Intent(this, AlarmRingingService::class.java).apply {
                action = Constants.ACTION_START_RINGING
                putExtra(Constants.EXTRA_ALARM_ID, alarm.id)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    private fun updateTrackingNotification(count: Int) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(Constants.NOTIFICATION_ID_TRACKING, NotificationHelper.buildTrackingNotification(this, count))
    }

    private fun app() = application as GeoAlarmApplication

    override fun onDestroy() {
        locationManager.removeUpdates(listener)
        super.onDestroy()
    }
}
