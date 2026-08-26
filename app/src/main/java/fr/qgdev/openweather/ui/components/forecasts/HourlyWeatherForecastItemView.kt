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

package fr.qgdev.openweather.ui.components.forecasts

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.repositories.FormattingService.FormattingSpec
import fr.qgdev.openweather.ui.components.forecasts.components.BarGraphChart
import fr.qgdev.openweather.ui.components.forecasts.components.SmoothLineGraphChart
import fr.qgdev.openweather.ui.components.forecasts.components.SmoothTwoLinesGraphChart
import fr.qgdev.openweather.ui.components.forecasts.components.UvIndicator
import fr.qgdev.openweather.ui.components.forecasts.components.WindDirectionIndicator
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import fr.qgdev.openweather.ui.utils.toPercentage
import java.util.Date
import java.util.TimeZone

data class HourlyWeatherForecastItemViewData(
    val hourlyWeatherForecast: HourlyForecast,
    val temperatureGraphPoints: FloatArray,
    val feelsLikeTemperatureGraphPoints: FloatArray,
    val temperatureGraphBounds: Pair<Float, Float>,
    val humidityGraphPoints: FloatArray,
    val humidityGraphBounds: Pair<Float, Float>,
    val pressureGraphPoints: FloatArray,
    val pressureGraphBounds: Pair<Float, Float>,
    val windSpeedGraphPoints: FloatArray,
    val windGustSpeedGraphPoints: FloatArray,
    val windSpeedGraphBounds: Pair<Float, Float>,
    val rainGraphPoints: FloatArray,
    val snowGraphPoints: FloatArray,
    val precipitationGraphBounds: Pair<Float, Float>,
    val isDayTime: Boolean,
    val timeZone: TimeZone,
) {
    companion object {
        /** Preview / test data */
        val Preview = HourlyWeatherForecastItemViewData(
            hourlyWeatherForecast = HourlyForecast.newBuilder()
                .setDt(1620000000)
                .setTemperature(10f)
                .setTemperatureFeelsLike(20f)
                .setWeatherCode(800)
                .setHumidity(50)
                .setPressure(1000)
                .setUvIndex(12)
                .setDewPoint(10f)
                .setCloudiness(50)
                .setVisibility(10000)
                .setWindSpeed(10f)
                .setWindGustSpeed(20f)
                .setWindDirection(180)
                .setRain(5f)
                .setSnow(5f)
                .setPop(50f)
                .build(),
            temperatureGraphPoints = floatArrayOf(0f, 10f, 20f),
            feelsLikeTemperatureGraphPoints = floatArrayOf(20f, 10f, 0f),
            temperatureGraphBounds = Pair(0f, 20f),
            humidityGraphPoints = floatArrayOf(0f, 50f, 100f),
            humidityGraphBounds = Pair(0f, 100f),
            pressureGraphPoints = floatArrayOf(0f, 500f, 1000f),
            pressureGraphBounds = Pair(0f, 1000f),
            windSpeedGraphPoints = floatArrayOf(0f, 10f, 20f),
            windGustSpeedGraphPoints = floatArrayOf(20f, 10f, 0f),
            windSpeedGraphBounds = Pair(0f, 20f),
            rainGraphPoints = floatArrayOf(0f, 5f, 10f),
            snowGraphPoints = floatArrayOf(10f, 5f, 0f),
            precipitationGraphBounds = Pair(0f, 10f),
            isDayTime = true,
            timeZone = TimeZone.getDefault(),
        )
    }
}

