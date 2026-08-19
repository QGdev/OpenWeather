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

package fr.qgdev.openweather.data.settings

import android.content.Context
import fr.qgdev.openweather.data.storage.SecuredPreferenceDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * SettingsRepository
 *
 * Manages application settings, providing a centralized way to access and modify them.
 * It utilizes the `SecuredPreferenceDataStore` for secure storage of settings and exposes
 * them through a reactive `Flow` for easy observation and updates.
 *
 * This class follows the Singleton design pattern to ensure a single instance throughout the application.
 *
 * Settings include:
 * - Temperature Unit (Celsius, Fahrenheit, Kelvin)
 * - Measurement Unit (Metric, Imperial)
 * - Pressure Unit (hPa, mbar, psi, inHg)
 * - Wind Direction Unit (Cardinal Points, Angular)
 * - Time Format (12-hour, 24-hour)
 * - Default Locale
 * - API Key
 * - Periodic Update Enable/Disable
 *
 * @property settingsFlow A `Flow` that emits the current `Settings` state whenever it changes.
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @constructor Creates a SettingsService instance. Should be accessed through `getInstance(context)`.
 * @param context The application context.
 */
class SettingsRepository private constructor(context: Context) {

    private val securedPreferenceDataStore: SecuredPreferenceDataStore
    private val _settingsFlow: MutableStateFlow<Settings>

    val settingsFlow: StateFlow<Settings>

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        @Synchronized
        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE?: synchronized(this) {
                INSTANCE = SettingsRepository(context)
                return INSTANCE!!
            }
        }
    }

    init {
        securedPreferenceDataStore = SecuredPreferenceDataStore(
            context,
            "fr.qgdev.openweather_preferences"
        )

        _settingsFlow = MutableStateFlow(
            Settings(
                getTemperatureSetting(),
                getMeasureSetting(),
                getPressureSetting(),
                getWindDirectionSetting(),
                getTimeSetting(),
                getDefaultLocale(),
                getApiKey(),
                isPeriodicUpdateEnabled(),
                getUpdatePeriodSetting()
            )
        )

        settingsFlow = _settingsFlow
    }

    private enum class PreferenceKey(val key: String) {
        TEMPERATURE_UNIT("conf_temperature_unit"),
        MEASURE_UNIT("conf_measure_unit"),
        PRESSURE_UNIT("conf_pressure_unit"),
        DIRECTION_UNIT("conf_direction_unit"),
        TIME_FORMAT("conf_time_format"),
        API_KEY("conf_api_key"),
        UPDATE_PERIODIC("conf_update_periodic"),
        UPDATE_PERIOD("conf_update_period")
    }

    fun getTemperatureSetting(): TemperatureSettings {
        return when (securedPreferenceDataStore.getString(PreferenceKey.TEMPERATURE_UNIT.key, "")) {
            "fahrenheit" -> TemperatureSettings.FAHRENHEIT
            "kelvin" -> TemperatureSettings.KELVIN
            else -> TemperatureSettings.CELSIUS
        }
    }

    fun setTemperatureSetting(setting: TemperatureSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.TEMPERATURE_UNIT.key, setting.name.lowercase())
        _settingsFlow.value = _settingsFlow.value.copy(temperatureUnit = setting)
    }

    fun getMeasureSetting(): MeasureSettings {
        return when (securedPreferenceDataStore.getString(PreferenceKey.MEASURE_UNIT.key, "")) {
            "imperial" -> MeasureSettings.IMPERIAL
            else -> MeasureSettings.METRIC
        }
    }

    fun setMeasureSetting(setting: MeasureSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.MEASURE_UNIT.key, setting.name.lowercase())
        _settingsFlow.value = _settingsFlow.value.copy(measureUnit = setting)
    }

    fun getPressureSetting(): PressureSettings {
        return when (securedPreferenceDataStore.getString(PreferenceKey.PRESSURE_UNIT.key, "")) {
            "mbar" -> PressureSettings.BAROMETRIC
            "psi" -> PressureSettings.POUNDS_SQUARE_INCH
            "inhg" -> PressureSettings.INCH_MERCURY
            else -> PressureSettings.HECTOPASCAL
        }
    }

    fun setPressureSetting(setting: PressureSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.PRESSURE_UNIT.key, setting.name.lowercase())
        _settingsFlow.value = _settingsFlow.value.copy(pressureUnit = setting)
    }

    fun getWindDirectionSetting(): WindDirectionSettings {
        return when (securedPreferenceDataStore.getString(PreferenceKey.DIRECTION_UNIT.key, "")) {
            "angular" -> WindDirectionSettings.ANGULAR
            else -> WindDirectionSettings.CARDINAL_POINTS
        }
    }

    fun setWindDirectionSetting(setting: WindDirectionSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.DIRECTION_UNIT.key, setting.name.lowercase())
        _settingsFlow.value = _settingsFlow.value.copy(windDirectionUnit = setting)
    }

    fun getTimeSetting(): TimeSettings {
        return when (securedPreferenceDataStore.getString(PreferenceKey.TIME_FORMAT.key, "")) {
            "12" -> TimeSettings.TWELVE_HOURS
            else -> TimeSettings.TWENTY_FOUR_HOURS
        }
    }

    fun setTimeSetting(setting: TimeSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.TIME_FORMAT.key, setting.name.split("_").first())
        _settingsFlow.value = _settingsFlow.value.copy(timeFormat = setting)
    }

    fun getDefaultLocale(): Locale {
        return Locale.getDefault()
    }

    fun getApiKey(): String? {
        return securedPreferenceDataStore.getString(PreferenceKey.API_KEY.key, null)
    }

    fun setApiKey(apiKey: String) {
        securedPreferenceDataStore.putString(PreferenceKey.API_KEY.key, apiKey)
        _settingsFlow.value = _settingsFlow.value.copy(apiKey = apiKey)
    }

    fun isPeriodicUpdateEnabled(): Boolean {
        return securedPreferenceDataStore.getBoolean(PreferenceKey.UPDATE_PERIODIC.key, false)
    }

    fun setPeriodicUpdateEnabled(enabled: Boolean) {
        securedPreferenceDataStore.putBoolean(PreferenceKey.UPDATE_PERIODIC.key, enabled)
        _settingsFlow.value = _settingsFlow.value.copy(periodicUpdateEnabled = enabled)
    }

    fun getUpdatePeriodSetting(): UpdatePeriodSettings {
        val stored = securedPreferenceDataStore.getString(PreferenceKey.UPDATE_PERIOD.key, null)
        return UpdatePeriodSettings.entries.find { it.name == stored }
            ?: UpdatePeriodSettings.THIRTY_MINUTES
    }

    fun setUpdatePeriodSetting(period: UpdatePeriodSettings) {
        securedPreferenceDataStore.putString(PreferenceKey.UPDATE_PERIOD.key, period.name)
        _settingsFlow.value = _settingsFlow.value.copy(updatePeriod = period)
    }
}