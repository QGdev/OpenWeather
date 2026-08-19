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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.qgdev.openweather.BuildConfig
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog

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

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_about_app),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())

        ) {
            //  Logo section
            //  Icon, appName and version section
            val appLogoModifier = Modifier
                .size(150.dp)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onDebugModeToggle(true)
                        }
                    )
                }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val displayedVersion =
                    "%s (%d)".format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_small),
                    contentDescription = stringResource(id = R.string.app_name),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surfaceTint),
                    modifier = appLogoModifier
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = displayedVersion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            //  Developer section
            SectionTitle(text = stringResource(id = R.string.title_about_the_developer_section))
            SectionContent(text = stringResource(id = R.string.content_about_the_developer_section))
            //  Thanks section
            SectionTitle(text = stringResource(id = R.string.title_about_thanks_section))
            SectionContent(text = stringResource(id = R.string.content_about_thanks_section))
            //  Data section
            SectionTitle(text = stringResource(id = R.string.title_about_your_data_section))
            SectionContent(text = stringResource(id = R.string.content_about_your_data_section))
            //  Application section
            SectionTitle(text = stringResource(id = R.string.title_about_application_section))
            SectionContent(text = stringResource(id = R.string.content_about_application_section))

            //  Attribution section
            SectionTitle(text = stringResource(id = R.string.title_about_attributions_section))
            AttributionSection(
                titleList = stringArrayResource(id = R.array.attribution_title).toList(),
                contentList = stringArrayResource(id = R.array.attribution_content).toList()
            )
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

@Composable
private fun SectionTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.titleLarge,
    )
}

@Composable
private fun SectionContent(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(8.dp, bottom = 0.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Justify,
    )
}

@Composable
private fun SectionSubTitle(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(top = 16.dp),
        text = text,
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun SectionSubContent(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Justify,
    )
}

@Composable
private fun AttributionSection(
    titleList: List<String>,
    contentList: List<String>
) {
    val modifier = Modifier.padding(start = 8.dp)
    Column {
        for (i in titleList.indices) {
            SectionSubTitle(
                modifier = modifier,
                text = titleList[i]
            )
            SectionSubContent(
                modifier = modifier,
                text = contentList[i]
            )
        }
    }
}
