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

package fr.qgdev.openweather.data.remote.mappers

import fr.qgdev.openweather.data.models.DailyForecast
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.data.models.MinutelyForecast
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.Properties
import fr.qgdev.openweather.data.models.WeatherAlert
import org.json.JSONException
import org.json.JSONObject

/**
 * PlaceDataMapper
 * <p>
 * Maps a whole OneCall response of an OpenWeatherMap response to a Place protobuf message.
 * Sections missing from a shortened response are left empty instead of failing the whole mapping,
 * the OneCall plan not always returning the minutely, hourly, daily and alerts sections.
 * The companion object is the only instance, the class being open only in order to let it extend
 * Mapper.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Mapper
 */
open class PlaceDataMapper private constructor() : Mapper<Place> {
    companion object : PlaceDataMapper() {
        fun fromOWMToProtoBuilder(jsonObject: JSONObject): Place.Builder? {

            val tmpLastUpdateAttemptTime = System.currentTimeMillis()
            var tmpLastAvailableDataTime: Long

            // Parse Current Weather
            val crtWeatherJSON = jsonObject.optJSONObject("current")
                ?: throw JSONException("Cannot find current weather data in PlaceObjectJSON")
            tmpLastAvailableDataTime = crtWeatherJSON.getLong("dt")
            val tmpCurrentWeather = CurrentWeatherMapper.fromOWMToProto(crtWeatherJSON)

            //  All four forecast arrays are optional here. One Call omits minutely for locations it
            //  does not cover and omits alerts when none are active; hourly and daily are normally
            //  always present, but a truncated response should degrade to an empty list rather than
            //  throw. A missing "current" above is different - that response is unusable.
            //  Note optJSONArray returns null rather than throwing, so it must be null-checked:
            //  has(name) is not enough, since the value could be present but not an array.

            // Parse Minutely Weather Forecast
            val tmpMinutelyWeatherForecasts = mutableListOf<MinutelyForecast>()
            val minutelyWeatherJSON = jsonObject.optJSONArray("minutely")
            if (minutelyWeatherJSON != null) {
                for (i in 0 until minutelyWeatherJSON.length()) {
                    val forecast = MinutelyForecastMapper.fromOWMToProto(minutelyWeatherJSON.getJSONObject(i))
                    tmpMinutelyWeatherForecasts.add(forecast)
                }
            }

            // Parse Hourly Weather Forecast
            val tmpHourlyWeatherForecasts = mutableListOf<HourlyForecast>()
            val hourlyWeatherJSON = jsonObject.optJSONArray("hourly")
            if (hourlyWeatherJSON != null) {
                for (i in 0 until hourlyWeatherJSON.length()) {
                    val forecast = HourlyForecastMapper.fromOWMToProto(hourlyWeatherJSON.getJSONObject(i))
                    tmpHourlyWeatherForecasts.add(forecast)
                }
            }

            // Parse Daily Weather Forecast
            val tmpDailyWeatherForecasts = mutableListOf<DailyForecast>()
            val dailyWeatherJSON = jsonObject.optJSONArray("daily")
            if (dailyWeatherJSON != null) {
                for (i in 0 until dailyWeatherJSON.length()) {
                    val forecast = DailyForecastMapper.fromOWMToProto(dailyWeatherJSON.getJSONObject(i))
                    tmpDailyWeatherForecasts.add(forecast)
                }
            }

            // Parse Weather Alerts
            val tmpWeatherAlerts = mutableListOf<WeatherAlert>()
            val weatherAlertJSON = jsonObject.optJSONArray("alerts")
            if (weatherAlertJSON != null) {
                for (i in 0 until weatherAlertJSON.length()) {
                    val alert = WeatherAlertMapper.fromOWMToProto(weatherAlertJSON.getJSONObject(i))
                    tmpWeatherAlerts.add(alert)
                }
            }

            //  One Call reports the place's UTC offset in seconds; timeOffset is consumed by
            //  SimpleTimeZone, which takes milliseconds. Converting here keeps every read site
            //  correct. Until this was populated, every place time rendered as UTC.
            val tmpTimeOffsetMillis = jsonObject.optInt("timezone_offset", 0) * 1000

            val properties = Properties.newBuilder()
                .setLastWeatherUpdateAttemptTime(tmpLastUpdateAttemptTime)
                .setLastAvailableWeatherDataTime(tmpLastAvailableDataTime)
                .setTimeOffset(tmpTimeOffsetMillis)
                .build()

            return Place.newBuilder()
                .setCurrentWeather(tmpCurrentWeather)
                .addAllMinutelyForecastList(tmpMinutelyWeatherForecasts)
                .addAllHourlyForecastList(tmpHourlyWeatherForecasts)
                .addAllDailyForecastList(tmpDailyWeatherForecasts)
                .addAllWeatherAlertsList(tmpWeatherAlerts)
                .setProperties(properties)
        }


        override fun fromOWMToProto(jsonObject: JSONObject): Place {

            return fromOWMToProtoBuilder(jsonObject)?.buildPartial()
                ?: throw JSONException("Cannot find current weather data in PlaceObjectJSON")
        }
    }

    override fun fromOWMToProto(jsonObject: JSONObject): Place {
        return PlaceDataMapper.fromOWMToProto(jsonObject);
    }
}