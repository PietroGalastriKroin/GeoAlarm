package com.geoalarm.app.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.geoalarm.app.R
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.service.AlarmActionReceiver
import com.geoalarm.app.ui.trigger.AlarmTriggerActivity

object NotificationHelper {

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val alarmChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ALARM,
            context.getString(R.string.notification_channel_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_alarm_desc)
            enableVibration(false) // vibration is driven manually so it can follow custom patterns
            setBypassDnd(true) // requires the user to also allow this app under DND settings
            setSound(null, null) // sound is played manually with fade-in via AlarmSoundController
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }

        val serviceChannel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_SERVICE,
            context.getString(R.string.notification_channel_service_name),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = context.getString(R.string.notification_channel_service_desc)
        }

        manager.createNotificationChannels(listOf(alarmChannel, serviceChannel))
    }

    fun buildRingingNotification(context: Context, alarm: GeoAlarm): Notification {
        val fullScreenIntent = Intent(context, AlarmTriggerActivity::class.java).apply {
            putExtra(Constants.EXTRA_ALARM_ID, alarm.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val dismissIntent = actionPendingIntent(context, Constants.ACTION_DISMISS_ALARM, alarm.id)
        val snoozeIntent = actionPendingIntent(context, Constants.ACTION_SNOOZE_ALARM, alarm.id)

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_ringing_title, alarm.title))
            .setContentText(alarm.message)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(0, context.getString(R.string.notification_action_dismiss), dismissIntent)
            .apply {
                if (alarm.snoozeType != com.geoalarm.app.domain.model.SnoozeType.NONE) {
                    addAction(0, context.getString(R.string.notification_action_snooze), snoozeIntent)
                }
            }
            .build()
    }

    fun buildTrackingNotification(context: Context, activeAlarmCount: Int): Notification =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_tracking_title))
            .setContentText(context.getString(R.string.notification_tracking_text, activeAlarmCount))
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

    private fun actionPendingIntent(context: Context, action: String, alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmActionReceiver::class.java).apply {
            this.action = action
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
        }
        val requestCode = (action + alarmId).hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
