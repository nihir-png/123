package com.designer.alarmclock.data

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Applies the user-selected language to a Context by overriding its resource
 * Configuration locale. This works on every supported API level (26+) without
 * needing AppCompat — call it from Activity.attachBaseContext so all resources
 * (including Compose stringResource lookups) resolve to the chosen language.
 *
 * An empty tag means "system default" → return the context unchanged so the
 * device locale is used.
 */
object LocaleHelper {

    fun wrap(context: Context, languageTag: String): Context {
        if (languageTag.isBlank()) return context

        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    /** Synchronously read the persisted language tag (used from attachBaseContext). */
    fun persistedTag(context: Context): String =
        kotlinx.coroutines.runBlocking { SettingsRepository(context).current().languageTag }
}
