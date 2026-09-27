package com.geoalarm.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geo_alarms")
data class GeoAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val message: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val transition: String,
    val isEnabled: Boolean,
    /** Bitmask over java.time.DayOfWeek ordinals (bit 0 = MONDAY .. bit 6 = SUNDAY). */
    val activeDaysMask: Int,
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean,
    val soundUri: String?,
    val vibrationPattern: String,
    val volumeFadeInSeconds: Int,
    val snoozeType: String,
    val snoozeMinutes: Int,
    val snoozeDistanceMeters: Float,
    val dismissStyle: String,
    val showClock: Boolean,
    val showDistance: Boolean,
    val backgroundColorArgb: Long?,
    val backgroundImageUri: String?,
)
