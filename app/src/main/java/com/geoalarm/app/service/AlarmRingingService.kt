package com.geoalarm.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.geoalarm.app.GeoAlarmApplication
import com.geoalarm.app.data.audio.AlarmSoundController
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.model.SnoozeType
import com.geoalarm.app.domain.util.HaversineUtil
import com.geoalarm.app.util.Constants
import com.geoalarm.app.util.NotificationHelper
import com.geoalarm.app.util.SnoozeScheduler
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Foreground service that owns the whole "alarm is ringing" lifecycle: loads the alarm,
 * shows the high-priority full-screen notification, plays sound/vibration, holds a wake
 * lock so the CPU doesn't sleep, and reacts to dismiss/snooze actions.
 */
class AlarmRingingService : LifecycleService() {

    private lateinit var soundController: AlarmSoundController
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentAlarmId: Long? = null

    private val app get() = application as GeoAlarmApplication

    override fun onCreate() {
        super.onCreate()
        soundController = app.serviceLocator.newAlarmSoundController()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // This service is always started via startForegroundService()/getForegroundService();
        // the system requires startForeground() within seconds of every cold start, regardless
        // of which action it was started with. The real notification (or a prompt teardown, if
        // a distance-snooze recheck decides there's nothing to ring) replaces this right after.
        startForeground(Constants.NOTIFICATION_ID_RINGING, NotificationHelper.buildPlaceholderNotification(this))
        val alarmId = intent?.getLongExtra(Constants.EXTRA_ALARM_ID, -1L)?.takeIf { it >= 0 }
        when (intent?.action) {
            Constants.ACTION_DISMISS_ALARM -> alarmId?.let { dismiss(it) }
            Constants.ACTION_SNOOZE_ALARM -> alarmId?.let { snooze(it) }
            Constants.ACTION_SNOOZE_DISTANCE_RECHECK -> alarmId?.let { recheckDistanceSnooze(it) }
            else -> alarmId?.let { startRinging(it) }
        }
        return START_NOT_STICKY
    }

    private fun startRinging(alarmId: Long) {
        currentAlarmId = alarmId
        lifecycleScope.launch {
            val alarm = app.serviceLocator.getAlarmUseCase()(alarmId) ?: return@launch stopSelf()
            acquireWakeLock()
            startForeground(Constants.NOTIFICATION_ID_RINGING, NotificationHelper.buildRingingNotification(this@AlarmRingingService, alarm))
            soundController.start(alarm)
        }
    }

    private fun dismiss(alarmId: Long) {
        SnoozeScheduler.cancel(this, alarmId)
        stopRingingAndFinish()
    }

    private fun snooze(alarmId: Long) {
        lifecycleScope.launch {
            val alarm = app.serviceLocator.getAlarmUseCase()(alarmId)
            soundController.stop()
            when (alarm?.snoozeType ?: SnoozeType.NONE) {
                SnoozeType.TIME -> SnoozeScheduler.scheduleTimeSnooze(this@AlarmRingingService, alarmId, alarm!!.snoozeMinutes)
                SnoozeType.DISTANCE -> SnoozeScheduler.scheduleDistanceRecheck(this@AlarmRingingService, alarmId)
                SnoozeType.NONE -> Unit
            }
            stopForegroundCompat()
            stopSelf()
        }
    }

    /** Re-checks a distance-based snooze: rings again only if still inside the snooze radius. */
    private fun recheckDistanceSnooze(alarmId: Long) {
        lifecycleScope.launch {
            val alarm = app.serviceLocator.getAlarmUseCase()(alarmId) ?: return@launch
            val location = lastKnownLocation()
            val stillClose = location != null && HaversineUtil.isWithinRadius(
                alarm.latitude, alarm.longitude, location.latitude, location.longitude, alarm.snoozeDistanceMeters,
            )
            if (stillClose) {
                startRinging(alarmId)
            } else {
                SnoozeScheduler.scheduleDistanceRecheck(this@AlarmRingingService, alarmId)
                stopForegroundCompat()
                stopSelf()
            }
        }
    }

    private suspend fun lastKnownLocation(): Location? = try {
        LocationServices.getFusedLocationProviderClient(this).lastLocation.await()
    } catch (e: Exception) {
        null
    }

    private fun stopRingingAndFinish() {
        soundController.stop()
        stopForegroundCompat()
        releaseWakeLock()
        stopSelf()
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Constants.WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire(Constants.WAKE_LOCK_TIMEOUT_MS)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    override fun onDestroy() {
        soundController.stop()
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent) = super.onBind(intent)
}
