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

package fr.qgdev.openweather.ui.components.forecasts

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.DailyForecast
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.components.forecasts.components.BarGraphChart
import fr.qgdev.openweather.ui.components.forecasts.components.MoonPhaseIndicator
import fr.qgdev.openweather.ui.components.forecasts.components.SmoothTwoLinesGraphChart
import fr.qgdev.openweather.ui.components.forecasts.components.UvIndicator
import fr.qgdev.openweather.ui.components.forecasts.components.WindDirectionIndicator
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.toPercentage
import java.util.Date
import java.util.TimeZone

/**
 * Data class holding all the data needed to render a single daily forecast item.
 */
data class DailyWeatherForecastItemViewData(
    val dailyWeatherForecast: DailyForecast,
    /** 4 temperature points: morning, day, evening, night */
    val temperatureGraphPoints: FloatArray,
    /** 4 feelsLike points: morning, day, evening, night */
    val feelsLikeTemperatureGraphPoints: FloatArray,
    val temperatureGraphBounds: Pair<Float, Float>,
    val windSpeedGraphPoints: FloatArray,
    val windGustSpeedGraphPoints: FloatArray,
    val windSpeedGraphBounds: Pair<Float, Float>,
    val rainGraphPoints: FloatArray,
    val snowGraphPoints: FloatArray,
    val popGraphPoint: Float,
    val precipitationGraphBounds: Pair<Float, Float>,
    val timeZone: TimeZone,
) {
    companion object {
        /** Preview / test data */
        val Preview = DailyWeatherForecastItemViewData(
            dailyWeatherForecast = DailyForecast.newBuilder()
                .setDt(1620000000)
                .setWeather("Clear")
                .setWeatherDescription("Clear sky")
                .setWeatherCode(800)
                .setTemperatureMorning(285f)
                .setTemperatureDay(295f)
                .setTemperatureEvening(290f)
                .setTemperatureNight(280f)
                .setTemperatureMinimum(278f)
                .setTemperatureMaximum(296f)
                .setTemperatureMorningFeelsLike(283f)
                .setTemperatureDayFeelsLike(293f)
                .setTemperatureEveningFeelsLike(288f)
                .setTemperatureNightFeelsLike(278f)
                .setPressure(1013)
                .setHumidity(65)
                .setDewPoint(280f)
                .setCloudiness(20)
                .setSunriseDt(1619990000)
                .setSunsetDt(1620040000)
                .setUvIndex(6)
                .setMoonriseDt(1620010000)
                .setMoonsetDt(1620060000)
                .setMoonPhase(0.25f)
                .setWindSpeed(5f)
                .setWindGustSpeed(12f)
                .setWindDirection(220)
                .setPop(0.3f)
                .setRain(2f)
                .setSnow(0f)
                .build(),
            temperatureGraphPoints = floatArrayOf(285f, 295f, 290f, 280f),
            feelsLikeTemperatureGraphPoints = floatArrayOf(283f, 293f, 288f, 278f),
            temperatureGraphBounds = 278f to 296f,
            windSpeedGraphPoints = floatArrayOf(4f, 5f, 6f),
            windGustSpeedGraphPoints = floatArrayOf(10f, 12f, 11f),
            windSpeedGraphBounds = 0f to 15f,
            rainGraphPoints = floatArrayOf(0f, 2f, 1f),
            snowGraphPoints = floatArrayOf(0f, 0f, 0f),
            popGraphPoint = 0.3f,
            precipitationGraphBounds = 0f to 5f,
            timeZone = TimeZone.getDefault(),
        )
    }
}

/**
 * Reusable row composable that pairs a vector drawable icon with a text label,
 * mirroring the Java drawTextWithDrawable() helper from DailyForecastGraphView.
 */
@Composable
fun IconWithText(
    @DrawableRes iconResId: Int,
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = typography.titleSmall,
    tint: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            colorFilter = if (tint != Color.Unspecified)
                androidx.compose.ui.graphics.ColorFilter.tint(tint)
            else null
        )
        Text(
            text = text,
            style = style,
            color = if (tint != Color.Unspecified) tint else Color.Unspecified
        )
    }
}

/**
 * Composable for a single daily weather forecast item (one column in the horizontal scroll).
 *
 * Displays: date, weather icon, min/max temperatures, 4-period temperatures with graph,
 * environmental variables (pressure, humidity, dew point, cloudiness, sunrise/sunset, UV),
 * wind (speed, gust, direction with indicator), and precipitations (rain, snow, PoP) with graphs.
 */
