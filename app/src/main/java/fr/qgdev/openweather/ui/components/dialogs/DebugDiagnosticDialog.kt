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

package fr.qgdev.openweather.ui.components.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.PlexMono
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import kotlin.coroutines.cancellation.CancellationException
import fr.qgdev.openweather.data.models.AirQuality
import fr.qgdev.openweather.data.models.CurrentWeather
import fr.qgdev.openweather.data.models.DailyForecast
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.HourlyForecast
import fr.qgdev.openweather.data.models.MinutelyForecast
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.Properties
import fr.qgdev.openweather.data.models.WeatherAlert
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/*
 * A browsable view of everything the app has stored, rather than a summary.
 *
 * The tree is built from the protobuf schema field by field: every field of every message is
 * rendered, including ones left at their proto3 default, which is exactly what you need when the
 * question is "why is this value zero". protobuf-javalite strips descriptors, so the field lists
 * below are written out by hand - if a .proto gains a field, add it here too. Each message also
 * carries a raw node showing the javalite toString(), which is a useful cross-check but only
 * prints non-default fields.
 *
 * Children are produced lazily so expanding a place does not build 48 hourly entries until asked.
 */

// region Tree model

private sealed interface DebugNode {
    val id: String
    val label: String
}

private data class DebugField(
    override val id: String,
    override val label: String,
    val value: String
) : DebugNode

private data class DebugGroup(
    override val id: String,
    override val label: String,
    val badge: String? = null,
    val children: () -> List<DebugNode>
) : DebugNode

private data class FlatRow(val depth: Int, val node: DebugNode, val path: String)

/** Walks the visible part of the tree into a flat list the LazyColumn can render. */
private fun flatten(
    nodes: List<DebugNode>,
    expanded: Set<String>,
    depth: Int = 0,
    path: String = "",
    out: MutableList<FlatRow> = mutableListOf()
): MutableList<FlatRow> {
    nodes.forEach { node ->
        out.add(FlatRow(depth, node, path))
        if (node is DebugGroup && node.id in expanded) {
            val childPath = if (path.isEmpty()) node.label else "$path › ${node.label}"
            flatten(node.children(), expanded, depth + 1, childPath, out)
        }
    }
    return out
}

/** Walks the whole tree, expanded or not, collecting fields whose label or value matches. */
private fun search(
    nodes: List<DebugNode>,
    query: String,
    path: String = "",
    out: MutableList<FlatRow> = mutableListOf()
): MutableList<FlatRow> {
    nodes.forEach { node ->
        when (node) {
            is DebugField ->
                if (node.label.contains(query, true) || node.value.contains(query, true)) {
                    out.add(FlatRow(0, node, path))
                }

            is DebugGroup -> {
                val childPath = if (path.isEmpty()) node.label else "$path › ${node.label}"
                search(node.children(), query, childPath, out)
            }
        }
    }
    return out
}

/** Renders the whole tree as indented text, for the copy-to-clipboard action. */
private fun asText(nodes: List<DebugNode>, depth: Int = 0, sb: StringBuilder = StringBuilder()): String {
    val pad = "  ".repeat(depth)
    nodes.forEach { node ->
        when (node) {
            is DebugField -> sb.append(pad).append(node.label).append(": ").append(node.value).append('\n')
            is DebugGroup -> {
                sb.append(pad).append(node.label)
                node.badge?.let { sb.append(" [").append(it).append(']') }
                sb.append('\n')
                asText(node.children(), depth + 1, sb)
            }
        }
    }
    return sb.toString()
}

// endregion

// region Value formatting

/** Unique work name enqueued by WidgetsManager. */
private const val PERIODIC_WORK_NAME = "PeriodicUpdaterWorker"

private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    .apply { timeZone = TimeZone.getTimeZone("UTC") }

/** Epoch milliseconds shown raw and decoded, because "0" and "unset" are different problems. */
private fun ts(value: Long): String =
    if (value <= 0L) "$value  (unset)"
    else "$value  (${timestampFormat.format(Date(value))} UTC)"

