package com.ivor.ivormusic.data

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/** Applies Indonesian by default and keeps English available as an explicit choice. */
object AppLocaleManager {
    fun wrap(base: Context): Context {
        val tag = ExperiencePreferences(base).appLanguage

        if (Build.VERSION.SDK_INT >= 33) {
            val requestedLocales = LocaleList.forLanguageTags(tag)
            val manager = base.getSystemService(LocaleManager::class.java)
            if (manager.applicationLocales.toLanguageTags() != requestedLocales.toLanguageTags()) {
                manager.applicationLocales = requestedLocales
            }
            return base
        }

        // Android 11–12 use the legacy ISO code "in" for Indonesian resources.
        // Keep the saved/app-facing code as "id", but resolve resources through
        // the legacy qualifier so values-in is selected on older Android builds.
        val resourceTag = if (tag == "id") "in" else tag
        val requestedLocales = LocaleList.forLanguageTags(resourceTag)
        val locale = requestedLocales[0] ?: Locale.forLanguageTag(resourceTag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration).apply {
            setLocales(requestedLocales)
            setLayoutDirection(locale)
        }
        return base.createConfigurationContext(configuration)
    }

    fun currentLanguage(context: Context): String {
        val savedLanguage = ExperiencePreferences(context).appLanguage
        if (Build.VERSION.SDK_INT >= 33) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (!locales.isEmpty) return locales[0]?.language ?: savedLanguage
        }
        return savedLanguage
    }

    fun setLanguage(context: Context, language: String) {
        val safeLanguage = if (language == "en") "en" else "id"
        ExperiencePreferences(context).setAppLanguage(safeLanguage)
        if (Build.VERSION.SDK_INT >= 33) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(safeLanguage)
        } else {
            // Match the legacy Android resource qualifier used by API 30–32.
            val resourceTag = if (safeLanguage == "id") "in" else safeLanguage
            val locale = Locale.forLanguageTag(resourceTag)
            Locale.setDefault(locale)
            context.findActivity()?.recreate()
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
