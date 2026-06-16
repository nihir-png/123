package com.designer.alarmclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "world_clock_cities")
data class WorldClockCity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val country: String,
    val timezoneId: String
) {
    fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("hh:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date())
    }

    fun getCurrentAmPm(): String {
        val sdf = SimpleDateFormat("a", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone(timezoneId)
        return sdf.format(Date())
    }

    fun getTimeDifferenceString(): String {
        val targetTz = TimeZone.getTimeZone(timezoneId)
        val localTz = TimeZone.getDefault()
        val now = System.currentTimeMillis()
        
        val diffMillis = targetTz.getOffset(now) - localTz.getOffset(now)
        val diffMinutes = Math.abs(diffMillis) / (1000 * 60)
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        val direction = if (diffMillis >= 0) "+" else "-"
        val timeDiff = when {
            diffMillis == 0 -> "Same as local"
            minutes == 0 -> "$direction${hours}h"
            else -> "$direction${hours}h ${minutes}m"
        }
        
        val relativeDay = getRelativeDayString()
        return if (diffMillis == 0) timeDiff else "$timeDiff, $relativeDay"
    }

    private fun getRelativeDayString(): String {
        val targetCal = Calendar.getInstance(TimeZone.getTimeZone(timezoneId))
        val localCal = Calendar.getInstance()
        
        val targetDay = targetCal.get(Calendar.DAY_OF_YEAR)
        val localDay = localCal.get(Calendar.DAY_OF_YEAR)
        val targetYear = targetCal.get(Calendar.YEAR)
        val localYear = localCal.get(Calendar.YEAR)
        
        return when {
            targetYear == localYear && targetDay == localDay -> "Today"
            targetYear == localYear && targetDay == localDay + 1 -> "Tomorrow"
            targetYear == localYear && targetDay == localDay - 1 -> "Yesterday"
            targetYear > localYear -> "Tomorrow"
            targetYear < localYear -> "Yesterday"
            targetDay > localDay -> "Tomorrow"
            else -> "Yesterday"
        }
    }
}