private fun field(id: String, label: String, value: Any?) = DebugField(id, label, value.toString())

// endregion

// region Per-message field lists - one entry per field in the .proto

private fun geolocationNodes(id: String, g: Geolocation): List<DebugNode> = listOf(
    field("$id.city", "city", g.city),
    field("$id.countryCode", "countryCode", g.countryCode),
    field("$id.latitude", "coordinates.latitude", g.coordinates.latitude),
    field("$id.longitude", "coordinates.longitude", g.coordinates.longitude),
    field("$id.hasCoordinates", "(hasCoordinates)", g.hasCoordinates())
)

private fun propertiesNodes(id: String, p: Properties): List<DebugNode> = listOf(
    DebugField("$id.lswut", "lastSuccessfulWeatherUpdateTime", ts(p.lastSuccessfulWeatherUpdateTime)),
    DebugField("$id.lsaqut", "lastSuccessfulAirQualityUpdateTime", ts(p.lastSuccessfulAirQualityUpdateTime)),
    DebugField("$id.lwuat", "lastWeatherUpdateAttemptTime", ts(p.lastWeatherUpdateAttemptTime)),
    DebugField("$id.laqiat", "lastAirQualityUpdateAttemptTime", ts(p.lastAirQualityUpdateAttemptTime)),
    DebugField("$id.ct", "creationTime", ts(p.creationTime)),
    DebugField("$id.lawdt", "lastAvailableWeatherDataTime", ts(p.lastAvailableWeatherDataTime)),
    DebugField("$id.laaqdt", "lastAvailableAirQualityDataTime", ts(p.lastAvailableAirQualityDataTime)),
    field("$id.tz", "timeOffset", "${p.timeOffset}  (never populated by PlaceDataMapper)")
)

private fun currentWeatherNodes(id: String, w: CurrentWeather): List<DebugNode> = listOf(
    DebugField("$id.dt", "dt", ts(w.dt)),
    field("$id.weather", "weather", w.weather),
    field("$id.weatherDescription", "weatherDescription", w.weatherDescription),
    field("$id.weatherCode", "weatherCode", w.weatherCode),
    field("$id.temperature", "temperature", w.temperature),
    field("$id.temperatureFeelsLike", "temperatureFeelsLike", w.temperatureFeelsLike),
    field("$id.pressure", "pressure", w.pressure),
    field("$id.humidity", "humidity", w.humidity),
    field("$id.dewPoint", "dewPoint", w.dewPoint),
    field("$id.cloudiness", "cloudiness", w.cloudiness),
    field("$id.uvIndex", "uvIndex", w.uvIndex),
    field("$id.visibility", "visibility", w.visibility),
    DebugField("$id.sunrise", "sunrise", ts(w.sunrise)),
    DebugField("$id.sunset", "sunset", ts(w.sunset)),
    field("$id.windSpeed", "windSpeed", w.windSpeed),
    field("$id.windGustSpeed", "windGustSpeed", w.windGustSpeed),
    field("$id.isWindDirectionReadable", "isWindDirectionReadable", w.isWindDirectionReadable),
    field("$id.windDirection", "windDirection", w.windDirection),
    field("$id.rain", "rain", w.rain),
    field("$id.snow", "snow", w.snow)
)

private fun airQualityNodes(id: String, a: AirQuality): List<DebugNode> = listOf(
    field("$id.aqi", "aqi", a.aqi),
    field("$id.co", "co", a.co),
    field("$id.no", "no", a.no),
    field("$id.no2", "no2", a.no2),
    field("$id.o3", "o3", a.o3),
    field("$id.so2", "so2", a.so2),
    field("$id.pm25", "pm2_5", a.pm25),
    field("$id.pm10", "pm10", a.pm10),
    field("$id.nh3", "nh3", a.nh3)
)

