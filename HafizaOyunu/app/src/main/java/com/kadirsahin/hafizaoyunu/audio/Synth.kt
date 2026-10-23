package com.kadirsahin.hafizaoyunu.audio

import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/** A tiny additive synthesiser that renders the game's sound effects to 16-bit mono PCM. */
internal object Synth {
    private const val SAMPLE_RATE = 44_100

    private enum class Wave { SINE, TRIANGLE, SOFT_SQUARE, NOISE }

    private class Buffer(durationMs: Int) {
        val data = FloatArray(SAMPLE_RATE * durationMs / 1000)

        fun tone(
            startMs: Int,
            durationMs: Int,
            frequency: Double,
            endFrequency: Double = frequency,
            volume: Float = 0.5f,
            wave: Wave = Wave.SINE,
            attackMs: Int = 4,
            decay: Double = 6.0,
            vibrato: Double = 0.0,
        ) {
            val start = SAMPLE_RATE * startMs / 1000
            val length = SAMPLE_RATE * durationMs / 1000
            val attack = max(1, SAMPLE_RATE * attackMs / 1000)
            val fadeOut = SAMPLE_RATE * 6 / 1000
            val noise = Random(startMs * 31 + durationMs)
            var phase = 0.0
            var smoothedNoise = 0f
            for (i in 0 until length) {
                val index = start + i
                if (index >= data.size) break
                val t = i.toDouble() / SAMPLE_RATE
                val progress = i.toDouble() / length
                val freq = frequency + (endFrequency - frequency) * progress
                val wobble = if (vibrato > 0) 1 + 0.012 * sin(2 * PI * vibrato * t) else 1.0
                phase += 2 * PI * freq * wobble / SAMPLE_RATE
                val sample = when (wave) {
                    Wave.SINE -> sin(phase).toFloat()
                    Wave.TRIANGLE -> (2 / PI * kotlin.math.asin(sin(phase))).toFloat()
                    Wave.SOFT_SQUARE -> (kotlin.math.tanh(3 * sin(phase)) * 0.8).toFloat()
                    Wave.NOISE -> {
                        // A one pole filter whose cutoff follows the frequency argument.
                        val alpha = (freq / 8000.0).coerceIn(0.02, 1.0).toFloat()
                        smoothedNoise += alpha * (noise.nextFloat() * 2 - 1 - smoothedNoise)
                        smoothedNoise * 1.6f
                    }
                }
                var envelope = if (i < attack) i.toFloat() / attack else exp(-decay * (t - attackMs / 1000.0)).toFloat()
                if (i > length - fadeOut) envelope *= (length - i).toFloat() / fadeOut
                data[index] += sample * envelope * volume
            }
        }

        /** A bell: fundamental plus an inharmonic partial gives the "chime" character. */
        fun bell(startMs: Int, durationMs: Int, frequency: Double, volume: Float = 0.5f, decay: Double = 5.0) {
            tone(startMs, durationMs, frequency, volume = volume, decay = decay)
            tone(startMs, durationMs, frequency * 2.0, volume = volume * 0.35f, decay = decay * 1.6)
            tone(startMs, durationMs / 2, frequency * 2.76, volume = volume * 0.18f, decay = decay * 2.5)
        }

        fun toWav(): ByteArray {
            val peak = data.maxOfOrNull { abs(it) }?.takeIf { it > 0f } ?: 1f
            val gain = 0.88f / max(peak, 0.88f)
            val pcm = ByteArray(data.size * 2)
            data.forEachIndexed { i, value ->
                val sample = (value * gain * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767)
                pcm[i * 2] = (sample and 0xFF).toByte()
                pcm[i * 2 + 1] = (sample shr 8 and 0xFF).toByte()
            }
            return ByteArrayOutputStream().apply {
                write("RIFF".toByteArray())
                writeInt(36 + pcm.size)
                write("WAVEfmt ".toByteArray())
                writeInt(16)
                writeShort(1)
                writeShort(1)
                writeInt(SAMPLE_RATE)
                writeInt(SAMPLE_RATE * 2)
                writeShort(2)
                writeShort(16)
                write("data".toByteArray())
                writeInt(pcm.size)
                write(pcm)
            }.toByteArray()
        }

        private fun ByteArrayOutputStream.writeInt(value: Int) {
            write(value and 0xFF)
            write(value shr 8 and 0xFF)
            write(value shr 16 and 0xFF)
            write(value shr 24 and 0xFF)
        }

        private fun ByteArrayOutputStream.writeShort(value: Int) {
            write(value and 0xFF)
            write(value shr 8 and 0xFF)
        }
    }

