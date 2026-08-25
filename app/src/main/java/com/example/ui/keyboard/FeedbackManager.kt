package com.example.ui.keyboard

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.data.model.HapticStrength
import com.example.data.model.KeyboardSoundType

class FeedbackManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playFeedback(hapticStrength: HapticStrength, soundType: KeyboardSoundType, view: View? = null) {
        // Haptics
        if (hapticStrength != HapticStrength.OFF) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = hapticStrength.durationMs.coerceAtLeast(10L)
                    val amplitude = when (hapticStrength) {
                        HapticStrength.LIGHT -> 50
                        HapticStrength.MEDIUM -> 120
                        HapticStrength.STRONG -> 240
                        HapticStrength.OFF -> 0
                    }
                    vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(hapticStrength.durationMs)
                }
            } catch (_: Exception) {
                view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            }
        }

        // Sound
        if (soundType != KeyboardSoundType.OFF) {
            try {
                val soundEffect = when (soundType) {
                    KeyboardSoundType.GBOARD_CLICK -> AudioManager.FX_KEYPRESS_STANDARD
                    KeyboardSoundType.MODERN -> AudioManager.FX_KEYPRESS_STANDARD
                    KeyboardSoundType.MECHANICAL -> AudioManager.FX_KEYPRESS_DELETE
                    KeyboardSoundType.TYPEWRITER -> AudioManager.FX_KEYPRESS_SPACEBAR
                    KeyboardSoundType.OFF -> -1
                }
                if (soundEffect >= 0) {
                    audioManager?.playSoundEffect(soundEffect, 0.6f)
                }
            } catch (_: Exception) {
                // Ignore audio failure
            }
        }
    }
}
