package com.geoalarm.app.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.geoalarm.app.domain.model.GeoAlarm

/**
 * Owns the [MediaPlayer] + [Vibrator] pair for a ringing alarm: independent sound/vibration
 * toggles, a user-chosen or imported ringtone, and a gradual (fade-in) volume ramp.
 */
class AlarmSoundController(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val fadeHandler = Handler(Looper.getMainLooper())
    private var fadeRunnable: Runnable? = null

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun start(alarm: GeoAlarm) {
        if (alarm.soundEnabled) startSound(alarm)
        if (alarm.vibrationEnabled) startVibration(alarm)
    }

    private fun startSound(alarm: GeoAlarm) {
        val uri: Uri = alarm.soundUri?.let(Uri::parse)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setDataSource(context, uri)
                isLooping = true
                val fadeInSeconds = alarm.volumeFadeInSeconds.coerceAtLeast(0)
                setVolume(if (fadeInSeconds == 0) 1f else 0.05f, if (fadeInSeconds == 0) 1f else 0.05f)
                setOnPreparedListener {
                    it.start()
                    if (fadeInSeconds > 0) fadeInVolume(fadeInSeconds)
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start alarm sound", e)
        }
    }

    private fun fadeInVolume(durationSeconds: Int) {
        val steps = (durationSeconds * 4).coerceAtLeast(1) // ~250ms resolution
        val stepDurationMs = (durationSeconds * 1000L) / steps
        var currentStep = 0
        fadeRunnable = object : Runnable {
            override fun run() {
                currentStep++
                val fraction = (currentStep.toFloat() / steps).coerceIn(0.05f, 1f)
                mediaPlayer?.setVolume(fraction, fraction)
                if (currentStep < steps) {
                    fadeHandler.postDelayed(this, stepDurationMs)
                }
            }
        }
        fadeHandler.postDelayed(fadeRunnable!!, stepDurationMs)
    }

    private fun startVibration(alarm: GeoAlarm) {
        val effect: VibrationEffect = VibrationPatterns.effectFor(alarm.vibrationPattern) ?: return
        vibrator.vibrate(effect)
    }

    fun stop() {
        fadeRunnable?.let(fadeHandler::removeCallbacks)
        fadeRunnable = null
        mediaPlayer?.let {
            runCatching { if (it.isPlaying) it.stop() }
            it.release()
        }
        mediaPlayer = null
        vibrator.cancel()
    }

    companion object {
        private const val TAG = "AlarmSoundController"
    }
}
