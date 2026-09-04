/*
 *  Copyright (c) 2019 - 2024
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
package fr.qgdev.openweather

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.repositories.WidgetRepository
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.ui.fragment.place.PlacesScreenView
import fr.qgdev.openweather.ui.fragment.settings.SettingsScreenView
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fr.qgdev.openweather.ui.components.dialogs.AirQualityInfoDialog
import fr.qgdev.openweather.ui.components.dialogs.WeatherAlertDialog
import fr.qgdev.openweather.ui.place.detail.PlaceDetailScreen
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModelFactory
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModel
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModelFactory
import fr.qgdev.openweather.widgets.WidgetsManager
import kotlinx.coroutines.launch

/**
 * MainActivity
 *
 *
 * The main activity of the application.
 * Contains a navigation bar to navigate between the live data, the forecasts and the settings.
 * Also schedules a periodic work request to update the widgets.
 *
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see AppCompatActivity
 */
class MainActivity : AppCompatActivity() {

    private lateinit var placeRepository: PlaceRepository
    private lateinit var widgetRepository: WidgetRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var formattingService: FormattingService

    //  Obtained through the ViewModelStore rather than constructed. Building them in onCreate gave
    //  a brand new instance on every configuration change, so a rotation discarded the collected
    //  places and settings and left the previous viewModelScope uncleared - losing exactly the
    //  state a ViewModel exists to preserve.
    private val placeViewModel: PlaceViewModel by viewModels {
        PlaceViewModelFactory(applicationContext)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(applicationContext)
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("MainActivity", "🚀 [onCreate] Starting MainActivity initialization...")

        placeRepository = PlaceRepository.getInstance(applicationContext)
        widgetRepository = WidgetRepository.getInstance(applicationContext)
        settingsRepository = SettingsRepository.getInstance(applicationContext)
        formattingService = FormattingService.getInstance(applicationContext)

        android.util.Log.d("MainActivity", "✅ [onCreate] All repositories initialized")

        // Check initial settings state
        val initialSettings = settingsRepository.settingsFlow.value
        android.util.Log.d("MainActivity", "📋 [onCreate] Initial settings - Periodic update: ${initialSettings.periodicUpdateEnabled}, Period: ${initialSettings.updatePeriod.name}")

        // Observer les changements de paramètres pour réagir aux modifications
        lifecycleScope.launch {
            android.util.Log.d("MainActivity", "📋 [onCreate] Settings observer launched")
            settingsRepository.settingsFlow.collect { settings ->
                android.util.Log.d("MainActivity", "📋 [Settings] Periodic update changed: ${settings.periodicUpdateEnabled}")
                if (settings.periodicUpdateEnabled) {
                    android.util.Log.d("MainActivity", "✅ [Settings] Enabling periodic updates with period: ${settings.updatePeriod.name}")
                    schedulePeriodicWidgetUpdate()
                } else {
                    android.util.Log.d("MainActivity", "❌ [Settings] Disabling periodic updates")
                    unschedulePeriodicWidgetUpdate()
                }
            }
        }

        android.util.Log.d("MainActivity", "✅ [onCreate] MainActivity initialized")

        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }

    /**
     * Planifie la mise à jour périodique des widgets.
     * Calcule le temps jusqu'au prochain quart d'heure et planifie une mise à jour.
     */
    private fun schedulePeriodicWidgetUpdate() {
        val tag = "MainActivity.schedulePeriodicWidgetUpdate"
        
        try {
            val widgetsManager = WidgetsManager.getInstance(applicationContext)
            val settings = settingsRepository.settingsFlow.value
            val periodMillis = settings.updatePeriod.durationMillis
            
            val now = System.currentTimeMillis()
            // Calculate time until next aligned mark for the chosen period
            // e.g. if period=60000ms (1 min) and now=12:00:30 → next mark at 12:01:00 (30 sec wait)
            val timeUntilNextMark = periodMillis - (now % periodMillis)
            
            android.util.Log.d(tag, "🎯 [schedulePeriodicWidgetUpdate] Starting...")
            android.util.Log.d(tag, "   ⏱️  Current time: $now ms")
            android.util.Log.d(tag, "   📅 Period: ${settings.updatePeriod.name} ($periodMillis ms)")
            android.util.Log.d(tag, "   ⏱️  Time until next mark: $timeUntilNextMark ms (${timeUntilNextMark / 1000}s)")
            
            android.util.Log.d(tag, "📤 Calling WidgetsManager.scheduleWorkRequest()...")
            widgetsManager.scheduleWorkRequest(applicationContext, java.time.Duration.ofMillis(timeUntilNextMark))
            
            android.util.Log.d(tag, "✅ Work scheduled successfully")
        } catch (e: Exception) {
            android.util.Log.e(tag, "❌ Failed to schedule work: ${e.message}", e)
        }
    }

