package com.designer.alarmclock.ui.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.designer.alarmclock.data.Alarm
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.GoldenYellowGradient
import com.designer.alarmclock.ui.theme.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlarmSheet(
    alarmToEdit: Alarm? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onSave: (hour: Int, minute: Int, repeatDays: List<Int>, label: String, isVibrate: Boolean, snoozeMinutes: Int) -> Unit
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    // Initialize states
    val initialHour = alarmToEdit?.hour ?: 6
    val initialMinute = alarmToEdit?.minute ?: 30
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    var label by remember { mutableStateOf(alarmToEdit?.label ?: "") }
    var isVibrate by remember { mutableStateOf(alarmToEdit?.isVibrate ?: true) }
    var snoozeMinutes by remember { mutableStateOf(alarmToEdit?.snoozeDurationMinutes ?: 10) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    
    // Repeat Days State (Sunday=7, Monday=1..Saturday=6)
    val selectedDays = remember { mutableStateListOf<Int>().apply { 
        alarmToEdit?.repeatDays?.let { addAll(it) }
    } }

    // Render as full-screen styled dialog
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Soft Cream
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = if (alarmToEdit == null) "Add Alarm" else "Edit Alarm",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(start = spacing.small)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Delete button (only when editing an existing alarm).
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Alarm",
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
                    val timeString = String.format("%02d:%02d", hour12, timePickerState.minute)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { showTimePickerDialog = true },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TAP TO SET TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB800),
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
                                    color = Color(0xFF1E1E1E)
                                )
                                Spacer(modifier = Modifier.width(spacing.small))
                                Text(
                                    text = amPm,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Alarm Label Field
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        placeholder = { Text("Alarm Label (e.g. Wake up)", color = Color(0xFF7E7E7E)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFB800),
                            unfocusedBorderColor = Color(0xFFEFEFEF),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1E1E1E),
                            unfocusedTextColor = Color(0xFF1E1E1E)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Repeat Days Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(spacing.medium)) {
                            Text(
                                text = "Repeat",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E1E)
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
                                        Modifier.background(Color(0xFFFFB800))
                                    } else {
                                        Modifier.background(Color(0xFFFAF8F5))
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
                                            color = if (isSelected) Color(0xFF1E1E1E) else Color(0xFF7E7E7E),
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                text = "Vibrate on Ring",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E1E)
                                )
                            )
                            Switch(
                                checked = isVibrate,
                                onCheckedChange = { isVibrate = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFFB800),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFE2E2E2),
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(spacing.medium)) {
                            Text(
                                text = "Snooze",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E1E)
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
                                    val chipBg = if (isSelected) Color(0xFFFFB800) else Color(0xFFFAF8F5)
                                    val labelColor = if (isSelected) Color(0xFF1E1E1E) else Color(0xFF7E7E7E)

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
                                            text = "$duration min",
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
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                text = "Ringtone",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1E1E)
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Morning Dew",
                                    fontSize = 14.sp,
                                    color = Color(0xFF7E7E7E),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Go",
                                    tint = Color(0xFF7E7E7E)
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
                            contentColor = Color(0xFF1E1E1E)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Save Alarm",
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
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(spacing.medium)
                    ) {
                        Column(
                            modifier = Modifier.padding(spacing.medium),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Set Alarm Time",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(bottom = spacing.medium)
                            )
                            TimePicker(
                                state = timePickerState,
                                colors = TimePickerDefaults.colors(
                                    clockDialColor = Color(0xFFFAF8F5),
                                    clockDialSelectedContentColor = Color(0xFF1E1E1E),
                                    clockDialUnselectedContentColor = Color(0xFF7E7E7E),
                                    selectorColor = Color(0xFFFFB800),
                                    periodSelectorBorderColor = Color(0xFFEFEFEF),
                                    periodSelectorSelectedContainerColor = Color(0xFFFFB800).copy(alpha = 0.2f),
                                    periodSelectorUnselectedContainerColor = Color.White,
                                    periodSelectorSelectedContentColor = Color(0xFFFFB800),
                                    periodSelectorUnselectedContentColor = Color(0xFF7E7E7E),
                                    timeSelectorSelectedContainerColor = Color(0xFFFFB800).copy(alpha = 0.2f),
                                    timeSelectorUnselectedContainerColor = Color(0xFFFAF8F5),
                                    timeSelectorSelectedContentColor = Color(0xFFFFB800),
                                    timeSelectorUnselectedContentColor = Color(0xFF1E1E1E)
                                )
                            )
                            Spacer(modifier = Modifier.height(spacing.medium))
                            Button(
                                onClick = { showTimePickerDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFB800),
                                    contentColor = Color(0xFF1E1E1E)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Select", fontWeight = FontWeight.Bold)
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