private fun minutelyNodes(id: String, m: MinutelyForecast): List<DebugNode> = listOf(
    DebugField("$id.dt", "dt", ts(m.dt)),
    field("$id.precipitation", "precipitation", m.precipitation)
)

private fun hourlyNodes(id: String, h: HourlyForecast): List<DebugNode> = listOf(
    DebugField("$id.dt", "dt", ts(h.dt)),
    field("$id.weather", "weather", h.weather),
    field("$id.weatherDescription", "weatherDescription", h.weatherDescription),
    field("$id.weatherCode", "weatherCode", h.weatherCode),
    field("$id.temperature", "temperature", h.temperature),
    field("$id.temperatureFeelsLike", "temperatureFeelsLike", h.temperatureFeelsLike),
    field("$id.pressure", "pressure", h.pressure),
    field("$id.humidity", "humidity", h.humidity),
    field("$id.dewPoint", "dewPoint", h.dewPoint),
    field("$id.cloudiness", "cloudiness", h.cloudiness),
    field("$id.visibility", "visibility", h.visibility),
    field("$id.uvIndex", "uvIndex", h.uvIndex),
    field("$id.windSpeed", "windSpeed", h.windSpeed),
    field("$id.windGustSpeed", "windGustSpeed", h.windGustSpeed),
    field("$id.windDirection", "windDirection", h.windDirection),
    field("$id.pop", "pop", h.pop),
    field("$id.rain", "rain", h.rain),
    field("$id.snow", "snow", h.snow)
)

private fun dailyNodes(id: String, d: DailyForecast): List<DebugNode> = listOf(
    DebugField("$id.dt", "dt", ts(d.dt)),
    field("$id.weather", "weather", d.weather),
    field("$id.weatherDescription", "weatherDescription", d.weatherDescription),
    field("$id.weatherCode", "weatherCode", d.weatherCode),
    field("$id.tMorning", "temperatureMorning", d.temperatureMorning),
    field("$id.tDay", "temperatureDay", d.temperatureDay),
    field("$id.tEvening", "temperatureEvening", d.temperatureEvening),
    field("$id.tNight", "temperatureNight", d.temperatureNight),
    field("$id.tMin", "temperatureMinimum", d.temperatureMinimum),
    field("$id.tMax", "temperatureMaximum", d.temperatureMaximum),
    field("$id.tMornFL", "temperatureMorningFeelsLike", d.temperatureMorningFeelsLike),
    field("$id.tDayFL", "temperatureDayFeelsLike", d.temperatureDayFeelsLike),
    field("$id.tEveFL", "temperatureEveningFeelsLike", d.temperatureEveningFeelsLike),
    field("$id.tNightFL", "temperatureNightFeelsLike", d.temperatureNightFeelsLike),
    field("$id.pressure", "pressure", d.pressure),
    field("$id.humidity", "humidity", d.humidity),
    field("$id.dewPoint", "dewPoint", d.dewPoint),
    field("$id.cloudiness", "cloudiness", d.cloudiness),
    DebugField("$id.sunriseDt", "sunriseDt", ts(d.sunriseDt)),
    DebugField("$id.sunsetDt", "sunsetDt", ts(d.sunsetDt)),
    field("$id.uvIndex", "uvIndex", d.uvIndex),
    DebugField("$id.moonriseDt", "moonriseDt", ts(d.moonriseDt)),
    DebugField("$id.moonsetDt", "moonsetDt", ts(d.moonsetDt)),
    field("$id.moonPhase", "moonPhase", d.moonPhase),
    field("$id.windSpeed", "windSpeed", d.windSpeed),
    field("$id.windGustSpeed", "windGustSpeed", d.windGustSpeed),
    field("$id.windDirection", "windDirection", d.windDirection),
    field("$id.pop", "pop", d.pop),
    field("$id.rain", "rain", d.rain),
    field("$id.snow", "snow", d.snow)
)

