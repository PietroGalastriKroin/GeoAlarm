package com.geoalarm.app

import android.app.Application
import com.geoalarm.app.di.ServiceLocator
import com.geoalarm.app.util.NotificationHelper
import org.osmdroid.config.Configuration

class GeoAlarmApplication : Application() {

    val serviceLocator: ServiceLocator by lazy { ServiceLocator.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        // osmdroid requires a user agent + a writable cache dir for offline tile caching.
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
    }
}
