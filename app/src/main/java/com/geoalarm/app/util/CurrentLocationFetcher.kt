package com.geoalarm.app.util

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

/** One-shot "where am I now" lookup, preferring the fused provider and falling back to the
 *  plain [LocationManager] last-known fix so it still works without Play services. */
object CurrentLocationFetcher {

    suspend fun fetch(context: Context): Pair<Double, Double>? {
        if (!PermissionUtils.hasFineLocation(context)) return null

        if (PermissionUtils.isPlayServicesAvailable(context)) {
            try {
                val request = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .build()
                val location = LocationServices.getFusedLocationProviderClient(context)
                    .getCurrentLocation(request, null)
                    .await()
                if (location != null) return location.latitude to location.longitude
            } catch (e: SecurityException) {
                return null
            }
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return try {
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            providers.firstNotNullOfOrNull { provider ->
                locationManager.getLastKnownLocation(provider)?.let { it.latitude to it.longitude }
            }
        } catch (e: SecurityException) {
            null
        }
    }
}
