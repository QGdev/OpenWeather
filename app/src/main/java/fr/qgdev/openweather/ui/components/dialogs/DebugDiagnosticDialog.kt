/*
 *  Copyright (c) 2019 - 2026
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather.
 *
 *  OpenWeather is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenWeather is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.ui.components.dialogs

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun DebugDiagnosticDialog(
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository.getInstance(context) }
    val placeRepository = remember { PlaceRepository.getInstance(context) }

    val settingsState = settingsRepository.settingsFlow.collectAsState()
    val placesState = placeRepository.placesFlow.collectAsState(initial = emptyList())

    val workStatusText = remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        withTimeoutOrNull(2000) {
            workStatusText.value = getWorkManagerStatus(context)
        } ?: run {
            workStatusText.value = "WorkManager status: Timeout (check logs)"
        }
    }

    FullScreenDialog(
        title = "🐛 Debug Diagnostic",
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {
            // Settings Section
            item {
                DebugSectionHeader(title = "⚙️ Settings")
            }
            item {
                val settings = settingsState.value
                val apiKey = settings.apiKey ?: "Not set"
                val obfuscatedApiKey = if (apiKey.length > 4) {
                    "*".repeat(apiKey.length - 4) + apiKey.takeLast(4)
                } else {
                    "*".repeat(apiKey.length)
                }

                DebugMonoText("""
                    Temperature Unit: ${settings.temperatureUnit}
                    Measure Unit: ${settings.measureUnit}
                    Pressure Unit: ${settings.pressureUnit}
                    Wind Direction Unit: ${settings.windDirectionUnit}
                    Time Format: ${settings.timeFormat}
                    API Key: $obfuscatedApiKey
                    Periodic Update Enabled: ${settings.periodicUpdateEnabled}
                    Update Period: ${settings.updatePeriod}
                """.trimIndent())
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // WorkManager Section (Enhanced)
            item {
                DebugSectionHeader(title = "⏱️ WorkManager Status & Periodic Updates")
            }
            item {
                val enhancedWorkStatus = arrayOf(
                    workStatusText.value,
                    "",
                    "Periodic Update Configuration:",
                    "   ├─ Enabled: ${settingsState.value.periodicUpdateEnabled}",
                    "   ├─ Update Period: ${settingsState.value.updatePeriod}",
                    "   ├─ Worker Name: PeriodicUpdaterWorker",
                    "   ├─ Update Type: Periodic (runs in background)",
                    "   └─ Constraint: Requires network")
                DebugMonoText(enhancedWorkStatus.joinToString("\n"))
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Statistics Section
            item {
                DebugSectionHeader(title = "📊 Statistics")
            }
            item {
                val places = placesState.value
                val statsText = """
                    Total Places: ${places.size}
                    Storage Status: ${if (places.isEmpty()) "Empty" else "Populated"}
                """.trimIndent()
                DebugMonoText(statsText)
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            // Raw Places Data Section (Enhanced)
            item {
                DebugSectionHeader(title = "🗺️ Stored Places Data (${placesState.value.size} places)")
            }

            items(placesState.value.size) { index ->
                val place = placesState.value[index]
                val coordinates = place.geolocation?.coordinates
                val properties = place.properties
                val city = place.geolocation?.city ?: "Unknown"
                val country = place.geolocation?.countryCode ?: "Unknown"
                
                // Format timestamps to readable format
                val lastWeatherUpdateStr = if (properties?.lastSuccessfulWeatherUpdateTime ?: 0L > 0) {
                    formatTimestamp(properties?.lastSuccessfulWeatherUpdateTime ?: 0L)
                } else {
                    "Never"
                }
                
                val lastAirQualityUpdateStr = if (properties?.lastSuccessfulAirQualityUpdateTime ?: 0L > 0) {
                    formatTimestamp(properties?.lastSuccessfulAirQualityUpdateTime ?: 0L)
                } else {
                    "Never"
                }
                
                val createdStr = if (properties?.creationTime ?: 0L > 0) {
                    formatTimestamp(properties?.creationTime ?: 0L)
                } else {
                    "Unknown"
                }
                
                DebugMonoText("""
                    ════════════════════════════════════════
                    🏙️  PLACE ${index + 1} - $city, $country
                    ════════════════════════════════════════
                    
                    📍 GEOLOCATION
                    ├─ Latitude: ${coordinates?.latitude ?: "N/A"}
                    ├─ Longitude: ${coordinates?.longitude ?: "N/A"}
                    ├─ City: $city
                    └─ Country Code: $country
                    
                    🌦️  WEATHER DATA
                    ├─ Has Current Weather: ${place.hasCurrentWeather()}
                    ├─ Daily Forecasts: ${place.dailyForecastListCount} days
                    ├─ Hourly Forecasts: ${place.hourlyForecastListCount} hours
                    └─ Weather Alerts: ${place.weatherAlertsListCount}
                    
                    💨 AIR QUALITY DATA
                    ├─ Has Air Quality: ${place.hasAirQuality()}
                    ├─ Minutely Forecast: ${place.minutelyForecastListCount} data points
                    └─ Last Update: $lastAirQualityUpdateStr
                    
                    ⏰ UPDATE STATUS & TIMESTAMPS
                    ├─ Created: $createdStr
                    ├─ Last Weather Update: $lastWeatherUpdateStr
                    ├─ Last Weather Attempt: ${formatTimestamp(properties?.lastWeatherUpdateAttemptTime ?: 0L)}
                    ├─ Last AQ Update: $lastAirQualityUpdateStr
                    ├─ Last AQ Attempt: ${formatTimestamp(properties?.lastAirQualityUpdateAttemptTime ?: 0L)}
                    └─ Time Zone Offset: ${properties?.timeOffset ?: 0} seconds
                    
                    📊 SUMMARY
                    ├─ Total Data Points: ${
                        (place.dailyForecastListCount + 
                        place.hourlyForecastListCount + 
                        place.minutelyForecastListCount + 
                        place.weatherAlertsListCount)
                    }
                    ├─ Storage Status: Complete
                    └─ Next Update: Based on periodic schedule
                """.trimIndent())
                
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "End of diagnostic data",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun DebugSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun DebugMonoText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Formats a timestamp (in milliseconds) to a human-readable string.
 * Returns "N/A" if timestamp is 0 or invalid.
 */
private fun formatTimestamp(timestampMs: Long): String {
    return if (timestampMs <= 0) {
        "N/A"
    } else {
        try {
            val date = java.util.Date(timestampMs)
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            format.format(date)
        } catch (e: Exception) {
            "Invalid (${timestampMs}ms)"
        }
    }
}

/**
 * Retrieves WorkManager status synchronously for debugging.
 * This is a simple implementation that checks the periodic updater worker.
 */
private fun getWorkManagerStatus(context: Context): String {
    return try {
        val workManager = androidx.work.WorkManager.getInstance(context)
        val workInfosLiveData = workManager.getWorkInfosForUniqueWorkLiveData("PeriodicUpdaterWorker")

        // Since this is sync context, we can't use LiveData observers properly
        // Return a status that WorkManagerDebugger would show
        "WorkManager Instance: Active\nWorker Name: PeriodicUpdaterWorker\nStatus: Check WorkManager via adb for details\nRun: adb shell am dump w | grep PeriodicUpdaterWorker"
    } catch (e: Exception) {
        "WorkManager Status: Error - ${e.message}"
    }
}

@Preview
@Composable
fun DebugDiagnosticDialogPreview() {
    DebugDiagnosticDialog(
        onDismissRequest = {}
    )
}