@Composable
fun DailyWeatherForecastItemView(
    modifier: Modifier = Modifier,
    formattingService: FormattingService,
    data: DailyWeatherForecastItemViewData
) {
    val forecast = data.dailyWeatherForecast
    val weatherIconId = getDrawableResIdFromWeatherCode(forecast.weatherCode, true)
    val date = Date(forecast.dt)

    // Theme-aware colors matching the original Java ForecastView paints
    val primaryColor = colorResource(R.color.colorPrimaryPaint)
    val secondaryColor = colorResource(R.color.colorSecondaryPaint)    // colorAccent (#F96824)
    val tertiaryColor = colorResource(R.color.colorTertiaryPaint)      // #5DA3EA
    val primaryGraphColor = colorResource(R.color.colorPrimaryGraphPaint).copy(alpha = 155f / 255f)
    val secondaryGraphColor = colorResource(R.color.colorSecondaryGraphPaint).copy(alpha = 155f / 255f)
    val tertiaryGraphColor = colorResource(R.color.colorTertiaryGraphPaint).copy(alpha = 155f / 255f)

    val width: Dp = 280.dp

    Column(
        modifier = modifier
            .width(width),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Date header
        Text(
            text = formattingService.getFormattedShortDayName(date, data.timeZone),
            style = typography.titleMedium
        )
        Text(
            text = formattingService.getFormattedDayMonth(date, data.timeZone),
            style = typography.bodySmall
        )

        // Weather icon and min/max temperatures
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = weatherIconId),
                contentDescription = forecast.weatherDescription,
                modifier = Modifier.size(70.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IconWithText(
                    iconResId = R.drawable.temperature_maximum_material,
                    text = formattingService.getFloatFormattedTemperature(
                        forecast.temperatureMaximum, FormattingSpec.UNIT_AND_SPACE
                    ),
                    style = typography.titleSmall,
                    tint = secondaryColor
                )
                IconWithText(
                    iconResId = R.drawable.temperature_minimum_material,
                    text = formattingService.getFloatFormattedTemperature(
                        forecast.temperatureMinimum, FormattingSpec.UNIT_AND_SPACE
                    ),
                    style = typography.titleSmall,
                    tint = tertiaryColor
                )
            }
        }

        // Day period labels
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val periodModifier = Modifier.weight(1f)
            val periodStyle = typography.labelSmall.copy(textAlign = TextAlign.Center, fontSize = 10.sp)
            Text(text = stringResource(R.string.title_daily_forecast_morning), modifier = periodModifier, style = periodStyle)
            Text(text = stringResource(R.string.title_daily_forecast_noon), modifier = periodModifier, style = periodStyle)
            Text(text = stringResource(R.string.title_daily_forecast_evening), modifier = periodModifier, style = periodStyle)
            Text(text = stringResource(R.string.title_daily_forecast_night), modifier = periodModifier, style = periodStyle)
        }

        // 4-period temperatures (actual + feels like)
        Row(
            modifier = Modifier.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val temps = listOf(
                forecast.temperatureMorning, forecast.temperatureDay,
                forecast.temperatureEvening, forecast.temperatureNight
            )
            val feelsLike = listOf(
                forecast.temperatureMorningFeelsLike, forecast.temperatureDayFeelsLike,
                forecast.temperatureEveningFeelsLike, forecast.temperatureNightFeelsLike
            )
            for (i in 0 until 4) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formattingService.getFloatFormattedTemperature(temps[i], FormattingSpec.UNIT_BUT_NO_SPACE),
                        style = typography.labelSmall
                    )
                    Text(
                        text = formattingService.getFloatFormattedTemperature(feelsLike[i], FormattingSpec.UNIT_BUT_NO_SPACE),
                        style = typography.labelSmall,
                        color = secondaryColor
                    )
                }
            }
        }

        // Temperature graph (4 points: morning, day, evening, night)
        SmoothTwoLinesGraphChart(
            modifier = Modifier
                .height(60.dp)
                .width(width),
            valuesA = data.temperatureGraphPoints,
            valuesB = data.feelsLikeTemperatureGraphPoints,
            min = data.temperatureGraphBounds.first,
            max = data.temperatureGraphBounds.second,
            colorA = primaryGraphColor,
            colorB = secondaryGraphColor,
            debug = true
        )

        // Environmental variables section
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val itemMod = Modifier.padding(2.dp)

            // Pressure & Cloudiness
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconWithText(
                    iconResId = R.drawable.barometer_material,
                    text = formattingService.getFormattedPressure(forecast.pressure.toFloat(), FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
                IconWithText(
                    iconResId = R.drawable.cloudy_material,
                    text = formattingService.getIntFormattedPercentage(forecast.cloudiness, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
            }

            // Humidity & Dew point
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconWithText(
                    iconResId = R.drawable.humidity_material,
                    text = formattingService.getIntFormattedPercentage(forecast.humidity, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
                IconWithText(
                    iconResId = R.drawable.dew_point_material,
                    text = formattingService.getFloatFormattedTemperature(forecast.dewPoint, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
            }

            // Sunrise & Sunset
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconWithText(
                    iconResId = R.drawable.sunrise_material,
                    text = formattingService.getFormattedTime(Date(forecast.sunriseDt), data.timeZone),
                    modifier = itemMod,
                    tint = primaryColor
                )
                IconWithText(
                    iconResId = R.drawable.sunset_material,
                    text = formattingService.getFormattedTime(Date(forecast.sunsetDt), data.timeZone),
                    modifier = itemMod,
                    tint = primaryColor
                )
            }

            // UV indicator
            UvIndicator(
                modifier = Modifier
                    .height(75.dp)
                    .width(75.dp)
                    .padding(8.dp),
                uvIndex = forecast.uvIndex
            )

            // Moonrise, Moonset & Moon phase indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    IconWithText(
                        iconResId = R.drawable.moonrise_material,
                        text = formattingService.getFormattedTime(Date(forecast.moonriseDt), data.timeZone),
                        modifier = itemMod,
                        tint = primaryColor
                    )
                    IconWithText(
                        iconResId = R.drawable.moonset_material,
                        text = formattingService.getFormattedTime(Date(forecast.moonsetDt), data.timeZone),
                        modifier = itemMod,
                        tint = primaryColor
                    )
                }
                MoonPhaseIndicator(
                    modifier = Modifier
                        .size(70.dp)
                        .padding(4.dp),
                    moonPhase = forecast.moonPhase
                )
            }
        }

        // Wind section
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val itemMod = Modifier.padding(2.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconWithText(
                    iconResId = R.drawable.windsock_material,
                    text = formattingService.getFloatFormattedSpeed(forecast.windSpeed, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
                IconWithText(
                    iconResId = R.drawable.wind_material,
                    text = formattingService.getFloatFormattedSpeed(forecast.windGustSpeed, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = secondaryColor
                )
            }

            WindDirectionIndicator(
                modifier = Modifier
                    .height(70.dp)
                    .width(65.dp)
                    .padding(top = 8.dp),
                windDirection = forecast.windDirection
            )
            Text(
                text = formattingService.getFormattedDirectionInCardinalPoints(forecast.windDirection),
                style = typography.titleSmall,
                modifier = itemMod
            )
            Text(
                text = formattingService.getFormattedDirectionInDegrees(forecast.windDirection),
                style = typography.titleSmall,
                modifier = itemMod
            )

            SmoothTwoLinesGraphChart(
                modifier = Modifier
                    .height(60.dp)
                    .width(width),
                valuesA = data.windSpeedGraphPoints,
                valuesB = data.windGustSpeedGraphPoints,
                min = data.windSpeedGraphBounds.first,
                max = data.windSpeedGraphBounds.second,
                colorA = primaryGraphColor,
                colorB = secondaryGraphColor,
            )
        }

        // Precipitations section
        Column(
            modifier = Modifier.padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val itemMod = Modifier.padding(2.dp)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconWithText(
                    iconResId = R.drawable.rain_material,
                    text = formattingService.getFloatFormattedShortDistance(forecast.rain, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = tertiaryColor
                )
                IconWithText(
                    iconResId = R.drawable.snow_material,
                    text = formattingService.getFloatFormattedShortDistance(forecast.snow, FormattingSpec.UNIT_AND_SPACE),
                    modifier = itemMod,
                    tint = primaryColor
                )
            }
            IconWithText(
                iconResId = R.drawable.umbrella_material,
                text = formattingService.getIntFormattedPercentage(forecast.pop.toPercentage(), FormattingSpec.UNIT_AND_SPACE),
                modifier = itemMod,
                tint = secondaryColor
            )

            val precipGraphModifier = Modifier
                .height(60.dp)
                .width(width)
            Box(modifier = precipGraphModifier) {
                BarGraphChart(
                    modifier = Modifier.fillMaxSize(),
                    values = floatArrayOf(data.popGraphPoint),
                    color = secondaryGraphColor.copy(alpha = 155f / 255f),
                    max = 1f,
                    min = 0f,
                )
                SmoothTwoLinesGraphChart(
                    modifier = Modifier.fillMaxSize(),
                    valuesA = data.rainGraphPoints,
                    valuesB = data.snowGraphPoints,
                    min = data.precipitationGraphBounds.first,
                    max = data.precipitationGraphBounds.second,
                    colorA = tertiaryGraphColor,
                    colorB = primaryGraphColor,
                )
            }
        }
    }
}

@Preview(device = "id:resizable")
@Composable
fun DailyWeatherForecastItemPreview() {
    val data = DailyWeatherForecastItemViewData.Preview

    DailyWeatherForecastItemView(
        modifier = Modifier,
        formattingService = FormattingService.getDumbInstance(LocalContext.current),
        data = data
    )
}

