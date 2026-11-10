package com.ivor.ivormusic.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small, independent preferences for optional device-level Yinnlycs features. */
class ExperiencePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _highRefreshRate = MutableStateFlow(isHighRefreshRateEnabled)
    val highRefreshRate: StateFlow<Boolean> = _highRefreshRate.asStateFlow()

    private val _dynamicIslandEnabled = MutableStateFlow(isDynamicIslandEnabled)
    val dynamicIslandEnabled: StateFlow<Boolean> = _dynamicIslandEnabled.asStateFlow()

    private val _dynamicIslandWidthDp = MutableStateFlow(
        prefs.getInt(KEY_ISLAND_WIDTH, DEFAULT_ISLAND_WIDTH).coerceIn(220, 360)
    )
    val dynamicIslandWidthDp: StateFlow<Int> = _dynamicIslandWidthDp.asStateFlow()

    private val _dynamicIslandHeightDp = MutableStateFlow(
        prefs.getInt(KEY_ISLAND_HEIGHT, DEFAULT_ISLAND_HEIGHT).coerceIn(36, 64)
    )
    val dynamicIslandHeightDp: StateFlow<Int> = _dynamicIslandHeightDp.asStateFlow()

    private val _dynamicIslandTheme = MutableStateFlow(
        prefs.getString(KEY_ISLAND_THEME, DEFAULT_ISLAND_THEME) ?: DEFAULT_ISLAND_THEME
    )
    val dynamicIslandTheme: StateFlow<String> = _dynamicIslandTheme.asStateFlow()

    val isHighRefreshRateEnabled: Boolean
        get() = prefs.getBoolean(KEY_HIGH_REFRESH_RATE, false)

    val isDynamicIslandEnabled: Boolean
        get() = prefs.getBoolean(KEY_ISLAND_ENABLED, false)

    val appLanguage: String
        get() = prefs.getString(KEY_APP_LANGUAGE, "system") ?: "system"

    fun setHighRefreshRate(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HIGH_REFRESH_RATE, enabled).apply()
        _highRefreshRate.value = enabled
    }

    fun setDynamicIslandEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ISLAND_ENABLED, enabled).apply()
        _dynamicIslandEnabled.value = enabled
    }

    fun setDynamicIslandWidthDp(width: Int) {
        val value = width.coerceIn(220, 360)
        prefs.edit().putInt(KEY_ISLAND_WIDTH, value).apply()
        _dynamicIslandWidthDp.value = value
    }

    fun setDynamicIslandHeightDp(height: Int) {
        val value = height.coerceIn(36, 64)
        prefs.edit().putInt(KEY_ISLAND_HEIGHT, value).apply()
        _dynamicIslandHeightDp.value = value
    }

    fun setDynamicIslandTheme(theme: String) {
        val value = theme.takeIf { it in ISLAND_THEMES } ?: DEFAULT_ISLAND_THEME
        prefs.edit().putString(KEY_ISLAND_THEME, value).apply()
        _dynamicIslandTheme.value = value
    }

    fun setAppLanguage(language: String) {
        prefs.edit().putString(KEY_APP_LANGUAGE, language).apply()
    }

    companion object {
        const val PREFS_NAME = "yinnlycs_experience_prefs"
        const val DEFAULT_ISLAND_WIDTH = 276
        const val DEFAULT_ISLAND_HEIGHT = 44
        const val DEFAULT_ISLAND_THEME = "glass"
        val ISLAND_THEMES = setOf("glass", "violet", "ocean")

        private const val KEY_HIGH_REFRESH_RATE = "high_refresh_rate"
        private const val KEY_ISLAND_ENABLED = "dynamic_island_enabled"
        private const val KEY_ISLAND_WIDTH = "dynamic_island_width"
        private const val KEY_ISLAND_HEIGHT = "dynamic_island_height"
        private const val KEY_ISLAND_THEME = "dynamic_island_theme"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}
