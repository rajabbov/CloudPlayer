package com.example.cloudplayer.data.repository

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    fun setup(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBandCount(): Int = equalizer?.numberOfBands?.toInt() ?: 5

    fun getBandLevelRange(): Pair<Int, Int> {
        val range = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
        return Pair(range[0].toInt(), range[1].toInt())
    }

    fun getCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 1000000
    }

    fun getBandLevel(band: Short): Int {
        return equalizer?.getBandLevel(band)?.toInt() ?: 0
    }

    fun setBandLevel(band: Short, level: Int) {
        try {
            equalizer?.setBandLevel(band, level.toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setBassBoostStrength(strength: Int) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000).toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        equalizer = null
        bassBoost = null
    }
}
