package com.geoalarm.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.geoalarm.app.GeoAlarmApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Geofences don't survive a reboot — re-arm every enabled alarm once the device is back up. */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
        if (intent.action !in validActions) return

        val app = context.applicationContext as GeoAlarmApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.serviceLocator.resyncGeofencesUseCase()()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
