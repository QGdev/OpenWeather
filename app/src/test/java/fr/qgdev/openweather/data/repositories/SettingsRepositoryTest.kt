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

package fr.qgdev.openweather.data.repositories

import androidx.preference.PreferenceDataStore
import fr.qgdev.openweather.data.settings.MeasureSettings
import fr.qgdev.openweather.data.settings.OneCallVersion
import fr.qgdev.openweather.data.settings.PressureSettings
import fr.qgdev.openweather.data.settings.Settings
import fr.qgdev.openweather.data.settings.TemperatureSettings
import fr.qgdev.openweather.data.settings.TimeSettings
import fr.qgdev.openweather.data.settings.UpdatePeriodSettings
import fr.qgdev.openweather.data.settings.WindDirectionSettings
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Round-trip tests for [SettingsRepository].
 *
 * The bug these cover was silent: `setTimeSetting` wrote "TWELVE" while `getTimeSetting` matched
 * "12", and `setPressureSetting` wrote "pounds_square_inch" while the getter matched "psi". The UI
 * updated from the in-memory StateFlow, so the setting looked applied until the next launch.
 */
class SettingsRepositoryTest {

    /** In-memory stand-in for SecuredPreferenceDataStore - no Context, no keystore. */
    private class FakePreferenceDataStore : PreferenceDataStore() {
        val values = mutableMapOf<String, Any?>()
        override fun putString(key: String, value: String?) { values[key] = value }
        override fun getString(key: String, defValue: String?) = values[key] as? String ?: defValue
        override fun putBoolean(key: String, value: Boolean) { values[key] = value }
        override fun getBoolean(key: String, defValue: Boolean) = values[key] as? Boolean ?: defValue
    }

    private fun repository(store: PreferenceDataStore = FakePreferenceDataStore()) =
        SettingsRepository.createForTest(store)

    // region Round-trips - every constant of every enum setting

    @Test
    fun `every temperature unit survives a write and read`() {
        TemperatureSettings.entries.forEach { unit ->
            val store = FakePreferenceDataStore()
            repository(store).setTemperatureSetting(unit)
            assertEquals(unit, repository(store).getTemperatureSetting())
        }
    }

    @Test
    fun `every measure unit survives a write and read`() {
        MeasureSettings.entries.forEach { unit ->
            val store = FakePreferenceDataStore()
            repository(store).setMeasureSetting(unit)
            assertEquals(unit, repository(store).getMeasureSetting())
        }
    }

    @Test
    fun `every pressure unit survives a write and read`() {
        //  Three of these four used to fall back to HECTOPASCAL.
        PressureSettings.entries.forEach { unit ->
            val store = FakePreferenceDataStore()
            repository(store).setPressureSetting(unit)
            assertEquals(unit, repository(store).getPressureSetting())
        }
    }

    @Test
    fun `every wind direction unit survives a write and read`() {
        WindDirectionSettings.entries.forEach { unit ->
            val store = FakePreferenceDataStore()
            repository(store).setWindDirectionSetting(unit)
            assertEquals(unit, repository(store).getWindDirectionSetting())
        }
    }

    @Test
    fun `every time format survives a write and read`() {
        //  TWELVE_HOURS used to fall back to TWENTY_FOUR_HOURS.
        TimeSettings.entries.forEach { format ->
            val store = FakePreferenceDataStore()
            repository(store).setTimeSetting(format)
            assertEquals(format, repository(store).getTimeSetting())
        }
    }

    @Test
    fun `every update period survives a write and read`() {
        UpdatePeriodSettings.entries.forEach { period ->
            val store = FakePreferenceDataStore()
            repository(store).setUpdatePeriodSetting(period)
            assertEquals(period, repository(store).getUpdatePeriodSetting())
        }
    }

    @Test
    fun `every One Call version survives a write and read`() {
        OneCallVersion.entries.forEach { version ->
            val store = FakePreferenceDataStore()
            repository(store).setOneCallVersion(version)
            assertEquals(version, repository(store).getOneCallVersion())
        }
    }

    // endregion

    // region One Call version on first launch

    @Test
    fun `a fresh install starts on One Call 3_0`() {
        assertEquals(OneCallVersion.V3_0, repository().getOneCallVersion())
    }

    @Test
    fun `an install upgraded with a key stays on One Call 2_5`() {
        val store = FakePreferenceDataStore()
        store.putString("conf_api_key", "0123456789abcdef0123456789abcdef")
        assertEquals(OneCallVersion.V2_5, repository(store).getOneCallVersion())
    }

