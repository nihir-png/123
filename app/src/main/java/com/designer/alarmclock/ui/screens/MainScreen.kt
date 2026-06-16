package com.designer.alarmclock.ui.screens

import com.designer.alarmclock.ui.alarm.AlarmListScreen
import com.designer.alarmclock.ui.worldclock.WorldClockScreen
import com.designer.alarmclock.ui.stopwatch.StopwatchScreen
import com.designer.alarmclock.ui.timer.TimerScreen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.FigmaBackground
import com.designer.alarmclock.ui.theme.LocalSpacing
import com.designer.alarmclock.ui.theme.NavActive
import com.designer.alarmclock.ui.theme.NavBarBackground
import com.designer.alarmclock.ui.theme.NavInactive

enum class AppTab(val title: String, val icon: ImageVector) {
    Alarm("Alarm", Icons.Default.Alarm),
    WorldClock("Clock", Icons.Default.AccessTime),
    Timer("Timer", Icons.Default.HourglassBottom),
    Stopwatch("Stopwatch", Icons.Default.Timer)
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(AppTab.Alarm) }

    Scaffold(
        containerColor = FigmaBackground,
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.Alarm -> AlarmListScreen()
                AppTab.WorldClock -> WorldClockScreen()
                AppTab.Timer -> TimerScreen()
                AppTab.Stopwatch -> StopwatchScreen()
            }
        }
    }
}

@Composable
private fun CustomBottomNavigation(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit
) {
    // Bottom nav: 85% translucent white bar, 30dp top corners, soft top shadow
    // (Figma node 1:2164/1:2478). Active tab = gold, inactive = grey.
    Surface(
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        color = NavBarBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 14.dp, bottom = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.12f else 1.0f,
                    label = "iconScale"
                )
                val tabColor by animateColorAsState(
                    targetValue = if (isSelected) NavActive else NavInactive,
                    label = "tabColor"
                )

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) }
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tabColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = tabColor
                    )
                }
            }
        }
    }
}

@Preview(name = "Main Screen Light Theme", showSystemUi = true)
@Composable
fun MainScreenLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        MainScreen()
    }
}
