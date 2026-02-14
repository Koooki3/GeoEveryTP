package com.example.geoeverytp

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Application entry: applies saved language (API 33+ [LocaleManager]) on create
 * and when [applySavedLanguage] is called after user changes language in settings.
 */
class GeoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        applySavedLanguage()
    }

    /** Applies language from [PREFS_NAME]/[KEY_LANGUAGE]; no-op on API &lt; 33. Call after user changes language. */
    fun applySavedLanguage() {
        if (Build.VERSION.SDK_INT < 33) return
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val tag = prefs.getString(KEY_LANGUAGE, VALUE_FOLLOW_SYSTEM) ?: VALUE_FOLLOW_SYSTEM
        val localeManager = getSystemService(Context.LOCALE_SERVICE) as android.app.LocaleManager
        when (tag) {
            VALUE_LANG_ZH -> localeManager.setApplicationLocales(LocaleList(Locale.SIMPLIFIED_CHINESE))
            VALUE_LANG_EN -> localeManager.setApplicationLocales(LocaleList(Locale.ENGLISH))
            else -> localeManager.setApplicationLocales(LocaleList.getEmptyLocaleList())
        }
    }

    companion object {
        const val PREFS_NAME = "geoeverytp_prefs"
        const val KEY_LANGUAGE = "language"
        const val VALUE_FOLLOW_SYSTEM = "system"
        const val VALUE_LANG_ZH = "zh"
        const val VALUE_LANG_EN = "en"
    }
}
