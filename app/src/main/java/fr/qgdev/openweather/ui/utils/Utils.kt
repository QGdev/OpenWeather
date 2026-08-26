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

package fr.qgdev.openweather.ui.utils

import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.CurrentWeather
import java.util.Locale


/**
 * True when the observation was taken between sunrise and sunset.
 *
 * The card used to test `sunset < dt && sunrise > dt`, which asks for an instant that is both after
 * today's sunset and before today's sunrise - never true - so the collapsed card always drew the
 * night icon, a moon at midday included.
 */
fun CurrentWeather.isDaytime(): Boolean = dt in sunrise..sunset

/**
 * Turns OpenWeatherMap's probability of precipitation into a percentage.
 *
 * `pop` is a fraction between 0 and 1, but it was handed straight to the integer percentage
 * formatter, which truncates: every hour and every day read "0 %", and only a certain forecast
 * would have read "1 %".
 */
fun Float.toPercentage(): Float = this * 100f

/**
 * The country's name in the user's language, from an ISO 3166-1 alpha-2 code.
 *
 * Nominatim returns the code in lower case ("fr"), which is what the app displayed. When the code is
 * not one the platform knows, its upper-case form is the best remaining answer - the code is checked
 * against the ISO list first, because the JDK answers an unknown region with a translated
 * "Unknown Region" rather than with the code it was given.
 */
fun countryNameFromCode(countryCode: String): String {
    if (countryCode.isBlank()) return ""
    val upperCased = countryCode.uppercase(Locale.ROOT)
    if (upperCased !in Locale.getISOCountries()) return upperCased
    val name = Locale.Builder().setRegion(upperCased).build().getDisplayCountry(Locale.getDefault())
    return name.ifBlank { upperCased }
}

/**
 * Get the drawable resource id from the weather code
 * @param weatherCode the weather code
 * @param isDaytime true if it's daytime
 * @return the drawable resource id
 */
fun getDrawableResIdFromWeatherCode(weatherCode: Int, isDaytime: Boolean): Int {
    return when (weatherCode) {
        //  Dry thunderstorm
        210, 211, 212, 221 -> R.drawable.thunderstorm_flat

        //  Drizzle, Rain with sunny spells
        300, 310, 500, 501, 520 -> if (isDaytime) {
            R.drawable.rain_and_sun_flat
        } else {
            R.drawable.rainy_night_flat
        }

        //  Rain with sunny spells
        301, 302, 311, 313, 321, 511, 521, 531 -> R.drawable.rain_flat

        //  Heavy rain
        312, 314, 502, 503, 504, 522 -> R.drawable.heavy_rain_flat

        //  Snow with sunny spells
        600, 601, 620, 621 -> if (isDaytime) {
            R.drawable.snow_flat
        } else {
            R.drawable.snow_and_night_flat
        }

        //  Snow
        602, 622 -> R.drawable.snow_flat

        //  Sleet
        611, 612, 613, 615, 616 -> R.drawable.sleet_flat

        //  Mist, Smoke, Haze, Dust, Fog
        701, 711, 721, 731, 741, 751, 761, 762, 771, 781 -> if (isDaytime) {
            R.drawable.fog_flat
        } else {
            R.drawable.fog_and_night_flat
        }

        //  Clear sky
        800 ->
            if (isDaytime) {
                R.drawable.sun_flat
            } else {
                R.drawable.moon_phase_flat
            }

        //  Few clouds, scattered clouds, broken clouds
        801, 802, 803 -> if (isDaytime) {
            R.drawable.clouds_and_sun_flat
        } else {
            R.drawable.cloudy_night_flat
        }

        //  Overcast clouds but no precipitation
        804 -> R.drawable.cloudy_flat

        //  Wet thunderstorm
        200, 201, 202, 230, 231, 232 -> R.drawable.storm_flat

        //  Missing icon
        else -> R.drawable.danger
    }
}