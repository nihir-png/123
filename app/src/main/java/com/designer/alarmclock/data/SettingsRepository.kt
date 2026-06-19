package com.designer.alarmclock.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// ─────────────────────────────────────────────────────────────────────────────
// Single, app-wide DataStore. A `preferencesDataStore(name = ...)` delegate may
// be declared only ONCE per file name in the whole process — declaring it twice
// crashes at runtime. Everything (onboarding flag + all settings) lives here.
// ─────────────────────────────────────────────────────────────────────────────
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Light / Dark / follow-system. Default is LIGHT to preserve the existing design. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/** 12-hour (AM/PM) vs 24-hour clock display. */
enum class TimeFormat { TWELVE_HOUR, TWENTY_FOUR_HOUR }

/** Immutable snapshot of all persisted preferences. */
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val languageTag: String = "",                       // "" = system default
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val timeFormat: TimeFormat = TimeFormat.TWELVE_HOUR,
    val defaultRingtoneUri: String = "",                // "" = system default alarm sound
    val defaultRingtoneName: String = "Default (System)",
    val defaultVibrate: Boolean = true,
    val defaultSnoozeMinutes: Int = 10,
    val defaultVolume: Int = 80                          // 0..100 (relative alarm volume)
)

private object SettingsKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val TIME_FORMAT = stringPreferencesKey("time_format")
    val DEFAULT_RINGTONE_URI = stringPreferencesKey("default_ringtone_uri")
    val DEFAULT_RINGTONE_NAME = stringPreferencesKey("default_ringtone_name")
    val DEFAULT_VIBRATE = booleanPreferencesKey("default_vibrate")
    val DEFAULT_SNOOZE = intPreferencesKey("default_snooze_minutes")
    val DEFAULT_VOLUME = intPreferencesKey("default_volume")
}

private fun Preferences.toAppSettings(): AppSettings = AppSettings(
    onboardingCompleted = this[SettingsKeys.ONBOARDING_COMPLETED] ?: false,
    languageTag = this[SettingsKeys.LANGUAGE_TAG] ?: "",
    themeMode = this[SettingsKeys.THEME_MODE]
        ?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.LIGHT,
    timeFormat = this[SettingsKeys.TIME_FORMAT]
        ?.let { runCatching { TimeFormat.valueOf(it) }.getOrNull() } ?: TimeFormat.TWELVE_HOUR,
    defaultRingtoneUri = this[SettingsKeys.DEFAULT_RINGTONE_URI] ?: "",
    defaultRingtoneName = this[SettingsKeys.DEFAULT_RINGTONE_NAME] ?: "Default (System)",
    defaultVibrate = this[SettingsKeys.DEFAULT_VIBRATE] ?: true,
    defaultSnoozeMinutes = this[SettingsKeys.DEFAULT_SNOOZE] ?: 10,
    defaultVolume = (this[SettingsKeys.DEFAULT_VOLUME] ?: 80).coerceIn(0, 100)
)

/**
 * Thin wrapper over the shared DataStore. Exposes a reactive [settings] flow plus
 * suspend setters. Persisted values survive app restarts automatically.
 */
class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { it.toAppSettings() }

    /** One-shot read for non-Compose callers (e.g. AlarmService). */
    suspend fun current(): AppSettings = context.settingsDataStore.data.first().toAppSettings()

    suspend fun setOnboardingCompleted(value: Boolean) =
        context.settingsDataStore.edit { it[SettingsKeys.ONBOARDING_COMPLETED] = value }

    suspend fun setLanguageTag(tag: String) =
        context.settingsDataStore.edit { it[SettingsKeys.LANGUAGE_TAG] = tag }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.settingsDataStore.edit { it[SettingsKeys.THEME_MODE] = mode.name }

    suspend fun setTimeFormat(format: TimeFormat) =
        context.settingsDataStore.edit { it[SettingsKeys.TIME_FORMAT] = format.name }

    suspend fun setDefaultRingtone(uri: String, name: String) =
        context.settingsDataStore.edit {
            it[SettingsKeys.DEFAULT_RINGTONE_URI] = uri
            it[SettingsKeys.DEFAULT_RINGTONE_NAME] = name
        }

    suspend fun setDefaultVibrate(value: Boolean) =
        context.settingsDataStore.edit { it[SettingsKeys.DEFAULT_VIBRATE] = value }

    suspend fun setDefaultSnoozeMinutes(minutes: Int) =
        context.settingsDataStore.edit { it[SettingsKeys.DEFAULT_SNOOZE] = minutes }

    suspend fun setDefaultVolume(volume: Int) =
        context.settingsDataStore.edit { it[SettingsKeys.DEFAULT_VOLUME] = volume.coerceIn(0, 100) }
}
