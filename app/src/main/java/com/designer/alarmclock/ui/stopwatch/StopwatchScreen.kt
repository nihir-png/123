package com.designer.alarmclock.ui.stopwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.R
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.Gold
import com.designer.alarmclock.ui.theme.GoldenYellowGradient
import com.designer.alarmclock.ui.theme.LocalAppColors
import com.designer.alarmclock.ui.theme.LocalSpacing

private val OnGold = Color(0xFF1A1A1A)

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = viewModel()
) {
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val laps by viewModel.laps.collectAsState()
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current

    // Format: MM:SS:CC (where CC is centiseconds)
    val minutes = (elapsedTime % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (elapsedTime % (1000 * 60)) / 1000
    val centiseconds = (elapsedTime % 1000) / 10
    val timeStr = String.format("%02d:%02d:%02d", minutes, seconds, centiseconds)

    Scaffold(containerColor = appColors.background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(spacing.medium))

            // Stopwatch Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stopwatch_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = appColors.textPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.weight(0.4f))

            // Large Centered Time Display "00:00:00"
            Text(
                text = timeStr,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(0.4f))

            // Control Buttons: side-by-side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                // Left: Lap or Reset button (styled minimal card style)
                val isLapResetEnabled = isRunning || elapsedTime > 0
                val leftButtonText = if (isRunning) stringResource(R.string.action_lap) else stringResource(R.string.action_reset)

                Button(
                    onClick = {
                        if (isRunning) {
                            viewModel.recordLap()
                        } else {
                            viewModel.reset()
                        }
                    },
                    enabled = isLapResetEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.card,
                        contentColor = appColors.textPrimary,
                        disabledContainerColor = appColors.card.copy(alpha = 0.5f),
                        disabledContentColor = appColors.textSecondary.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(text = leftButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // Right: Start or Pause button (styled yellow gradient)
                val rightButtonText = if (isRunning) stringResource(R.string.action_pause) else stringResource(R.string.action_start)
                Button(
                    onClick = {
                        if (isRunning) {
                            viewModel.pause()
                        } else {
                            viewModel.start()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .background(
                            if (isRunning) Brush.linearGradient(listOf(Color(0xFFFF5252), Color(0xFFFF2D55)))
                            else GoldenYellowGradient,
                            shape = RoundedCornerShape(28.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = if (isRunning) Color.White else OnGold
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(text = rightButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(spacing.medium))

            // Lap List with alternate row tints
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = spacing.large),
                colors = CardDefaults.cardColors(containerColor = appColors.card),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (laps.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.stopwatch_laps_empty),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = appColors.textSecondary
                            )
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(laps.reversed(), key = { _, lap -> lap.lapNumber }) { index, lap ->
                            val isEven = index % 2 == 0
                            val rowColor = if (isEven) appColors.card else appColors.chipUnselected

                            LapRow(
                                lap = lap,
                                isLatest = index == 0,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor)
                                    .padding(horizontal = spacing.medium, vertical = spacing.medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LapRow(
    lap: Lap,
    isLatest: Boolean,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val minutes = (lap.lapTime % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (lap.lapTime % (1000 * 60)) / 1000
    val centiseconds = (lap.lapTime % 1000) / 10
    val lapTimeStr = String.format("%02d:%02d.%02d", minutes, seconds, centiseconds)

    val totMinutes = (lap.totalTime % (1000 * 60 * 60)) / (1000 * 60)
    val totSeconds = (lap.totalTime % (1000 * 60)) / 1000
    val totCentiseconds = (lap.totalTime % 1000) / 10
    val totalTimeStr = String.format("%02d:%02d.%02d", totMinutes, totSeconds, totCentiseconds)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%s %02d", stringResource(R.string.action_lap), lap.lapNumber),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = if (isLatest) Gold else appColors.textPrimary
            )
        )
        Text(
            text = "+$lapTimeStr",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isLatest) Gold else appColors.textSecondary
            )
        )
        Text(
            text = totalTimeStr,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            )
        )
    }
}

@Preview(name = "Stopwatch Light Theme", showSystemUi = true)
@Composable
fun StopwatchScreenLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        Surface(color = LocalAppColors.current.background) {
            LapRow(lap = Lap(1, 40200L, 40200L), isLatest = true)
        }
    }
}

@Preview(name = "Stopwatch Dark Theme", showSystemUi = true)
@Composable
fun StopwatchScreenDarkPreview() {
    AlarmClockTheme(darkTheme = true) {
        Surface(color = LocalAppColors.current.background) {
            LapRow(lap = Lap(2, 25220L, 65420L), isLatest = false)
        }
    }
}
