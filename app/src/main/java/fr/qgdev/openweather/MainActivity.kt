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
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModel
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

    private lateinit var placeViewModel: PlaceViewModel
    private lateinit var settingsViewModel: SettingsViewModel

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

        placeViewModel = PlaceViewModel(placeRepository)
        settingsViewModel = SettingsViewModel(settingsRepository, formattingService)

        android.util.Log.d("MainActivity", "✅ [onCreate] All ViewModels initialized")

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

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val items = listOf(
            Screen.Places,
            Screen.Settings
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                NavigationBar (

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
                            label = { Text(stringResource(screen.title)) },
                            selected = currentDestination?.route == screen.route,
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
                        settingsViewModel = settingsViewModel
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreenView(
                        settingsRepository = settingsRepository
                    )
                }

            }
        }
    }
}
