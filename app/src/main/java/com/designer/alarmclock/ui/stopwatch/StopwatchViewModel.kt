package com.designer.alarmclock.ui.stopwatch

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Lap(
    val lapNumber: Int,
    val lapTime: Long,
    val totalTime: Long
)

class StopwatchViewModel : ViewModel() {
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _laps = MutableStateFlow<List<Lap>>(emptyList())
    val laps: StateFlow<List<Lap>> = _laps.asStateFlow()

    private var startTime = 0L
    private var accumulatedTime = 0L
    private var tickerJob: Job? = null

    fun start() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTime = SystemClock.elapsedRealtime()
        startTicker()
    }

    fun pause() {
        if (!_isRunning.value) return
        _isRunning.value = false
        accumulatedTime += SystemClock.elapsedRealtime() - startTime
        _elapsedTime.value = accumulatedTime
        stopTicker()
    }

    fun resume() {
        start()
    }

    fun reset() {
        pause()
        accumulatedTime = 0L
        _elapsedTime.value = 0L
        _laps.value = emptyList()
        _isRunning.value = false
    }

    fun recordLap() {
        val total = _elapsedTime.value
        val currentLaps = _laps.value
        val lapNumber = currentLaps.size + 1
        
        val lastLapTotal = currentLaps.lastOrNull()?.totalTime ?: 0L
        val lapTime = total - lastLapTotal
        
        val newLap = Lap(lapNumber, lapTime, total)
        _laps.value = currentLaps + newLap
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedTime.value = accumulatedTime + (SystemClock.elapsedRealtime() - startTime)
                delay(10) // Tick approximately every 10 milliseconds
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
