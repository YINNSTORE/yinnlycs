package com.ivor.ivormusic.data

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/** Follows the system by default and uses Android's per-app locale API where available. */
object AppLocaleManager {
    fun wrap(base: Context): Context {
        val tag = ExperiencePreferences(base).appLanguage
        if (Build.VERSION.SDK_INT >= 33) {
            val manager = base.getSystemService(LocaleManager::class.java)
            val requestedLocales = if (tag == "system") {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(tag)
            }
            if (manager.applicationLocales.toLanguageTags() != requestedLocales.toLanguageTags()) {
                manager.applicationLocales = requestedLocales
            }
            return base
        }
        if (tag == "system") {
            // Keep the process default in step with the phone when the user
            // returns from an app-specific language.
            val systemLocales = base.resources.configuration.locales
            if (!systemLocales.isEmpty) Locale.setDefault(systemLocales[0])
            return base
        }
        val requestedLocales = LocaleList.forLanguageTags(tag)
        val locale = requestedLocales[0] ?: Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val configuration = Configuration(base.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(requestedLocales)
        } else {
            configuration.setLocale(locale)
        }
        configuration.setLayoutDirection(locale)
        return base.createConfigurationContext(configuration)
    }

    fun currentLanguage(context: Context): String {
        val savedLanguage = ExperiencePreferences(context).appLanguage
        if (Build.VERSION.SDK_INT >= 33) {
            val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
            if (locales.isEmpty) return savedLanguage
            return locales[0]?.language ?: savedLanguage
        }
        return savedLanguage
    }

    fun setLanguage(context: Context, language: String) {
        ExperiencePreferences(context).setAppLanguage(language)
        if (Build.VERSION.SDK_INT >= 33) {
            val manager = context.getSystemService(LocaleManager::class.java)
            manager.applicationLocales = if (language == "system") {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(language)
            }
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
