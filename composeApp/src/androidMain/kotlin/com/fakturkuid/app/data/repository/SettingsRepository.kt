package com.fakturkuid.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.fakturkuid.app.widget.FakturkuWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsRepository(private val context: Context) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    private fun triggerWidgetUpdate() {
        repositoryScope.launch {
            try {
                FakturkuWidget().updateAll(context)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "app_theme"           // "dark" or "light"
        private const val KEY_LANGUAGE = "app_language"     // "id", "en", "jp", "cn"
        private const val KEY_CURRENCY = "app_currency"     // "id", "en", "ja", "zh"
        private const val KEY_APP_LOCK = "app_lock_enabled" // true or false
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
    }

    // Theme
    fun getTheme(): String = prefs.getString(KEY_THEME, "dark") ?: "dark"
    fun setTheme(theme: String) = prefs.edit().putString(KEY_THEME, theme).apply()

    // Language
    fun getLanguage(): String {
        var lang = prefs.getString(KEY_LANGUAGE, "id") ?: "id"
        if (lang == "jp") { lang = "ja"; setLanguage("ja") }
        if (lang == "cn") { lang = "zh"; setLanguage("zh") }
        return lang
    }
    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        triggerWidgetUpdate()
    }

    // Currency
    fun getCurrency(): String {
        var curr = prefs.getString(KEY_CURRENCY, "id") ?: "id"
        if (curr == "jp") { curr = "ja"; setCurrency("ja") }
        if (curr == "cn") { curr = "zh"; setCurrency("zh") }
        return curr
    }
    fun setCurrency(curr: String) {
        prefs.edit().putString(KEY_CURRENCY, curr).apply()
        triggerWidgetUpdate()
    }

    // App Lock
    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK, false)
    fun setAppLockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply()

    // Onboarding (one-time permission flow)
    fun isOnboardingDone(): Boolean = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    fun setOnboardingDone(done: Boolean) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()

    // Cache
    fun clearCache(context: Context) {
        context.cacheDir.deleteRecursively()
    }
}
