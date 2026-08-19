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