package com.example.alleycat

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Handles haptic feedback (vibration) for game events.
 * Automatically detects and uses appropriate API levels.
 */
object HapticFeedback {
    private var vibrator: Vibrator? = null
    private const val TAG = "HapticFeedback"

    /**
     * Initializes the haptic feedback system.
     * Call this once during app initialization.
     */
    fun init(context: Context) {
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+ - Use VibratorManager
                try {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get VibratorManager", e)
                    null
                }
            } else {
                // API <31 - Use Vibrator service directly
                try {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get Vibrator service", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize HapticFeedback", e)
            vibrator = null
        }
    }

    /**
     * Provides light haptic feedback for successful landing.
     */
    fun landingFeedback() {
        vibratePattern(longArrayOf(0, 50), intArrayOf(0, 255))
    }

    /**
     * Provides medium haptic feedback for streaks.
     */
    fun streakBonusFeedback() {
        vibratePattern(longArrayOf(0, 100, 50, 100), intArrayOf(0, 200, 0, 200))
    }

    /**
     * Provides strong haptic feedback for collision/death.
     */
    fun collisionFeedback() {
        vibratePattern(longArrayOf(0, 150, 100, 150), intArrayOf(0, 255, 100, 255))
    }

    /**
     * Provides celebratory haptic feedback for level completion.
     */
    fun levelCompleteFeedback() {
        vibratePattern(longArrayOf(0, 100, 100, 100, 100, 100), intArrayOf(0, 255, 0, 255, 0, 255))
    }

    /**
     * Provides warning haptic feedback when hazard appears.
     */
    fun hazardWarningFeedback() {
        vibratePattern(longArrayOf(0, 50, 50, 50), intArrayOf(0, 200, 100, 200))
    }

    /**
     * Generic vibration for various feedback patterns.
     */
    private fun vibratePattern(timings: LongArray, amplitudes: IntArray) {
        try {
            if (vibrator?.hasVibrator() != true) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26+ - Use VibrationEffect
                val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                // API <26 - Fallback (deprecated)
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing vibration pattern", e)
        }
    }

    /**
     * Releases haptic resources.
     * Call this in onDestroy() to clean up.
     */
    fun release() {
        vibrator = null
    }

    /**
     * Checks if haptic feedback is available on the device.
     */
    fun isAvailable(): Boolean = vibrator?.hasVibrator() == true
}
