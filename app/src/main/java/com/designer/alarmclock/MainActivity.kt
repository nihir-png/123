package com.designer.alarmclock

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.data.LocaleHelper
import com.designer.alarmclock.data.SettingsRepository
import com.designer.alarmclock.data.ThemeMode
import com.designer.alarmclock.ui.screens.MainScreen
import com.designer.alarmclock.ui.screens.OnboardingScreen
import com.designer.alarmclock.ui.settings.SettingsViewModel
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    // Apply the saved language to every resource lookup in this Activity (incl.
    // Compose stringResource). Reads the persisted tag synchronously so the very
    // first frame is already in the right language. recreate() (on language change)
    // re-runs this with the new tag.
    override fun attachBaseContext(newBase: Context) {
        val tag = LocaleHelper.persistedTag(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, tag))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Read initial value synchronously to avoid launch UI flicker
        val isCompleted = runBlocking {
            SettingsRepository(applicationContext).current().onboardingCompleted
        }

        setContent {
            // Theme mode comes from Settings (default LIGHT). DataStore hasn't loaded
            // on the very first frame, so we start from the default (LIGHT) — no flash.
            val settingsViewModel: SettingsViewModel = viewModel()
            val appSettings by settingsViewModel.settings.collectAsState()
            val darkTheme = when (appSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            // Keep status/navigation bar icons readable: dark icons on the light
            // theme, light icons on the dark theme.
            val view = LocalView.current
            LaunchedEffect(darkTheme) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }

            AlarmClockTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Ask for notification permission on Android 13+ so the alarm
                    // and timer notifications (and the full-screen ringing intent)
                    // can actually appear.
                    val context = LocalContext.current
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* result handled by the system UI */ }
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }

                    // Ensure the alarm can ACTUALLY open the full-screen ringing
                    // screen rather than just a heads-up notification. These two
                    // special-access permissions are the common reasons the
                    // full-screen alarm is downgraded on modern Android, so we send
                    // the user to grant them when missing (each only fires once,
                    // because the check passes once granted).
                    LaunchedEffect(Unit) {
                        // Full-screen intent (Android 14+/API 34+): without this the
                        // system shows the alarm as a heads-up notification only.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val nm = context.getSystemService(NotificationManager::class.java)
                            if (nm != null && !nm.canUseFullScreenIntent()) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                            Uri.parse("package:${context.packageName}")
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    }

                    // Exact alarm permission (Android 12+/API 31+). Usually already
                    // granted via the USE_EXACT_ALARM manifest permission, but if the
                    // user revoked it we route them to the system setting so alarms
                    // still fire at the exact time.
                    LaunchedEffect(Unit) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val am = context.getSystemService(AlarmManager::class.java)
                            if (am != null && !am.canScheduleExactAlarms()) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                            Uri.parse("package:${context.packageName}")
                                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            }
                        }
                    }

                    var showOnboarding by remember { mutableStateOf(!isCompleted) }

                    if (showOnboarding) {
                        OnboardingScreen(
                            onFinished = {
                                settingsViewModel.completeOnboarding()
                                showOnboarding = false
                            }
                        )
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}
