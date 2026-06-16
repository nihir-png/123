package com.designer.alarmclock.ui.timer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.GoldButtonGradient
import com.designer.alarmclock.ui.theme.GoldenYellowGradient
import com.designer.alarmclock.ui.theme.LocalSpacing

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val totalDuration by viewModel.totalDuration.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isFinished by viewModel.isFinished.collectAsState()
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    // Store original duration for reset action
    var originalDurationMillis by remember { mutableStateOf(0L) }

    // Input States for wheels
    var selectedHours by remember { mutableStateOf(0) }
    var selectedMinutes by remember { mutableStateOf(5) }
    var selectedSeconds by remember { mutableStateOf(0) }

    val showSetup = totalDuration == 0L && !isFinished

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = spacing.large)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.medium))

            // Timer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Timer",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))

            if (showSetup) {
                // TIMER SETUP SCREEN
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Hours / Minutes / Seconds wheel pickers side by side
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = spacing.medium)
                    ) {
                        WheelPickerColumn(value = selectedHours, max = 23, label = "Hours", onValueChange = { selectedHours = it })
                        Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800), modifier = Modifier.padding(top = 20.dp))
                        WheelPickerColumn(value = selectedMinutes, max = 59, label = "Minutes", onValueChange = { selectedMinutes = it })
                        Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800), modifier = Modifier.padding(top = 20.dp))
                        WheelPickerColumn(value = selectedSeconds, max = 59, label = "Seconds", onValueChange = { selectedSeconds = it })
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Quick preset chips (1 / 5 / 10 / 15 min)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        listOf(1, 5, 10, 15).forEach { presetMinutes ->
                            val selected = selectedHours == 0 &&
                                selectedMinutes == presetMinutes && selectedSeconds == 0
                            val chipBg = if (selected) {
                                Modifier.background(GoldButtonGradient)
                            } else {
                                Modifier.background(Color.White)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .then(chipBg)
                                    .clickable {
                                        selectedHours = 0
                                        selectedMinutes = presetMinutes
                                        selectedSeconds = 0
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$presetMinutes min",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color(0xFF1E1E1E) else Color(0xFF7E7E7E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.large))

                    // Large white card showing selected time
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val timeStr = String.format("%02d:%02d:%02d", selectedHours, selectedMinutes, selectedSeconds)
                            Text(
                                text = timeStr,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E1E), // Bold dark charcoal
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.extraLarge))

                    // Start Action (Full-width yellow gradient button)
                    Button(
                        onClick = {
                            val duration = (selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds) * 1000L
                            if (duration > 0) {
                                originalDurationMillis = duration
                                viewModel.setDuration(duration)
                                viewModel.start()
                            }
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
                            text = "Start",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            } else {
                // TIMER RUNNING / COUNTDOWN SCREEN
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(spacing.medium))

                    // Countdown Circle (Cream / Yellow accent progress)
                    Box(
                        modifier = Modifier.size(260.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Progress Arc
                        val progressFraction = if (totalDuration > 0) timeRemaining.toFloat() / totalDuration else 0f
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 10.dp.toPx()
                            
                            // Background track
                            drawCircle(
                                color = Color.White,
                                radius = size.minDimension / 2 - strokeWidth,
                                style = Stroke(width = strokeWidth)
                            )
                            
                            // Yellow arc progress
                            drawArc(
                                color = Color(0xFFFFB800),
                                startAngle = -90f,
                                sweepAngle = progressFraction * 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Time remaining text inside
                        if (isFinished) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 0.9f,
                                targetValue = 1.1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            Text(
                                text = "Time's Up!",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.scale(pulseScale)
                            )
                        } else {
                            val totalSecondsRemaining = timeRemaining / 1000
                            val h = totalSecondsRemaining / 3600
                            val m = (totalSecondsRemaining % 3600) / 60
                            val s = totalSecondsRemaining % 60
                            val timeStr = String.format("%02d:%02d:%02d", h, m, s)
                            
                            Text(
                                text = timeStr,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1E1E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacing.doubleLarge))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Reset
                        OutlinedButton(
                            onClick = {
                                viewModel.reset(originalDurationMillis)
                            },
                            modifier = Modifier
                                .width(110.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1E1E1E)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Text("Reset", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        // Right: Pause / Resume / Done
                        if (isFinished) {
                            Button(
                                onClick = {
                                    viewModel.reset(0L)
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp)
                                    .background(GoldenYellowGradient, shape = RoundedCornerShape(25.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF1E1E1E)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (isRunning) {
                                        viewModel.pause()
                                    } else {
                                        viewModel.resume()
                                    }
                                },
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(50.dp)
                                    .background(GoldenYellowGradient, shape = RoundedCornerShape(25.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFF1E1E1E)
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = if (isRunning) "Pause" else "Resume",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WheelPickerColumn(
    value: Int,
    max: Int,
    label: String,
    onValueChange: (Int) -> Unit
) {
    val prev = if (value > 0) value - 1 else max
    val next = if (value < max) value + 1 else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7E7E7E),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                // Value above (faded)
                Text(
                    text = String.format("%02d", prev),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7E7E7E).copy(alpha = 0.35f),
                    modifier = Modifier.clickable { onValueChange(prev) }
                )
                // Selected value
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB800).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", value),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                }
                // Value below (faded)
                Text(
                    text = String.format("%02d", next),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7E7E7E).copy(alpha = 0.35f),
                    modifier = Modifier.clickable { onValueChange(next) }
                )
            }
        }
    }
}

@Preview(name = "Timer Setup Light Theme", showSystemUi = true)
@Composable
fun TimerSetupLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        TimerScreenPreviewHelper(totalDuration = 0L, timeRemaining = 0L, isFinished = false)
    }
}

@Preview(name = "Timer Running Light Theme", showSystemUi = true)
@Composable
fun TimerRunningLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        TimerScreenPreviewHelper(totalDuration = 60000L, timeRemaining = 45000L, isFinished = false)
    }
}

@Composable
fun TimerScreenPreviewHelper(
    totalDuration: Long,
    timeRemaining: Long,
    isFinished: Boolean
) {
    val spacing = LocalSpacing.current
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Timer", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }

            Spacer(modifier = Modifier.height(spacing.large))

            if (totalDuration == 0L) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = spacing.medium)
                ) {
                    WheelPickerColumn(value = 0, max = 23, label = "Hours", onValueChange = {})
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800), modifier = Modifier.padding(top = 20.dp))
                    WheelPickerColumn(value = 5, max = 59, label = "Minutes", onValueChange = {})
                    Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB800), modifier = Modifier.padding(top = 20.dp))
                    WheelPickerColumn(value = 0, max = 59, label = "Seconds", onValueChange = {})
                }
                Spacer(modifier = Modifier.height(spacing.large))
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("00:05:00", fontSize = 42.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                    }
                }
                Spacer(modifier = Modifier.height(spacing.extraLarge))
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(GoldenYellowGradient, shape = RoundedCornerShape(28.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFF1E1E1E)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Start", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp))
                }
            } else {
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.White, radius = size.minDimension / 2 - 20, style = Stroke(width = 20f))
                        drawArc(color = Color(0xFFFFB800), startAngle = -90f, sweepAngle = 270f, useCenter = false, style = Stroke(width = 20f, cap = StrokeCap.Round))
                    }
                    Text("00:00:45", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                }
            }
        }
    }
}
