package com.geoalarm.app.data.audio

import android.os.VibrationEffect
import com.geoalarm.app.domain.model.VibrationPattern

/** Maps each [VibrationPattern] to a timings/amplitudes pair repeated from the given index. */
object VibrationPatterns {

    /** @return null for [VibrationPattern.NONE]. */
    fun effectFor(pattern: VibrationPattern): VibrationEffect? = when (pattern) {
        VibrationPattern.NONE -> null

        VibrationPattern.CONTINUOUS -> VibrationEffect.createWaveform(
            longArrayOf(0, 1000, 500),
            intArrayOf(0, 255, 0),
            1, // repeat from index 1
        )

        VibrationPattern.SHORT_PULSE -> VibrationEffect.createWaveform(
            longArrayOf(0, 150, 150),
            intArrayOf(0, 255, 0),
            0,
        )

        VibrationPattern.SOS -> VibrationEffect.createWaveform(
            // ... --- ... in Morse-style short/long/short bursts
            longArrayOf(0, 150, 150, 150, 150, 150, 400, 400, 400, 400, 400, 400, 150, 150, 150, 150, 150, 700),
            intArrayOf(0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 255, 0, 0),
            0,
        )

        VibrationPattern.HEARTBEAT -> VibrationEffect.createWaveform(
            longArrayOf(0, 120, 120, 180, 700),
            intArrayOf(0, 200, 0, 255, 0),
            0,
        )
    }
}
