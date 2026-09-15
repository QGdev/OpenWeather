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

import fr.qgdev.openweather.ui.place.detail.startsNewDay
import fr.qgdev.openweather.ui.theme.PlexMono
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.WeatherAlert
import fr.qgdev.openweather.repositories.FormattingService
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone

//  The event icon sets the text column for the whole alert: everything under the heading lines up
//  past the icon and its gap, so the inset is derived from them instead of being repeated by hand.

/**
 * Shows the weather alerts attached to a place.
 *
 * Replaces a View-based `Dialog` that inflated `dialog_weather_alert.xml` and filled it through a
 * `BaseAdapter`, so the alerts now sit on [FullScreenDialog] like every other dialog in the app.
 */
@Composable
fun WeatherAlertDialog(
    place: Place,
    formattingService: FormattingService,
    onDismissRequest: () -> Unit = {}
) {
    //  Alert times are reported for the place, not the reader, so they are rendered in the place's
    //  own zone.
    val timeZone: TimeZone = SimpleTimeZone(place.properties.timeOffset, "")
    val alerts = place.weatherAlertsListList

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_weather_alert),
        subtitle = stringResource(
            R.string.alert_subtitle,
            place.geolocation.city,
            pluralStringResource(R.plurals.status_active_alerts, alerts.size, alerts.size)
        ),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            alerts.forEach { alert ->
                WeatherAlertCard(
                    alert = alert,
                    cityName = place.geolocation.city,
                    formattingService = formattingService,
                    timeZone = timeZone
                )
            }
            LanguageNote()
        }
    }
}

/**
 * One alert as the mock-up draws it: a card tinted by its severity, when it applies before what it
 * says, then the agency's words.
 */
@Composable
private fun WeatherAlertCard(
    alert: WeatherAlert,
    cityName: String,
    formattingService: FormattingService,
    timeZone: TimeZone
) {
    val palette = LocalWeatherPalette.current
    val severity = severityOf(alert.event)
    val severityColor = when (severity) {
        AlertSeverity.RED -> palette.red
        AlertSeverity.ORANGE -> palette.orange
        AlertSeverity.YELLOW -> palette.amber
        AlertSeverity.UNKNOWN -> palette.textMuted
    }

    val now = System.currentTimeMillis()
    val ongoing = now in alert.startDt..alert.endDt
    val ended = now > alert.endDt

    var expanded by remember(alert.event, alert.startDt) { mutableStateOf(false) }
    //  Whether three lines cut the text: measured, since only the laid-out text knows.
    var truncated by remember(alert.event, alert.startDt) { mutableStateOf(false) }
    val shape = RoundedCornerShape(22.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    0f to severityColor.copy(alpha = 0.16f),
                    0.6f to severityColor.copy(alpha = 0.03f),
                    1f to severityColor.copy(alpha = 0f)
                )
            )
            .border(1.dp, severityColor.copy(alpha = 0.4f), shape)
            .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 14.dp)
    ) {
        //  When it applies, before what it says: two alerts follow one another far more often than
        //  they overlap, and "ongoing" or "from 22:00" is what decides whether to read on.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.danger),
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(22.dp)
            )
            val status = when {
                ended -> stringResource(R.string.alert_status_ended)
                ongoing -> stringResource(R.string.alert_status_ongoing)
                else -> stringResource(
                    R.string.alert_status_starting_at,
                    formattingService.getFormattedTime(Date(alert.startDt), timeZone)
                )
            }
            Text(
                text = listOfNotNull(severity.labelRes?.let { stringResource(it) }, status).joinToString(" · "),
                color = if (ended) palette.textQuiet else severityColor,
                fontSize = 9.5.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.95.sp
            )
        }

        Text(
            //  The title stays exactly as the agency wrote it: it is the official wording, and the
            //  severity above is only our reading of it.
            text = alert.event,
            color = palette.textPrimary,
            fontSize = 17.5.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 11.dp)
        )

        //  The categories the API already sends and the app never showed.
        if (alert.tagsCount > 0) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                alert.tagsList.forEach { tag ->
                    Text(
                        text = tag,
                        color = palette.textMuted,
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, palette.outlineStrong, CircleShape)
                            .padding(horizontal = 9.dp, vertical = 3.dp)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 14.dp), color = palette.outline)
        Text(
            text = alertPeriod(alert, formattingService, timeZone),
            color = palette.textPrimary,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = PlexMono,
            modifier = Modifier.padding(top = 13.dp)
        )
        //  Said explicitly, because these times are the place's and not the reader's - an Italian
        //  alert read from France is two hours out otherwise.
        Text(
            text = listOfNotNull(
                stringResource(R.string.alert_local_time, cityName),
                if (ongoing) stringResource(R.string.alert_remaining, formatDuration(alert.endDt - now)) else null
            ).joinToString(" · "),
            color = palette.textQuiet,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            modifier = Modifier.padding(top = 3.dp)
        )
        //  How far into the alert we are, while it runs.
        if (ongoing) {
            val elapsed = ((now - alert.startDt).toFloat() / (alert.endDt - alert.startDt).coerceAtLeast(1L))
                .coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(palette.outline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(elapsed)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(severityColor)
                )
            }
        }

        Text(
            text = alert.sender,
            color = palette.textQuiet,
            fontSize = 11.5.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 14.dp)
        )
        Text(
            text = alert.description.withClickableLinks(),
            color = palette.textSecondary,
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            //  Alert bodies repeat the same legal disclaimer at length; three lines is enough to see
            //  whether this one says anything else.
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout -> if (!expanded) truncated = layout.hasVisualOverflow },
            modifier = Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
        )
        //  Only when there is more to read: on a short text the button would open nothing.
        if (truncated || expanded) Text(
            text = stringResource(
                if (expanded) R.string.action_collapse_text else R.string.action_read_full_text
            ) + " ›",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable { expanded = !expanded }
        )
    }
}

