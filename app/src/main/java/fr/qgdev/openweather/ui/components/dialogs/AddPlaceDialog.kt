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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.remote.FetchCallback
import fr.qgdev.openweather.data.remote.RequestStatus
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import fr.qgdev.openweather.ui.fragment.place.addPlaceErrorRes
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.utils.countryNameFromCode
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.identityKey
import java.text.DecimalFormat

/**
 * Searching for a city and adding it.
 *
 * Two things changed against the old dialog. A result now leads with its region - "Pays de la
 * Loire" tells two Nantes apart far better than a pair of coordinates does, and Nominatim was
 * already returning it. And a city already in the list is no longer an error that replaces the
 * results: the search succeeded, so the results stay and that one row is simply marked and inert.
 *
 * Choosing a result closes the dialog immediately; the place appears in the list as pending while
 * its forecast downloads. Waiting here would hold the user in a modal for a network round trip.
 */
@Composable
fun AddPlaceDialog(
    placeViewModel: PlaceViewModel,
    onDismissRequest: () -> Unit = {}
) {
    val palette = LocalWeatherPalette.current
    val places by placeViewModel.placesState.collectAsState()

    var query by remember { mutableStateOf("") }
    var searching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<RequestStatus?>(null) }
    var results by remember { mutableStateOf(emptyList<Geolocation>()) }

    val storedKeys = places.orEmpty().map { it.identityKey }.toSet()
    val storageFull = (places?.size ?: 0) >= MAX_PLACES

    val searchCallback = object : FetchCallback<List<Geolocation>> {
        override fun onSuccess(result: List<Geolocation>) {
            results = result
            error = if (result.isEmpty()) RequestStatus.NOT_FOUND else null
            searching = false
        }

        override fun onError(requestStatus: RequestStatus?) {
            results = emptyList()
            error = requestStatus ?: RequestStatus.UNKNOWN_ERROR
            searching = false
        }
    }

    fun search() {
        if (searching || query.isBlank()) return
        searching = true
        error = null
        placeViewModel.seachPlaces(query, searchCallback)
    }

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_add_place),
        onDismissRequest = onDismissRequest
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.hint_search_city)) },
                    singleLine = true,
                    enabled = !searching,
                    modifier = Modifier.weight(1f),
                    //  The button is kept - each search is a call to Nominatim, and typing should
                    //  not fire one per keystroke - but the keyboard's own search key now works
                    //  too, which is where a thumb already is.
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search() })
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(enabled = !searching) { search() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (searching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.action_search),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (storageFull) {
                Text(
                    text = stringResource(R.string.error_places_full, MAX_PLACES),
                    color = palette.orange,
                    fontSize = 12.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                return@Column
            }

            val currentError = error
            if (currentError != null) {
                Text(
                    text = stringResource(addPlaceErrorRes(currentError)),
                    color = palette.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                return@Column
            }

            if (results.isEmpty()) return@Column

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_results_count, results.size),
                    color = palette.textSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = stringResource(R.string.label_cities_only),
                    color = palette.textSecondary,
                    fontSize = 11.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                results.forEach { geolocation ->
                    SearchResultRow(
                        geolocation = geolocation,
                        alreadyAdded = geolocation.identityKey in storedKeys,
                        onClick = {
                            placeViewModel.addPlaceFromSearch(geolocation)
                            onDismissRequest()
                        }
                    )
                }

                Text(
                    text = stringResource(R.string.info_same_name_cities),
                    color = palette.textQuiet,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    geolocation: Geolocation,
    alreadyAdded: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val coordinates = DecimalFormat("#.###").let { format ->
        "${format.format(geolocation.coordinates.latitude)} · " +
                format.format(geolocation.coordinates.longitude)
    }
    val place = listOfNotNull(
        geolocation.region.ifEmpty { null },
        countryNameFromCode(geolocation.countryCode).ifEmpty { null }
    ).joinToString(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .clickable(enabled = !alreadyAdded, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = geolocation.countryCode.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = geolocation.city,
                color = if (alreadyAdded) palette.textMuted else palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (place.isNotEmpty()) {
                Text(
                    text = place,
                    color = palette.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(text = coordinates, color = palette.textQuiet, fontSize = 10.5.sp)
        }

        if (alreadyAdded) {
            Text(
                text = stringResource(R.string.label_already_added),
                color = palette.textQuiet,
                fontSize = 10.5.sp
            )
        } else {
            Text(text = "›", color = palette.textSecondary, fontSize = 15.sp)
        }
    }
}

/** Mirrors the repository's own ceiling, so the dialog can say so before a search. */
private const val MAX_PLACES = 100
