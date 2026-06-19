package com.designer.alarmclock.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.designer.alarmclock.R
import com.designer.alarmclock.data.Alarm
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.Gold
import com.designer.alarmclock.ui.theme.GoldenYellowGradient
import com.designer.alarmclock.ui.theme.LocalAppColors
import com.designer.alarmclock.ui.theme.LocalSpacing

// Text/icons that sit on a gold surface must stay dark in BOTH themes for contrast.
private val OnGold = Color(0xFF1A1A1A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmSheet(
    alarmToEdit: Alarm? = null,
    is24Hour: Boolean = false,
    defaultVibrate: Boolean = true,
    defaultSnoozeMinutes: Int = 10,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (hour: Int, minute: Int, repeatDays: List<Int>, label: String, isVibrate: Boolean, snoozeMinutes: Int) -> Unit
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    val scrollState = rememberScrollState()

    // Initialize states
    val initialHour = alarmToEdit?.hour ?: 6
    val initialMinute = alarmToEdit?.minute ?: 30
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    var label by remember { mutableStateOf(alarmToEdit?.label ?: "") }
    // New alarms inherit the user's Settings defaults; existing alarms keep their own values.
    var isVibrate by remember { mutableStateOf(alarmToEdit?.isVibrate ?: defaultVibrate) }
    var snoozeMinutes by remember { mutableStateOf(alarmToEdit?.snoozeDurationMinutes ?: defaultSnoozeMinutes) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    // Repeat Days State (Sunday=7, Monday=1..Saturday=6)
    val selectedDays = remember { mutableStateListOf<Int>().apply {
        alarmToEdit?.repeatDays?.let { addAll(it) }
    } }

    // Render as full-screen styled dialog.
    // decorFitsSystemWindows = false makes the dialog window draw edge-to-edge so
    // WindowInsets (status/navigation bars) report real values inside the dialog.
    // Without it the dialog consumes the insets, navigationBarsPadding() becomes a
    // no-op, and the bottom "Save Alarm" CTA slides under the gesture/nav bar on
    // real devices (the emulator's button nav happened to hide the bug).
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = appColors.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium, vertical = spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = appColors.textPrimary
                        )
                    }
                    Text(
                        text = stringResource(
                            if (alarmToEdit == null) R.string.add_alarm_title else R.string.edit_alarm_title
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        ),
                        modifier = Modifier.padding(start = spacing.small)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Delete button (only when editing an existing alarm).
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.cd_delete_alarm),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Scrollable Form content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(horizontal = spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(spacing.medium))

                    // Large editable time display card
                    val hour12 = if (timePickerState.hour % 12 == 0) 12 else timePickerState.hour % 12
                    val amPm = if (timePickerState.hour < 12) "AM" else "PM"
                    val timeString = if (is24Hour) {
                        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                    } else {
                        String.format("%02d:%02d", hour12, timePickerState.minute)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { showTimePickerDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.tap_to_set_time),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Gold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(spacing.small))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = timeString,
                                    fontSize = 56.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                                if (!is24Hour) {
                                    Spacer(modifier = Modifier.width(spacing.small))
                                    Text(
                                        text = amPm,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textSecondary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Alarm Label Field
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        placeholder = {
                            Text(
                                stringResource(R.string.alarm_label_hint),
                                color = appColors.textSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = appColors.fieldBorder,
                            focusedContainerColor = appColors.card,
                            unfocusedContainerColor = appColors.card,
                            focusedTextColor = appColors.textPrimary,
                            unfocusedTextColor = appColors.textPrimary
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Repeat Days Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(spacing.medium)) {
                            Text(
                                text = stringResource(R.string.repeat),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Sunday (7), Monday (1)..Saturday (6)
                                val days = listOf(
                                    Pair("S", 7),
                                    Pair("M", 1),
                                    Pair("T", 2),
                                    Pair("W", 3),
                                    Pair("T", 4),
                                    Pair("F", 5),
                                    Pair("S", 6)
                               )

                                days.forEach { (name, dayNum) ->
                                    val isSelected = selectedDays.contains(dayNum)
                                    val bgModifier = if (isSelected) {
                                        Modifier.background(Gold)
                                    } else {
                                        Modifier.background(appColors.chipUnselected)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .then(bgModifier)
                                            .clickable {
                                                if (isSelected) {
                                                    selectedDays.remove(dayNum)
                                                } else {
                                                    selectedDays.add(dayNum)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            color = if (isSelected) OnGold else appColors.textSecondary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.medium))

                    // Vibrate Toggle Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.vibrate_on_ring),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Switch(
                                checked = isVibrate,
                                onCheckedChange = { isVibrate = it },
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

                    Spacer(modifier = Modifier.height(spacing.medium))

                    // Snooze row card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(spacing.medium)) {
                            Text(
                                text = stringResource(R.string.snooze),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(spacing.small))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.small)
                            ) {
                                val durations = listOf(5, 10, 15)
                                durations.forEach { duration ->
                                    val isSelected = snoozeMinutes == duration
                                    val chipBg = if (isSelected) Gold else appColors.chipUnselected
                                    val labelColor = if (isSelected) OnGold else appColors.textSecondary

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(chipBg)
                                            .clickable { snoozeMinutes = duration },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = stringResource(R.string.snooze_minutes, duration),
                                            color = labelColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.medium))

                    // Ringtone Row card (Mock selection)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* ringtone dialog */ }
                                .padding(spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.ringtone),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Morning Dew",
                                    fontSize = 14.sp,
                                    color = appColors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = appColors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Save button lives at the end of the scrollable form so it
                    // is always reachable regardless of screen height.
                    Button(
                        onClick = {
                            onSave(
                                timePickerState.hour,
                                timePickerState.minute,
                                selectedDays.toList(),
                                label,
                                isVibrate,
                                snoozeMinutes
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(GoldenYellowGradient, shape = RoundedCornerShape(28.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = OnGold
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.save_alarm),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.large))
                }
            }

            // Material TimePicker Overlay Dialog
            if (showTimePickerDialog) {
                Dialog(onDismissRequest = { showTimePickerDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = appColors.card,
                        modifier = Modifier.padding(spacing.medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.set_alarm_time),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textPrimary
                                ),
                                modifier = Modifier.padding(bottom = spacing.medium)
                            )
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = appColors.chipUnselected,
                                    clockDialSelectedContentColor = OnGold,
                                    clockDialUnselectedContentColor = appColors.textPrimary,
                                    selectorColor = Gold,
                                    periodSelectorBorderColor = appColors.fieldBorder,
                                    periodSelectorSelectedContainerColor = Gold.copy(alpha = 0.2f),
                                    periodSelectorUnselectedContainerColor = appColors.card,
                                    periodSelectorSelectedContentColor = Gold,
                                    periodSelectorUnselectedContentColor = appColors.textSecondary,
                                    timeSelectorSelectedContainerColor = Gold.copy(alpha = 0.2f),
                                    timeSelectorUnselectedContainerColor = appColors.chipUnselected,
                                    timeSelectorSelectedContentColor = Gold,
                                    timeSelectorUnselectedContentColor = appColors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                            Button(
                                onClick = { showTimePickerDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Gold,
                                    contentColor = OnGold
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(stringResource(R.string.action_select), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Add Alarm Light Theme", showSystemUi = true)
@Composable
fun AddEditAlarmSheetPreview() {
    AlarmClockTheme(darkTheme = false) {
        AddEditAlarmSheet(onDismiss = {}, onSave = { _, _, _, _, _, _ -> })
    }
}
