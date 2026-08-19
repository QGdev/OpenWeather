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

            // Parse Minutely Weather Forecast
            val tmpMinutelyWeatherForecasts = mutableListOf<MinutelyForecast>()
            if (jsonObject.has("minutely")) {
                val minutelyWeatherJSON = jsonObject.optJSONArray("minutely")
                for (i in 0 until minutelyWeatherJSON.length()) {
                    val forecast = MinutelyForecastMapper.fromOWMToProto(minutelyWeatherJSON.getJSONObject(i))
                    tmpMinutelyWeatherForecasts.add(forecast)
                }
            }

            // Parse Hourly Weather Forecast
            val tmpHourlyWeatherForecasts = mutableListOf<HourlyForecast>()
            val hourlyWeatherJSON = jsonObject.getJSONArray("hourly")
            for (i in 0 until hourlyWeatherJSON.length()) {
                val forecast = HourlyForecastMapper.fromOWMToProto(hourlyWeatherJSON.getJSONObject(i))
                tmpHourlyWeatherForecasts.add(forecast)
            }

            // Parse Daily Weather Forecast
            val tmpDailyWeatherForecasts = mutableListOf<DailyForecast>()
            val dailyWeatherJSON = jsonObject.getJSONArray("daily")
            for (i in 0 until dailyWeatherJSON.length()) {
                val forecast = DailyForecastMapper.fromOWMToProto(dailyWeatherJSON.getJSONObject(i))
                tmpDailyWeatherForecasts.add(forecast)
            }

            // Parse Weather Alerts
            val tmpWeatherAlerts = mutableListOf<WeatherAlert>()
            if (jsonObject.has("alerts")) {
                val weatherAlertJSON = jsonObject.optJSONArray("alerts")
                for (i in 0 until weatherAlertJSON.length()) {
                    val alert = WeatherAlertMapper.fromOWMToProto(weatherAlertJSON.getJSONObject(i))
                    tmpWeatherAlerts.add(alert)
                }
            }

            val properties = Properties.newBuilder()
                .setLastWeatherUpdateAttemptTime(tmpLastUpdateAttemptTime)
                .setLastAvailableWeatherDataTime(tmpLastAvailableDataTime)
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