    @Test
    fun `a key saved after the first launch does not move the version`() {
        //  The fresh install settles on 3.0 before the onboarding saves a key; the key must not
        //  then be mistaken for one from an older install.
        val store = FakePreferenceDataStore()
        repository(store).setApiKey("0123456789abcdef0123456789abcdef")
        assertEquals(OneCallVersion.V3_0, repository(store).getOneCallVersion())
    }

    @Test
    fun `the One Call wire values are the URL version segments`() {
        assertEquals("3.0", OneCallVersion.V3_0.wireValue)
        assertEquals("2.5", OneCallVersion.V2_5.wireValue)
    }

    // endregion

    /**
     * Pins the stored spellings against the `entryValues` shipped in `res/values/array.xml` by
     * 0.9.4. These are a compatibility contract, not an implementation detail: changing one - for
     * instance "tidying" them into `enum.name` - silently resets the units of every installed app
     * on upgrade. If this test and array.xml disagree, array.xml is right.
     */
    @Test
    fun `wire values match the format shipped in 0_9_4`() {
        assertEquals("celsius", TemperatureSettings.CELSIUS.wireValue)
        assertEquals("fahrenheit", TemperatureSettings.FAHRENHEIT.wireValue)
        assertEquals("kelvin", TemperatureSettings.KELVIN.wireValue)

        assertEquals("metric", MeasureSettings.METRIC.wireValue)
        assertEquals("imperial", MeasureSettings.IMPERIAL.wireValue)

        assertEquals("hpa", PressureSettings.HECTOPASCAL.wireValue)
        assertEquals("mbar", PressureSettings.BAROMETRIC.wireValue)
        assertEquals("psi", PressureSettings.POUNDS_SQUARE_INCH.wireValue)
        assertEquals("inhg", PressureSettings.INCH_MERCURY.wireValue)

        assertEquals("cardinal", WindDirectionSettings.CARDINAL_POINTS.wireValue)
        assertEquals("angular", WindDirectionSettings.ANGULAR.wireValue)

        assertEquals("12", TimeSettings.TWELVE_HOURS.wireValue)
        assertEquals("24", TimeSettings.TWENTY_FOUR_HOURS.wireValue)
    }

    /** A preferences file written by 0.9.4 has to keep reading correctly after the upgrade. */
    @Test
    fun `settings stored by 0_9_4 are still readable`() {
        val store = FakePreferenceDataStore().apply {
            values["conf_temperature_unit"] = "fahrenheit"
            values["conf_measure_unit"] = "imperial"
            values["conf_pressure_unit"] = "inhg"
            values["conf_direction_unit"] = "angular"
            values["conf_time_format"] = "12"
        }
        val repository = repository(store)

        assertEquals(TemperatureSettings.FAHRENHEIT, repository.getTemperatureSetting())
        assertEquals(MeasureSettings.IMPERIAL, repository.getMeasureSetting())
        assertEquals(PressureSettings.INCH_MERCURY, repository.getPressureSetting())
        assertEquals(WindDirectionSettings.ANGULAR, repository.getWindDirectionSetting())
        assertEquals(TimeSettings.TWELVE_HOURS, repository.getTimeSetting())
    }

    @Test
    fun `an unset or unrecognised preference falls back to the default`() {
        val store = FakePreferenceDataStore().apply {
            //  e.g. a value written by the broken setters this change replaces
            values["conf_pressure_unit"] = "pounds_square_inch"
        }
        val repository = repository(store)

        assertEquals(PressureSettings.HECTOPASCAL, repository.getPressureSetting())
        assertEquals(TemperatureSettings.CELSIUS, repository.getTemperatureSetting())
        assertEquals(TimeSettings.TWENTY_FOUR_HOURS, repository.getTimeSetting())
    }

    @Test
    fun `the settings flow reflects a change immediately`() {
        val repository = repository()

        repository.setPressureSetting(PressureSettings.POUNDS_SQUARE_INCH)
        assertEquals(PressureSettings.POUNDS_SQUARE_INCH, repository.settingsFlow.value.pressureUnit)

        repository.setTimeSetting(TimeSettings.TWELVE_HOURS)
        assertEquals(TimeSettings.TWELVE_HOURS, repository.settingsFlow.value.timeFormat)
    }
}
