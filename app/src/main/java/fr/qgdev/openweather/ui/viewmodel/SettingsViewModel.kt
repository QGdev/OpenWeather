/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.qgdev.openweather.data.settings.Settings
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.ui.format.FormattingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val formattingService: FormattingService,
    ) : ViewModel() {

    private val _settingsState = MutableStateFlow<Settings>(Settings())
    val settingsState: StateFlow<Settings?> = _settingsState.asStateFlow()

    private val _formattingServiceState = MutableStateFlow(formattingService)

    /**
     * The formatter the UI should use, republished as a new instance whenever the settings change.
     *
     * Collect this rather than calling [FormattingService.getInstance]: the singleton is mutated in
     * place, and Compose compares it by instance identity, so a screen holding the singleton never
     * notices a unit change. See [FormattingService.snapshotFor].
     */
    val formattingServiceState: StateFlow<FormattingService> = _formattingServiceState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _settingsState.value = settings
                //  Still mutate the shared instance, because the widgets read it directly...
                formattingService.update(settings)
                //  ...and publish a fresh one for Compose, which needs a new identity to redraw.
                _formattingServiceState.value = formattingService.snapshotFor(settings)
            }
        }
    }

    fun setApiKey(apiKey: String) {
        settingsRepository.setApiKey(apiKey)
    }

    fun isApiKeyValid(): Boolean {
        return settingsState.value?.apiKey?.length == 32
    }

    fun isPeriodicUpdateEnabled(): Boolean {
        return settingsState.value?.periodicUpdateEnabled ?: false
    }
}