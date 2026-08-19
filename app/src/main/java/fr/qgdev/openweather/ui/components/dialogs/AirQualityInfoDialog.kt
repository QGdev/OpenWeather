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

import android.R.attr.padding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import fr.qgdev.openweather.R
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog


@Composable
fun AirQualityInfoDialog(
    onDismissRequest: () -> Unit = {}
) {
    FullScreenDialog(
        title = stringResource(R.string.title_dialog_air_quality_information),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_so2),
                content = stringResource(id = R.string.content_air_quality_so2_information)
            )
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_no2),
                content = stringResource(id = R.string.content_air_quality_no2_information)
            )
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_pm10),
                content = stringResource(id = R.string.content_air_quality_pm10_information)
            )
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_pm25),
                content = stringResource(id = R.string.content_air_quality_pm25_information)
            )
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_o3),
                content = stringResource(id = R.string.content_air_quality_o3_information)
            )
            AirQualitySection(
                title = stringResource(id = R.string.title_air_quality_co),
                content = stringResource(id = R.string.content_air_quality_co_information)
            )
        }
    }
}


@Composable
private fun AirQualitySection(title: String, content: String) {
    Text(
        text = title,
        color = colorResource(id = R.color.colorFirstText),
        fontSize = dimensionResource(id = R.dimen.text_size_dialog_title).value.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = content,
        color = colorResource(id = R.color.colorSecondaryText),
        fontSize = dimensionResource(id = R.dimen.text_size_dialog_content).value.sp,
        modifier = Modifier
            .padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
            .fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
private fun AirQualityInfoDialogPreview() {
    AirQualityInfoDialog()
}