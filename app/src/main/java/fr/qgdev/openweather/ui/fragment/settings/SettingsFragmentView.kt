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

package fr.qgdev.openweather.ui.fragment.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.BuildConfig
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.settings.MeasureSettings
import fr.qgdev.openweather.data.settings.PressureSettings
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.data.settings.TemperatureSettings
import fr.qgdev.openweather.data.settings.TimeSettings
import fr.qgdev.openweather.data.settings.UpdatePeriodSettings
import fr.qgdev.openweather.data.settings.WindDirectionSettings
import fr.qgdev.openweather.repositories.FormattingService.Conversion
import fr.qgdev.openweather.ui.components.dialogs.AboutAppDialog
import fr.qgdev.openweather.ui.icons.VisibilityOff
import fr.qgdev.openweather.ui.icons.VisibilityOn
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.PlexMono
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * The settings, as the redesign's four categories.
 *
 * Every unit choice shows its result: the same value, converted, under each option - nobody should
 * have to know what an inHg is to pick one. The value is the first place's current observation, so
 * it is a number the user has already seen; without a place, a reference value stands in.
 *
 * Surfaces and selections follow `MaterialTheme.colorScheme`, so dynamic colour still reaches this
 * screen; only the state colours (a valid or incomplete key, an exceeded quota) come from the
 * weather palette, since they carry meaning.
 */
