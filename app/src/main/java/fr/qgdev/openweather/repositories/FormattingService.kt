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

package fr.qgdev.openweather.repositories

import android.content.Context
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.settings.MeasureSettings
import fr.qgdev.openweather.data.settings.PressureSettings
import fr.qgdev.openweather.data.settings.Settings
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.data.settings.TemperatureSettings
import fr.qgdev.openweather.data.settings.TimeSettings
import fr.qgdev.openweather.data.settings.WindDirectionSettings
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference

/**
 * FormattingService
 *
 * A service to format any data according to the user settings.
 * It also provides some conversion methods to convert data.
 *
 * @author Quentin GOMES DOS REIS
 * @version 2
 */
class FormattingService private constructor(
    private val context: Context,
    private val settingsRepository: SettingsRepository?
) {

    private var temperatureConversion: (Float) -> Float = { it }
    private var measureDistanceConversion: (Float) -> Float = { it }
    private var measureShortDistanceConversion: (Float) -> Float = { it }
    private var measureSpeedConversion: (Float) -> Float = { it }
    private var pressureConversion: (Float) -> Float = { it }
    private var directionConversion: (Int) -> String = { "N/A" }

    // Temperature format specifier for int or float format
    private var temperatureUnitSymbol: String = ""
    private var temperatureFormatSpecifierInt: String = ""
    private var temperatureFormatSpecifierFloat: String = ""

    // Short distance format specifier for int or float format
    private var shortDistanceUnitSymbol: String = ""
    private var shortDistanceFormatSpecifierInt: String = ""
    private var shortDistanceFormatSpecifierFloat: String = ""

    // Distance format specifier for int or float format
    private var distanceUnitSymbol: String = ""
    private var distanceFormatSpecifierInt: String = ""
    private var distanceFormatSpecifierFloat: String = ""

    // Speed format specifier for int or float format
    private var speedUnitSymbol: String = ""
    private var speedFormatSpecifierInt: String = ""
    private var speedFormatSpecifierFloat: String = ""

    // Pressure format specifier
    private var pressureUnitSymbol: String = ""
    private var pressureFormatSpecifier: String = ""

    // Humidity or percentage values
    private var percentageSymbol: String = ""
    private var percentageFormatSpecifierInt: String = ""
    private var percentageFormatSpecifierFloat: String = ""

    // TimeHour format specifier
    private lateinit var hourFormat: SimpleDateFormat
    private lateinit var shortHourFormat: SimpleDateFormat
    private lateinit var timeFormat: SimpleDateFormat
    private lateinit var shortDayNameFormat: SimpleDateFormat
    private lateinit var dayMonthFormat: SimpleDateFormat
    private lateinit var fullTimeHourFormat: SimpleDateFormat

    init {
        if (settingsRepository != null) {
            update()
        }
    }

    /**
     * Secondary constructor for "dumb" instances (testing / Compose preview).
     * No SettingsRepository, uses default settings.
     */
    private constructor(context: Context) : this(context, null as SettingsRepository?) {
        temperatureUnitInit(TemperatureSettings.CELSIUS)
        measureUnitInit(MeasureSettings.METRIC)
        pressureUnitInit(PressureSettings.HECTOPASCAL)
        percentageUnitInit()
        directionUnitInit(WindDirectionSettings.CARDINAL_POINTS)
        timeDateFormatInit(TimeSettings.TWENTY_FOUR_HOURS)
    }

    companion object {
        private val instance = AtomicReference<FormattingService>(null)

        @JvmStatic
        fun getInstance(context: Context): FormattingService {
            val applicationContext = context.applicationContext
            if (instance.get() == null) {
                synchronized(FormattingService::class.java) {
                    instance.compareAndSet(
                        null,
                        FormattingService(
                            applicationContext,
                            SettingsRepository.getInstance(applicationContext)
                        )
                    )
                }
            }
            return instance.get()
        }

        /**
         * Returns a new instance of the class without any settings repository.
         * Useful for testing purposes and compose preview.
         *
         * **Do not use in production code** and do not call [update] on the returned instance
         * as it will throw a NullPointerException.
         */
        @JvmStatic
        fun getDumbInstance(context: Context): FormattingService {
            return FormattingService(context)
        }
    }

    // region Conversion utilities

    /**
     * Conversion
     *
     * Static conversion utility classes for temperature, measure, pressure, and direction.
     */
    object Conversion {

        /**
         * Temperature conversion methods.
         * All methods take a temperature in Kelvin and convert it.
         */
        object Temperature {
            @JvmStatic
            fun toCelsius(temperature: Float): Float = temperature - 273.15f

            @JvmStatic
            fun toFahrenheit(temperature: Float): Float = toCelsius(temperature) * (9f / 5f) + 32f

            @JvmStatic
            fun toKelvin(temperature: Float): Float = temperature
        }

        /**
         * Measure conversion methods.
         */
        object Measure {
            @JvmStatic
            fun distanceToMiles(distance: Float): Float = distance * 0.000621371f

            @JvmStatic
            fun distanceToInches(distance: Float): Float = distance * 0.0393701f

            @JvmStatic
            fun distanceToMetric(distance: Float): Float = distance / 1000f

            @JvmStatic
            fun speedToImperial(speed: Float): Float = speed * 2.23694f

            @JvmStatic
            fun speedToMetric(speed: Float): Float = speed * 3.6f
        }

        /**
         * Pressure conversion methods.
         */
        object Pressure {
            @JvmStatic
            fun toHpa(pressure: Float): Float = pressure

            @JvmStatic
            fun toMbar(pressure: Float): Float = pressure

            @JvmStatic
            fun toPsi(pressure: Float): Float = pressure * 0.0145038f

            @JvmStatic
            fun toInhg(pressure: Float): Float = pressure * 0.02953f
        }

        /**
         * Direction conversion methods.
         */
        object Direction {
            @JvmStatic
            fun toDegrees(direction: Int): String =
                String.format(Locale.getDefault(), "%d°", direction)

            @JvmStatic
            private fun toCardinalResourceId(direction: Int): Int {
                val directions = intArrayOf(
                    R.string.wind_direction_north,
                    R.string.wind_direction_northnortheast,
                    R.string.wind_direction_northeast,
                    R.string.wind_direction_eastnortheast,
                    R.string.wind_direction_east,
                    R.string.wind_direction_eastsoutheast,
                    R.string.wind_direction_southeast,
                    R.string.wind_direction_southsoutheast,
                    R.string.wind_direction_south,
                    R.string.wind_direction_southsouthwest,
                    R.string.wind_direction_southwest,
                    R.string.wind_direction_westsouthwest,
                    R.string.wind_direction_west,
                    R.string.wind_direction_westnorthwest,
                    R.string.wind_direction_northwest,
                    R.string.wind_direction_northnorthwest
                )

                val index = ((direction + 11.25) / 22.5).toInt() % directions.size
                if (index < 0 || index >= directions.size) return -1
                return directions[index]
            }

            @JvmStatic
            fun toCardinal(context: Context, direction: Int): String {
                val resourceId = toCardinalResourceId(direction)
                if (resourceId == -1) return "N/A"
                return context.resources.getString(resourceId)
            }
        }
    }

    // endregion

    // region FormattingSpec enum

    enum class FormattingSpec(@JvmField val value: Int) {
        NO_UNIT_NO_SPACE(0),
        NO_UNIT_BUT_SPACE(1),    // Actually only useful for temperature formatting
        UNIT_BUT_NO_SPACE(2),
        UNIT_AND_SPACE(3);

        fun isSpaceNeeded(): Boolean = value % 2 == 1
        fun isUnitNeeded(): Boolean = value >= 2
    }

    // endregion

    // region Update methods

    fun update() {
        settingsRepository!!.let {
            temperatureUnitInit(it.getTemperatureSetting())
            measureUnitInit(it.getMeasureSetting())
            pressureUnitInit(it.getPressureSetting())
            percentageUnitInit()
            directionUnitInit(it.getWindDirectionSetting())
            timeDateFormatInit(it.getTimeSetting())
        }
    }

    fun update(settings: Settings) {
        temperatureUnitInit(settings.temperatureUnit)
        measureUnitInit(settings.measureUnit)
        pressureUnitInit(settings.pressureUnit)
        percentageUnitInit()
        directionUnitInit(settings.windDirectionUnit)
        timeDateFormatInit(settings.timeFormat)
    }

    /**
     * Returns a **new** instance configured for [settings], leaving this one untouched.
     *
     * This exists because [update] mutates in place, which Compose cannot observe. The compiler
     * infers this class as unstable - it has 29 mutable fields - and under strong skipping an
     * unstable parameter is compared by instance identity. A singleton mutated in place therefore
     * always compares equal, so `PlaceCardView` is skipped and keeps rendering the old units until
     * something else forces it to recompose.
     *
     * Handing the UI a different instance per settings change makes that comparison fail, which is
     * what makes a unit change redraw immediately.
     *
     * The mutating [update] methods stay for the View-based widgets, which read the singleton
     * through `AppRepository.getFormattingService()` and are frozen.
     */
    fun snapshotFor(settings: Settings): FormattingService =
        FormattingService(context, null).also { it.update(settings) }

    // endregion

    // region Public conversion methods

    fun convertTemperature(temperature: Float): Float = temperatureConversion(temperature)

    fun convertDistance(distance: Float): Float = measureDistanceConversion(distance)

    fun convertShortDistance(distance: Float): Float = measureShortDistanceConversion(distance)

    fun convertSpeed(speed: Float): Float = measureSpeedConversion(speed)

    fun convertPressure(pressure: Float): Float = pressureConversion(pressure)

    fun convertDirection(direction: Int, isReadable: Boolean): String {
        return if (isReadable && direction in 0..360) {
            directionConversion(direction)
        } else {
            "N/A"
        }
    }

    // endregion

    // region Private helper

    private fun getSimpleDateFormatForTimeZone(
        simpleDateFormat: SimpleDateFormat,
        timeZone: TimeZone
    ): SimpleDateFormat {
        val cloned = simpleDateFormat.clone() as SimpleDateFormat
        cloned.timeZone = timeZone
        return cloned
    }

    // endregion

    // region Temperature formatting

    fun getIntFormattedTemperature(temperature: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            temperatureFormatSpecifierInt,
            BigDecimal.valueOf(convertTemperature(temperature).toDouble()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) temperatureUnitSymbol else ""
        )
    }

    fun getFloatFormattedTemperature(temperature: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            temperatureFormatSpecifierFloat,
            convertTemperature(temperature),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) temperatureUnitSymbol else ""
        )
    }

    // endregion

    // region Short distance formatting

    fun getIntFormattedShortDistance(shortDistance: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            shortDistanceFormatSpecifierInt,
            BigDecimal.valueOf(convertShortDistance(shortDistance).toDouble()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) shortDistanceUnitSymbol else ""
        )
    }

    fun getFloatFormattedShortDistance(shortDistance: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            shortDistanceFormatSpecifierFloat,
            convertShortDistance(shortDistance),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) shortDistanceUnitSymbol else ""
        )
    }

    // endregion

    // region Distance formatting

    fun getIntFormattedDistance(distance: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            distanceFormatSpecifierInt,
            BigDecimal.valueOf(convertDistance(distance).toDouble()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) distanceUnitSymbol else ""
        )
    }

    fun getFloatFormattedDistance(distance: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            distanceFormatSpecifierFloat,
            convertDistance(distance),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) distanceUnitSymbol else ""
        )
    }

    // endregion

    // region Speed formatting

    fun getIntFormattedSpeed(speed: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            speedFormatSpecifierInt,
            BigDecimal.valueOf(convertSpeed(speed).toDouble()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) speedUnitSymbol else ""
        )
    }

    fun getFloatFormattedSpeed(speed: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            speedFormatSpecifierFloat,
            convertSpeed(speed),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) speedUnitSymbol else ""
        )
    }

    // endregion

    // region Pressure formatting

    fun getFormattedPressure(pressure: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            pressureFormatSpecifier,
            convertPressure(pressure),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) pressureUnitSymbol else ""
        )
    }

    // endregion

    // region Percentage formatting

    fun getIntFormattedPercentage(percentage: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            percentageFormatSpecifierInt,
            BigDecimal.valueOf(percentage.toDouble()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) percentageSymbol else ""
        )
    }

    fun getIntFormattedPercentage(percentage: Int, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            percentageFormatSpecifierInt,
            BigDecimal.valueOf(percentage.toLong()).toInt(),
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) percentageSymbol else ""
        )
    }

    fun getFloatFormattedPercentage(percentage: Float, formattingSpec: FormattingSpec): String {
        return String.format(
            defaultLocale,
            percentageFormatSpecifierFloat,
            percentage,
            if (formattingSpec.isSpaceNeeded()) " " else "",
            if (formattingSpec.isUnitNeeded()) percentageSymbol else ""
        )
    }

    // endregion

    // region Direction formatting

    fun getFormattedDirection(direction: Int, isReadable: Boolean): String {
        return convertDirection(direction, isReadable)
    }

    fun getFormattedDirectionInCardinalPoints(direction: Int): String {
        return if (direction in 0..360) {
            Conversion.Direction.toCardinal(context, direction)
        } else {
            "N/A"
        }
    }

    fun getFormattedDirectionInDegrees(direction: Int): String {
        return if (direction in 0..360) {
            Conversion.Direction.toDegrees(direction)
        } else {
            "N/A"
        }
    }

    // endregion

    // region Time/Date formatting

    fun getFormattedHour(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(hourFormat, timeZone).format(date)
    }

    fun getFormattedShortHour(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(shortHourFormat, timeZone).format(date)
    }

    fun getFormattedTime(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(timeFormat, timeZone).format(date)
    }

    fun getFormattedShortDayName(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(shortDayNameFormat, timeZone).format(date)
    }

    fun getFormattedDayMonth(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(dayMonthFormat, timeZone).format(date)
    }

    fun getFormattedFullTimeHour(date: Date, timeZone: TimeZone): String {
        return getSimpleDateFormatForTimeZone(fullTimeHourFormat, timeZone).format(date)
    }

    // endregion

    // region Private initialization methods

    private fun temperatureUnitInit(settings: TemperatureSettings) {
        temperatureFormatSpecifierInt = "%d%s°%s"
        temperatureFormatSpecifierFloat = "%.1f%s°%s"

        when (settings) {
            TemperatureSettings.FAHRENHEIT -> {
                temperatureUnitSymbol = "F"
                temperatureConversion = Conversion.Temperature::toFahrenheit
            }

            TemperatureSettings.KELVIN -> {
                temperatureUnitSymbol = "K"
                temperatureConversion = Conversion.Temperature::toKelvin
            }

            TemperatureSettings.CELSIUS -> {
                temperatureUnitSymbol = "C"
                temperatureConversion = Conversion.Temperature::toCelsius
            }
        }
    }

    private fun measureUnitInit(setting: MeasureSettings) {
        shortDistanceFormatSpecifierInt = "%d%s%s"
        shortDistanceFormatSpecifierFloat = "%.2f%s%s"
        distanceFormatSpecifierInt = "%d%s%s"
        distanceFormatSpecifierFloat = "%.1f%s%s"
        speedFormatSpecifierInt = "%d%s%s"
        speedFormatSpecifierFloat = "%.1f%s%s"

        when (setting) {
            MeasureSettings.IMPERIAL -> {
                shortDistanceUnitSymbol = "in"
                distanceUnitSymbol = "mi"
                speedUnitSymbol = "mph"

                measureDistanceConversion = Conversion.Measure::distanceToMiles
                measureShortDistanceConversion = Conversion.Measure::distanceToInches
                measureSpeedConversion = Conversion.Measure::speedToImperial
            }

            MeasureSettings.METRIC -> {
                shortDistanceUnitSymbol = "mm"
                distanceUnitSymbol = "km"
                speedUnitSymbol = "km/h"

                measureDistanceConversion = Conversion.Measure::distanceToMetric
                measureShortDistanceConversion = { it }
                measureSpeedConversion = Conversion.Measure::speedToMetric
            }
        }
    }

    private fun pressureUnitInit(setting: PressureSettings) {
        when (setting) {
            PressureSettings.BAROMETRIC -> {
                pressureFormatSpecifier = "%.0f%s%s"
                pressureUnitSymbol = "mBar"
                pressureConversion = Conversion.Pressure::toMbar
            }

            PressureSettings.POUNDS_SQUARE_INCH -> {
                pressureFormatSpecifier = "%.2f%s%s"
                pressureUnitSymbol = "psi"
                pressureConversion = Conversion.Pressure::toPsi
            }

            PressureSettings.INCH_MERCURY -> {
                pressureFormatSpecifier = "%.2f%s%s"
                pressureUnitSymbol = "inHg"
                pressureConversion = Conversion.Pressure::toInhg
            }

            PressureSettings.HECTOPASCAL -> {
                pressureFormatSpecifier = "%.0f%s%s"
                pressureUnitSymbol = "hPa"
                pressureConversion = Conversion.Pressure::toHpa
            }
        }
    }

    private fun percentageUnitInit() {
        percentageSymbol = "%"
        percentageFormatSpecifierInt = "%d%s%s"
        percentageFormatSpecifierFloat = "%.1f%s%s"
    }

    private fun directionUnitInit(settings: WindDirectionSettings) {
        when (settings) {
            WindDirectionSettings.ANGULAR -> {
                directionConversion = Conversion.Direction::toDegrees
            }

            WindDirectionSettings.CARDINAL_POINTS -> {
                directionConversion = { direction ->
                    Conversion.Direction.toCardinal(context, direction)
                }
            }
        }
    }

    private fun timeDateFormatInit(settings: TimeSettings) {
        val locale = defaultLocale

        when (settings) {
            TimeSettings.TWELVE_HOURS -> {
                hourFormat = SimpleDateFormat("hh:00 a", locale)
                shortHourFormat = SimpleDateFormat("ha", locale)
                timeFormat = SimpleDateFormat("hh:mm a", locale)
                fullTimeHourFormat = SimpleDateFormat("dd/MM/yy hh:mm a", locale)
            }

            TimeSettings.TWENTY_FOUR_HOURS -> {
                hourFormat = SimpleDateFormat("HH:00", locale)
                shortHourFormat = SimpleDateFormat("H'h'", locale)
                timeFormat = SimpleDateFormat("HH:mm", locale)
                fullTimeHourFormat = SimpleDateFormat("dd/MM/yy HH:mm", locale)
            }
        }

        shortDayNameFormat = SimpleDateFormat("EE", locale)
        dayMonthFormat = SimpleDateFormat("dd/MM", locale)
    }

    private val defaultLocale: Locale
        get() = settingsRepository?.getDefaultLocale() ?: Locale.getDefault()

    // endregion
}

