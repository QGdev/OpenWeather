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

package fr.qgdev.openweather.ui.components.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.BuildConfig
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.theme.PlexMono

private const val SOURCE_CODE_URL = "https://github.com/QGdev/OpenWeather"
private const val LICENSE_URL = "https://www.gnu.org/licenses/gpl-3.0.html"
private const val PRIVACY_URL = "https://github.com/QGdev/OpenWeather/blob/master/PRIVACY.md"
private const val OPENWEATHER_URL = "https://openweathermap.org/"
private const val OPENWEATHER_DATA_LICENSES = "ODbL · CC BY-SA 4.0"

//  The licences shipped in the assets, listed first by third_party.txt which says what each covers.
private const val LICENSES_DIR = "licenses"
private const val LICENSES_INDEX = "third_party.txt"

/**
 * About the app, in the order the redesign gives it: what happens to your data first, since it is
 * the product's argument, then the developer, the thanks, and the attributions as one card per
 * source so each obligation can be checked at a glance.
 *
 * A long press on the logo still opens the stored data tool. It stays unannounced: it is a
 * development tool, not a feature.
 */
@Composable
fun AboutAppDialog(
    onDismissRequest: () -> Unit = {},
    debugMode: Boolean = false,
    onDebugModeToggle: (Boolean) -> Unit = {}
) {
    if (debugMode) {
        DebugDiagnosticDialog(onDismissRequest = {
            onDebugModeToggle(false)
        })
        return
    }

    var showLicenses by remember { mutableStateOf(false) }
    if (showLicenses) {
        OpenSourceLicensesDialog(onDismissRequest = { showLicenses = false })
    }

    val palette = LocalWeatherPalette.current
    val uriHandler = LocalUriHandler.current

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_about_app),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            IdentityCard(onLogoLongPress = { onDebugModeToggle(true) })

            Section(
                title = stringResource(R.string.title_about_your_data_section),
                body = stringResource(R.string.content_about_your_data_section)
            )
            Section(
                title = stringResource(R.string.title_about_application_section),
                body = stringResource(R.string.content_about_application_section)
            )
            DeveloperSection()
            Section(
                title = stringResource(R.string.title_about_thanks_section),
                body = stringResource(R.string.content_about_thanks_section)
            )

            Column {
                SectionTitle(stringResource(R.string.title_about_attributions_section))
                OpenWeatherAttribution(onClick = { uriHandler.openUri(OPENWEATHER_URL) })
                AttributionCards(
                    titleList = stringArrayResource(id = R.array.attribution_title).toList(),
                    contentList = stringArrayResource(id = R.array.attribution_content).toList(),
                    urlList = stringArrayResource(id = R.array.attribution_url).toList(),
                    onUrlClick = { uriHandler.openUri(it) }
                )
            }

            //  The GPL's notice for an interactive program: no warranty, and the right to redistribute.
            Text(
                text = stringResource(R.string.about_no_warranty),
                color = palette.textQuiet,
                fontSize = 11.5.sp,
                lineHeight = 17.sp
            )

            Column(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    LinkButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.action_about_source_code),
                        onClick = { uriHandler.openUri(SOURCE_CODE_URL) }
                    )
                    LinkButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.action_about_license),
                        onClick = { uriHandler.openUri(LICENSE_URL) }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    LinkButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.action_about_privacy),
                        onClick = { uriHandler.openUri(PRIVACY_URL) }
                    )
                    LinkButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.action_about_open_source_licenses),
                        onClick = { showLicenses = true }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AboutAppDialogPreview() {
    AboutAppDialog(
        onDismissRequest = {}
    )
}

