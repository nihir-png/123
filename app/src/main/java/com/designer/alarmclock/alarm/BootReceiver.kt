package com.designer.alarmclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.designer.alarmclock.data.AlarmDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            Log.i("BootReceiver", "Device reboot completed: Rescheduling alarms...")
            val pendingResult = goAsync()
            val scheduler = AlarmScheduler(context.applicationContext)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AlarmDatabase.getDatabase(context.applicationContext)
                    val alarms = db.alarmDao().getAllAlarms().first()
                    
                    var count = 0
                    alarms.forEach { alarm ->
                        if (alarm.isEnabled) {
                            scheduler.schedule(alarm)
                            count++
                        }
                    }
                    Log.i("BootReceiver", "Rescheduled $count active alarms successfully")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
