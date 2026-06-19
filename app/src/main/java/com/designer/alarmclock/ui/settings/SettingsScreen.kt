package com.designer.alarmclock.ui.settings

import android.app.Activity
import android.app.NotificationManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.R
import com.designer.alarmclock.data.ThemeMode
import com.designer.alarmclock.data.TimeFormat
import com.designer.alarmclock.ui.theme.Gold
import com.designer.alarmclock.ui.theme.GoldLight
import com.designer.alarmclock.ui.theme.LocalAppColors

private data class LanguageOption(val tag: String, @StringRes val labelRes: Int)

private val LANGUAGES = listOf(
    LanguageOption("", R.string.language_system),
    LanguageOption("en", R.string.language_english),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("fr", R.string.language_french),
    LanguageOption("de", R.string.language_german),
    LanguageOption("hi", R.string.language_hindi),
    LanguageOption("pt", R.string.language_portuguese)
)

private val SNOOZE_OPTIONS = listOf(5, 10, 15, 20, 30)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val appColors = LocalAppColors.current

    BackHandler(enabled = true) { onBack() }

    // Which option-picker dialog (if any) is open.
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTimeFormatDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // App version, read once from the package manager (no BuildConfig needed).
    val versionName = remember {
        runCatching {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    // System ringtone picker → save the chosen URI + display name as the default.
    val ringtoneLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri: Uri? = result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val name = uri?.let {
                runCatching { RingtoneManager.getRingtone(context, it)?.getTitle(context) }.getOrNull()
            } ?: context.getString(R.string.ringtone_default_system)
            viewModel.setDefaultRingtone(uri?.toString() ?: "", name)
        }
    }

    fun openRingtonePicker() {
        val current = settings.defaultRingtoneUri
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.select_alarm_sound))
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                if (current.isNotEmpty()) Uri.parse(current)
                else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
        }
        runCatching { ringtoneLauncher.launch(intent) }
    }

    val notificationsEnabled = remember {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val canUseFullScreen = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.getSystemService(NotificationManager::class.java)?.canUseFullScreenIntent() ?: true
        } else true
    }

    Surface(modifier = Modifier.fillMaxSize(), color = appColors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Header (back arrow + title), styled like the Add/Edit Alarm header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = appColors.textPrimary
                    )
                }
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                // ── General ──
                SettingsSection(title = stringResource(R.string.settings_section_general)) {
                    ClickableSettingRow(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        value = stringResource(
                            LANGUAGES.firstOrNull { it.tag == settings.languageTag }?.labelRes
                                ?: R.string.language_system
                        ),
                        onClick = { showLanguageDialog = true }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.DarkMode,
                        title = stringResource(R.string.settings_theme),
                        value = stringResource(
                            when (settings.themeMode) {
                                ThemeMode.LIGHT -> R.string.theme_light
                                ThemeMode.DARK -> R.string.theme_dark
                                ThemeMode.SYSTEM -> R.string.theme_system
                            }
                        ),
                        onClick = { showThemeDialog = true }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.Schedule,
                        title = stringResource(R.string.settings_time_format),
                        value = stringResource(
                            if (settings.timeFormat == TimeFormat.TWENTY_FOUR_HOUR) R.string.time_format_24
                            else R.string.time_format_12
                        ),
                        onClick = { showTimeFormatDialog = true }
                    )
                }

                // ── Alarm Defaults ──
                SettingsSection(title = stringResource(R.string.settings_section_alarm_defaults)) {
                    ClickableSettingRow(
                        icon = Icons.Default.MusicNote,
                        title = stringResource(R.string.settings_default_ringtone),
                        value = if (settings.defaultRingtoneUri.isEmpty())
                            stringResource(R.string.ringtone_default_system)
                        else settings.defaultRingtoneName,
                        onClick = { openRingtonePicker() }
                    )
                    SettingDivider()
                    SwitchSettingRow(
                        icon = Icons.Default.Vibration,
                        title = stringResource(R.string.settings_vibrate_default),
                        checked = settings.defaultVibrate,
                        onCheckedChange = { viewModel.setDefaultVibrate(it) }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.Snooze,
                        title = stringResource(R.string.settings_default_snooze),
                        value = stringResource(R.string.snooze_minutes, settings.defaultSnoozeMinutes),
                        onClick = { showSnoozeDialog = true }
                    )
                    SettingDivider()
                    SliderSettingRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = stringResource(R.string.settings_default_volume),
                        value = settings.defaultVolume,
                        onValueChange = { viewModel.setDefaultVolume(it) }
                    )
                }

                // ── Notification & Alarm ──
                SettingsSection(title = stringResource(R.string.settings_section_notifications)) {
                    ClickableSettingRow(
                        icon = Icons.Default.Notifications,
                        title = stringResource(R.string.settings_alarm_notifications),
                        value = stringResource(
                            if (notificationsEnabled) R.string.notif_enabled else R.string.notif_disabled
                        ),
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                )
                            }
                        }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.Fullscreen,
                        title = stringResource(R.string.settings_fullscreen_alarm),
                        value = stringResource(
                            if (canUseFullScreen) R.string.fullscreen_allowed else R.string.fullscreen_tap_allow
                        ),
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                runCatching {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                    )
                                }
                            }
                        }
                    )
                }

                // ── App ──
                SettingsSection(title = stringResource(R.string.settings_section_app)) {
                    ClickableSettingRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_about),
                        value = "",
                        onClick = { showAboutDialog = true }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.Android,
                        title = stringResource(R.string.settings_version),
                        value = versionName,
                        showChevron = false,
                        onClick = { }
                    )
                    SettingDivider()
                    ClickableSettingRow(
                        icon = Icons.Default.Lock,
                        title = stringResource(R.string.settings_privacy),
                        value = "",
                        onClick = { showPrivacyDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ── Option-picker dialogs ──
    if (showLanguageDialog) {
        OptionPickerDialog(
            title = stringResource(R.string.settings_language),
            options = LANGUAGES.map { stringResource(it.labelRes) },
            selectedIndex = LANGUAGES.indexOfFirst { it.tag == settings.languageTag }.coerceAtLeast(0),
            onSelect = { index ->
                // Persist, then recreate the Activity so the new locale applies app-wide.
                viewModel.setLanguage(LANGUAGES[index].tag) {
                    (context as? Activity)?.recreate()
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    if (showThemeDialog) {
        val modes = listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM)
        OptionPickerDialog(
            title = stringResource(R.string.settings_theme),
            options = listOf(
                stringResource(R.string.theme_light),
                stringResource(R.string.theme_dark),
                stringResource(R.string.theme_system)
            ),
            selectedIndex = modes.indexOf(settings.themeMode).coerceAtLeast(0),
            onSelect = { viewModel.setThemeMode(modes[it]) },
            onDismiss = { showThemeDialog = false }
        )
    }
    if (showTimeFormatDialog) {
        val formats = listOf(TimeFormat.TWELVE_HOUR, TimeFormat.TWENTY_FOUR_HOUR)
        OptionPickerDialog(
            title = stringResource(R.string.settings_time_format),
            options = listOf(
                stringResource(R.string.time_format_12_long),
                stringResource(R.string.time_format_24_long)
            ),
            selectedIndex = formats.indexOf(settings.timeFormat).coerceAtLeast(0),
            onSelect = { viewModel.setTimeFormat(formats[it]) },
            onDismiss = { showTimeFormatDialog = false }
        )
    }
    if (showSnoozeDialog) {
        OptionPickerDialog(
            title = stringResource(R.string.settings_default_snooze),
            options = SNOOZE_OPTIONS.map { stringResource(R.string.snooze_minutes_long, it) },
            selectedIndex = SNOOZE_OPTIONS.indexOf(settings.defaultSnoozeMinutes).coerceAtLeast(0),
            onSelect = { viewModel.setDefaultSnoozeMinutes(SNOOZE_OPTIONS[it]) },
            onDismiss = { showSnoozeDialog = false }
        )
    }
    if (showAboutDialog) {
        InfoDialog(
            title = stringResource(R.string.about_title),
            message = stringResource(R.string.about_message),
            onDismiss = { showAboutDialog = false }
        )
    }
    if (showPrivacyDialog) {
        InfoDialog(
            title = stringResource(R.string.privacy_title),
            message = stringResource(R.string.privacy_message),
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable building blocks (theme-aware cards + gold accents).
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val appColors = LocalAppColors.current
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            color = appColors.textSubtle,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
private fun LeadingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(GoldLight.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ClickableSettingRow(
    icon: ImageVector,
    title: String,
    value: String,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(color = appColors.textMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 150.dp)
            )
        }
        if (showChevron) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "›", fontSize = 20.sp, color = appColors.textSubtle, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SwitchSettingRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeadingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Gold,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = appColors.toggleTrackOff,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SliderSettingRow(
    icon: ImageVector,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val appColors = LocalAppColors.current
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LeadingIcon(icon)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.textPrimary
                ),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$value%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = Gold,
                activeTrackColor = Gold,
                inactiveTrackColor = appColors.toggleTrackOff
            )
        )
    }
}

@Composable
private fun SettingDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
        thickness = 1.dp,
        color = LocalAppColors.current.divider
    )
}

@Composable
private fun OptionPickerDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.card,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
            )
        },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onSelect(index)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = {
                                onSelect(index)
                                onDismiss()
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Gold,
                                unselectedColor = appColors.textSubtle
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge.copy(color = appColors.textPrimary)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done), color = Gold, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun InfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.card,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(color = appColors.textMuted)
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_ok), color = Gold, fontWeight = FontWeight.Bold)
            }
        }
    )
}
