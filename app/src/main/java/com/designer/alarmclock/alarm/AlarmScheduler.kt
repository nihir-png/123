package com.designer.alarmclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.designer.alarmclock.data.Alarm

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return

        // Verify exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmScheduler", "Cannot schedule exact alarm: Permission missing")
            }
        }

        val triggerTime = alarm.getNextTriggerMillis()

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
        }

        // Distinct requestCode per alarm using its unique DB ID
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ShowIntent: Opened when tapping the upcoming system clock bar icon
        val mainIntent = Intent(context, com.designer.alarmclock.MainActivity::class.java)
        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prefer a precise alarm-clock entry. If exact-alarm permission was
        // revoked on Android 12+, setAlarmClock throws SecurityException — fall
        // back to an inexact-but-reliable alarm instead of crashing.
        Log.d("AlarmFlow", "AlarmScheduler.schedule() alarm=${alarm.id} triggerInMs=${triggerTime - System.currentTimeMillis()} " +
                "canScheduleExact=${canScheduleExact()} target=AlarmReceiver(broadcast)")
        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmFlow", "AlarmScheduler: setAlarmClock OK for alarm ${alarm.id} at $triggerTime")
        } catch (e: SecurityException) {
            Log.w("AlarmFlow", "AlarmScheduler: exact alarm not permitted; using fallback", e)
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /** True if the OS will let us schedule exact alarms (always true pre-Android 12). */
    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i("AlarmScheduler", "Cancelled alarm ${alarm.id}")
        }
    }
}
