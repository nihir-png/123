package com.designer.alarmclock.ui.timer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val _totalDuration = MutableStateFlow(0L) // in milliseconds
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _timeRemaining = MutableStateFlow(0L) // in milliseconds
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private var startTime = 0L
    private var timerJob: Job? = null

    fun setDuration(millis: Long) {
        _totalDuration.value = millis
        _timeRemaining.value = millis
        _isFinished.value = false
    }

    fun start() {
        if (_isRunning.value || _totalDuration.value <= 0L) return
        _isRunning.value = true
        _isFinished.value = false
        startTime = SystemClock.elapsedRealtime()
        startTicker()
    }

    fun pause() {
        if (!_isRunning.value) return
        _isRunning.value = false
        _totalDuration.value = _timeRemaining.value // Shrink total duration to remaining time for offset calculations
        stopTicker()
    }

    fun resume() {
        if (_timeRemaining.value <= 0L) return
        _isRunning.value = true
        startTime = SystemClock.elapsedRealtime()
        _totalDuration.value = _timeRemaining.value
        startTicker()
    }

    fun reset(originalDurationMillis: Long = 0L) {
        pause()
        _isRunning.value = false
        _isFinished.value = false
        _totalDuration.value = originalDurationMillis
        _timeRemaining.value = originalDurationMillis
        stopTicker()
    }

    private fun startTicker() {
        timerJob?.cancel()
        val currentTotal = _totalDuration.value
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                val remaining = currentTotal - elapsed
                
                if (remaining <= 0L) {
                    _timeRemaining.value = 0L
                    _isRunning.value = false
                    _isFinished.value = true
                    triggerFinishedNotification()
                    stopTicker()
                } else {
                    _timeRemaining.value = remaining
                    delay(50) // Update approximately 20 times per second for smooth circular progress animation
                }
            }
        }
    }

    private fun stopTicker() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun triggerFinishedNotification() {
        val context = getApplication<Application>().applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Timer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_channel",
                "Timer Finished",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when your countdown timers finish."
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notification = NotificationCompat.Builder(context, "timer_channel")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Timer Finished")
            .setContentText("Your countdown has completed!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
