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

package fr.qgdev.openweather.ui.place

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.AirQuality
import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.CurrentWeather
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.Properties
import fr.qgdev.openweather.repositories.FormattingService

@Preview
@Composable
fun DetailedViewPreview() {
    val coordinates = Coordinates.newBuilder()
        .setLatitude(0.0)
        .setLongitude(0.0)
        .build()

    val geolocation = Geolocation.newBuilder()
        .setCity("Paris")
        .setCountryCode("FR")
        .setCoordinates(coordinates)
        .build()

    val properties = Properties.newBuilder()
        .setTimeOffset(0)
        .setCreationTime(0L)
        .setLastAvailableWeatherDataTime(0L)
        .setLastSuccessfulWeatherUpdateTime(0L)
        .setLastWeatherUpdateAttemptTime(0L)
        .setLastAirQualityUpdateAttemptTime(0L)
        .setLastAvailableAirQualityDataTime(0L)
        .setLastSuccessfulAirQualityUpdateTime(0L)
        .build()

    val currentWeather = CurrentWeather.newBuilder()
        .setWeather("Clear")
        .setWeatherCode(1)
        .setTemperature(0.0F)
        .setTemperatureFeelsLike(0.0F)
        .setCloudiness(0)
        .setHumidity(0)
        .setPressure(0)
        .setWindSpeed(0.0F)
        .setWindDirection(0)
        .setWindGustSpeed(0.0F)
        .setSunrise(0L)
        .setSunset(0L)
        .setUvIndex(0)
        .setVisibility(0)
        .setRain(0.0F)
        .setSnow(0.0F)
        .build()

    val airQuality = AirQuality.newBuilder()
        .setAqi(0)
        .setCo(0.0F)
        .setNo(0.0F)
        .setO3(0.0F)
        .setNo2(0.0F)
        .setPm25(0.0F)
        .setPm10(0.0F)
        .setNh3(0.0F)
        .setSo2(0.0F)
        .build()


    val place = Place.newBuilder()
        .setGeolocation(geolocation)
        .setProperties(properties)
        .setCurrentWeather(currentWeather)
        .setAirQuality(airQuality)
        .addAllMinutelyForecastList(emptyList())
        .addAllHourlyForecastList(emptyList())
        .addAllDailyForecastList(emptyList())
        .addAllWeatherAlertsList(emptyList())
        .build()

    DetailedView(place = place, formattingService = FormattingService.getDumbInstance(LocalContext.current))
}

@Composable
fun DetailedView(
    modifier: Modifier = Modifier,
    place: Place,
    formattingService: FormattingService
) {
    Column(modifier = modifier
        .animateContentSize()) {
        WeatherAlertSection()
        PrecipitationsSection()
        // Assuming AirQualityIndex() is a composable function for the air quality information
        AirQualityIndex()
    }
}

@Composable
fun WeatherAlertSection() {
    Row(
        modifier = Modifier
            .padding(10.dp)
            .border(
                width = 1.dp,
                color = Color.Gray, // Replace with actual color resource
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.danger),
            contentDescription = stringResource(id = R.string.title_dialog_weather_alert),
            tint = colorResource(id = R.color.colorFirstText),
            modifier = Modifier
                .size(30.dp)
                .padding(end = 10.dp)
        )
        Text(
            text = stringResource(id = R.string.title_dialog_weather_alert),
            color = colorResource(id = R.color.colorFirstText),
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun PrecipitationsSection() {
    Column {
        TitleRow()
        RainPrecipitations()
        SnowPrecipitations()
    }
}

@Composable
fun TitleRow() {
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.umbrella_material),
            contentDescription = stringResource(id = R.string.title_precipitations_1h),
            tint = colorResource(id = R.color.colorFirstText),
            modifier = Modifier
                .size(width = 40.dp, height = 30.dp)
                .padding(end = 10.dp)
        )
        Text(
            text = stringResource(id = R.string.title_precipitations_1h),
            color = colorResource(id = R.color.colorFirstText),
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
fun RainPrecipitations() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.rain_material),
            contentDescription = stringResource(id = R.string.title_precipitation_rain),
            tint = colorResource(id = R.color.colorSecondaryText),
            modifier = Modifier
                .size(30.dp)
                .padding(end = 10.dp)
        )
        Text(
            text = stringResource(id = R.string.title_precipitation_rain),
            color = colorResource(id = R.color.colorSecondaryText),
            fontSize = 20.sp,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "10 mm",
            color = colorResource(id = R.color.colorSecondaryText),
            fontSize = 20.sp
        )
    }
}

@Composable
fun SnowPrecipitations() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.snow_material),
            contentDescription = stringResource(id = R.string.title_precipitation_snow),
            tint = colorResource(id = R.color.colorSecondaryText),
            modifier = Modifier
                .size(30.dp)
                .padding(end = 10.dp)
        )
        Text(
            text = stringResource(id = R.string.title_precipitation_snow),
            color = colorResource(id = R.color.colorSecondaryText),
            fontSize = 20.sp,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = "10 mm",
            color = colorResource(id = R.color.colorSecondaryText),
            fontSize = 20.sp
        )
    }
}

// Placeholder for AirQualityIndex composable function
@Composable
fun AirQualityIndex() {
    // Implementation depends on the layout of adapter_air_quality_index.xml
}