/**
 * "jeu. 18/09 · 00:00 → 21:59" within a day, "jeu. 18/09 22:00 → ven. 19/09 12:00" across two:
 * the day is written once when both ends share it.
 */
@Composable
private fun alertPeriod(alert: WeatherAlert, formatting: FormattingService, timeZone: TimeZone): String {
    val start = Date(alert.startDt)
    val end = Date(alert.endDt)
    fun day(date: Date) = formatting.getFormattedShortDayName(date, timeZone) + " " +
            formatting.getFormattedDayMonth(date, timeZone)
    return if (!startsNewDay(alert.startDt, alert.endDt, timeZone)) {
        "${day(start)} · ${formatting.getFormattedTime(start, timeZone)} → ${formatting.getFormattedTime(end, timeZone)}"
    } else {
        "${day(start)} ${formatting.getFormattedTime(start, timeZone)} → ${day(end)} ${formatting.getFormattedTime(end, timeZone)}"
    }
}

/** The alerts are written by the issuing agency, in its language rather than the app's. */
@Composable
private fun LanguageNote() {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, palette.outline, shape)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(18.dp)
                .border(1.3.dp, palette.textQuiet, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "i", color = palette.textQuiet, fontSize = 10.sp, lineHeight = 10.sp, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = stringResource(R.string.alert_language_note),
            color = palette.textQuiet,
            fontSize = 11.5.sp,
            lineHeight = 17.sp
        )
    }
}

/** "2 h 53" while an alert runs, or "45 min" in its last hour. */
@Composable
private fun formatDuration(millis: Long): String {
    val totalMinutes = (millis / 60_000L).coerceAtLeast(0L)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.duration_hours_minutes, hours.toInt(), minutes.toInt())
    } else {
        stringResource(R.string.duration_minutes, minutes.toInt())
    }
}

private val URL_PATTERN = Regex("""https?://\S*[^\s.,;:!?)\]}"'<>]""")

/**
 * Marks any URL in the text as a link.
 *
 * The old adapter got this from `TextView.setLinksClickable`; a Compose `Text` has no equivalent,
 * so the links are annotated explicitly. Alert bodies from national weather services routinely
 * carry a link to the full bulletin, and losing it in the port would lose real information.
 */
@Composable
private fun String.withClickableLinks(): AnnotatedString {
    val styles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

    return buildAnnotatedString {
        var lastIndex = 0
        URL_PATTERN.findAll(this@withClickableLinks).forEach { match ->
            append(this@withClickableLinks.substring(lastIndex, match.range.first))
            withLink(LinkAnnotation.Url(url = match.value, styles = styles)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        append(this@withClickableLinks.substring(lastIndex))
    }
}

@Preview
@Composable
private fun WeatherAlertDialogPreview() {
    val alert = WeatherAlert.newBuilder()
        .setSender("Météo-France")
        .setEvent("Vent violent")
        .setStartDt(1700010000L * 1000)
        .setEndDt(1700038800L * 1000)
        .setDescription(
            "Des rafales de vent pouvant atteindre 100 km/h sont attendues sur l'ensemble du " +
                    "département. Plus d'informations sur https://vigilance.meteofrance.fr"
        )
        .build()

    val place = Place.newBuilder()
        .addWeatherAlertsList(alert)
        .buildPartial()

    WeatherAlertDialog(
        place = place,
        formattingService = FormattingService.getDumbInstance(LocalContext.current)
    )
}
