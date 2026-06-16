package com.designer.alarmclock.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.designer.alarmclock.R
import com.designer.alarmclock.data.Alarm
import com.designer.alarmclock.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val TAG = "AlarmFlow"
        // NOTE: channel ID is versioned. A NotificationChannel is immutable after
        // its first creation, so if an older build created this channel below
        // IMPORTANCE_HIGH, the full-screen intent would NEVER fire on that device.
        // Bumping the ID forces a fresh channel at IMPORTANCE_HIGH.
        const val CHANNEL_ID = "alarm_ringing_channel_v2"
        const val NOTIFICATION_ID = 1001
        const val ACTION_DISMISS = "ACTION_DISMISS"
        const val ACTION_SNOOZE = "ACTION_SNOOZE"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getIntExtra("ALARM_ID", -1) ?: -1

        if (action == ACTION_DISMISS) {
            dismissAlarm(alarmId)
            return START_NOT_STICKY
        } else if (action == ACTION_SNOOZE) {
            snoozeAlarm(alarmId)
            return START_NOT_STICKY
        }

        Log.d(TAG, "AlarmService.onStartCommand() action=$action alarmId=$alarmId")

        if (alarmId != -1) {
            // Post a minimal foreground notification immediately (required within 5s on O+)
            // so Android doesn't kill us for being a background service.
            // This notification carries the full-screen intent — the reliable path
            // to open AlarmRingingActivity automatically when the screen is locked/off.
            startForeground(NOTIFICATION_ID, buildPlaceholderNotification(alarmId))
            Log.d(TAG, "AlarmService: startForeground() posted (full-screen-intent notification)")

            // Also try to launch the ringing activity from the service as backup.
            // A foreground service is always allowed to start activities.
            launchRingingActivity(alarmId)

            // Now fetch alarm details and upgrade the notification + start audio/vibration.
            startRinging(alarmId)
        }

        return START_STICKY
    }

    /** Launch AlarmRingingActivity from the foreground service as a backup mechanism. */
    private fun launchRingingActivity(alarmId: Int) {
        try {
            val activityIntent = Intent(this, AlarmRingingActivity::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
            }
            startActivity(activityIntent)
            Log.d(TAG, "AlarmService: startActivity(AlarmRingingActivity) CALLED for alarm $alarmId")
        } catch (e: Exception) {
            Log.e(TAG, "AlarmService: failed to launch AlarmRingingActivity from service", e)
        }
    }

    /** A bare-bones foreground notification posted synchronously so we meet the 5-second window. */
    private fun buildPlaceholderNotification(alarmId: Int): android.app.Notification {
        val fullScreenIntent = buildFullScreenPendingIntent(alarmId)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Alarm")
            .setContentText("Alarm ringing…")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()
    }

    private fun startRinging(alarmId: Int) {
        serviceScope.launch {
            val db = AlarmDatabase.getDatabase(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId) ?: return@launch

            // Play ringtone
            playRingtone()

            // Vibrate
            if (alarm.isVibrate) {
                startVibrator()
            }

            // Upgrade the foreground notification with full alarm details.
            // The ringing Activity is already launched directly by AlarmReceiver
            // and by this service. The full-screen intent on this notification
            // serves as a tertiary backup.
            showForegroundNotification(alarm)
        }
    }



    private fun playRingtone() {
        try {
            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, alarmUri!!)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play alarm ringtone", e)
        }
    }

    private fun startVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun buildFullScreenPendingIntent(alarmId: Int): PendingIntent {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            alarmId + 10000,  // offset to avoid collision with dismiss/snooze PIs
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showForegroundNotification(alarm: Alarm) {
        val dismissIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_DISMISS
            putExtra("ALARM_ID", alarm.id)
        }
        val dismissPendingIntent = PendingIntent.getService(
            this,
            alarm.id,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = ACTION_SNOOZE
            putExtra("ALARM_ID", alarm.id)
        }
        val snoozePendingIntent = PendingIntent.getService(
            this,
            alarm.id + 5000,  // offset to avoid collision with dismiss PI
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenPendingIntent = buildFullScreenPendingIntent(alarm.id)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(if (alarm.label.isNotEmpty()) alarm.label else "Alarm")
            .setContentText("Ringing at ${alarm.formattedTime}")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_menu_send, "Snooze (${alarm.snoozeDurationMinutes}m)", snoozePendingIntent)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun dismissAlarm(alarmId: Int) {
        Log.i("AlarmService", "Dismissing alarm: $alarmId")
        stopSelf()

        serviceScope.launch {
            val db = AlarmDatabase.getDatabase(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId) ?: return@launch

            if (alarm.isRepeating) {
                AlarmScheduler(applicationContext).schedule(alarm)
            } else {
                db.alarmDao().updateAlarm(alarm.copy(isEnabled = false))
            }
        }
    }

    private fun snoozeAlarm(alarmId: Int) {
        Log.i("AlarmService", "Snoozing alarm: $alarmId")
        stopSelf()

        serviceScope.launch {
            val db = AlarmDatabase.getDatabase(applicationContext)
            val alarm = db.alarmDao().getAlarmById(alarmId) ?: return@launch

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val snoozeTime = System.currentTimeMillis() + alarm.snoozeDurationMinutes * 60 * 1000

            val intent = Intent(applicationContext, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarm.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            }
            Log.i("AlarmService", "Snoozed alarm $alarmId for ${alarm.snoozeDurationMinutes} minutes")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Ringing",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full-screen alarm notification."
                enableLights(true)
                enableVibration(false)   // we handle vibration ourselves
                setSound(null, null)     // we handle audio ourselves
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)       // alarms should break through Do Not Disturb
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)

            // Diagnostics: confirm the channel's ACTUAL importance (a stale channel
            // would report a lower value) and whether the app may use full-screen
            // intents. If importance < HIGH or canUseFullScreenIntent == false, the
            // OS will only show a heads-up notification instead of opening the
            // ringing screen. Watch with:  adb logcat -s AlarmFlow
            val actual = manager.getNotificationChannel(CHANNEL_ID)
            val canFsi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                manager.canUseFullScreenIntent()
            } else true
            Log.d(TAG, "Channel '$CHANNEL_ID' importance=${actual?.importance} (HIGH=${NotificationManager.IMPORTANCE_HIGH}), " +
                    "canUseFullScreenIntent=$canFsi, notificationsEnabled=${manager.areNotificationsEnabled()}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        // Send local broadcast to close the full-screen activity
        val closeIntent = Intent("CLOSE_RINGING_ACTIVITY")
        sendBroadcast(closeIntent)
        Log.i("AlarmService", "Alarm service stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
