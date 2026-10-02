package com.geoalarm.app.data.local

import com.geoalarm.app.domain.model.DismissStyle
import com.geoalarm.app.domain.model.GeoAlarm
import com.geoalarm.app.domain.model.GeofenceTransition
import com.geoalarm.app.domain.model.SnoozeType
import com.geoalarm.app.domain.model.VibrationPattern
import java.time.DayOfWeek

private fun Set<DayOfWeek>.toBitmask(): Int =
    fold(0) { mask, day -> mask or (1 shl day.value) }

private fun Int.toDayOfWeekSet(): Set<DayOfWeek> =
    DayOfWeek.values().filterTo(mutableSetOf()) { (this shr it.value) and 1 == 1 }

fun GeoAlarmEntity.toDomain(): GeoAlarm = GeoAlarm(
    id = id,
    title = title,
    message = message,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    transition = GeofenceTransition.valueOf(transition),
    isEnabled = isEnabled,
    activeDays = activeDaysMask.toDayOfWeekSet(),
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    soundUri = soundUri,
    vibrationPattern = VibrationPattern.valueOf(vibrationPattern),
    volumeFadeInSeconds = volumeFadeInSeconds,
    snoozeType = SnoozeType.valueOf(snoozeType),
    snoozeMinutes = snoozeMinutes,
    snoozeDistanceMeters = snoozeDistanceMeters,
    dismissStyle = DismissStyle.valueOf(dismissStyle),
    showClock = showClock,
    showDistance = showDistance,
    backgroundColorArgb = backgroundColorArgb,
    backgroundImageUri = backgroundImageUri,
)

fun GeoAlarm.toEntity(): GeoAlarmEntity = GeoAlarmEntity(
    id = id,
    title = title,
    message = message,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    transition = transition.name,
    isEnabled = isEnabled,
    activeDaysMask = activeDays.toBitmask(),
    soundEnabled = soundEnabled,
    vibrationEnabled = vibrationEnabled,
    soundUri = soundUri,
    vibrationPattern = vibrationPattern.name,
    volumeFadeInSeconds = volumeFadeInSeconds,
    snoozeType = snoozeType.name,
    snoozeMinutes = snoozeMinutes,
    snoozeDistanceMeters = snoozeDistanceMeters,
    dismissStyle = dismissStyle.name,
    showClock = showClock,
    showDistance = showDistance,
    backgroundColorArgb = backgroundColorArgb,
    backgroundImageUri = backgroundImageUri,
)
