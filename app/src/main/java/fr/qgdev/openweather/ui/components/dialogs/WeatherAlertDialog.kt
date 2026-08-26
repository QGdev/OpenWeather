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
import androidx.compose.material3.Surface
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
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import java.util.Date
import java.util.SimpleTimeZone
import java.util.TimeZone

//  The event icon sets the text column for the whole alert: everything under the heading lines up
//  past the icon and its gap, so the inset is derived from them instead of being repeated by hand.
private val ALERT_ICON_SIZE = 28.dp
private val ALERT_ICON_GAP = 8.dp
private val ALERT_CONTENT_INSET = ALERT_ICON_SIZE + ALERT_ICON_GAP

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
    //  own zone. timeOffset is not currently populated by PlaceDataMapper, so this resolves to UTC
    //  until that is fixed - see PORTING.md.
    val timeZone: TimeZone = SimpleTimeZone(place.properties.timeOffset, "")

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_weather_alert),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            place.weatherAlertsListList.forEachIndexed { index, alert ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                WeatherAlertSection(
                    alert = alert,
                    formattingService = formattingService,
                    timeZone = timeZone
                )
            }
        }
    }
}

@Composable
private fun WeatherAlertSection(
    alert: WeatherAlert,
    formattingService: FormattingService,
    timeZone: TimeZone
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(ALERT_ICON_GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.danger),
            contentDescription = null,
            modifier = Modifier.size(ALERT_ICON_SIZE)
        )
        Text(
            text = alert.event,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }

    Text(
        text = alert.sender,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = ALERT_CONTENT_INSET, top = 2.dp)
    )

    //  Start and end sit together on a tinted strip: they are the one part of an alert a reader
    //  scans for first, and they should not be lost in the body copy. The strip carries its own
    //  content colour, so the times below inherit it.
    Surface(
        modifier = Modifier
            .padding(start = ALERT_CONTENT_INSET, top = 12.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formattingService.getFormattedFullTimeHour(Date(alert.startDt), timeZone),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "→",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formattingService.getFormattedFullTimeHour(Date(alert.endDt), timeZone),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    Text(
        text = alert.description.withClickableLinks(),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = ALERT_CONTENT_INSET, top = 12.dp)
            .fillMaxWidth()
    )
}

//  Trailing punctuation is part of the sentence, not of the address: alert bodies quote their
//  links inside parentheses, so the match stops at the last character a URL can really end on.
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
