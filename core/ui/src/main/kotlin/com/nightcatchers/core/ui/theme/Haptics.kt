package com.nightcatchers.core.ui.theme

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build

/**
 * Haptic patterns per section 09 spec.
 * Amplitudes: [0,255] — 0 = pause, 255 = max.
 * Timings: ms per amplitude step.
 */
object HapticPatterns {
    /** Double pulse — monster detected in frame. */
    val MONSTER_DETECTED = HapticPattern(
        timings = longArrayOf(0, 60, 60, 60),
        amplitudes = intArrayOf(0, 80, 0, 80),
    )

    /** Rising rumble — proton beam held on monster. */
    val BEAM_LOCK = HapticPattern(
        timings = longArrayOf(20, 25, 30, 35, 40, 50),
        amplitudes = intArrayOf(20, 40, 60, 80, 100, 120),
    )

    /** Triumphant 3-tap — capture success. */
    val CAPTURE_SUCCESS = HapticPattern(
        timings = longArrayOf(0, 80, 60, 80, 60, 80),
        amplitudes = intArrayOf(0, 100, 0, 100, 0, 100),
    )

    /** Soft single pulse — radar blip. */
    val RADAR_BLIP = HapticPattern(
        timings = longArrayOf(0, 40),
        amplitudes = intArrayOf(0, 50),
    )
}

data class HapticPattern(val timings: LongArray, val amplitudes: IntArray)

fun Context.vibrate(pattern: HapticPattern) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    val effect = VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, -1)
    vibrator.vibrate(effect)
}
