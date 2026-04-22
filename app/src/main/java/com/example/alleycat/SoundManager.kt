package com.example.alleycat

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * Manages game sound effects and audio playback.
 * Handles ToneGenerator lifecycle to prevent audio resource leaks.
 * Thread-safe implementation for concurrent sound playback.
 */
object SoundManager {
    private var toneGenerator: ToneGenerator? = null
    private val lock = ReentrantReadWriteLock()
    private const val TAG = "SoundManager"

    /**
     * Initializes the ToneGenerator for audio playback.
     * Call this once during app initialization.
     */
    fun init() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ToneGenerator", e)
        }
    }

    /**
     * Plays jump sound effect (ascending beep).
     */
    fun playJumpSound() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Ascending beep for jump - creates a "whoosh" effect
                startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing jump sound", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Plays death/life loss sound effect (descending tones).
     */
    fun playDeathSound() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Descending series of tones for death
                startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing death sound", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Plays streak bonus reward sound (celebratory beep).
     */
    fun playScoreBonus() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Celebratory triple beep for streak bonus
                startTone(ToneGenerator.TONE_CDMA_PIP, 80)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing score bonus sound", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Plays hazard warning sound.
     */
    fun playHazardWarning() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Warning sound when hazard appears
                startTone(ToneGenerator.TONE_PROP_ACK, 200)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing hazard warning", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Plays landing confirmation sound.
     */
    fun playLandingSound() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Soft confirmation sound for landing (use TONE_PROP_BEEP as fallback)
                startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing landing sound", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Plays level completion sound (triumphant tone).
     */
    fun playLevelUp() {
        lock.readLock().lock()
        try {
            toneGenerator?.apply {
                // Triumphant sound for level completion
                startTone(ToneGenerator.TONE_CDMA_CONFIRM, 300)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing level up sound", e)
        } finally {
            lock.readLock().unlock()
        }
    }

    /**
     * Releases all audio resources and cleans up ToneGenerator.
     * Call this in onDestroy() to prevent audio resource leaks.
     */
    fun release() {
        var generatorToRelease: ToneGenerator? = null
        lock.writeLock().lock()
        try {
            generatorToRelease = toneGenerator
            toneGenerator = null
        } finally {
            lock.writeLock().unlock()
        }
        try {
            generatorToRelease?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ToneGenerator", e)
        }
    }

    /**
     * Checks if SoundManager is initialized and ready to play sounds.
     */
    fun isInitialized(): Boolean = toneGenerator != null
}
