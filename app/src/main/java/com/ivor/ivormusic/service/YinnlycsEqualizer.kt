package com.ivor.ivormusic.service

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.widget.Toast
import com.ivor.ivormusic.R

/** Five-band app EQ attached to the same audio session used by Media3. */
object YinnlycsEqualizer {
    private const val PREFS = "yinnlycs_equalizer"
    private const val BAND_COUNT = 5
    private var effect: Equalizer? = null
    private var activeSessionId: Int = -1

    @Synchronized
    fun attach(context: Context, sessionId: Int) {
        release()
        if (sessionId <= 0) return
        activeSessionId = sessionId
        val instance = runCatching { Equalizer(0, sessionId) }.getOrNull() ?: return
        effect = instance
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val range = runCatching { instance.bandLevelRange }.getOrNull()
        runCatching {
            instance.enabled = prefs.getBoolean("enabled", false)
            if (range != null && range.size >= 2) {
                repeat(minOf(BAND_COUNT, instance.numberOfBands.toInt())) { index ->
                    val requested = prefs.getInt("band_" + index, 0) * 100
                    val level = requested.coerceIn(range[0].toInt(), range[1].toInt()).toShort()
                    instance.setBandLevel(index.toShort(), level)
                }
            }
        }
    }

    @Synchronized
    fun release() {
        runCatching { effect?.release() }
        effect = null
        activeSessionId = -1
    }

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean("enabled", enabled).apply()
        runCatching { effect?.enabled = enabled }
    }

    fun getBandLevels(context: Context): List<Int> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return (0 until BAND_COUNT).map { prefs.getInt("band_" + it, 0).coerceIn(-15, 15) }
    }

    @Synchronized
    fun setBandLevel(context: Context, band: Int, decibels: Int) {
        if (band !in 0 until BAND_COUNT) return
        val value = decibels.coerceIn(-15, 15)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt("band_" + band, value).apply()
        val current = effect ?: return
        runCatching {
            if (band < current.numberOfBands.toInt()) {
                val range = current.bandLevelRange
                val level = (value * 100).coerceIn(range[0].toInt(), range[1].toInt()).toShort()
                current.setBandLevel(band.toShort(), level)
            }
        }
    }

    fun openSystemEqualizer(context: Context) {
        val sessionId = activeSessionId
        if (sessionId <= 0) {
            Toast.makeText(context, R.string.settings_equalizer_session, Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            Toast.makeText(context, R.string.settings_equalizer_unavailable, Toast.LENGTH_LONG).show()
            return
        }
        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(context, R.string.settings_equalizer_unavailable, Toast.LENGTH_LONG).show()
            }
    }
}