@Composable
fun SettingsScreenView(
    settingsRepository: SettingsRepository,
    places: List<Place>? = null
) {
    val sample = remember(places) { UnitSample.from(places) }
    val placeCount = places?.size ?: 0

    AppTheme {
        val palette = LocalWeatherPalette.current

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.screen)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_settings),
                    color = palette.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp)
                )

                // API Settings
                SettingsCategory(title = stringResource(R.string.title_settings_api)) {
                    ApiKeySetting(
                        modifier = Modifier.padding(16.dp),
                        defaultValue = settingsRepository.getApiKey() ?: "",
                        onValueChanged = { settingsRepository.setApiKey(it) }
                    )
                }

                // Time Settings
                SettingsCategory(title = stringResource(R.string.title_settings_time)) {
                    val sampleHour = remember {
                        Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 14)
                            set(Calendar.MINUTE, 0)
                        }.time
                    }
                    SegmentedSetting(
                        title = stringResource(R.string.title_settings_hour_format),
                        entries = listOf(
                            SettingOption(
                                TimeSettings.TWENTY_FOUR_HOURS,
                                stringResource(R.string.title_settings_time_format_24h) + " · " + formatHour(sampleHour, "HH:mm")
                            ),
                            SettingOption(
                                TimeSettings.TWELVE_HOURS,
                                stringResource(R.string.title_settings_time_format_12h) + " · " + formatHour(sampleHour, "hh:mm a")
                            )
                        ),
                        defaultValue = settingsRepository.getTimeSetting(),
                        onSelectionChanged = { settingsRepository.setTimeSetting(it) }
                    )
                }

                // Units Settings
                SettingsCategory(title = stringResource(R.string.title_settings_units)) {
                    SegmentedSetting(
                        title = stringResource(R.string.title_settings_units_temperature),
                        entries = listOf(
                            SettingOption(TemperatureSettings.CELSIUS, "°C", sample.celsius),
                            SettingOption(TemperatureSettings.FAHRENHEIT, "°F", sample.fahrenheit),
                            SettingOption(TemperatureSettings.KELVIN, "K", sample.kelvin)
                        ),
                        defaultValue = settingsRepository.getTemperatureSetting(),
                        onSelectionChanged = { settingsRepository.setTemperatureSetting(it) }
                    )
                    SettingsDivider()
                    SegmentedSetting(
                        title = stringResource(R.string.title_settings_units_measure),
                        summary = stringResource(R.string.summary_settings_units_measure),
                        entries = listOf(
                            SettingOption(
                                MeasureSettings.METRIC,
                                stringResource(R.string.title_settings_units_measure_metric),
                                stringResource(R.string.units_measure_metric)
                            ),
                            SettingOption(
                                MeasureSettings.IMPERIAL,
                                stringResource(R.string.title_settings_units_measure_imperial),
                                stringResource(R.string.units_measure_imperial)
                            )
                        ),
                        defaultValue = settingsRepository.getMeasureSetting(),
                        onSelectionChanged = { settingsRepository.setMeasureSetting(it) }
                    )
                    SettingsDivider()
                    ChipSetting(
                        title = stringResource(R.string.title_settings_units_pressure),
                        entries = listOf(
                            SettingOption(PressureSettings.HECTOPASCAL, "hPa"),
                            SettingOption(PressureSettings.BAROMETRIC, "mBar"),
                            SettingOption(PressureSettings.POUNDS_SQUARE_INCH, "psi"),
                            SettingOption(PressureSettings.INCH_MERCURY, "inHg")
                        ),
                        //  Four units do not fit four sub-lines, so the conversions share one line.
                        footer = sample.pressures,
                        defaultValue = settingsRepository.getPressureSetting(),
                        onSelectionChanged = { settingsRepository.setPressureSetting(it) }
                    )
                    SettingsDivider()
                    val context = LocalContext.current
                    SegmentedSetting(
                        title = stringResource(R.string.title_settings_units_direction),
                        entries = listOf(
                            SettingOption(
                                WindDirectionSettings.CARDINAL_POINTS,
                                stringResource(R.string.title_settings_units_direction_cardinal),
                                Conversion.Direction.toCardinal(context, sample.windDirection)
                            ),
                            SettingOption(
                                WindDirectionSettings.ANGULAR,
                                stringResource(R.string.title_settings_units_direction_degrees),
                                Conversion.Direction.toDegrees(sample.windDirection)
                            )
                        ),
                        defaultValue = settingsRepository.getWindDirectionSetting(),
                        onSelectionChanged = { settingsRepository.setWindDirectionSetting(it) }
                    )
                }

                // Update Settings
                SettingsCategory(title = stringResource(R.string.title_settings_update)) {
                    var periodicEnabled by remember {
                        mutableStateOf(settingsRepository.isPeriodicUpdateEnabled())
                    }
                    var period by remember { mutableStateOf(settingsRepository.getUpdatePeriodSetting()) }

                    SwitchSetting(
                        title = stringResource(R.string.title_settings_update_periodic),
                        summaryOn = stringResource(R.string.summary_enabled_settings_update_periodic),
                        summaryOff = stringResource(R.string.summary_disabled_settings_update_periodic),
                        defaultValue = periodicEnabled,
                        onValueChanged = {
                            periodicEnabled = it
                            settingsRepository.setPeriodicUpdateEnabled(it)
                        }
                    )

                    if (periodicEnabled) {
                        SettingsDivider()

                        //  One minute is a debugging aid: it burns through the free API quota in an
                        //  afternoon, so it is offered only in debug builds.
                        val debugEntries =
                            if (BuildConfig.DEBUG) listOf(
                                SettingOption(UpdatePeriodSettings.ONE_MINUTE, stringResource(R.string.title_settings_update_period_short_1min))
                            )
                            else emptyList()

                        ChipSetting(
                            title = stringResource(R.string.title_settings_update_period),
                            entries = debugEntries + listOf(
                                SettingOption(UpdatePeriodSettings.FIVE_MINUTES, stringResource(R.string.title_settings_update_period_short_5min)),
                                SettingOption(UpdatePeriodSettings.TEN_MINUTES, stringResource(R.string.title_settings_update_period_short_10min)),
                                SettingOption(UpdatePeriodSettings.FIFTEEN_MINUTES, stringResource(R.string.title_settings_update_period_short_15min)),
                                SettingOption(UpdatePeriodSettings.THIRTY_MINUTES, stringResource(R.string.title_settings_update_period_short_30min)),
                                SettingOption(UpdatePeriodSettings.ONE_HOUR, stringResource(R.string.title_settings_update_period_short_1h)),
                                SettingOption(UpdatePeriodSettings.TWO_HOURS, stringResource(R.string.title_settings_update_period_short_2h)),
                                SettingOption(UpdatePeriodSettings.THREE_HOURS, stringResource(R.string.title_settings_update_period_short_3h)),
                                SettingOption(UpdatePeriodSettings.SIX_HOURS, stringResource(R.string.title_settings_update_period_short_6h)),
                                SettingOption(UpdatePeriodSettings.TWELVE_HOURS, stringResource(R.string.title_settings_update_period_short_12h))
                            ),
                            footer = if (placeCount > 0) callBudgetText(placeCount, period) else null,
                            defaultValue = period,
                            onSelectionChanged = {
                                period = it
                                settingsRepository.setUpdatePeriodSetting(it)
                            }
                        )
                    }
                }

                AboutAppEntry()

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenViewPreview() {
    SettingsScreenView(
        settingsRepository = SettingsRepository.getInstance(LocalContext.current),
    )
}

