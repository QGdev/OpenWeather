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

import kotlin.math.roundToInt
import fr.qgdev.openweather.data.remote.isSamePlaceAs
import fr.qgdev.openweather.data.remote.distanceKm
import fr.qgdev.openweather.data.models.Place
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import fr.qgdev.openweather.ui.theme.PlexMono
import fr.qgdev.openweather.ui.theme.Figtree
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
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
import fr.qgdev.openweather.data.repositories.identityKey
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
    //  A result chosen while a nearby place of the same name is already stored, and that place.
    var replaceChoice by remember { mutableStateOf<Pair<Geolocation, Place>?>(null) }

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

    replaceChoice?.let { (chosen, twin) ->
        ReplaceConfirmation(
            chosen = chosen,
            twin = twin,
            onReplace = {
                placeViewModel.replacePlaceFromSearch(twin, chosen)
                replaceChoice = null
                onDismissRequest()
            },
            onAddAnyway = {
                placeViewModel.addPlaceFromSearch(chosen)
                replaceChoice = null
                onDismissRequest()
            },
            onCancel = { replaceChoice = null }
        )
    }

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_add_place),
        onDismissRequest = onDismissRequest
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            //  One field with its button inside, as in the mock-up; faded while a search runs.
            val accent = MaterialTheme.colorScheme.primary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (searching) 0.6f else 1f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(start = 16.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                //  The hint shares the field's style: with the theme's own line height it sat lower
                //  than the cursor.
                val fieldStyle = TextStyle(color = palette.textPrimary, fontSize = 14.sp, fontFamily = Figtree)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.hint_search_city),
                            style = fieldStyle.copy(color = palette.textQuiet)
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        enabled = !searching,
                        textStyle = fieldStyle,
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.fillMaxWidth(),
                        //  The button is kept - each search is a call to Nominatim, and typing should
                        //  not fire one per keystroke - but the keyboard's own search key works too,
                        //  which is where a thumb already is.
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { search() })
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent)
                        .clickable(enabled = !searching) { search() }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
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
                //  In place of the results, marked by how serious it is: a query to fix, no
                //  answer, the network or servers, something unexpected.
                val dot = when (currentError) {
                    RequestStatus.TOO_SHORT -> palette.amber
                    RequestStatus.NOT_FOUND, RequestStatus.ALREADY_PRESENT -> palette.textQuiet
                    RequestStatus.UNKNOWN_ERROR -> palette.red
                    else -> palette.orange
                }
                Row(
                    modifier = Modifier.padding(start = 4.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dot)
                    )
                    Text(
                        text = stringResource(addPlaceErrorRes(currentError)),
                        color = palette.textPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                }
                return@Column
            }

            if (results.isEmpty()) return@Column

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.label_results_count, results.size),
                    color = palette.textQuiet,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.66.sp
                )
                Text(
                    text = stringResource(R.string.label_cities_only),
                    color = palette.textQuiet,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                results.forEachIndexed { index, geolocation ->
                    //  Near duplicates are merged before they get here; what remains under one name
                    //  says how far it is from the same-named result above it.
                    val kmFromAbove = results.take(index)
                        .filter {
                            it.city.equals(geolocation.city, ignoreCase = true) &&
                                    it.countryCode.equals(geolocation.countryCode, ignoreCase = true)
                        }
                        .minOfOrNull { distanceKm(it.coordinates, geolocation.coordinates) }
                    SearchResultRow(
                        geolocation = geolocation,
                        alreadyAdded = geolocation.identityKey in storedKeys,
                        kmFromAbove = kmFromAbove,
                        onClick = {
                            val twin = places.orEmpty().firstOrNull { it.geolocation.isSamePlaceAs(geolocation) }
                            if (twin != null) {
                                replaceChoice = geolocation to twin
                            } else {
                                placeViewModel.addPlaceFromSearch(geolocation)
                                onDismissRequest()
                            }
                        }
                    )
                }

                InfoNote(
                    text = stringResource(R.string.info_same_name_cities),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    geolocation: Geolocation,
    alreadyAdded: Boolean,
    kmFromAbove: Double?,
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
            //  A place already in the list stays shown - the search did find it - but steps back.
            .alpha(if (alreadyAdded) 0.55f else 1f)
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, palette.outline, RoundedCornerShape(18.dp))
            .clickable(enabled = !alreadyAdded, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        val accent = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (alreadyAdded) palette.outline else accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = geolocation.countryCode.uppercase(),
                color = if (alreadyAdded) palette.textMuted else accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PlexMono
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = geolocation.city,
                color = palette.textPrimary,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (place.isNotEmpty()) {
                Text(
                    text = place,
                    color = palette.textQuiet,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = coordinates + (kmFromAbove?.let {
                    " · " + stringResource(R.string.label_km_from_above, it.roundToInt())
                } ?: ""),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                fontFamily = PlexMono,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        if (alreadyAdded) {
            Text(
                text = stringResource(R.string.label_already_added),
                color = palette.textQuiet,
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp)
            )
        } else {
            Text(text = "›", color = palette.textQuiet, fontSize = 15.sp)
        }
    }
}

/** A note under the results, boxed with an "i" like the other notes in the redesign. */
@Composable
private fun InfoNote(text: String, modifier: Modifier = Modifier) {
    val palette = LocalWeatherPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, palette.outline, RoundedCornerShape(16.dp))
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
        Text(text = text, color = palette.textQuiet, fontSize = 11.5.sp, lineHeight = 17.sp)
    }
}

/** Mirrors the repository's own ceiling, so the dialog can say so before a search. */
private const val MAX_PLACES = 100

/**
 * Asked when the chosen result is a town already in the list under nearby coordinates: swap the
 * stored one for it, keep both, or neither. Replacing keeps the place's position and its widgets.
 */
@Composable
private fun ReplaceConfirmation(
    chosen: Geolocation,
    twin: Place,
    onReplace: () -> Unit,
    onAddAnyway: () -> Unit,
    onCancel: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val km = distanceKm(chosen.coordinates, twin.geolocation.coordinates).roundToInt()
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.replace_place_title, twin.geolocation.city),
                color = palette.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = stringResource(R.string.replace_place_body, km),
                color = palette.textSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onAddAnyway) { Text(stringResource(R.string.action_add_anyway)) }
                TextButton(onClick = onReplace) {
                    Text(stringResource(R.string.action_replace), fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
