package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEffectManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun playPickup(enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate(12)
        if (enabled) playToneSequence(listOf(520.0 to 35))
    }

    fun playPlace(enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate(20)
        if (enabled) playToneSequence(listOf(330.0 to 45, 440.0 to 45))
    }

    fun playLineClear(lines: Int, combo: Int, enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate((35 + lines * 15).toLong().coerceAtMost(120L))
        if (!enabled) return
        val baseFreq = 440.0 + (combo.coerceAtMost(8) * 55.0)
        val notes = mutableListOf(
            baseFreq to 60,
            (baseFreq * 1.25) to 60,
            (baseFreq * 1.5) to 80
        )
        if (lines >= 2 || combo >= 2) {
            notes.add((baseFreq * 2.0) to 110)
        }
        playToneSequence(notes)
    }

    fun playPowerUp(enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate(45)
        if (enabled) playToneSequence(listOf(600.0 to 50, 800.0 to 60, 1050.0 to 80))
    }

    fun playVictory(enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate(90)
        if (enabled) {
            playToneSequence(
                listOf(
                    523.25 to 80,
                    659.25 to 80,
                    783.99 to 90,
                    1046.50 to 180
                )
            )
        }
    }

    fun playGameOver(enabled: Boolean, haptic: Boolean) {
        if (haptic) vibrate(70)
        if (enabled) {
            playToneSequence(
                listOf(
                    392.0 to 90,
                    349.23 to 90,
                    311.13 to 150
                )
            )
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Throwable) {
            // Ignore in test/unsupported environments
        }
    }

    private fun playToneSequence(notes: List<Pair<Double, Int>>) {
        scope.launch {
            try {
                val sampleRate = 22050
                val totalDurationMs = notes.sumOf { it.second }.coerceAtLeast(20)
                val totalSamples = (sampleRate * totalDurationMs) / 1000
                val buffer = ShortArray(totalSamples)
                var cursor = 0

                for ((freq, durationMs) in notes) {
                    val count = (sampleRate * durationMs) / 1000
                    for (i in 0 until count) {
                        if (cursor >= buffer.size) break
                        val t = i.toDouble() / sampleRate
                        val envelope = exp(-4.5 * (i.toDouble() / count))
                        val wave = sin(2.0 * PI * freq * t) + 0.3 * sin(4.0 * PI * freq * t)
                        val sample = (wave * envelope * 9000).toInt()
                            .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                        buffer[cursor++] = sample.toShort()
                    }
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()
                kotlinx.coroutines.delay(totalDurationMs.toLong() + 60L)
                track.release()
            } catch (_: Throwable) {
                // Safe no-op on JVM tests or audio-restricted environments
            }
        }
    }
}
