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

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
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
import fr.qgdev.openweather.ui.components.dialogs.AirQualityInfoDialog
import fr.qgdev.openweather.ui.components.dialogs.WeatherAlertDialog
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.ui.components.AirQualityIndexBar
import fr.qgdev.openweather.ui.components.forecasts.DailyWeatherForecastsView
import fr.qgdev.openweather.ui.components.forecasts.HourlyWeatherForecastsView
import fr.qgdev.openweather.ui.common.components.ButtonWithIconAndText
import fr.qgdev.openweather.ui.common.components.ClassicAnimatedGrid
import fr.qgdev.openweather.ui.components.EnvironmentalVariableItem
import fr.qgdev.openweather.ui.common.components.FoldableContainer
import fr.qgdev.openweather.ui.components.GaugeBarView
import fr.qgdev.openweather.ui.common.components.ItemWithIconAndTitle
import fr.qgdev.openweather.ui.common.components.Rect
import fr.qgdev.openweather.ui.common.components.TitleWithIcon
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.utils.getDrawableResIdFromWeatherCode
import java.util.Date
import java.util.SimpleTimeZone

@Composable
fun PlaceCardView(
    modifier: Modifier = Modifier,
    place: Place,
    formattingService: FormattingService,
    globalState: MutableState<Boolean> = remember { mutableStateOf(false) },
    airQualityState: MutableState<Boolean> = remember { mutableStateOf(false) },
    hourlyForecastState: MutableState<Boolean> = remember { mutableStateOf(false) },
    dailyForecastState: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
    val fragmentManager = (LocalContext.current as? AppCompatActivity)?.supportFragmentManager

    //  The alerts UI has not been ported to Compose yet, so this shows the existing View-based
    //  dialog imperatively rather than through a dialogBoxOpened flag like AirQualityInfoDialog.
    //  Both alert entry points below share it. See PORTING.md.
    val context = LocalContext.current
    val showWeatherAlerts = {
        WeatherAlertDialog(context, place, formattingService).build()
    }

    val gridColumns = integerResource(id = R.integer.env_variables_column_count)
    val colorFirstText = colorResource(id = R.color.colorFirstText)
    val colorSecondaryText = colorResource(id = R.color.colorSecondaryText)
    val colorIcons = colorResource(id = R.color.colorIcons)

    val animatedExtensionAlpha by animateFloatAsState(
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        targetValue = if (globalState.value) 1.0f else 0f,
        label = "animatedExtensionAlpha"
    )

    val animatedReductionAlpha by animateFloatAsState(
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        targetValue = if (!globalState.value) 1.0f else 0.0f,
        label = "animatedReductionAlpha"
    )

    val hiddenElementsModifier = Modifier
    //.graphicsLayer { alpha = animatedExtensionAlpha }

    val currentWeather = place.currentWeather
    val airQuality = place.airQuality
    val geolocation = place.geolocation

    val hasWarnings = place.weatherAlertsListCount > 0
    val hasRainPrecipitations = currentWeather.rain > 0.0f
    val hasSnowPrecipitations = currentWeather.snow > 0.0f
    val hasPrecipitations = hasRainPrecipitations || hasSnowPrecipitations

    val weatherIconId = getDrawableResIdFromWeatherCode(
        currentWeather.weatherCode,
        currentWeather.sunset < currentWeather.dt && currentWeather.sunrise > currentWeather.dt
    )

    val backgroundColor =
        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun AirQualityBlock() {
        val dialogBoxOpened = remember { mutableStateOf(false) }

        if (dialogBoxOpened.value) {
            AirQualityInfoDialog(
                onDismissRequest = { dialogBoxOpened.value = false }
            )
        }

        Column(
            modifier = Modifier
                .padding(top = 15.dp)
                .fillMaxWidth()
                .graphicsLayer { alpha = animatedExtensionAlpha }

        ) {
            val aqiColor: Int
            val aqiMessage: Int

            when (airQuality.aqi) {
                5 -> {
                    aqiColor = R.color.colorAqiHazardous
                    aqiMessage = R.string.air_quality_5
                }

                4 -> {
                    aqiColor = R.color.colorAqiVeryBad
                    aqiMessage = R.string.air_quality_4
                }

                3 -> {
                    aqiColor = R.color.colorAqiBad
                    aqiMessage = R.string.air_quality_3
                }

                2 -> {
                    aqiColor = R.color.colorAqiModerate
                    aqiMessage = R.string.air_quality_2
                }

                else -> {
                    aqiColor = R.color.colorAqiGood
                    aqiMessage = R.string.air_quality_1
                }
            }

            TitleWithIcon(
                iconResId = R.drawable.airquality,
                textResId = R.string.title_air_quality,
                descriptionResId = R.string.title_air_quality //TODO
            )

            AirQualityIndexBar(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 0.dp)
                    .widthIn(min = 180.dp, max = 500.dp),
                value = airQuality.aqi,
                labelText = stringResource(id = aqiMessage),
                mainColor = colorResource(id = aqiColor),
                backgroundColor = backgroundColor,
                textColor = colorResource(id = R.color.colorIcons),
                subtextColor = colorResource(id = R.color.colorSecondaryText)
            )

            FoldableContainer(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                title = stringResource(id = R.string.title_air_quality_concentrations),
                headerColor = colorFirstText,
                isUnFolded = airQualityState
            ) {
                val sectionsColors = listOf(
                    colorResource(id = R.color.colorAqiGood),
                    colorResource(id = R.color.colorAqiModerate),
                    colorResource(id = R.color.colorAqiBad),
                    colorResource(id = R.color.colorAqiVeryBad),
                    colorResource(id = R.color.colorAqiHazardous)
                )

                val gaugeBarModifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth()

                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.so2,
                    labelText = "SO²",
                    sectionsBoundaries = listOf(0f, 20f, 80f, 250f, 350f),
                    sectionsColors = sectionsColors
                )
                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.no2,
                    labelText = "NO²",
                    sectionsBoundaries = listOf(0f, 40f, 70f, 150f, 200f),
                    sectionsColors = sectionsColors
                )
                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.pm10,
                    labelText = "PM₁₀",
                    sectionsBoundaries = listOf(0f, 20f, 50f, 100f, 200f),
                    sectionsColors = sectionsColors
                )
                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.pm25,
                    labelText = "PM₂.₅",
                    sectionsBoundaries = listOf(0f, 10f, 25f, 50f, 75f),
                    sectionsColors = sectionsColors
                )
                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.o3,
                    labelText = "O³",
                    sectionsBoundaries = listOf(0f, 60f, 100f, 140f, 180f),
                    sectionsColors = sectionsColors
                )

                GaugeBarView(
                    modifier = gaugeBarModifier,
                    value = airQuality.co,
                    labelText = "CO",
                    sectionsBoundaries = listOf(0f, 4400f, 9400f, 12400f, 15400f),
                    sectionsColors = sectionsColors
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.air_quality_composition_unit),
                        color = colorFirstText,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                    )

                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = stringResource(id = R.string.title_air_quality_concentrations),
                        tint = colorIcons,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                onClick = { dialogBoxOpened.value = true },
                                enabled = true,
                            )
                    )
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .animateContentSize()
            .padding(5.dp)
            .clickable {
                globalState.value = !globalState.value
                airQualityState.value = false
                hourlyForecastState.value = false
                dailyForecastState.value = false
            },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (globalState.value) 5.dp else 2.dp
            ),
        border = BorderStroke(
            width = 0.1.dp,
            color = MaterialTheme.colorScheme.outlineVariant)

    ) {

        Column(
            modifier = Modifier
                .padding(15.dp)
        ) {
            // Principal weather content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .defaultMinSize(minHeight = 150.dp)
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, bottom = 10.dp),

                    ) {
                    // Place content
                    Text(
                        text = geolocation.city,
                        color = colorFirstText,
                        fontSize = 18.sp,
                        maxLines = 4
                    )
                    Text(
                        text = geolocation.countryCode,
                        color = colorSecondaryText,
                        fontSize = 14.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(end = 10.dp, top = 10.dp)
                        .align(Alignment.BottomStart),
                ) {
                    // Temperature
                    Text(
                        text = formattingService.getFloatFormattedTemperature(
                            currentWeather.temperature,
                            FormattingService.FormattingSpec.UNIT_AND_SPACE
                        ),
                        color = colorFirstText,
                        fontSize = 35.sp
                    )

                    // Feels Like Temperature
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.title_temperature_feelslike),
                            color = colorSecondaryText,
                            fontSize = 16.sp
                        )
                        Text(
                            text = formattingService.getFloatFormattedTemperature(
                                currentWeather.temperatureFeelsLike,
                                FormattingService.FormattingSpec.UNIT_AND_SPACE
                            ),
                            color = colorSecondaryText,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(start = 5.dp)
                        )
                    }
                }


                // Weather description
                Column(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.TopEnd),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top,
                ) {
                    Image(
                        painter = painterResource(id = weatherIconId),
                        contentDescription = "Weather description",
                        modifier = Modifier
                            .size(75.dp)
                    )
                    Text(
                        text = currentWeather.weatherDescription,
                        color = colorFirstText,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(top = 15.dp)
                    )
                }
            }
            //  Environmental variables & weather alert
            Box(
                Modifier
                    .animateContentSize()
                    .fillMaxWidth()
            ) {
                // Weather environmental variables
                ClassicAnimatedGrid(
                    columns = gridColumns,
                    modifier = Modifier
                        .padding(top = 5.dp, end = 10.dp)
                        .align(Alignment.TopStart),
                    verticalSpacing = 5.dp,
                    horizontalSpacing = 5.dp,
                    visibleArea = Rect(0, 0, 2, 2),
                    showOutsideArea = globalState.value
                ) {
                    // Wind
                    EnvironmentalVariableItem(
                        variable = formattingService.getFormattedDirection(
                            currentWeather.windDirection,
                            currentWeather.isWindDirectionReadable
                        ),
                        drawableId = R.drawable.wind_vane_material,
                        contentDescription = stringResource(id = R.string.description_wind_direction)
                    )
                    EnvironmentalVariableItem(
                        variable = formattingService.getFloatFormattedSpeed(
                            currentWeather.windSpeed,
                            FormattingService.FormattingSpec.UNIT_AND_SPACE
                        ),
                        drawableId = R.drawable.windsock_material,
                        contentDescription = stringResource(id = R.string.description_wind_speed)
                    )
                    EnvironmentalVariableItem(
                        modifier = hiddenElementsModifier,
                        variable = formattingService.getFloatFormattedSpeed(
                            currentWeather.windGustSpeed,
                            FormattingService.FormattingSpec.UNIT_AND_SPACE
                        ),
                        drawableId = R.drawable.wind_material,
                        contentDescription = stringResource(id = R.string.description_wind_gust_speed)
                    )
                    // Humidity, Pressure, Visibility
                    EnvironmentalVariableItem(
                        variable = "${currentWeather.humidity}%",
                        drawableId = R.drawable.humidity_material,
                        contentDescription = stringResource(id = R.string.description_humidity)
                    )
                    EnvironmentalVariableItem(
                        variable = formattingService.getFormattedPressure(
                            currentWeather.pressure.toFloat(),
                            FormattingService.FormattingSpec.UNIT_AND_SPACE
                        ),
                        drawableId = R.drawable.barometer_material,
                        contentDescription = stringResource(id = R.string.description_pressure)
                    )
                    EnvironmentalVariableItem(
                        modifier = hiddenElementsModifier,
                        variable = formattingService.getIntFormattedDistance(
                            currentWeather.visibility.toFloat(),
                            FormattingService.FormattingSpec.UNIT_AND_SPACE
                        ),
                        drawableId = R.drawable.visibility,
                        contentDescription = stringResource(id = R.string.description_visibility),
                    )

                    // Sunrise, Sunset, Cloudiness
                    EnvironmentalVariableItem(
                        modifier = hiddenElementsModifier,
                        variable = formattingService.getFormattedTime(
                            Date(currentWeather.sunrise),
                            SimpleTimeZone(place.properties.timeOffset, "null")
                        ),
                        drawableId = R.drawable.sunrise_material,
                        contentDescription = stringResource(id = R.string.description_sunrise)
                    )
                    EnvironmentalVariableItem(
                        modifier = hiddenElementsModifier,
                        variable = formattingService.getFormattedTime(
                            Date(currentWeather.sunset),
                            SimpleTimeZone(place.properties.timeOffset, "null")
                        ),
                        drawableId = R.drawable.sunset_material,
                        contentDescription = stringResource(id = R.string.description_sunset)
                    )
                    EnvironmentalVariableItem(
                        modifier = hiddenElementsModifier,
                        variable = "${currentWeather.cloudiness}%",
                        drawableId = R.drawable.cloudy_material,
                        contentDescription = stringResource(id = R.string.description_cloudiness)
                    )
                }
                //  Weather alert icon
                if (hasWarnings && animatedReductionAlpha > 0f) {
                    // Warning icon
                    Icon(
                        modifier = Modifier
                            .size(45.dp)
                            .align(Alignment.BottomEnd)
                            .graphicsLayer { alpha = animatedReductionAlpha }
                            .clickable(onClick = showWeatherAlerts),
                        painter = painterResource(id = R.drawable.danger),
                        contentDescription = stringResource(id = R.string.description_weather_alert),
                        tint = colorIcons,
                    )
                }
            }

            if (animatedExtensionAlpha > 0.0f) {
                //  Precipitations
                Column(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = animatedExtensionAlpha }
                ) {
                    // Weather alerts button
                    if (hasWarnings) {
                        ButtonWithIconAndText(
                            Modifier.padding(bottom = 15.dp),
                            iconResId = R.drawable.danger,
                            textResId = R.string.title_dialog_weather_alert,
                            descriptionResId = R.string.description_weather_alert,
                            onClick = showWeatherAlerts
                        )
                    }

                    // Precipitations
                    if (hasPrecipitations) {
                        TitleWithIcon(
                            Modifier.padding(bottom = 15.dp),
                            iconResId = R.drawable.umbrella_material,
                            textResId = R.string.title_precipitations_1h,
                            descriptionResId = R.string.description_precipitations_1h
                        )

                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp)
                        ) {
                            if (hasRainPrecipitations) {
                                ItemWithIconAndTitle(
                                    iconResId = R.drawable.rain_material,
                                    title = stringResource(id = R.string.title_precipitation_rain),
                                    itemData = formattingService.getFloatFormattedShortDistance(
                                        currentWeather.rain,
                                        FormattingService.FormattingSpec.UNIT_AND_SPACE
                                    ),
                                    description = stringResource(id = R.string.title_precipitation_rain)
                                )
                            }

                            if (hasSnowPrecipitations) {
                                ItemWithIconAndTitle(
                                    iconResId = R.drawable.snow_material,
                                    title = stringResource(id = R.string.title_precipitation_snow),
                                    itemData = formattingService.getFloatFormattedShortDistance(
                                        currentWeather.snow,
                                        FormattingService.FormattingSpec.UNIT_AND_SPACE
                                    ),
                                    description = stringResource(id = R.string.title_precipitation_snow)
                                )
                            }
                        }

                    }
                }

                // Air Quality
                AirQualityBlock()

                // Forecasts
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .graphicsLayer { alpha = animatedExtensionAlpha }
                )
                {
                    TitleWithIcon(
                        modifier = Modifier,
                        iconResId = R.drawable.weather_forecast_material,
                        textResId = R.string.title_forecast,
                        descriptionResId = R.string.title_forecast //TODO
                    )

                    FoldableContainer(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        title = stringResource(id = R.string.title_forecast_hourly),
                        headerColor = colorFirstText,
                        isUnFolded = hourlyForecastState
                    ) {
                        HourlyWeatherForecastsView(
                            modifier = Modifier.fillMaxWidth(),
                            hourlyWeatherForecasts = place.hourlyForecastListList,
                            sunriseTimestamps = place.dailyForecastListList.map { it.sunriseDt },
                            sunsetTimestamps = place.dailyForecastListList.map { it.sunsetDt },
                            formattingService = formattingService,
                            timeZone = SimpleTimeZone(place.properties.timeOffset, "")
                        )
                    }

                    FoldableContainer(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                            .fillMaxWidth(),
                        title = stringResource(id = R.string.title_forecast_daily),
                        headerColor = colorFirstText,
                        isUnFolded = dailyForecastState
                    ) {
                        DailyWeatherForecastsView(
                            modifier = Modifier.fillMaxWidth(),
                            dailyWeatherForecasts = place.dailyForecastListList,
                            formattingService = formattingService,
                            timeZone = SimpleTimeZone(place.properties.timeOffset, "")
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PlaceCardViewPreview(
    extended: MutableState<Boolean> = remember { mutableStateOf(false) }
) {
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

    AppTheme {
        PlaceCardView(
            place = place,
            formattingService = FormattingService.getDumbInstance(LocalContext.current),
            globalState = extended,
            airQualityState = remember { mutableStateOf(false) },
            hourlyForecastState = remember { mutableStateOf(false) },
            dailyForecastState = remember { mutableStateOf(false) }
        )
    }
}

@Preview(
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewLightCompactPreview() {
    PlaceCardViewPreview()
}

@Preview(
    device = "spec:width=1080px,height=2280px,dpi=440",
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewLightExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}


@Preview(
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewDarkPreview() {
    PlaceCardViewPreview()
}

@Preview(
    device = Devices.PIXEL_4,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewDarkExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}



@Preview(
    device = Devices.NEXUS_5,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewSmallLightCompactPreview() {
    PlaceCardViewPreview()
}

@Preview(
    device = Devices.NEXUS_5,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewSmallLightExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}


@Preview(
    device = Devices.NEXUS_5,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewSmallDarkPreview() {
    PlaceCardViewPreview()
}

@Preview(
    device = Devices.NEXUS_5,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true)
@Composable
fun PlaceCardViewSmallDarkExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}

@Preview(
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun PlaceCardViewFullLightExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PlaceCardViewFullDarkExtendedPreview() {
    PlaceCardViewPreview(remember { mutableStateOf(true) })
}