// region Call budget

/**
 * Requests a single place costs per refresh: the One Call weather data, then its air quality.
 * See WeatherService.
 */
private const val CALLS_PER_PLACE = 2

/** Daily calls OpenWeatherMap's free One Call plan allows. */
private const val FREE_PLAN_DAILY_CALLS = 1_000

private const val DAY_MILLIS = 24 * 60 * 60 * 1_000L

/**
 * What a background interval costs per day, for this many places.
 *
 * Only the periodic work is counted: refreshes on opening the app come on top, but depend on the
 * user rather than the setting, and the setting is what this number has to make decidable.
 */
private fun dailyCalls(placeCount: Int, period: UpdatePeriodSettings): Int =
    (placeCount * CALLS_PER_PLACE * (DAY_MILLIS / period.durationMillis)).toInt()

/**
 * "7 lieux · 2 appels par lieu, environ 56 par jour." The free plan's limit is only named, on a line
 * of its own, once it is exceeded: below it, it was a figure to read for nothing.
 */
@Composable
private fun callBudgetText(placeCount: Int, period: UpdatePeriodSettings): String {
    val calls = dailyCalls(placeCount, period)
    val budget = pluralStringResource(R.plurals.settings_place_count, placeCount, placeCount) + " · " +
            stringResource(R.string.settings_call_budget, CALLS_PER_PLACE, formatCount(calls))
    return if (calls > FREE_PLAN_DAILY_CALLS) {
        budget + "\n" + stringResource(R.string.settings_free_plan_exceeded, formatCount(FREE_PLAN_DAILY_CALLS))
    } else {
        budget
    }
}

/**
 * A count with the locale's digit grouping, never split across lines: French groups with a narrow
 * space, which the line breaker otherwise treats as a break opportunity ("1" / "000").
 */
private fun formatCount(count: Int): String =
    String.format(Locale.getDefault(), "%,d", count)
        .replace('\u202F', '\u00A0')
        .replace(' ', '\u00A0')

// endregion

// region Unit samples

/**
 * One observation, converted into every unit the settings offer.
 *
 * Converted with [Conversion] directly rather than through the FormattingService, which only
 * knows the unit currently selected.
 */
private class UnitSample(
    temperature: Float,
    pressure: Float,
    val windDirection: Int
) {
    private val locale = Locale.getDefault()

    val celsius = String.format(locale, "%.1f", Conversion.Temperature.toCelsius(temperature))
    val fahrenheit = String.format(locale, "%.1f", Conversion.Temperature.toFahrenheit(temperature))
    val kelvin = String.format(locale, "%.1f", Conversion.Temperature.toKelvin(temperature))

    val pressures = listOf(
        String.format(locale, "%.0f hPa", Conversion.Pressure.toHpa(pressure)),
        String.format(locale, "%.0f mBar", Conversion.Pressure.toMbar(pressure)),
        String.format(locale, "%.2f psi", Conversion.Pressure.toPsi(pressure)),
        String.format(locale, "%.2f inHg", Conversion.Pressure.toInhg(pressure))
    ).joinToString(" · ")

    companion object {
        //  A mild, ordinary day: stands in until a place has been downloaded.
        private const val REFERENCE_TEMPERATURE = 287.05f
        private const val REFERENCE_PRESSURE = 1025f
        private const val REFERENCE_WIND_DIRECTION = 225

        fun from(places: List<Place>?): UnitSample {
            //  A place still being added has no observation yet, and its zeros would read as 0 K.
            val current = places?.firstOrNull { it.currentWeather.dt != 0L }?.currentWeather
                ?: return UnitSample(REFERENCE_TEMPERATURE, REFERENCE_PRESSURE, REFERENCE_WIND_DIRECTION)
            return UnitSample(current.temperature, current.pressure.toFloat(), current.windDirection)
        }
    }
}

