package com.designer.alarmclock.ui.screens

import com.designer.alarmclock.ui.alarm.AlarmListScreen
import com.designer.alarmclock.ui.worldclock.WorldClockScreen
import com.designer.alarmclock.ui.stopwatch.StopwatchScreen
import com.designer.alarmclock.ui.timer.TimerScreen
import com.designer.alarmclock.ui.settings.SettingsScreen

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
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.designer.alarmclock.R
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.LocalAppColors

enum class AppTab(@StringRes val title: Int, val icon: ImageVector) {
    Alarm(R.string.tab_alarm, Icons.Default.Alarm),
    WorldClock(R.string.tab_clock, Icons.Default.AccessTime),
    Timer(R.string.tab_timer, Icons.Default.HourglassBottom),
    Stopwatch(R.string.tab_stopwatch, Icons.Default.Timer)
}

@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(AppTab.Alarm) }
    var showSettings by remember { mutableStateOf(false) }

    // Settings is a full-screen destination opened from the Alarm header gear icon.
    // It draws over the tab UI and returns here when dismissed (back arrow / system back).
    if (showSettings) {
        SettingsScreen(onBack = { showSettings = false })
        return
    }

    Scaffold(
        containerColor = LocalAppColors.current.background,
        bottomBar = {
            CustomBottomNavigation(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { paddingValues ->
        // Apply the Scaffold insets (status bar at the top, bottom-nav height at the
        // bottom) AND consume them. Each tab screen below has its own Scaffold; without
        // consuming here those inner Scaffolds would re-add the same system-bar insets,
        // double-padding the content and squeezing it on real devices.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            when (currentTab) {
                AppTab.Alarm -> AlarmListScreen(onOpenSettings = { showSettings = true })
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
    // Bottom nav: opaque surface, 30dp top corners, soft top shadow
    // (Figma node 1:2164/1:2478). Active tab = gold, inactive = grey.
    // Surface color flips to a dark surface in dark mode for proper contrast.
    val appColors = LocalAppColors.current
    Surface(
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        color = appColors.navBar,
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
                    targetValue = if (isSelected) appColors.navActive else appColors.navInactive,
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
                    val title = stringResource(tab.title)
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = title,
                        tint = tabColor,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = title,
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
