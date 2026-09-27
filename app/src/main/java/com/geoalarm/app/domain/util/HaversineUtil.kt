package com.geoalarm.app.domain.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Great-circle distance between two coordinates. Pure math, no location services or
 * network involved — used both to evaluate the LocationManager geofence fallback and to
 * show "distance remaining" on the trigger screen.
 */
object HaversineUtil {
    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun distanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).let { it * it } +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).let { it * it }
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isWithinRadius(
        centerLat: Double,
        centerLon: Double,
        currentLat: Double,
        currentLon: Double,
        radiusMeters: Float,
    ): Boolean = distanceMeters(centerLat, centerLon, currentLat, currentLon) <= radiusMeters
}
