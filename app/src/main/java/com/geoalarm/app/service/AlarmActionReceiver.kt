package com.geoalarm.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.geoalarm.app.util.Constants

/**
 * Forwards dismiss/snooze taps (from the notification action buttons or the trigger
 * screen's own broadcast) to [AlarmRingingService], which owns all the ringing state.
 */
class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(Constants.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0 || intent.action == null) return
        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            action = intent.action
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
