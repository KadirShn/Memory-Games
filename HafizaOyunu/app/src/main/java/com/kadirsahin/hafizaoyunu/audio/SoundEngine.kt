package com.kadirsahin.hafizaoyunu.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

enum class Sfx { FLIP, CLICK, MATCH, COMBO, MISMATCH, WIN, LOSE, ICE, COIN, TICK, SHUFFLE, ACHIEVEMENT, POWER_UP, ROUND }

/**
 * Low latency sound effects without shipping a single audio asset: every effect is synthesised once into a
 * small WAV file in the cache directory and then played through [SoundPool].
 */
class SoundEngine(context: Context) {
    private val pool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val soundIds = ConcurrentHashMap<Sfx, Int>()
    private val loaded = ConcurrentHashMap.newKeySet<Int>()

    @Volatile
    var enabled: Boolean = true

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status -> if (status == 0) loaded += sampleId }
        val directory = File(context.cacheDir, "sfx").apply { mkdirs() }
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            Sfx.entries.forEach { sfx ->
                val file = File(directory, "${sfx.name.lowercase()}_$SYNTH_VERSION.wav")
                if (!file.exists()) runCatching { file.writeBytes(Synth.render(sfx)) }
                if (file.exists()) soundIds[sfx] = pool.load(file.path, 1)
            }
        }
    }

    /** [rate] shifts the pitch (0.5–2.0); used to make combos climb. */
    fun play(sfx: Sfx, rate: Float = 1f, volume: Float = 1f) {
        if (!enabled) return
        val id = soundIds[sfx] ?: return
        if (id !in loaded) return
        pool.play(id, volume, volume, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    private companion object {
        const val SYNTH_VERSION = 3
    }
}
