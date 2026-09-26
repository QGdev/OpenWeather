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
import androidx.annotation.VisibleForTesting
import androidx.preference.PreferenceDataStore
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
class SettingsRepository private constructor(
    private val securedPreferenceDataStore: PreferenceDataStore
) {

    private val _settingsFlow: MutableStateFlow<Settings>

    val settingsFlow: StateFlow<Settings>

    companion object {
        @Volatile
        private var INSTANCE: SettingsRepository? = null

        //  The second null check inside the lock is what makes this safe. @Synchronized on the
        //  method was masking its absence, at the cost of locking every single call.
        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(
                    SecuredPreferenceDataStore(context, PREFERENCES_FILENAME)
                ).also { INSTANCE = it }
            }
        }

        /**
         * Builds an instance over an arbitrary store, so the persistence round-trip can be
         * exercised on the JVM without a Context or the Android keystore.
         */
        @VisibleForTesting
        fun createForTest(preferenceDataStore: PreferenceDataStore): SettingsRepository =
            SettingsRepository(preferenceDataStore)

        private const val PREFERENCES_FILENAME = "fr.qgdev.openweather_preferences"
    }

    init {
        //  Settled once, before anything reads it. A key already saved means an install from
        //  before the choice existed, whose key was working with 2.5: it stays there rather than
        //  being broken by an update. A fresh install has no key yet and starts on 3.0, the only
        //  version new keys are accepted on.
        if (securedPreferenceDataStore.getString(PreferenceKey.ONE_CALL_VERSION.key, null) == null) {
            putEnum(
                PreferenceKey.ONE_CALL_VERSION,
                if (getApiKey() != null) OneCallVersion.V2_5 else OneCallVersion.V3_0
            )
        }

        _settingsFlow = MutableStateFlow(
            Settings(
                getTemperatureSetting(),
                getMeasureSetting(),
                getPressureSetting(),
                getWindDirectionSetting(),
                getTimeSetting(),
                getDefaultLocale(),
                getApiKey(),
                getOneCallVersion(),
                isPeriodicUpdateEnabled(),
                getUpdatePeriodSetting(),
                getOnboardingVersion(),
                isOneMinuteUpdateAllowed()
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
        ONE_CALL_VERSION("conf_one_call_version"),
        UPDATE_PERIODIC("conf_update_periodic"),
        UPDATE_PERIOD("conf_update_period"),
        ONBOARDING_VERSION("onboarding_version"),
        DEBUG_ONE_MINUTE_UPDATE("debug_one_minute_update")
    }

    /**
     * Reads an enum setting by its stored [StoredSetting.wireValue].
     *
     * Every enum setting goes through this and [putEnum] so the read and write spellings cannot
     * drift apart, which is the bug this replaced.
     */
    private inline fun <reified T> getEnum(key: PreferenceKey, default: T): T
            where T : Enum<T>, T : StoredSetting =
        storedSettingOf(securedPreferenceDataStore.getString(key.key, null), default)

    private fun <T> putEnum(key: PreferenceKey, setting: T) where T : Enum<T>, T : StoredSetting =
        securedPreferenceDataStore.putString(key.key, setting.wireValue)

    fun getTemperatureSetting(): TemperatureSettings =
        getEnum(PreferenceKey.TEMPERATURE_UNIT, TemperatureSettings.CELSIUS)

    fun setTemperatureSetting(setting: TemperatureSettings) {
        putEnum(PreferenceKey.TEMPERATURE_UNIT, setting)
        _settingsFlow.value = _settingsFlow.value.copy(temperatureUnit = setting)
    }

    fun getMeasureSetting(): MeasureSettings =
        getEnum(PreferenceKey.MEASURE_UNIT, MeasureSettings.METRIC)

    fun setMeasureSetting(setting: MeasureSettings) {
        putEnum(PreferenceKey.MEASURE_UNIT, setting)
        _settingsFlow.value = _settingsFlow.value.copy(measureUnit = setting)
    }

    fun getPressureSetting(): PressureSettings =
        getEnum(PreferenceKey.PRESSURE_UNIT, PressureSettings.HECTOPASCAL)

    fun setPressureSetting(setting: PressureSettings) {
        putEnum(PreferenceKey.PRESSURE_UNIT, setting)
        _settingsFlow.value = _settingsFlow.value.copy(pressureUnit = setting)
    }

    fun getWindDirectionSetting(): WindDirectionSettings =
        getEnum(PreferenceKey.DIRECTION_UNIT, WindDirectionSettings.CARDINAL_POINTS)

    fun setWindDirectionSetting(setting: WindDirectionSettings) {
        putEnum(PreferenceKey.DIRECTION_UNIT, setting)
        _settingsFlow.value = _settingsFlow.value.copy(windDirectionUnit = setting)
    }

    fun getTimeSetting(): TimeSettings =
        getEnum(PreferenceKey.TIME_FORMAT, TimeSettings.TWENTY_FOUR_HOURS)

    fun setTimeSetting(setting: TimeSettings) {
        putEnum(PreferenceKey.TIME_FORMAT, setting)
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

    fun getOneCallVersion(): OneCallVersion =
        getEnum(PreferenceKey.ONE_CALL_VERSION, OneCallVersion.V3_0)

    fun setOneCallVersion(version: OneCallVersion) {
        putEnum(PreferenceKey.ONE_CALL_VERSION, version)
        _settingsFlow.value = _settingsFlow.value.copy(oneCallVersion = version)
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

    fun isOneMinuteUpdateAllowed(): Boolean =
        securedPreferenceDataStore.getBoolean(PreferenceKey.DEBUG_ONE_MINUTE_UPDATE.key, false)

    /**
     * Offers or withdraws the 1-minute interval. Withdrawn while in use, the interval falls back
     * to the shortest one left, rather than staying on an option the settings no longer show.
     */
    fun setOneMinuteUpdateAllowed(allowed: Boolean) {
        securedPreferenceDataStore.putBoolean(PreferenceKey.DEBUG_ONE_MINUTE_UPDATE.key, allowed)
        _settingsFlow.value = _settingsFlow.value.copy(oneMinuteUpdateAllowed = allowed)
        if (!allowed && getUpdatePeriodSetting() == UpdatePeriodSettings.ONE_MINUTE) {
            setUpdatePeriodSetting(UpdatePeriodSettings.FIVE_MINUTES)
        }
    }

    fun getOnboardingVersion(): String? =
        securedPreferenceDataStore.getString(PreferenceKey.ONBOARDING_VERSION.key, null)

    /** Marks the onboarding as completed with [version], the app's own version name. */
    fun setOnboardingVersion(version: String) {
        securedPreferenceDataStore.putString(PreferenceKey.ONBOARDING_VERSION.key, version)
        _settingsFlow.value = _settingsFlow.value.copy(onboardingVersion = version)
    }
}
