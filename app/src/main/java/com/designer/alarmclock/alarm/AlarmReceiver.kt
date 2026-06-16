package com.designer.alarmclock.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        // Single tag so the whole alarm flow can be watched with:
        //   adb logcat -s AlarmFlow
        const val TAG = "AlarmFlow"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        Log.d(TAG, "AlarmReceiver.onReceive() FIRED — real alarm trigger received. ALARM_ID=$alarmId")

        // Log runtime capability state so it's visible in logcat why the
        // full-screen UI may or may not appear automatically.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d(TAG, "canUseFullScreenIntent=${runCatching { nm.canUseFullScreenIntent() }.getOrNull()}, " +
                    "notificationsEnabled=${nm.areNotificationsEnabled()}")
        }

        if (alarmId == -1) {
            Log.w(TAG, "AlarmReceiver.onReceive(): ALARM_ID == -1, aborting.")
            return
        }

        // Acquire a FULL wake lock so the CPU stays on AND the screen lights up.
        // A PARTIAL_WAKE_LOCK only keeps the CPU running but does NOT turn on the
        // screen, which is why the activity was launching "behind" the lock screen.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE,
            "AlarmClock:AlarmReceiverWakeLock"
        )
        wakeLock.acquire(30_000L) // 30 seconds — enough for the activity to take over

        // 1) Start the foreground service FIRST.
        //    The service needs to call startForeground() within 5 seconds,
        //    and it handles audio + vibration + the backup notification.
        try {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "AlarmReceiver: startForegroundService(AlarmService) OK for alarm $alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "AlarmReceiver: failed to start AlarmService", e)
        }

        // 2) Launch the full-screen ringing Activity directly.
        //    BroadcastReceiver.onReceive() runs in a context that is allowed
        //    to start activities (it is considered a "foreground" context by
        //    the OS). Combined with the FULL wake lock above and the manifest
        //    attributes showWhenLocked + turnScreenOn, this will bring the
        //    alarm screen right to the front even from a locked / screen-off state.
        try {
            val activityIntent = Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            context.startActivity(activityIntent)
            Log.d(TAG, "AlarmReceiver: context.startActivity(AlarmRingingActivity) CALLED for alarm $alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "AlarmReceiver: failed to start AlarmRingingActivity directly", e)
        }

        // Release the wake lock after a short delay.
        // The activity and service will manage their own screen-on state.
        try {
            if (wakeLock.isHeld) wakeLock.release()
        } catch (_: Exception) { }
    }
}