@Composable
fun HourlyWeatherForecastItemView(
    modifier: Modifier = Modifier,
    textMeasurer: TextMeasurer = rememberTextMeasurer(),
    formattingService: FormattingService,
    data: HourlyWeatherForecastItemViewData
) {
    val modifier = modifier.then(
        Modifier
            .padding(16.dp)
    )
    val weatherIconId = getDrawableResIdFromWeatherCode(
        data.hourlyWeatherForecast.weatherCode,
        data.isDayTime
    )

    Column(
        modifier = Modifier
            .width(100.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        //  Center horizontally the text
        Text(
            text = formattingService.getFormattedHour(Date(data.hourlyWeatherForecast.dt), data.timeZone),
            style = typography.titleSmall,
        )
        //  Display weather icon

        Image(
            painter = painterResource(id = weatherIconId),
            contentDescription = "Weather description",
            modifier = Modifier
                .size(70.dp)
                .padding(8.dp)
        )

        //  Temperatures (values and graphics)
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        )
        {
            val modifier = Modifier.padding(2.dp)
            //  Temperature
            Text(
                modifier = modifier,
                text = formattingService.getFloatFormattedTemperature(data.hourlyWeatherForecast.temperature, FormattingSpec.UNIT_BUT_NO_SPACE),
                style = typography.titleSmall
            )
            //  Feel like temperature
            Text(
                modifier = modifier,
                text = formattingService.getFloatFormattedTemperature(data.hourlyWeatherForecast.temperatureFeelsLike, FormattingSpec.UNIT_BUT_NO_SPACE),
                style = typography.titleSmall
            )
            SmoothTwoLinesGraphChart(
                modifier = Modifier
                    .height(70.dp)
                    .width(100.dp),
                valuesA = data.temperatureGraphPoints,
                valuesB = data.feelsLikeTemperatureGraphPoints,
                min = data.temperatureGraphBounds.first,
                max = data.temperatureGraphBounds.second,
                colorA = Color.Blue,
                colorB = Color.Red,
            )
        }

        //  Humidity (value and graph)
        Column (
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.padding(2.dp)
            //  Humidity
            Text(
                text = formattingService.getIntFormattedPercentage(
                    data.hourlyWeatherForecast.humidity,
                    FormattingSpec.UNIT_BUT_NO_SPACE),
                style = typography.titleSmall,
                modifier = modifier
            )
            SmoothLineGraphChart(
                modifier = Modifier
                    .height(35.dp)
                    .width(100.dp),
                values = data.humidityGraphPoints,
                min = data.humidityGraphBounds.first,
                max = data.humidityGraphBounds.second,
                color = Color.Blue,
            )
        }

        //  Pressure (value and graph)
        Column(
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.padding(2.dp)
            //  Pressure
            Text(
                text = formattingService.getFormattedPressure(
                    data.hourlyWeatherForecast.pressure.toFloat(),
                    FormattingSpec.UNIT_BUT_NO_SPACE),
                style = typography.titleSmall,
                modifier = modifier
            )
            SmoothLineGraphChart(
                modifier = Modifier
                    .height(35.dp)
                    .width(100.dp),
                values = data.pressureGraphPoints,
                min = data.pressureGraphBounds.first,
                max = data.pressureGraphBounds.second,
                color = Color.Blue,
            )
        }

        //  UV indicator
        UvIndicator(
            modifier = Modifier
                .height(75.dp)
                .width(75.dp)
                .padding(8.dp),
            uvIndex = data.hourlyWeatherForecast.uvIndex
        )

        Column(
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.padding(2.dp)
            //  Dew point
            Text(
                text = formattingService.getFloatFormattedTemperature(
                    data.hourlyWeatherForecast.dewPoint,
                    FormattingSpec.UNIT_AND_SPACE
                ),
                style = typography.titleSmall,
                modifier = modifier
            )
            //  Cloudiness
            Text(
                text = formattingService.getIntFormattedPercentage(
                    data.hourlyWeatherForecast.cloudiness,
                    FormattingSpec.UNIT_AND_SPACE
                ),
                style = typography.titleSmall,
                modifier = modifier
            )
            //  Visibility
            Text(
                text = formattingService.getIntFormattedDistance(
                    data.hourlyWeatherForecast.visibility.toFloat(),
                    FormattingSpec.UNIT_AND_SPACE
                ),
                style = typography.titleSmall,
                modifier = modifier
            )
        }

        //  Wind speed, gust speed and direction
        Column(
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.padding(2.dp)
            //  Wind speed
            Text(
                text = formattingService.getFloatFormattedSpeed(
                    data.hourlyWeatherForecast.windSpeed,
                    FormattingSpec.UNIT_AND_SPACE
                ),
                style = typography.titleSmall,
                modifier = modifier
            )
            //  Wind gust
            Text(
                text = formattingService.getFloatFormattedSpeed(
                    data.hourlyWeatherForecast.windGustSpeed,
                    FormattingSpec.UNIT_AND_SPACE
                ),
                style = typography.titleSmall,
                modifier = modifier
            )

            //  Wind direction (cardinal points and degrees)

            WindDirectionIndicator(
                modifier = Modifier
                    .height(70.dp)
                    .width(65.dp)
                    .padding(top=8.dp)
                    .align(androidx.compose.ui.Alignment.CenterHorizontally),
                windDirection = data.hourlyWeatherForecast.windDirection.toInt()
            )
            Text(
                modifier = modifier,
                text = formattingService.getFormattedDirectionInCardinalPoints(
                    data.hourlyWeatherForecast.windDirection),
                style = typography.titleSmall
            )
            Text(
                modifier = modifier,
                text = formattingService.getFormattedDirectionInDegrees(
                    data.hourlyWeatherForecast.windDirection),
                style = typography.titleSmall
            )
            SmoothTwoLinesGraphChart(
                modifier = Modifier
                    .height(70.dp)
                    .width(100.dp),
                valuesA = data.windSpeedGraphPoints,
                valuesB = data.windGustSpeedGraphPoints,
                min = data.windSpeedGraphBounds.first,
                max = data.windSpeedGraphBounds.second,
                colorA = Color.Blue,
                colorB = Color.Red
            )
        }

        //  Precipitations
        Column(
            modifier = Modifier
                .padding(top = 8.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val modifier = Modifier.padding(2.dp)
            Text(
                modifier = modifier,
                text = formattingService.getFloatFormattedShortDistance(
                    data.hourlyWeatherForecast.rain,
                    FormattingSpec.UNIT_AND_SPACE),
                style = typography.titleSmall
            )
            Text(
                modifier = modifier,
                text = formattingService.getFloatFormattedShortDistance(
                    data.hourlyWeatherForecast.snow,
                    FormattingSpec.UNIT_AND_SPACE),
                style = typography.titleSmall
            )
            Text(
                modifier = modifier,
                text = formattingService.getIntFormattedPercentage(
                    data.hourlyWeatherForecast.pop.toPercentage(),
                    FormattingSpec.UNIT_AND_SPACE),
                style = typography.titleSmall
            )
            val precipitationGraphModifier = Modifier
                .height(70.dp)
                .width(100.dp)
            Box(
                modifier = precipitationGraphModifier
            ) {
                BarGraphChart(
                    modifier = Modifier.fillMaxSize(),
                    values = floatArrayOf(data.hourlyWeatherForecast.pop.toPercentage()),
                    color = Color.Yellow,
                    max = 100f,
                    min = 0f,
                )
                SmoothTwoLinesGraphChart(
                    modifier = Modifier.fillMaxSize(),
                    valuesA = data.rainGraphPoints,
                    valuesB = data.snowGraphPoints,
                    min = data.precipitationGraphBounds.first,
                    max = data.precipitationGraphBounds.second,
                    colorA = Color.Blue,
                    colorB = Color.Red
                )
            }
        }
    }

}

@Preview(device = "id:resizable")
@Composable
fun HourlyWeatherForecastItemPreview() {
    val data = HourlyWeatherForecastItemViewData.Preview

    HourlyWeatherForecastItemView(
        modifier = Modifier,
        textMeasurer = rememberTextMeasurer(),
        formattingService = FormattingService.getDumbInstance(androidx.compose.ui.platform.LocalContext.current),
        data = data
    )
}