private fun formatHour(date: Date, pattern: String): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(date)

// endregion

// region Components

private class SettingOption<T>(
    val value: T,
    val label: String,
    val detail: String? = null
)

/** A category: its small capitals label, then its settings grouped in one outlined card. */
@Composable
fun SettingsCategory(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    val palette = LocalWeatherPalette.current

    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            color = palette.textQuiet,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.05.sp,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 9.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(color = LocalWeatherPalette.current.outline)
}

@Composable
private fun SettingTitle(title: String, summary: String? = null) {
    val palette = LocalWeatherPalette.current
    Text(text = title, color = palette.textPrimary, fontSize = 13.5.sp)
    if (summary != null) {
        Text(
            text = summary,
            color = palette.textQuiet,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}

/**
 * The key, masked but for its last four characters, with how long it is and whether that is the
 * length OpenWeatherMap keys have.
 *
 * Still saved when the field loses focus, as before; the difference is that the save is now said.
 */
@Composable
fun ApiKeySetting(
    modifier: Modifier = Modifier,
    defaultValue: String = "",
    onValueChanged: (String) -> Unit
) {
    val palette = LocalWeatherPalette.current
    var apiKey by remember { mutableStateOf(defaultValue) }
    var savedKey by remember { mutableStateOf(defaultValue) }
    var savedAt by remember { mutableStateOf<Date?>(null) }
    var isObfuscated by remember { mutableStateOf(true) }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                SettingTitle(title = stringResource(R.string.title_settings_api_key))
            }
            if (apiKey.isNotEmpty()) {
                val isComplete = apiKey.length == API_KEY_LENGTH
                StatusPill(
                    text = stringResource(
                        if (isComplete) R.string.status_api_key_valid else R.string.status_api_key_incomplete
                    ),
                    color = if (isComplete) palette.green else palette.orange
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 11.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(13.dp))
                .border(1.dp, palette.outlineStrong, RoundedCornerShape(13.dp))
                .padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = apiKey,
                onValueChange = { apiKey = it.trim() },
                singleLine = true,
                textStyle = TextStyle(
                    color = palette.textMuted,
                    fontFamily = PlexMono,
                    fontSize = 13.sp,
                    letterSpacing = 1.56.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = if (isObfuscated) MaskAllButLast(4) else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && apiKey != savedKey) {
                            onValueChanged(apiKey)
                            savedKey = apiKey
                            savedAt = Date()
                        }
                    }
            )
            IconButton(onClick = { isObfuscated = !isObfuscated }) {
                Icon(
                    imageVector = if (isObfuscated) Icons.VisibilityOn else Icons.VisibilityOff,
                    contentDescription = stringResource(
                        if (isObfuscated) R.string.description_show_api_key else R.string.description_hide_api_key
                    ),
                    tint = palette.textQuiet
                )
            }
        }

        Row(modifier = Modifier.padding(top = 9.dp)) {
            Text(
                text = stringResource(R.string.settings_api_key_length, apiKey.length, API_KEY_LENGTH),
                color = palette.textQuiet,
                fontSize = 11.sp,
                fontFamily = PlexMono,
                modifier = Modifier.weight(1f)
            )
            savedAt?.let {
                Text(
                    text = stringResource(R.string.settings_api_key_saved_at, formatHour(it, "HH:mm")),
                    color = palette.textQuiet,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private const val API_KEY_LENGTH = 32

/** Shows the last [visible] characters and a bullet for each of the others. */
private class MaskAllButLast(private val visible: Int) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val hidden = (text.length - visible).coerceAtLeast(0)
        return TransformedText(
            AnnotatedString("•".repeat(hidden) + text.text.drop(hidden)),
            OffsetMapping.Identity
        )
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/** Two or three options side by side, each able to show what it produces underneath. */
@Composable
private fun <T> SegmentedSetting(
    title: String,
    entries: List<SettingOption<T>>,
    defaultValue: T,
    summary: String? = null,
    onSelectionChanged: (T) -> Unit
) {
    var selected by remember { mutableStateOf(defaultValue) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
        SettingTitle(title, summary)
        Row(
            modifier = Modifier.padding(top = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            entries.forEach { option ->
                OptionBox(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(11.dp),
                    selected = option.value == selected,
                    onClick = {
                        selected = option.value
                        onSelectionChanged(option.value)
                    }
                ) { contentColor, detailColor ->
                    Column(
                        modifier = Modifier.padding(9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = option.label,
                            color = contentColor,
                            fontSize = 12.5.sp,
                            fontWeight = if (option.value == selected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        if (option.detail != null) {
                            Text(
                                text = option.detail,
                                color = detailColor,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Options too many for one row, as wrapping pills. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipSetting(
    title: String,
    entries: List<SettingOption<T>>,
    defaultValue: T,
    footer: String? = null,
    onSelectionChanged: (T) -> Unit
) {
    val palette = LocalWeatherPalette.current
    var selected by remember { mutableStateOf(defaultValue) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
        SettingTitle(title)
        FlowRow(
            modifier = Modifier.padding(top = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            entries.forEach { option ->
                OptionBox(
                    shape = CircleShape,
                    selected = option.value == selected,
                    onClick = {
                        selected = option.value
                        onSelectionChanged(option.value)
                    }
                ) { contentColor, _ ->
                    Text(
                        text = option.label,
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = if (option.value == selected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
                    )
                }
            }
        }
        if (footer != null) {
            Text(
                text = footer,
                color = palette.textQuiet,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 11.dp)
            )
        }
    }
}

/** An option: filled with the Material primary colour when selected, outlined otherwise. */
@Composable
private fun OptionBox(
    shape: Shape,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (contentColor: Color, detailColor: Color) -> Unit
) {
    val palette = LocalWeatherPalette.current
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (selected) Modifier.background(colorScheme.primary)
                else Modifier.border(1.dp, palette.outlineStrong, shape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) content(colorScheme.onPrimary, colorScheme.onPrimary.copy(alpha = 0.65f))
        else content(palette.textPrimary, palette.textQuiet)
    }
}

@Composable
fun SwitchSetting(
    modifier: Modifier = Modifier,
    title: String,
    summaryOn: String,
    summaryOff: String,
    defaultValue: Boolean = false,
    onValueChanged: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(defaultValue) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                isChecked = !isChecked
                onValueChanged(isChecked)
            }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SettingTitle(title, if (isChecked) summaryOn else summaryOff)
        }
        Switch(
            modifier = Modifier.padding(start = 14.dp),
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onValueChanged(isChecked)
            }
        )
    }
}

/** A row at the end of the settings, in place of the floating button, that opens About. */
@Composable
private fun AboutAppEntry() {
    val palette = LocalWeatherPalette.current
    var isAboutAppDialogOpened by remember { mutableStateOf(false) }
    var debugMode by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .clickable {
                isAboutAppDialogOpened = true
                debugMode = false
            }
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(R.string.about_us), color = palette.textPrimary, fontSize = 13.5.sp)
            Text(
                text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                color = palette.textQuiet,
                fontSize = 11.sp,
                fontFamily = PlexMono,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Text(text = "›", color = palette.textQuiet, fontSize = 15.sp)
    }

    if (isAboutAppDialogOpened) {
        AboutAppDialog(
            onDismissRequest = { isAboutAppDialogOpened = false },
            debugMode = debugMode,
            onDebugModeToggle = { debugMode = it }
        )
    }
}

// endregion

private enum class TestEnum { OPTION_1, OPTION_2, OPTION_3 }

@Preview(showBackground = true)
@Composable
fun SettingsCategoryPreview() {
    AppTheme {
        SettingsCategory(title = "Title") {
            ApiKeySetting(
                modifier = Modifier.padding(16.dp),
                defaultValue = "0123456789abcdef0123456789ab4f7a",
                onValueChanged = {}
            )
            SettingsDivider()
            SegmentedSetting(
                title = "Title",
                entries = listOf(
                    SettingOption(TestEnum.OPTION_1, "Option 1", "13,9"),
                    SettingOption(TestEnum.OPTION_2, "Option 2", "57,0"),
                    SettingOption(TestEnum.OPTION_3, "Option 3", "287,1")
                ),
                defaultValue = TestEnum.OPTION_1,
                onSelectionChanged = {}
            )
            SettingsDivider()
            SwitchSetting(
                title = "Title",
                summaryOn = "Summary On",
                summaryOff = "Summary Off",
                onValueChanged = {}
            )
        }
    }
}
