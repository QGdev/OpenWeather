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

package fr.qgdev.openweather.ui.fragment.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.settings.MeasureSettings
import fr.qgdev.openweather.data.settings.PressureSettings
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.data.settings.TemperatureSettings
import fr.qgdev.openweather.data.settings.TimeSettings
import fr.qgdev.openweather.data.settings.UpdatePeriodSettings
import fr.qgdev.openweather.data.settings.WindDirectionSettings
import fr.qgdev.openweather.ui.components.dialogs.AboutAppDialog
import fr.qgdev.openweather.ui.icons.VisibilityOff
import fr.qgdev.openweather.ui.icons.VisibilityOn
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreenView(
    settingsRepository: SettingsRepository
) {

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // API Settings
                SettingsCategory(title = stringResource(R.string.title_settings_api)) {
                    ApiKeySetting(
                        obfuscated = true,
                        needsObfuscation = true,
                        defaultValue = settingsRepository.getApiKey()?: "",
                        onValueChanged = { settingsRepository.setApiKey(it) }
                    )
                }

                // Time Settings
                SettingsCategory(title = stringResource(R.string.title_settings_time)) {
                    ListSetting(
                        title = stringResource(R.string.title_settings_hour_format),
                        entries = arrayOf(
                            Pair(stringResource(R.string.title_settings_time_format_12h), TimeSettings.TWELVE_HOURS),
                            Pair(stringResource(R.string.title_settings_time_format_24h), TimeSettings.TWENTY_FOUR_HOURS)
                        ),
                        defaultValue = settingsRepository.getTimeSetting(),
                        onSelectionChanged = { settingsRepository.setTimeSetting(it) }
                    )
                }

                // Units Settings
                SettingsCategory(title = stringResource(R.string.title_settings_units)) {
                    ListSetting(
                        title = stringResource(R.string.title_settings_units_temperature),
                        entries = arrayOf(
                            Pair(stringResource(R.string.title_settings_units_temperature_celsius), TemperatureSettings.CELSIUS),
                            Pair(stringResource(R.string.title_settings_units_temperature_fahrenheit), TemperatureSettings.FAHRENHEIT),
                            Pair(stringResource(R.string.title_settings_units_temperature_kelvin), TemperatureSettings.KELVIN)
                        ),
                        defaultValue = settingsRepository.getTemperatureSetting(),
                        onSelectionChanged = { settingsRepository.setTemperatureSetting(it) }
                    )
                    ListSetting(
                        title = stringResource(R.string.title_settings_units_measure),
                        entries = arrayOf(
                            Pair(stringResource(R.string.title_settings_units_measure_metric), MeasureSettings.METRIC),
                            Pair(stringResource(R.string.title_settings_units_measure_imperial), MeasureSettings.IMPERIAL)
                        ),
                        defaultValue = settingsRepository.getMeasureSetting(),
                        onSelectionChanged = { settingsRepository.setMeasureSetting(it) }
                    )
                    ListSetting(
                        title = stringResource(R.string.title_settings_units_pressure),
                        entries = arrayOf(
                            Pair(stringResource(R.string.title_settings_units_pressure_mbar), PressureSettings.BAROMETRIC),
                            Pair(stringResource(R.string.title_settings_units_pressure_psi), PressureSettings.POUNDS_SQUARE_INCH),
                            Pair(stringResource(R.string.title_settings_units_pressure_inhg), PressureSettings.INCH_MERCURY),
                            Pair(stringResource(R.string.title_settings_units_pressure_hpa), PressureSettings.HECTOPASCAL)
                        ),
                        defaultValue = settingsRepository.getPressureSetting(),
                        onSelectionChanged = { settingsRepository.setPressureSetting(it) }
                    )
                    ListSetting(
                        title = stringResource(R.string.title_settings_units_direction),
                        entries = arrayOf(
                            Pair(stringResource(R.string.title_settings_units_direction_degrees), WindDirectionSettings.ANGULAR),
                            Pair(stringResource(R.string.title_settings_units_direction_cardinal), WindDirectionSettings.CARDINAL_POINTS)
                        ),
                        defaultValue = settingsRepository.getWindDirectionSetting(),
                        onSelectionChanged = { settingsRepository.setWindDirectionSetting(it) }
                    )
                }

                // Update Settings
                SettingsCategory(
                    title = stringResource(R.string.title_settings_update)
                ) {
                    var periodicEnabled by remember {
                        mutableStateOf(settingsRepository.isPeriodicUpdateEnabled())
                    }

                    SwitchSetting(
                        title = stringResource(R.string.title_settings_update_periodic),
                        summaryOn = stringResource(R.string.summary_enabled_settings_update_periodic),
                        summaryOff = stringResource(R.string.summary_disabled_settings_update_periodic),
                        defaultValue = periodicEnabled,
                        onValueChanged = {
                            periodicEnabled = it
                            settingsRepository.setPeriodicUpdateEnabled(it)
                        }
                    )

                    if (periodicEnabled) {
                        ListSetting(
                            title = stringResource(R.string.title_settings_update_period),
                            entries = arrayOf(
                                Pair(stringResource(R.string.title_settings_update_period_1min),   UpdatePeriodSettings.ONE_MINUTE),
                                Pair(stringResource(R.string.title_settings_update_period_15min), UpdatePeriodSettings.FIFTEEN_MINUTES),
                                Pair(stringResource(R.string.title_settings_update_period_30min), UpdatePeriodSettings.THIRTY_MINUTES),
                                Pair(stringResource(R.string.title_settings_update_period_1h),    UpdatePeriodSettings.ONE_HOUR),
                                Pair(stringResource(R.string.title_settings_update_period_2h),    UpdatePeriodSettings.TWO_HOURS),
                                Pair(stringResource(R.string.title_settings_update_period_3h),    UpdatePeriodSettings.THREE_HOURS),
                                Pair(stringResource(R.string.title_settings_update_period_6h),    UpdatePeriodSettings.SIX_HOURS),
                                Pair(stringResource(R.string.title_settings_update_period_12h),   UpdatePeriodSettings.TWELVE_HOURS)
                            ),
                            defaultValue = settingsRepository.getUpdatePeriodSetting(),
                            onSelectionChanged = { settingsRepository.setUpdatePeriodSetting(it) }
                        )
                    }
                }
                Spacer(
                    modifier = Modifier
                        .padding(top = 96.dp)
                        .fillMaxWidth()
                )
            }

            AboutAppButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenViewPreview() {
    SettingsScreenView(
        settingsRepository = SettingsRepository.getInstance(LocalContext.current),
    )
}

