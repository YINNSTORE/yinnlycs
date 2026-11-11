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
        val requestedLocales = LocaleList.forLanguageTags(tag)

        if (Build.VERSION.SDK_INT >= 33) {
            val manager = base.getSystemService(LocaleManager::class.java)
            if (manager.applicationLocales.toLanguageTags() != requestedLocales.toLanguageTags()) {
                manager.applicationLocales = requestedLocales
            }
            return base
        }

        val locale = requestedLocales[0] ?: Locale.forLanguageTag(tag)
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
            context.findActivity()?.recreate()
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
