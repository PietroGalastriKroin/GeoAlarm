package com.geoalarm.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.geoalarm.app.service.AlarmRingingService

/** Schedules the two snooze flavors using [AlarmManager], waking the device if idle/Doze. */
object SnoozeScheduler {

    fun scheduleTimeSnooze(context: Context, alarmId: Long, minutes: Int) {
        val triggerAt = SystemClock.elapsedRealtime() + minutes * 60_000L
        schedule(context, triggerAt, ringAgainPendingIntent(context, alarmId))
    }

    fun scheduleDistanceRecheck(context: Context, alarmId: Long, delayMs: Long = 2 * 60_000L) {
        val triggerAt = SystemClock.elapsedRealtime() + delayMs
        schedule(context, triggerAt, distanceRecheckPendingIntent(context, alarmId))
    }

    fun cancel(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.cancel(ringAgainPendingIntent(context, alarmId))
        alarmManager.cancel(distanceRecheckPendingIntent(context, alarmId))
    }

    private fun schedule(context: Context, triggerAtElapsedRealtime: Long, pendingIntent: PendingIntent) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAtElapsedRealtime,
            pendingIntent,
        )
    }

    private fun ringAgainPendingIntent(context: Context, alarmId: Long): PendingIntent =
        servicePendingIntent(context, Constants.ACTION_START_RINGING, alarmId, requestCodeSalt = 1)

    private fun distanceRecheckPendingIntent(context: Context, alarmId: Long): PendingIntent =
        servicePendingIntent(context, Constants.ACTION_SNOOZE_DISTANCE_RECHECK, alarmId, requestCodeSalt = 2)

    private fun servicePendingIntent(context: Context, action: String, alarmId: Long, requestCodeSalt: Int): PendingIntent {
        val intent = Intent(context, AlarmRingingService::class.java).apply {
            this.action = action
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
        }
        val requestCode = (alarmId.toInt() * 10) + requestCodeSalt
        return PendingIntent.getForegroundService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