    fun render(sfx: Sfx): ByteArray = when (sfx) {
        Sfx.FLIP -> Buffer(90).apply {
            tone(0, 45, 3200.0, 900.0, volume = 0.22f, wave = Wave.NOISE, attackMs = 2, decay = 40.0)
            tone(0, 70, 1500.0, 620.0, volume = 0.3f, attackMs = 2, decay = 32.0)
        }
        Sfx.CLICK -> Buffer(50).apply {
            tone(0, 40, 1250.0, 900.0, volume = 0.35f, attackMs = 1, decay = 60.0)
        }
        Sfx.MATCH -> Buffer(520).apply {
            bell(0, 420, 880.0, volume = 0.45f, decay = 7.0)
            bell(70, 450, 1318.5, volume = 0.4f, decay = 6.0)
        }
        Sfx.COMBO -> Buffer(620).apply {
            listOf(1046.5, 1318.5, 1568.0, 2093.0).forEachIndexed { i, f ->
                bell(i * 55, 420, f, volume = 0.32f, decay = 7.0)
            }
            tone(220, 380, 4186.0, volume = 0.05f, decay = 9.0, vibrato = 7.0)
        }
        Sfx.MISMATCH -> Buffer(340).apply {
            tone(0, 170, 330.0, 270.0, volume = 0.32f, wave = Wave.TRIANGLE, decay = 9.0)
            tone(120, 210, 247.0, 196.0, volume = 0.32f, wave = Wave.TRIANGLE, decay = 8.0)
        }
        Sfx.WIN -> Buffer(1600).apply {
            listOf(523.25, 659.25, 783.99, 1046.5).forEachIndexed { i, f ->
                bell(i * 110, 500, f, volume = 0.38f, decay = 5.0)
            }
            listOf(1046.5, 1318.5, 1568.0).forEach { f -> bell(470, 1100, f, volume = 0.26f, decay = 2.6) }
            tone(470, 1100, 523.25, volume = 0.18f, wave = Wave.TRIANGLE, decay = 2.4, vibrato = 5.0)
        }
        Sfx.LOSE -> Buffer(1100).apply {
            listOf(392.0, 329.6, 261.6).forEachIndexed { i, f ->
                tone(i * 190, if (i == 2) 800 else 260, f, f * 0.97, volume = 0.3f, wave = Wave.TRIANGLE, decay = if (i == 2) 2.5 else 6.0)
            }
        }
        Sfx.ICE -> Buffer(220).apply {
            tone(0, 90, 7000.0, 3000.0, volume = 0.35f, wave = Wave.NOISE, attackMs = 1, decay = 30.0)
            tone(0, 160, 2637.0, volume = 0.18f, decay = 18.0)
            tone(20, 150, 3520.0, volume = 0.14f, decay = 20.0)
        }
        Sfx.COIN -> Buffer(420).apply {
            tone(0, 70, 987.8, volume = 0.3f, wave = Wave.SOFT_SQUARE, decay = 12.0)
            tone(65, 340, 1318.5, volume = 0.3f, wave = Wave.SOFT_SQUARE, decay = 7.0)
        }
        Sfx.TICK -> Buffer(60).apply {
            tone(0, 40, 1000.0, volume = 0.3f, attackMs = 1, decay = 45.0)
        }
        Sfx.SHUFFLE -> Buffer(520).apply {
            repeat(5) { i ->
                tone(i * 80, 110, 2500.0 + i * 500, 900.0, volume = 0.22f, wave = Wave.NOISE, attackMs = 30, decay = 18.0)
            }
        }
        Sfx.ACHIEVEMENT -> Buffer(1000).apply {
            listOf(783.99, 987.77, 1174.66, 1567.98).forEachIndexed { i, f ->
                bell(i * 70, 700, f, volume = 0.3f, decay = 3.6)
            }
            tone(280, 700, 3135.0, volume = 0.06f, decay = 4.0, vibrato = 9.0)
        }
        Sfx.POWER_UP -> Buffer(420).apply {
            tone(0, 320, 380.0, 1650.0, volume = 0.3f, wave = Wave.TRIANGLE, attackMs = 10, decay = 3.5, vibrato = 14.0)
            bell(260, 160, 2093.0, volume = 0.18f, decay = 14.0)
        }
        Sfx.ROUND -> Buffer(700).apply {
            listOf(659.25, 880.0, 1174.66).forEachIndexed { i, f -> bell(i * 90, 520, f, volume = 0.35f, decay = 5.0) }
        }
    }.toWav()
}
