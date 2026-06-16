package com.designer.alarmclock.ui.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.designer.alarmclock.alarm.AlarmScheduler
import com.designer.alarmclock.data.Alarm
import com.designer.alarmclock.data.AlarmDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AlarmDatabase.getDatabase(application)
    private val dao = db.alarmDao()
    private val scheduler = AlarmScheduler(application)

    val alarms: StateFlow<List<Alarm>> = dao.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(
        hour: Int, 
        minute: Int, 
        repeatDays: List<Int>, 
        label: String, 
        isVibrate: Boolean, 
        snoozeMinutes: Int
    ) {
        viewModelScope.launch {
            val alarm = Alarm(
                hour = hour,
                minute = minute,
                repeatDays = repeatDays,
                label = label,
                isVibrate = isVibrate,
                snoozeDurationMinutes = snoozeMinutes
            )
            val id = dao.insertAlarm(alarm)
            scheduler.schedule(alarm.copy(id = id.toInt()))
        }
    }

    fun updateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            dao.updateAlarm(alarm)
            if (alarm.isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm)
            }
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            dao.updateAlarm(updated)
            if (updated.isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            dao.deleteAlarm(alarm)
        }
    }
}