/** Logo, name, version and licence, on the same night gradient as the splash screen. */
@Composable
private fun IdentityCard(onLogoLongPress: () -> Unit) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(22.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(palette.accent.copy(alpha = 0.16f), palette.screen.copy(alpha = 0f))
                )
            )
            .border(1.dp, palette.outline, shape)
            .padding(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //  The app's own logo, in its colours. The developer's logo belongs to the developer's
        //  section, not to the app's identity.
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = stringResource(id = R.string.app_name),
            modifier = Modifier
                .size(56.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLogoLongPress() })
                }
        )
        Column {
            Text(
                text = stringResource(id = R.string.app_name),
                color = palette.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "%s (%d)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                color = palette.textQuiet,
                fontSize = 11.5.sp,
                fontFamily = PlexMono,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = stringResource(R.string.about_license_badge),
                color = palette.textMuted,
                fontSize = 10.5.sp,
                modifier = Modifier
                    .padding(top = 9.dp)
                    .border(1.dp, palette.outlineStrong, CircleShape)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            )
            Text(
                text = stringResource(R.string.about_copyright),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

/**
 * The developer's section, laid out like the others, with the QGDEV logo under its title as a
 * signature.
 */
@Composable
private fun DeveloperSection() {
    val palette = LocalWeatherPalette.current

    Column {
        SectionTitle(stringResource(R.string.title_about_the_developer_section))
        //  Drawn in black by its vector; tinted to the text so it holds on either theme.
        Image(
            painter = painterResource(id = R.drawable.ic_logo_small),
            contentDescription = null,
            colorFilter = ColorFilter.tint(palette.textPrimary),
            modifier = Modifier
                .padding(top = 9.dp)
                .height(18.dp)
        )
        Text(
            text = stringResource(R.string.content_about_the_developer_section),
            color = palette.textMuted,
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = LocalWeatherPalette.current.textPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun Section(title: String, body: String) {
    Column {
        SectionTitle(title)
        Text(
            text = body,
            color = LocalWeatherPalette.current.textMuted,
            fontSize = 12.5.sp,
            lineHeight = 19.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
    }
}

/**
 * OpenWeather's attribution in the form its free plan asks for: its logo, "Weather data provided by
 * OpenWeather" and a link to its website, along with the licences of its data.
 */
@Composable
private fun OpenWeatherAttribution(onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, palette.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        //  The negative logo has a white wordmark, the master one a dark grey wordmark.
        Image(
            painter = painterResource(
                id = if (palette.isDark) R.drawable.owm_logo_negative else R.drawable.owm_logo_master
            ),
            contentDescription = null,
            modifier = Modifier.height(32.dp)
        )
        Column {
            Text(
                text = stringResource(R.string.about_owm_attribution),
                color = palette.textPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = OPENWEATHER_URL.removePrefix("https://").removeSuffix("/") + " · " + OPENWEATHER_DATA_LICENSES,
                color = palette.textQuiet,
                fontSize = 11.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AttributionCards(
    titleList: List<String>,
    contentList: List<String>,
    urlList: List<String>,
    onUrlClick: (String) -> Unit
) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier.padding(top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        titleList.indices.forEach { index ->
            val title = titleList[index]
            val content = contentList[index]
            val url = urlList.getOrNull(index).orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .border(1.dp, palette.outline, shape)
                    .then(if (url.isEmpty()) Modifier else Modifier.clickable { onUrlClick(url) })
                    .padding(horizontal = 14.dp, vertical = 13.dp)
            ) {
                Text(
                    text = title,
                    color = palette.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = content,
                    color = palette.textQuiet,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * The licences of the libraries and typefaces built into the app, read from the assets: the
 * protobuf runtime's BSD licence asks for its notice in any binary distribution, and the Apache
 * licence for a copy of its text.
 */
@Composable
private fun OpenSourceLicensesDialog(onDismissRequest: () -> Unit) {
    val palette = LocalWeatherPalette.current
    val assets = LocalContext.current.assets
    val text = remember {
        assets.list(LICENSES_DIR).orEmpty()
            .sortedWith(compareBy({ it != LICENSES_INDEX }, { it }))
            .joinToString("\n\n\n") { name ->
                val body = assets.open("$LICENSES_DIR/$name").bufferedReader().use { it.readText() }
                if (name == LICENSES_INDEX) body else "── $name ──\n\n$body"
            }
    }

    FullScreenDialog(
        title = stringResource(R.string.action_about_open_source_licenses),
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = text,
            color = palette.textMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = PlexMono,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun LinkButton(modifier: Modifier = Modifier, text: String, onClick: () -> Unit) {
    val palette = LocalWeatherPalette.current

    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, palette.outlineStrong, CircleShape)
            .clickable(onClick = onClick)
            .padding(11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = palette.textPrimary, fontSize = 12.5.sp)
    }
}
