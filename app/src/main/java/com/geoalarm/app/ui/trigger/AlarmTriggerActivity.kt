package com.geoalarm.app.ui.trigger

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.geoalarm.app.GeoAlarmApplication
import com.geoalarm.app.service.AlarmRingingService
import com.geoalarm.app.ui.GenericViewModelFactory
import com.geoalarm.app.ui.settings.SettingsViewModel
import com.geoalarm.app.ui.theme.GeoAlarmTheme
import com.geoalarm.app.util.Constants

/**
 * Shown full-screen over the lock screen when a geo-alarm fires. [AlarmRingingService]
 * (already running in the foreground) keeps the sound/vibration/wake lock alive; this
 * Activity is purely the UI the user dismisses or snoozes from.
 */
class AlarmTriggerActivity : ComponentActivity() {

    private var alarmId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockScreenWindow()

        alarmId = intent.getLongExtra(Constants.EXTRA_ALARM_ID, -1L)
        if (alarmId < 0) {
            finish()
            return
        }

        val app = application as GeoAlarmApplication
        setContent {
            val triggerViewModel: AlarmTriggerViewModel = viewModel(
                factory = GenericViewModelFactory { AlarmTriggerViewModel(app.serviceLocator, alarmId) },
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = GenericViewModelFactory { SettingsViewModel(app.serviceLocator) },
            )
            val settings by settingsViewModel.settings.collectAsState()

            GeoAlarmTheme(themeMode = settings.themeMode, accentColor = Color(settings.accentColorArgb)) {
                AlarmTriggerScreen(
                    viewModel = triggerViewModel,
                    onDismiss = {
                        sendServiceAction(Constants.ACTION_DISMISS_ALARM)
                        finishAndRemoveTask()
                    },
                    onSnooze = {
                        sendServiceAction(Constants.ACTION_SNOOZE_ALARM)
                        finishAndRemoveTask()
                    },
                )
            }
        }
    }

    private fun configureLockScreenWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(this, AlarmRingingService::class.java).apply {
            this.action = action
            putExtra(Constants.EXTRA_ALARM_ID, alarmId)
        }
        ContextCompat.startForegroundService(this, intent)
    }

    @Suppress("DEPRECATION", "MissingSuperCall")
    override fun onBackPressed() {
        // The alarm must be dismissed or snoozed explicitly — back press does nothing.
    }
}
