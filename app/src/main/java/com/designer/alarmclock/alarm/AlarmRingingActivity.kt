package com.designer.alarmclock.alarm

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.designer.alarmclock.R
import com.designer.alarmclock.data.AlarmDatabase
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.GoldButtonGradient
import com.designer.alarmclock.ui.theme.Urbanist
import java.text.SimpleDateFormat
import java.util.*

class AlarmRingingActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null

    private val closeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "CLOSE_RINGING_ACTIVITY") {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake up screen and show over keyguard (lock screen)
        turnScreenOnAndShowOverKeyguard()

        val alarmId = intent.getIntExtra("ALARM_ID", -1)

        // Register receiver to finish this activity if alarm is dismissed externally
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(closeReceiver, IntentFilter("CLOSE_RINGING_ACTIVITY"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(closeReceiver, IntentFilter("CLOSE_RINGING_ACTIVITY"))
        }

        setContent {
            AlarmClockTheme {
                RingingScreen(
                    alarmId = alarmId,
                    onDismiss = {
                        val serviceIntent = Intent(this, AlarmService::class.java).apply {
                            action = AlarmService.ACTION_DISMISS
                            putExtra("ALARM_ID", alarmId)
                        }
                        startService(serviceIntent)
                        finish()
                    },
                    onSnooze = {
                        val serviceIntent = Intent(this, AlarmService::class.java).apply {
                            action = AlarmService.ACTION_SNOOZE
                            putExtra("ALARM_ID", alarmId)
                        }
                        startService(serviceIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndShowOverKeyguard() {
        // Acquire a screen-bright wake lock to guarantee the screen stays on
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP
                    or PowerManager.ON_AFTER_RELEASE,
            "AlarmClock:RingingActivityWakeLock"
        )
        wakeLock?.acquire(5 * 60 * 1000L) // 5 minutes max

        // Keep screen on via window flag (redundant safety net)
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Dismiss keyguard to show the alarm over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Bring to front if already running
        setIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(closeReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        // Release wake lock
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) { }
        wakeLock = null
    }
}

@Composable
fun RingingScreen(
    alarmId: Int,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var snoozeMinutes by remember { mutableStateOf(10) }
    var alarmLabel by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(alarmId) {
        if (alarmId != -1) {
            val db = AlarmDatabase.getDatabase(context)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            if (alarm != null) {
                snoozeMinutes = alarm.snoozeDurationMinutes
                alarmLabel = alarm.label
            }
        }
    }

    // Live ticking clock
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            kotlinx.coroutines.delay(1000)
        }
    }
    val hour       = SimpleDateFormat("h",        Locale.getDefault()).format(now)
    val minute     = SimpleDateFormat("mm",       Locale.getDefault()).format(now)
    val dateString = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now)

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background: user's custom wake-up image, full-screen, cropped to fill ──
        Image(
            painter = painterResource(id = R.drawable.wake_up_screen_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // ── Foreground content ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Top cluster: weather icon + date + big time ──
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_wake_up_weather),
                contentDescription = null,
                modifier = Modifier.height(62.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = dateString,
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = Color(0xFFF2F2F2)
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Big time — Figma "6 : 15", ExtraBold, white, no AM/PM
            Text(
                text = "$hour : $minute",
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 90.sp,
                    letterSpacing = (-2).sp,
                    color = Color.White
                ),
                maxLines = 1
            )

            // ── Middle: "Wake up!" (dark grey, vertically centred) ──
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Wake up!",
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = Color(0xB3000000)   // rgba(0,0,0,0.7)
                ),
                textAlign = TextAlign.Center
            )
            if (alarmLabel.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = alarmLabel,
                    style = TextStyle(
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color(0x99000000)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(2f))

            // ── Bottom: plain "Snooze" text + full-width gold pill Dismiss ──
            Text(
                text = "Snooze",
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF404040)
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onSnooze() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dismiss button — full-width gold gradient pill
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(GoldButtonGradient, shape = RoundedCornerShape(25.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Dismiss",
                    style = TextStyle(
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.3125).sp,
                        color = Color.Black
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(name = "Ringing Screen", showSystemUi = true)
@Composable
fun RingingScreenPreview() {
    AlarmClockTheme(darkTheme = false) {
        RingingScreen(alarmId = -1, onDismiss = {}, onSnooze = {})
    }
}