private fun alertNodes(id: String, a: WeatherAlert): List<DebugNode> = listOf(
    field("$id.sender", "sender", a.sender),
    field("$id.event", "event", a.event),
    DebugField("$id.startDt", "startDt", ts(a.startDt)),
    DebugField("$id.endDt", "endDt", ts(a.endDt)),
    field("$id.tags", "tags", a.tagsList.joinToString(", ").ifEmpty { "(none)" }),
    field("$id.description", "description", a.description)
)

private fun placeNodes(id: String, place: Place): List<DebugNode> = buildList {
    add(DebugGroup("$id.geo", "Geolocation") { geolocationNodes("$id.geo", place.geolocation) })
    add(DebugGroup("$id.props", "Properties") { propertiesNodes("$id.props", place.properties) })
    add(
        DebugGroup(
            "$id.cw", "Current weather",
            badge = if (place.hasCurrentWeather()) null else "absent"
        ) { currentWeatherNodes("$id.cw", place.currentWeather) }
    )
    add(
        DebugGroup(
            "$id.aq", "Air quality",
            badge = if (place.hasAirQuality()) null else "absent"
        ) { airQualityNodes("$id.aq", place.airQuality) }
    )
    add(
        DebugGroup("$id.min", "Minutely forecast", "${place.minutelyForecastListCount}") {
            place.minutelyForecastListList.mapIndexed { i, m ->
                DebugGroup("$id.min.$i", "[$i]  ${timestampFormat.format(Date(m.dt))}") {
                    minutelyNodes("$id.min.$i", m)
                }
            }
        }
    )
    add(
        DebugGroup("$id.hr", "Hourly forecast", "${place.hourlyForecastListCount}") {
            place.hourlyForecastListList.mapIndexed { i, h ->
                DebugGroup("$id.hr.$i", "[$i]  ${timestampFormat.format(Date(h.dt))}  ${h.weather}") {
                    hourlyNodes("$id.hr.$i", h)
                }
            }
        }
    )
    add(
        DebugGroup("$id.day", "Daily forecast", "${place.dailyForecastListCount}") {
            place.dailyForecastListList.mapIndexed { i, d ->
                DebugGroup("$id.day.$i", "[$i]  ${timestampFormat.format(Date(d.dt))}  ${d.weather}") {
                    dailyNodes("$id.day.$i", d)
                }
            }
        }
    )
    add(
        DebugGroup("$id.alerts", "Weather alerts", "${place.weatherAlertsListCount}") {
            place.weatherAlertsListList.mapIndexed { i, a ->
                DebugGroup("$id.alerts.$i", "[$i]  ${a.event}") { alertNodes("$id.alerts.$i", a) }
            }
        }
    )
    add(
        DebugGroup("$id.raw", "Raw protobuf") {
            listOf(
                DebugField(
                    "$id.raw.text",
                    "toString()",
                    "javalite omits fields left at their default, so this is a cross-check " +
                            "rather than the full picture:\n\n${place}"
                )
            )
        }
    )
}

// endregion