@Composable
fun SettingsCategory(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
    )
    content()
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySetting(
    modifier: Modifier = Modifier,
    defaultValue: String = "",
    obfuscated: Boolean = true,
    needsObfuscation: Boolean = true,
    onValueChanged: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(defaultValue) }
    var isObfuscated by remember { mutableStateOf(obfuscated) }
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = apiKey,
        onValueChange = { apiKey = it },
        label = { Text(stringResource(R.string.title_settings_api_key)) },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    onValueChanged(apiKey)
                }
            },
        visualTransformation = if (isObfuscated) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions.Default,
        trailingIcon = {
            if (needsObfuscation) {
                val imageVector = if (isObfuscated) Icons.VisibilityOn else Icons.VisibilityOff
                val description = if (isObfuscated) "Show API Key" else "Hide API Key"
                IconButton(onClick = { isObfuscated = !isObfuscated }) {
                    Icon(imageVector = imageVector, contentDescription = description)
                }
            }
        }
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> ListSetting(
    modifier: Modifier = Modifier,
    title: String,
    entries: Array<Pair<String, T>>,
    defaultValue: T,
    onSelectionChanged: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(defaultValue) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = entries.find { it.second == selectedOption }?.first ?: "",
            onValueChange = {},
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            entries.forEach { (displayText, enumValue) ->
                DropdownMenuItem(
                    text = { Text(displayText) },
                    onClick = {
                        selectedOption = enumValue
                        onSelectionChanged(selectedOption)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    title: String,
    summaryOn: String,
    summaryOff: String,
    defaultValue: Boolean = false,
    onValueChanged: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(defaultValue) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isChecked = !isChecked
                onValueChanged(isChecked)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isChecked) summaryOn else summaryOff,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onValueChanged(isChecked)
            }
        )
    }
}

@Composable
private fun AboutAppButton(
    modifier: Modifier = Modifier,
) {
    val isAboutAppDialogOpened = remember { mutableStateOf(false) }
    val debugMode = remember { mutableStateOf(false) }
    
    ExtendedFloatingActionButton(
        modifier = modifier,
        text = { Text(stringResource(R.string.about_us)) },
        icon = { Icon(Icons.Filled.Info, contentDescription = "About Us") },
        onClick = {
            isAboutAppDialogOpened.value = true
            debugMode.value = false
        }
    )

    if (isAboutAppDialogOpened.value) {
        AboutAppDialog(
            onDismissRequest = { isAboutAppDialogOpened.value = false },
            debugMode = debugMode.value,
            onDebugModeToggle = { debugMode.value = it }
        )
    }
}


private enum class TestEnum { OPTION_1, OPTION_2, OPTION_3 }

@Preview(showBackground = true)
@Composable
fun SettingsCategoryPreview() {

    // Create enum classes for the settings
    SettingsCategory(title = "Title") {
        ApiKeySetting(
            defaultValue = "API_KEY",
            onValueChanged = {}
        )
        ListSetting(
            title = "Title",
            entries = arrayOf(
                Pair("Option 1", TestEnum.OPTION_1),
                Pair("Option 2", TestEnum.OPTION_2),
                Pair("Option 3", TestEnum.OPTION_3)
            ),
            defaultValue = TestEnum.OPTION_1,
            onSelectionChanged = {}
        )
        SwitchSetting(
            title = "Title",
            summaryOn = "Summary On",
            summaryOff = "Summary Off",
            onValueChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ApiKeySettingPreview() {
    ApiKeySetting(
        defaultValue = "API_KEY",
        onValueChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ListSettingPreview() {
    ListSetting(
        title = "Title",
        entries = arrayOf(
            Pair("Option 1", TestEnum.OPTION_1),
            Pair("Option 2", TestEnum.OPTION_2),
            Pair("Option 3", TestEnum.OPTION_3)
        ),
        defaultValue = TestEnum.OPTION_1,
        onSelectionChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SwitchSettingPreview() {
    SwitchSetting(
        title = "Title",
        summaryOn = "Summary On",
        summaryOff = "Summary Off",
        onValueChanged = {}
    )
}