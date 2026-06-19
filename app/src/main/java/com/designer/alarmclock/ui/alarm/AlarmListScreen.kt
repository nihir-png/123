@file:OptIn(ExperimentalFoundationApi::class)
package com.designer.alarmclock.ui.alarm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.R
import com.designer.alarmclock.data.Alarm
import com.designer.alarmclock.data.TimeFormat
import com.designer.alarmclock.ui.settings.SettingsViewModel
import com.designer.alarmclock.ui.theme.*

// Day letters as shown in Figma: Sunday first. Maps to repeatDays codes
// (1=Mon..6=Sat, 7=Sun).
private val DayLetters = listOf(
    "S" to 7, "M" to 1, "T" to 2, "W" to 3, "T" to 4, "F" to 5, "S" to 6
)

@Composable
fun AlarmListScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: AlarmViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val alarms by viewModel.alarms.collectAsState()
    val settings by settingsViewModel.settings.collectAsState()
    val is24Hour = settings.timeFormat == TimeFormat.TWENTY_FOUR_HOUR
    val appColors = LocalAppColors.current
    var showAddSheet by remember { mutableStateOf(false) }
    var alarmToEdit by remember { mutableStateOf<Alarm?>(null) }
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        containerColor = appColors.background,
        floatingActionButton = {
            // FAB: 66dp gold circle with a soft gold glow (Figma node 1:2160).
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        clip = false,
                        ambientColor = Gold,
                        spotColor = Gold
                    )
                    .background(GoldenYellowGradient, CircleShape)
                    .clickable {
                        alarmToEdit = null
                        showAddSheet = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_alarm),
                    tint = Color(0xFF1A1A1A),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.background)
                .padding(paddingValues)
        ) {
            if (alarms.isEmpty()) {
                EmptyAlarmState(onOpenSettings = onOpenSettings)
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        AlarmHeader(
                            modifier = Modifier.padding(bottom = 8.dp),
                            onSettingsClick = onOpenSettings
                        )
                    }
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            is24Hour = is24Hour,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onClick = {
                                alarmToEdit = alarm
                                showAddSheet = true
                            },
                            onLongClick = { alarmToDelete = alarm }
                        )
                    }
                }
            }

            if (showAddSheet) {
                AddEditAlarmSheet(
                    alarmToEdit = alarmToEdit,
                    is24Hour = is24Hour,
                    defaultVibrate = settings.defaultVibrate,
                    defaultSnoozeMinutes = settings.defaultSnoozeMinutes,
                    onDismiss = { showAddSheet = false },
                    onDelete = alarmToEdit?.let { editing ->
                        {
                            viewModel.deleteAlarm(editing)
                            showAddSheet = false
                        }
                    },
                    onSave = { hour, minute, repeatDays, label, isVibrate, snoozeMinutes ->
                        if (alarmToEdit == null) {
                            viewModel.addAlarm(hour, minute, repeatDays, label, isVibrate, snoozeMinutes)
                        } else {
                            viewModel.updateAlarm(
                                alarmToEdit!!.copy(
                                    hour = hour,
                                    minute = minute,
                                    repeatDays = repeatDays,
                                    label = label,
                                    isVibrate = isVibrate,
                                    snoozeDurationMinutes = snoozeMinutes
                                )
                            )
                        }
                        showAddSheet = false
                    }
                )
            }

            // Delete confirmation (long-press on a card).
            alarmToDelete?.let { alarm ->
                AlertDialog(
                    onDismissRequest = { alarmToDelete = null },
                    title = {
                        Text(
                            stringResource(R.string.delete_alarm_title),
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    },
                    text = {
                        Text(
                            stringResource(R.string.delete_alarm_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.textMuted
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.deleteAlarm(alarm)
                            alarmToDelete = null
                        }) { Text(stringResource(R.string.action_delete), color = LightError, fontWeight = FontWeight.Bold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { alarmToDelete = null }) {
                            Text(stringResource(R.string.action_cancel), color = appColors.textMuted)
                        }
                    },
                    containerColor = appColors.card
                )
            }
        }
    }
}

