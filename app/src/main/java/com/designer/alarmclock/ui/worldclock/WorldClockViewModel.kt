package com.designer.alarmclock.ui.worldclock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.designer.alarmclock.data.AlarmDatabase
import com.designer.alarmclock.data.WorldClockCity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PredefinedCity(
    val cityName: String,
    val country: String,
    val timezoneId: String
)

class WorldClockViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getDatabase(application)
    private val dao = db.worldClockDao()

    val selectedCities: StateFlow<List<WorldClockCity>> = dao.getAllCities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val predefinedCities = listOf(
        PredefinedCity("London", "United Kingdom", "Europe/London"),
        PredefinedCity("New York", "United States", "America/New_York"),
        PredefinedCity("Tokyo", "Japan", "Asia/Tokyo"),
        PredefinedCity("Paris", "France", "Europe/Paris"),
        PredefinedCity("Sydney", "Australia", "Australia/Sydney"),
        PredefinedCity("Singapore", "Singapore", "Asia/Singapore"),
        PredefinedCity("Mumbai", "India", "Asia/Kolkata"),
        PredefinedCity("Dubai", "United Arab Emirates", "Asia/Dubai"),
        PredefinedCity("Los Angeles", "United States", "America/Los_Angeles"),
        PredefinedCity("Cairo", "Egypt", "Africa/Cairo"),
        PredefinedCity("Rio de Janeiro", "Brazil", "America/Sao_Paulo"),
        PredefinedCity("Cape Town", "South Africa", "Africa/Johannesburg"),
        PredefinedCity("Hong Kong", "China", "Asia/Hong_Kong")
    )

    fun addCity(city: PredefinedCity) {
        viewModelScope.launch {
            dao.insertCity(
                WorldClockCity(
                    cityName = city.cityName,
                    country = city.country,
                    timezoneId = city.timezoneId
                )
            )
        }
    }

    fun removeCity(city: WorldClockCity) {
        viewModelScope.launch {
            dao.deleteCity(city)
        }
    }
}
