package com.ivor.ivormusic.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small, independent preferences for device-level Yinnlycs experience settings. */
class ExperiencePreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _highRefreshRate = MutableStateFlow(isHighRefreshRateEnabled)
    val highRefreshRate: StateFlow<Boolean> = _highRefreshRate.asStateFlow()

    val isHighRefreshRateEnabled: Boolean
        get() = prefs.getBoolean(KEY_HIGH_REFRESH_RATE, false)

    /** Indonesian is the default; English is the only alternate app language. */
    val appLanguage: String
        get() = if (prefs.getString(KEY_APP_LANGUAGE, "id") == "en") "en" else "id"

    fun setHighRefreshRate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_REFRESH_RATE, enabled).apply()
        _highRefreshRate.value = enabled
    }

    fun setAppLanguage(language: String) {
        val safeLanguage = if (language == "en") "en" else "id"
        prefs.edit().putString(KEY_APP_LANGUAGE, safeLanguage).apply()
    }

    companion object {
        const val PREFS_NAME = "yinnlycs_experience_prefs"
        private const val KEY_HIGH_REFRESH_RATE = "high_refresh_rate"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}
