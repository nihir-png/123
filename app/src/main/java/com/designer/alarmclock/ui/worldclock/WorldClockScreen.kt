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
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.designer.alarmclock.data.WorldClockCity
import com.designer.alarmclock.ui.theme.AlarmClockTheme
import com.designer.alarmclock.ui.theme.LocalSpacing
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorldClockScreen(
    viewModel: WorldClockViewModel = viewModel()
) {
    val selectedCities by viewModel.selectedCities.collectAsState()
    val spacing = LocalSpacing.current
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCityPicker = true },
                containerColor = Color(0xFFFFB800),
                contentColor = Color(0xFF1E1E1E),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add City",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                            text = "Clock",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        IconButton(onClick = { /* Menu */ }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Featured Local Time white card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp), // 24dp rounded corners
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(spacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Local time",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF7E7E7E),
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
                                        color = Color(0xFF1E1E1E)
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
                                        color = Color(0xFF7E7E7E)
                                    )
                                    Text(
                                        text = localAmPm,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB800) // Yellow active indicator
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(spacing.small))

                            Text(
                                text = dateString,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color(0xFF7E7E7E),
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
                        text = "World Clock",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
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
        shape = RoundedCornerShape(24.dp), // 24dp rounded corners
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.cityName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E),
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF7E7E7E)
                        )
                    )
                    Spacer(modifier = Modifier.width(spacing.small))
                    Text(
                        text = city.getTimeDifferenceString(),
                        fontSize = 12.sp,
                        color = Color(0xFFFFB800), // UTC offset highlighted in yellow
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
                            color = Color(0xFF1E1E1E)
                        )
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = amPm,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7E7E7E),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove City",
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
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background, // Cream background
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE2E2E2)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spacing.medium)
        ) {
            Text(
                text = "Select City",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
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
                                if (isAlreadyAdded) Color(0xFFFAF8F5).copy(alpha = 0.5f)
                                else Color.White
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
                                    color = if (isAlreadyAdded) Color(0xFF7E7E7E).copy(alpha = 0.5f)
                                            else Color(0xFF1E1E1E)
                                )
                            )
                            Text(
                                text = city.country,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF7E7E7E).copy(
                                        alpha = if (isAlreadyAdded) 0.5f else 1f
                                    )
                                )
                            )
                        }
                        
                        if (isAlreadyAdded) {
                            Text(
                                text = "Added",
                                fontSize = 12.sp,
                                color = Color(0xFF7E7E7E).copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            val tz = TimeZone.getTimeZone(city.timezoneId)
                            val now = System.currentTimeMillis()
                            val diffHours = (tz.getOffset(now) - TimeZone.getDefault().getOffset(now)) / (1000L * 60 * 60)
                            val diffStr = if (diffHours == 0L) "Same time" else if (diffHours > 0L) "+${diffHours}h" else "${diffHours}h"
                            
                            Text(
                                text = diffStr,
                                fontSize = 12.sp,
                                color = Color(0xFFFFB800),
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
                .background(Color.White)
                .padding(spacing.medium),
            contentAlignment = Alignment.Center
        ) {
            Text("🌐", fontSize = 40.sp)
        }
        
        Spacer(modifier = Modifier.height(spacing.large))
        
        Text(
            text = "No Cities Added",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(spacing.small))
        
        Text(
            text = "Track time in multiple locations around the world.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF7E7E7E)),
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 240.dp)
        )
    }
}

@Preview(name = "World Clock Screen Light", showSystemUi = true)
@Composable
fun WorldClockScreenLightPreview() {
    AlarmClockTheme(darkTheme = false) {
        WorldClockScreenPreviewHelper(
            listOf(
                WorldClockCity(id = 1, cityName = "Tokyo", country = "Japan", timezoneId = "Asia/Tokyo"),
                WorldClockCity(id = 2, cityName = "New York", country = "United States", timezoneId = "America/New_York"),
                WorldClockCity(id = 3, cityName = "London", country = "United Kingdom", timezoneId = "Europe/London")
            )
        )
    }
}

@Composable
fun WorldClockScreenPreviewHelper(cities: List<WorldClockCity>) {
    val spacing = LocalSpacing.current
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFFFFB800),
                contentColor = Color(0xFF1E1E1E),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add City")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Clock", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 32.sp))
                IconButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Local time", color = Color(0xFF7E7E7E), fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text("09:49", style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E)))
                        Text("AM", fontSize = 16.sp, color = Color(0xFFFFB800), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Text("Friday, 12 June", color = Color(0xFF7E7E7E), fontWeight = FontWeight.Bold)
                }
            }

            Text("World Clock", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp))

            cities.forEach { city ->
                WorldClockItem(city = city, onDelete = {})
            }
        }
    }
}
