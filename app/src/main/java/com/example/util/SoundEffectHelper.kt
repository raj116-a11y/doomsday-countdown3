package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoundEffectHelper {

    val soundOptions = listOf(
        "doomsday_alarm" to "Doomsday Siren Alarm",
        "cosmic_chime" to "Cosmic Chime",
        "fanfare" to "Victory Fanfare",
        "cyber_beep" to "Cyber Beeps",
        "silent" to "Silent (No Sound)"
    )

    fun playSoundEffect(effectId: String) {
        if (effectId == "silent") return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                when (effectId) {
                    "doomsday_alarm" -> playAlarmSiren()
                    "cosmic_chime" -> playChime()
                    "fanfare" -> playFanfare()
                    "cyber_beep" -> playCyberBeeps()
                    else -> playAlarmSiren()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun playAlarmSiren() {
        val sampleRate = 44100
        val durationMs = 1200
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Oscillating frequency between 440Hz and 880Hz
            val freq = 550.0 + 330.0 * sin(2 * PI * 3.5 * t)
            val sample = sin(2 * PI * freq * t)
            generatedSnd[i] = (sample * 28000).toInt().toShort()
        }

        playPcmTrack(generatedSnd, sampleRate)
    }

    private fun playChime() {
        val sampleRate = 44100
        val durationMs = 1500
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val generatedSnd = ShortArray(numSamples)

        val chords = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5 major
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = (1.0 - (t / 1.5)).coerceAtLeast(0.0)
            var sample = 0.0
            for (f in chords) {
                sample += sin(2 * PI * f * t) * 0.25
            }
            generatedSnd[i] = (sample * decay * 30000).toInt().toShort()
        }

        playPcmTrack(generatedSnd, sampleRate)
    }

    private fun playFanfare() {
        val sampleRate = 44100
        // Sequence of notes: C5, E5, G5, C6
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val noteDurationMs = 250
        val totalSamples = (sampleRate * (noteDurationMs * 4 / 1000f)).toInt()
        val generatedSnd = ShortArray(totalSamples)

        for (idx in notes.indices) {
            val freq = notes[idx]
            val startSample = idx * (sampleRate * (noteDurationMs / 1000f)).toInt()
            val endSample = startSample + (sampleRate * (noteDurationMs / 1000f)).toInt()

            for (i in startSample until endSample.coerceAtMost(totalSamples)) {
                val t = (i - startSample).toDouble() / sampleRate
                val decay = (1.0 - (t / (noteDurationMs / 1000.0) * 0.3)).coerceIn(0.0, 1.0)
                val sample = sin(2 * PI * freq * t)
                generatedSnd[i] = (sample * decay * 28000).toInt().toShort()
            }
        }

        playPcmTrack(generatedSnd, sampleRate)
    }

    private fun playCyberBeeps() {
        val sampleRate = 44100
        val durationMs = 800
        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // 3 short pulse beeps at 1200Hz
            val isBeep = (t in 0.0..0.15) || (t in 0.25..0.40) || (t in 0.50..0.75)
            val sample = if (isBeep) sin(2 * PI * 1200.0 * t) else 0.0
            generatedSnd[i] = (sample * 25000).toInt().toShort()
        }

        playPcmTrack(generatedSnd, sampleRate)
    }

    private fun playPcmTrack(audioData: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
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
            .setBufferSizeInBytes(audioData.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(audioData, 0, audioData.size)
        audioTrack.play()
    }
}