    /**
     * Désactive la mise à jour périodique des widgets.
     */
    private fun unschedulePeriodicWidgetUpdate() {
        val tag = "MainActivity.unschedulePeriodicWidgetUpdate"
        
        try {
            val widgetsManager = WidgetsManager.getInstance(applicationContext)
            
            android.util.Log.d(tag, "Cancelling periodic update work...")
            widgetsManager.unscheduleWorkRequest(applicationContext)
            
            android.util.Log.d(tag, "✅ Work cancelled successfully")
        } catch (e: Exception) {
            android.util.Log.e(tag, "❌ Failed to cancel work: ${e.message}", e)
        }
    }


    sealed class Screen(
        val route: String,
        val title: Int,
        val description: Int,
        val icon: ImageVector,
        val iconOnSelected: ImageVector,
    ) {
        object Places : Screen(
            "home",
            R.string.title_places,
            R.string.description_places,
            Icons.Outlined.Place,
            Icons.Filled.Place
        )

        object Settings : Screen(
            "settings",
            R.string.title_settings,
            R.string.description_settings,
            Icons.Outlined.Settings,
            Icons.Filled.Settings
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MainScreen()
    }

    /**
     * The detail screen, with the dialogs it can open.
     *
     * The place is re-read from the list on every change, so a refresh landing while the screen is
     * open updates it. If the place goes away - deleted from the list, or the process restored
     * without a selection - the screen steps back rather than showing an empty shell.
     */
    @Composable
    fun PlaceDetailRoute(
        placeViewModel: PlaceViewModel,
        settingsViewModel: SettingsViewModel,
        onBack: () -> Unit
    ) {
        val place by placeViewModel.selectedPlace.collectAsState()
        val formattingService by settingsViewModel.formattingServiceState.collectAsState()
        var alertsOpened by remember { mutableStateOf(false) }
        var airQualityOpened by remember { mutableStateOf(false) }

        val shownPlace = place
        if (shownPlace == null) {
            LaunchedEffect(Unit) { onBack() }
            return
        }

        AppTheme {
            PlaceDetailScreen(
                place = shownPlace,
                formattingService = formattingService,
                onBack = onBack,
                onOpenAlerts = { alertsOpened = true },
                onOpenAirQuality = { airQualityOpened = true }
            )
        }

        if (alertsOpened) {
            WeatherAlertDialog(
                place = shownPlace,
                formattingService = formattingService,
                onDismissRequest = { alertsOpened = false }
            )
        }
        if (airQualityOpened) {
            AirQualityInfoDialog(onDismissRequest = { airQualityOpened = false })
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val items = listOf(
            Screen.Places,
            Screen.Settings
        )

        val navBackStackEntry = navController.currentBackStackEntryAsState().value
        val onDetailScreen = navBackStackEntry?.destination?.route == PLACE_DETAIL_ROUTE

        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                //  The detail screen is stacked on top of Places rather than being a destination of
                //  its own, so the tabs step aside while it is open.
                if (onDetailScreen) return@Scaffold
                //  The bar sits on the screen's own ground under a hairline, as in the redesign,
                //  rather than on Material's tinted surface. The selected tab is marked by colour
                //  alone, in the system accent: no pill behind the icon.
                val palette = LocalWeatherPalette.current
                NavigationBar(
                    modifier = Modifier.drawBehind {
                        drawLine(palette.outline, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
                    },
                    containerColor = palette.screen,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry = navController.currentBackStackEntryAsState().value
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        val isSelected = currentDestination?.route == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.iconOnSelected else screen.icon,
                                    contentDescription = stringResource(screen.description)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(screen.title),
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                            },
                            selected = currentDestination?.route == screen.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = palette.textQuiet,
                                unselectedTextColor = palette.textQuiet,
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) {
            NavHost(
                modifier = Modifier.padding(it),
                navController = navController,
                startDestination = Screen.Places.route
            ) {
                composable(Screen.Places.route) {
                    PlacesScreenView(
                        placeViewModel = placeViewModel,
                        settingsViewModel = settingsViewModel,
                        onOpenPlace = { place ->
                            placeViewModel.selectPlace(place)
                            navController.navigate(PLACE_DETAIL_ROUTE)
                        }
                    )
                }
                composable(PLACE_DETAIL_ROUTE) {
                    PlaceDetailRoute(
                        placeViewModel = placeViewModel,
                        settingsViewModel = settingsViewModel,
                        onBack = {
                            placeViewModel.clearSelectedPlace()
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    //  The places give the unit choices a real value to convert, and the update
                    //  interval its daily call count.
                    val places by placeViewModel.placesState.collectAsState()
                    SettingsScreenView(
                        settingsRepository = settingsRepository,
                        places = places
                    )
                }

            }
        }
    }
}

/** The place detail screen, stacked over the Places tab. */
private const val PLACE_DETAIL_ROUTE = "place_detail"