@Composable
private fun AlarmHeader(
    modifier: Modifier = Modifier,
    onSettingsClick: () -> Unit = {}
) {
    val appColors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tab_alarm),
            style = TextStyle(
                fontFamily = Urbanist,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 36.sp,
                lineHeight = 48.sp,
                color = appColors.textPrimary
            )
        )
        // 44dp circle button holding the Settings gear (opens the Settings screen).
        // Same circle styling the old "…" menu used, so the header layout is unchanged.
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(elevation = 10.dp, shape = CircleShape, spotColor = Color(0x241C1F26))
                .background(appColors.card, CircleShape)
                .clickable { onSettingsClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.cd_settings),
                tint = appColors.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    is24Hour: Boolean = false
) {
    val appColors = LocalAppColors.current
    val enabled = alarm.isEnabled
    val hour12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
    val amPm = if (alarm.hour < 12) "AM" else "PM"
    val timeText = if (is24Hour) {
        String.format("%02d:%02d", alarm.hour, alarm.minute)
    } else {
        String.format("%02d:%02d", hour12, alarm.minute)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(162.dp)
            .then(
                if (enabled) Modifier.shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x241C1F26),
                    ambientColor = Color(0x141C1F26)
                ) else Modifier
            )
            .clip(RoundedCornerShape(26.dp))
            .background(if (enabled) appColors.cardGradient else appColors.cardDisabled)
            .alpha(if (enabled) 1f else 0.55f)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(16.dp)
    ) {
        // Label + toggle row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (alarm.label.isNotEmpty()) alarm.label else stringResource(R.string.tab_alarm),
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.15).sp,
                    color = appColors.textMuted
                ),
                maxLines = 1
            )
            GoldToggle(checked = enabled, onCheckedChange = { onToggle() })
        }

        // Time + AM/PM, anchored so the time baseline sits at ~72dp from the top.
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 28.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = timeText,
                style = TextStyle(
                    fontFamily = Urbanist,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-1.9).sp,
                    color = appColors.textPrimary
                )
            )
            if (!is24Hour) {
                Spacer(Modifier.width(6.dp))
                Text(
                    text = amPm,
                    style = TextStyle(
                        fontFamily = Urbanist,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = appColors.textFaint
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }

        // Day-letter row pinned to the bottom-left (width ~125dp).
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(125.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayLetters.forEach { (letter, code) ->
                val active = alarm.repeatDays.contains(code)
                Text(
                    text = letter,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        lineHeight = 16.5.sp,
                        letterSpacing = 0.06.sp,
                        color = if (active) DayActive else appColors.dayInactive
                    )
                )
            }
        }
    }
}

/** Custom 48×28 pill toggle matching the Figma alarm card. */
@Composable
private fun GoldToggle(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    val appColors = LocalAppColors.current
    Box(
        modifier = Modifier
            .then(
                if (checked) Modifier.shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = Color(0x80FFB800),
                    ambientColor = Color(0x40FFB800)
                ) else Modifier
            )
            .width(48.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) GoldButtonGradient else Brush.linearGradient(listOf(appColors.toggleTrackOff, appColors.toggleTrackOff)))
            .clickable { onCheckedChange() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyAlarmState(onOpenSettings: () -> Unit = {}) {
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlarmHeader(onSettingsClick = onOpenSettings)

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.alarm_empty_state),
            contentDescription = stringResource(R.string.alarm_empty_title),
            modifier = Modifier
                .fillMaxWidth(0.74f)
                .height(208.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.alarm_empty_title),
            style = TextStyle(
                fontFamily = Urbanist,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.89).sp,
                color = appColors.textPrimary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.alarm_empty_subtitle),
            style = TextStyle(
                fontFamily = Urbanist,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = (-0.15).sp,
                color = appColors.textSubtle
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1.3f))
    }
}

@Preview(name = "Alarm List", showSystemUi = true)
@Composable
private fun AlarmListPreview() {
    AlarmClockTheme(darkTheme = false) {
        Box(Modifier.fillMaxSize().background(FigmaBackground)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(30.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(2) }) { AlarmHeader() }
                items(
                    listOf(
                        Alarm(1, 6, 30, true, listOf(1, 2, 3, 4, 5), "Wake up"),
                        Alarm(2, 7, 45, true, listOf(1, 3, 5), "Exercise"),
                        Alarm(3, 10, 0, false, listOf(2, 3, 4), "Meeting"),
                        Alarm(4, 22, 30, true, listOf(7, 1, 2, 3, 4, 5, 6), "Sleep")
                    )
                ) { AlarmCard(it, {}, {}, {}) }
            }
        }
    }
}
