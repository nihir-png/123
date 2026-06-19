package com.designer.alarmclock.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.designer.alarmclock.data.AppSettings
import com.designer.alarmclock.data.SettingsRepository
import com.designer.alarmclock.data.ThemeMode
import com.designer.alarmclock.data.TimeFormat
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    val settings: StateFlow<AppSettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun completeOnboarding() = viewModelScope.launch { repo.setOnboardingCompleted(true) }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }

    fun setTimeFormat(format: TimeFormat) = viewModelScope.launch { repo.setTimeFormat(format) }

    fun setDefaultRingtone(uri: String, name: String) =
        viewModelScope.launch { repo.setDefaultRingtone(uri, name) }

    fun setDefaultVibrate(value: Boolean) = viewModelScope.launch { repo.setDefaultVibrate(value) }

    fun setDefaultSnoozeMinutes(minutes: Int) =
        viewModelScope.launch { repo.setDefaultSnoozeMinutes(minutes) }

    fun setDefaultVolume(volume: Int) = viewModelScope.launch { repo.setDefaultVolume(volume) }

    /**
     * Persist the chosen language, then invoke [onApplied] once the write has
     * committed. The caller recreates the Activity there; on recreation
     * MainActivity.attachBaseContext re-reads this tag and wraps the context in
     * the new locale, so the whole app refreshes immediately and consistently on
     * every API level (no AppCompat needed). The tag survives restarts because it
     * lives in DataStore.
     */
    fun setLanguage(tag: String, onApplied: () -> Unit) {
        viewModelScope.launch {
            repo.setLanguageTag(tag)
            onApplied()
        }
    }
}