@Composable
fun DebugDiagnosticDialog(
    onDismissRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository.getInstance(context) }
    val placeRepository = remember { PlaceRepository.getInstance(context) }

    val settings by settingsRepository.settingsFlow.collectAsState()
    val places by placeRepository.placesFlow.collectAsState(initial = emptyList())

    var expanded by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }

    //  The previous version of this dialog printed a fixed string telling you to go and check adb.
    //  This reads the actual WorkInfo for the unique work WidgetsManager enqueues.
    var workRows by remember { mutableStateOf(listOf<DebugNode>()) }
    LaunchedEffect(Unit) {
        //  Not runCatching: that would also swallow the CancellationException thrown when the
        //  dialog is dismissed, and report closing the dialog as a WorkManager error.
        try {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(PERIODIC_WORK_NAME)
                .collect { infos ->
                    workRows = if (infos.isEmpty()) {
                        listOf(field("work.none", "state", "no work enqueued under $PERIODIC_WORK_NAME"))
                    } else {
                        infos.flatMapIndexed { i, info ->
                            listOf(
                                field("work.$i.id", "[$i] id", info.id),
                                field("work.$i.state", "[$i] state", info.state),
                                field("work.$i.runAttempt", "[$i] runAttemptCount", info.runAttemptCount),
                                field("work.$i.tags", "[$i] tags", info.tags.joinToString(", ")),
                                field(
                                    "work.$i.nextSchedule", "[$i] nextScheduleTimeMillis",
                                    ts(info.nextScheduleTimeMillis)
                                ),
                                field("work.$i.stopReason", "[$i] stopReason", info.stopReason)
                            )
                        }
                    }
                }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            workRows = listOf(
                field("work.error", "error", error.message ?: error::class.java.simpleName)
            )
        }
    }

    val tree: List<DebugNode> = buildList {
        add(
            DebugGroup("settings", "Settings") {
                val key = settings.apiKey
                listOf(
                    field("settings.temperature", "temperatureUnit", settings.temperatureUnit),
                    field("settings.measure", "measureUnit", settings.measureUnit),
                    field("settings.pressure", "pressureUnit", settings.pressureUnit),
                    field("settings.direction", "windDirectionUnit", settings.windDirectionUnit),
                    field("settings.time", "timeFormat", settings.timeFormat),
                    field("settings.locale", "defaultLocale", settings.defaultLocale),
                    field(
                        "settings.apiKey", "apiKey",
                        when {
                            key.isNullOrEmpty() -> "(not set)"
                            key.length > 4 -> "*".repeat(key.length - 4) + key.takeLast(4) +
                                    "  (${key.length} chars)"

                            else -> "*".repeat(key.length) + "  (${key.length} chars)"
                        }
                    ),
                    field("settings.periodic", "periodicUpdateEnabled", settings.periodicUpdateEnabled),
                    field("settings.period", "updatePeriod", settings.updatePeriod)
                )
            }
        )
        add(DebugGroup("work", "Background work") { workRows })
        add(
            DebugGroup("storage", "Storage", "${places.size}") {
                listOf(
                    field("storage.count", "places", places.size),
                    field(
                        "storage.dataPoints", "total forecast entries",
                        places.sumOf {
                            it.minutelyForecastListCount + it.hourlyForecastListCount +
                                    it.dailyForecastListCount + it.weatherAlertsListCount
                        }
                    ),
                    field("storage.file", "datastore file", "placeStorage.pb")
                )
            }
        )
        places.forEachIndexed { index, place ->
            val city = place.geolocation.city.ifEmpty { "?" }
            val country = place.geolocation.countryCode.ifEmpty { "?" }
            add(DebugGroup("place.$index", "$city, $country") { placeNodes("place.$index", place) })
        }
    }

    val rows = remember(tree, expanded, query) {
        if (query.isBlank()) flatten(tree, expanded) else search(tree, query.trim())
    }

    val palette = LocalWeatherPalette.current

    FullScreenDialog(title = "Stored data", onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "development tool",
                color = palette.textQuiet,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            //  The filter field, drawn like the redesign's other inputs rather than as a Material
            //  outlined field: a dev tool still sits inside the app.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, palette.outlineStrong, RoundedCornerShape(14.dp))
                    .padding(start = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = palette.textQuiet,
                    modifier = Modifier.size(16.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    if (query.isEmpty()) {
                        Text("Filter fields", color = palette.textQuiet, fontSize = 12.5.sp, fontFamily = PlexMono)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 12.5.sp,
                            fontFamily = PlexMono
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear filter", tint = palette.textQuiet)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 11.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                //  One level: expanding everything would materialise every hourly entry.
                ToolChip("Expand") { expanded = tree.filterIsInstance<DebugGroup>().map { it.id }.toSet() }
                ToolChip("Collapse all") { expanded = emptySet() }
                ToolChip("Copy") { copyToClipboard(context, asText(tree)) }
                Text(
                    //  Without a count, an empty-looking filter cannot be told from a slow one.
                    text = if (query.isBlank()) "" else "${rows.size} match" + if (rows.size == 1) "" else "es",
                    color = palette.textQuiet,
                    fontSize = 10.5.sp,
                    fontFamily = PlexMono,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            if (rows.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (query.isBlank()) "No data stored" else "No field matches \"$query\"",
                        fontSize = 12.5.sp,
                        color = palette.textQuiet
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(rows, key = { "${it.path}|${it.node.id}" }) { row ->
                        when (val node = row.node) {
                            is DebugGroup -> GroupRow(
                                depth = row.depth,
                                label = node.label,
                                badge = node.badge,
                                isExpanded = node.id in expanded,
                                onToggle = {
                                    expanded = if (node.id in expanded) expanded - node.id
                                    else expanded + node.id
                                }
                            )

                            is DebugField -> FieldRow(
                                depth = row.depth,
                                label = node.label,
                                value = node.value,
                                path = if (query.isBlank()) null else row.path,
                                query = query.trim()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolChip(text: String, onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current
    Text(
        text = text,
        color = palette.textPrimary,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, palette.outlineStrong, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 5.dp)
    )
}

/** The rail a nested row hangs from, one per level, so depth reads without counting indents. */
private fun Modifier.depthRails(depth: Int, color: Color): Modifier = drawBehind {
    for (level in 0 until depth) {
        val x = (4 + level * 21).dp.toPx()
        drawLine(color, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
    }
}

@Composable
private fun GroupRow(
    depth: Int,
    label: String,
    badge: String?,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val palette = LocalWeatherPalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .depthRails(depth, palette.outline)
            .clickable(onClick = onToggle)
            .padding(start = (depth * 21).dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isExpanded) "⌄" else "›",
            color = if (isExpanded) MaterialTheme.colorScheme.primary else palette.textQuiet,
            fontFamily = PlexMono,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(9.dp)
        )
        Text(
            text = label,
            color = when {
                isExpanded -> palette.textPrimary
                depth == 0 -> palette.textMuted
                else -> palette.textQuiet
            },
            fontFamily = PlexMono,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        badge?.let {
            Text(text = it, color = palette.textQuiet, fontFamily = PlexMono, fontSize = 12.sp)
        }
    }
}

/**
 * A stored value, raw: 287.05 K stays 287.05, not 13.9 °C - this inspects storage, not the UI.
 * While filtering, the matched text is highlighted, since on a deep tree the row alone does not
 * say why it was kept.
 */
@Composable
private fun FieldRow(depth: Int, label: String, value: String, path: String?, query: String) {
    val palette = LocalWeatherPalette.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .depthRails(depth, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            .padding(start = (depth * 21 + 17).dp, top = 7.dp, bottom = 7.dp)
    ) {
        path?.let {
            Text(
                text = it,
                color = palette.textQuiet,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SelectionContainer {
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = highlighted(label, query),
                    modifier = Modifier.weight(1f),
                    fontFamily = PlexMono,
                    fontSize = 11.5.sp,
                    color = palette.textQuiet
                )
                Text(
                    text = highlighted(value, query),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    fontFamily = PlexMono,
                    fontSize = 11.5.sp,
                    color = palette.textPrimary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/** [text] with every case-insensitive occurrence of [query] marked. */
@Composable
private fun highlighted(text: String, query: String): AnnotatedString {
    val palette = LocalWeatherPalette.current
    if (query.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text)
        var index = text.indexOf(query, ignoreCase = true)
        while (index >= 0) {
            addStyle(
                SpanStyle(background = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f), color = palette.textPrimary),
                index,
                index + query.length
            )
            index = text.indexOf(query, index + query.length, ignoreCase = true)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("OpenWeather debug data", text))
}
