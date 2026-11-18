package ru.aether.music.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer

/**
 * System Equalizer bound to an audio session id from ExoPlayer/AudioTrack.
 * Availability depends on device HAL.
 */
class EqualizerEngine {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    val numberOfBands: Int get() = equalizer?.numberOfBands?.toInt() ?: 0
    val bandLevelRange: ShortArray get() = equalizer?.bandLevelRange ?: shortArrayOf(0, 0)

    fun attach(audioSessionId: Int) {
        release()
        if (audioSessionId == 0) return
        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
            bassBoost = BassBoost(0, audioSessionId).apply { enabled = false }
            virtualizer = Virtualizer(0, audioSessionId).apply { enabled = false }
        } catch (_: Throwable) {
            release()
        }
    }

    fun setEnabled(v: Boolean) {
        equalizer?.enabled = v
    }

    fun setBandLevel(band: Int, level: Short) {
        val eq = equalizer ?: return
        if (band in 0 until eq.numberOfBands) {
            eq.setBandLevel(band.toShort(), level)
        }
    }

    fun getBandLevel(band: Int): Short =
        equalizer?.getBandLevel(band.toShort()) ?: 0

    fun setBassStrength(permille: Int) {
        bassBoost?.setStrength(permille.coerceIn(0, 1000).toShort())
        bassBoost?.enabled = permille > 0
    }

    fun setVirtualizer(permille: Int) {
        virtualizer?.setStrength(permille.coerceIn(0, 1000).toShort())
        virtualizer?.enabled = permille > 0
    }

    fun applyPresetFlat() {
        val eq = equalizer ?: return
        val mid = ((eq.bandLevelRange[0] + eq.bandLevelRange[1]) / 2).toShort()
        for (i in 0 until eq.numberOfBands) eq.setBandLevel(i.toShort(), 0)
    }

    fun applyNamedPreset(name: String) {
        val eq = equalizer ?: return
        val n = eq.numberOfBands.toInt()
        val shape = when (name.lowercase()) {
            "bass", "deep bass", "hip-hop" -> FloatArray(n) { i -> if (i < n / 3) 0.7f else if (i > 2 * n / 3) 0.2f else 0f }
            "rock", "metal" -> FloatArray(n) { i -> if (i == 0 || i == n - 1) 0.6f else if (i == n / 2) -0.2f else 0.3f }
            "classical", "jazz", "acoustic" -> FloatArray(n) { i -> if (i > n / 2) 0.4f else 0.1f }
            "electronic", "pop" -> FloatArray(n) { i -> if (i < 2 || i > n - 3) 0.5f else 0.15f }
            "vocal" -> FloatArray(n) { i -> if (i in n / 3..(2 * n / 3)) 0.55f else -0.1f }
            else -> FloatArray(n) { 0f }
        }
        val min = eq.bandLevelRange[0].toInt()
        val max = eq.bandLevelRange[1].toInt()
        for (i in 0 until n) {
            val level = (shape[i] * max).toInt().coerceIn(min, max).toShort()
            eq.setBandLevel(i.toShort(), level)
        }
        eq.enabled = true
    }

    fun release() {
        try { equalizer?.release() } catch (_: Throwable) {}
        try { bassBoost?.release() } catch (_: Throwable) {}
        try { virtualizer?.release() } catch (_: Throwable) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
