package com.geoalarm.app.domain.model

import java.time.DayOfWeek

/** Trigger condition relative to the alarm's geofence. */
enum class GeofenceTransition { ENTER, EXIT }

/** Vibration pattern played alongside (or instead of) the alarm sound. */
enum class VibrationPattern { NONE, CONTINUOUS, SHORT_PULSE, SOS, HEARTBEAT }

/** How the alarm re-fires after being silenced once. */
enum class SnoozeType { NONE, TIME, DISTANCE }

/** How the user must interact with the lock-screen UI to silence the alarm. */
enum class DismissStyle { BUTTON, SWIPE, HOLD }

/**
 * A single geo-alarm: a place, a radius, a trigger condition and everything needed to
 * render and sound the lock-screen alert. Framework-free so it can be unit tested and
 * shared between the Play Services geofencing path and the native LocationManager fallback.
 */
data class GeoAlarm(
    val id: Long = 0L,
    val title: String,
    val message: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val transition: GeofenceTransition = GeofenceTransition.ENTER,
    val isEnabled: Boolean = true,
    val activeDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    /** null = system default alarm ringtone */
    val soundUri: String? = null,
    val vibrationPattern: VibrationPattern = VibrationPattern.CONTINUOUS,
    val volumeFadeInSeconds: Int = 15,
    val snoozeType: SnoozeType = SnoozeType.TIME,
    val snoozeMinutes: Int = 5,
    val snoozeDistanceMeters: Float = 50f,
    val dismissStyle: DismissStyle = DismissStyle.SWIPE,
    val showClock: Boolean = true,
    val showDistance: Boolean = true,
    val backgroundColorArgb: Long? = null,
    val backgroundImageUri: String? = null,
) {
    /** Whether this alarm is allowed to fire on [today], honoring the active-days schedule. */
    fun isActiveOn(today: DayOfWeek): Boolean = isEnabled && today in activeDays
}
