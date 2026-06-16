package com.designer.alarmclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val repeatDays: List<Int> = emptyList(), // 1 = Mon, 2 = Tue, ..., 7 = Sun
    val label: String = "",
    val isVibrate: Boolean = true,
    val snoozeDurationMinutes: Int = 5
) {
    val formattedTime: String
        get() {
            val hour12 = if (hour % 12 == 0) 12 else hour % 12
            val amPm = if (hour < 12) "AM" else "PM"
            return String.format("%d:%02d %s", hour12, minute, amPm)
        }

    val isRepeating: Boolean
        get() = repeatDays.isNotEmpty()

    fun getNextTriggerMillis(): Long {
        val now = java.util.Calendar.getInstance()
        val alarmTime = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (repeatDays.isEmpty()) {
            // One-time alarm
            if (alarmTime.before(now)) {
                alarmTime.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
            return alarmTime.timeInMillis
        } else {
            // Repeating alarm
            var minTriggerTime = Long.MAX_VALUE
            val currentDayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday, ...
            
            // Map Calendar days (1=Sun, 2=Mon) to our repeatDays (1=Mon, 2=Tue, ..., 7=Sun)
            val calendarToOurDay = mapOf(
                java.util.Calendar.MONDAY to 1,
                java.util.Calendar.TUESDAY to 2,
                java.util.Calendar.WEDNESDAY to 3,
                java.util.Calendar.THURSDAY to 4,
                java.util.Calendar.FRIDAY to 5,
                java.util.Calendar.SATURDAY to 6,
                java.util.Calendar.SUNDAY to 7
            )
            
            val ourToCalendarDay = calendarToOurDay.entries.associate { it.value to it.key }

            for (day in repeatDays) {
                val targetCalendarDay = ourToCalendarDay[day] ?: continue
                val tempAlarm = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, hour)
                    set(java.util.Calendar.MINUTE, minute)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                
                var daysDiff = targetCalendarDay - currentDayOfWeek
                if (daysDiff < 0 || (daysDiff == 0 && tempAlarm.before(now))) {
                    daysDiff += 7
                }
                tempAlarm.add(java.util.Calendar.DAY_OF_YEAR, daysDiff)
                
                if (tempAlarm.timeInMillis < minTriggerTime) {
                    minTriggerTime = tempAlarm.timeInMillis
                }
            }
            return minTriggerTime
        }
    }
}
