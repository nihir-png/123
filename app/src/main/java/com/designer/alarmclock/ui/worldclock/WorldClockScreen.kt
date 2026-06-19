package com.designer.alarmclock.ui.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.R
import com.designer.alarmclock.data.WorldClockCity
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.Gold
import com.designer.alarmclock.ui.theme.LocalAppColors
import com.designer.alarmclock.ui.theme.LocalSpacing
import java.text.SimpleDateFormat
import java.util.*

private val OnGold = Color(0xFF1A1A1A)

@Composable
fun WorldClockScreen(
    viewModel: WorldClockViewModel = viewModel()
) {
    val selectedCities by viewModel.selectedCities.collectAsState()
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    var showCityPicker by remember { mutableStateOf(false) }

    // Live Local Time ticker
    var localTime by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            localTime = Date()
            kotlinx.coroutines.delay(1000)
        }
    }

    val localTimeStr = SimpleDateFormat("hh:mm", Locale.getDefault()).format(localTime)
    val localAmPm = SimpleDateFormat("a", Locale.getDefault()).format(localTime)
    val localSec = SimpleDateFormat("ss", Locale.getDefault()).format(localTime)
    val dateString = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(localTime)

    Scaffold(
        containerColor = appColors.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCityPicker = true },
                containerColor = Gold,
                contentColor = OnGold,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_city),
                    modifier = Modifier.size(28.dp)
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = spacing.large, vertical = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                // Header Clock title row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.small),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.clock_title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = appColors.textPrimary
                            )
                        )
                    }
                }

                // Featured Local Time card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.local_time),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = appColors.textSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(spacing.small))

                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = localTimeStr,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(
                                        text = ":$localSec",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.textSecondary
                                    )
                                    Text(
                                        text = localAmPm,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(spacing.small))

                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = appColors.textSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(spacing.small))
                }

                // World Clock header label
                item {
                    Text(
                        text = stringResource(R.string.world_clock),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary,
                            fontSize = 22.sp
                        )
                    )
                }

                if (selectedCities.isEmpty()) {
                    item {
                        EmptyWorldClockState { showCityPicker = true }
                    }
                } else {
                    items(selectedCities, key = { it.id }) { city ->
                        WorldClockItem(
                            city = city,
                            onDelete = { viewModel.removeCity(city) }
                        )
                    }
                }
            }

            // City Picker Dialog Sheet
            if (showCityPicker) {
                CityPickerSheet(
                    predefinedCities = viewModel.predefinedCities,
                    addedCities = selectedCities,
                    onDismiss = { showCityPicker = false },
                    onCitySelected = { city ->
                        viewModel.addCity(city)
                        showCityPicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun WorldClockItem(
    city: WorldClockCity,
    onDelete: () -> Unit
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current

    // Live updates for times
    var timeFormatted by remember { mutableStateOf(city.getCurrentTimeFormatted()) }
    var amPm by remember { mutableStateOf(city.getCurrentAmPm()) }

    LaunchedEffect(Unit) {
        while (true) {
            timeFormatted = city.getCurrentTimeFormatted()
            amPm = city.getCurrentAmPm()
            kotlinx.coroutines.delay(1000)
        }
    }

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.cityName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = appColors.textPrimary,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = appColors.textSecondary
                        )
                    )
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = city.getTimeDifferenceString(),
                        fontSize = 12.sp,
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(end = spacing.small)
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = amPm,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_remove_city),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityPickerSheet(
    predefinedCities: List<PredefinedCity>,
    addedCities: List<WorldClockCity>,
    onDismiss: () -> Unit,
    onCitySelected: (PredefinedCity) -> Unit
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = appColors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = appColors.toggleTrackOff) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.medium)
        ) {
            Text(
                text = stringResource(R.string.select_city),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = appColors.textPrimary
                ),
                modifier = Modifier
                    .padding(vertical = spacing.small)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(spacing.medium))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(bottom = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                val addedTimezones = addedCities.map { it.timezoneId }.toSet()

                items(predefinedCities) { city ->
                    val isAlreadyAdded = addedTimezones.contains(city.timezoneId)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isAlreadyAdded) appColors.chipUnselected else appColors.card
                            )
                            .clickable(enabled = !isAlreadyAdded) {
                                onCitySelected(city)
                            }
                            .padding(spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = city.cityName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAlreadyAdded) appColors.textSecondary else appColors.textPrimary
                                )
                            )
                            Text(
                                text = city.country,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = appColors.textSecondary
                                )
                            )
                        }

                        if (isAlreadyAdded) {
                            Text(
                                text = stringResource(R.string.city_added),
                                fontSize = 12.sp,
                                color = appColors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            val tz = TimeZone.getTimeZone(city.timezoneId)
                            val now = System.currentTimeMillis()
                            val diffHours = (tz.getOffset(now) - TimeZone.getDefault().getOffset(now)) / (1000L * 60 * 60)
                            val diffStr = if (diffHours == 0L) stringResource(R.string.same_time)
                                else if (diffHours > 0L) "+${diffHours}h" else "${diffHours}h"

                            Text(
                                text = diffStr,
                                fontSize = 12.sp,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWorldClockState(
    onAddClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val appColors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.doubleLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(appColors.card)
                .padding(spacing.medium),
            contentAlignment = Alignment.Center
        ) {
            Text("🌐", fontSize = 40.sp)
        }

        Spacer(modifier = Modifier.height(spacing.large))

        Text(
            text = stringResource(R.string.no_cities_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = stringResource(R.string.no_cities_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = appColors.textSecondary),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 240.dp)
        )
    }
}

@Preview(name = "World Clock Light", showSystemUi = true)
@Composable
fun WorldClockScreenLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        Surface(color = LocalAppColors.current.background) {
            WorldClockItem(
                city = WorldClockCity(id = 1, cityName = "Tokyo", country = "Japan", timezoneId = "Asia/Tokyo"),
                onDelete = {}
            )
        }
    }
}

@Preview(name = "World Clock Dark", showSystemUi = true)
@Composable
fun WorldClockScreenDarkPreview() {
    AlarmClockTheme(darkTheme = true) {
        Surface(color = LocalAppColors.current.background) {
            WorldClockItem(
                city = WorldClockCity(id = 2, cityName = "London", country = "United Kingdom", timezoneId = "Europe/London"),
                onDelete = {}
            )
        }
    }
}
