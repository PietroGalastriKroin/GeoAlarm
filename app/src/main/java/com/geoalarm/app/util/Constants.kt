package com.geoalarm.app.util

object Constants {
    const val ACTION_GEOFENCE_EVENT = "com.geoalarm.app.action.GEOFENCE_EVENT"
    const val ACTION_DISMISS_ALARM = "com.geoalarm.app.action.DISMISS_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.geoalarm.app.action.SNOOZE_ALARM"
    const val ACTION_START_RINGING = "com.geoalarm.app.action.START_RINGING"
    const val ACTION_RESYNC_GEOFENCES = "com.geoalarm.app.action.RESYNC_GEOFENCES"
    const val ACTION_SNOOZE_DISTANCE_RECHECK = "com.geoalarm.app.action.SNOOZE_DISTANCE_RECHECK"

    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_DISTANCE_METERS = "extra_distance_meters"

    const val NOTIFICATION_CHANNEL_ALARM = "channel_alarm_high_priority"
    const val NOTIFICATION_CHANNEL_SERVICE = "channel_location_service"

    const val NOTIFICATION_ID_RINGING = 1001
    const val NOTIFICATION_ID_TRACKING = 1002

    /** Google's recommended floor to avoid excessive transition chatter. */
    const val GEOFENCE_NOTIFICATION_RESPONSIVENESS_MS = 0

    /** Fallback (no Play Services) location update interval while any alarm is enabled. */
    const val FALLBACK_LOCATION_INTERVAL_MS = 30_000L
    const val FALLBACK_LOCATION_MIN_DISTANCE_M = 5f

    const val WAKE_LOCK_TAG = "GeoAlarm:RingingWakeLock"
    const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L
}
