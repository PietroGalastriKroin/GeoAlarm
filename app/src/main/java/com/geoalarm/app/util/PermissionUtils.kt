package com.geoalarm.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

object PermissionUtils {

    fun hasFineLocation(context: Context): Boolean = granted(context, Manifest.permission.ACCESS_FINE_LOCATION)

    fun hasBackgroundLocation(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            granted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            true // background access implied by foreground grant pre-Q
        }

    fun hasNotifications(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            granted(context, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

    /** True when both fine and (where required) background location are granted. */
    fun hasAllLocationPermissions(context: Context): Boolean =
        hasFineLocation(context) && hasBackgroundLocation(context)

    fun isPlayServicesAvailable(context: Context): Boolean =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    private fun granted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}
