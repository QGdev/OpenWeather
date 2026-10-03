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

import android.app.Dialog
import android.content.Context
import android.icu.text.DecimalFormat
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.remote.FetchCallback
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.remote.FetchDataCallback
import fr.qgdev.openweather.data.remote.RequestStatus
import fr.qgdev.openweather.ui.common.dialogs.FullScreenDialog
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.logging.Level
import java.util.logging.Logger

@Composable
fun AddPlaceDialog(
    placeViewModel: PlaceViewModel,
    onDismissRequest: () -> Unit = {}
) {
    val placeQuery = remember { mutableStateOf("") }
    val currentlySearching = remember { mutableStateOf(false) }
    val searchError = remember { mutableStateOf(ErrorType.NONE) }
    val resultList = remember { mutableStateOf(listOf<Geolocation>()) }


    val fetchCallback = object : FetchCallback<List<Geolocation>> {
        override fun onSuccess(geolocations: List<Geolocation>) {
            if (geolocations.isEmpty()) {
                searchError.value = ErrorType.NO_RESULTS
                currentlySearching.value = false
                return
            }

            resultList.value = geolocations
            searchError.value = ErrorType.NONE
            currentlySearching.value = false
        }

        override fun onError(requestStatus: RequestStatus?) {
            if (requestStatus == null) return

            searchError.value = when (requestStatus) {
                RequestStatus.NO_ANSWER,
                RequestStatus.AUTH_FAILED,
                RequestStatus.TOO_MANY_REQUESTS -> ErrorType.SERVER_ERROR

                RequestStatus.NOT_FOUND -> ErrorType.NO_RESULTS
                RequestStatus.NOT_CONNECTED -> ErrorType.NO_CONNECTION
                RequestStatus.UNKNOWN_ERROR -> ErrorType.UNKNOWN_ERROR
                RequestStatus.ALREADY_PRESENT -> ErrorType.ALREADY_PRESENT
                RequestStatus.TOO_SHORT -> ErrorType.QUERY_TOO_SHORT
            }

            resultList.value = emptyList()
            currentlySearching.value = false
        }
    }

    val fetchDataCallback = object : FetchDataCallback {
        override suspend fun onSuccess(place: Place) {
            onDismissRequest()
        }

        override suspend fun onPartialSuccess(
            place: Place,
            requestStatus: RequestStatus
        ) {
            onDismissRequest()
        }

        override suspend fun onError(requestStatus: RequestStatus) {
            searchError.value = when (requestStatus) {
                RequestStatus.NO_ANSWER,
                RequestStatus.AUTH_FAILED,
                RequestStatus.TOO_MANY_REQUESTS -> ErrorType.SERVER_ERROR

                RequestStatus.NOT_FOUND -> ErrorType.NO_RESULTS
                RequestStatus.NOT_CONNECTED -> ErrorType.NO_CONNECTION
                RequestStatus.UNKNOWN_ERROR -> ErrorType.UNKNOWN_ERROR
                RequestStatus.ALREADY_PRESENT -> ErrorType.ALREADY_PRESENT
                RequestStatus.TOO_SHORT -> ErrorType.QUERY_TOO_SHORT
            }

            currentlySearching.value = false
        }
    }

    fun searchPlace() {
        if (currentlySearching.value) return
        currentlySearching.value = true
        placeViewModel.seachPlaces(
            placeQuery.value,
            fetchCallback
        )
    }

    FullScreenDialog(
        title = stringResource(R.string.title_dialog_add_place),
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //  The Search bar (input field)
                //  Only one text input field is required, we did make an evolution of the search engine
                //  The search bar will be a TextInputEditText
                //  The country field is useless now



                OutlinedTextField(
                    value = placeQuery.value,
                    onValueChange = { placeQuery.value = it },
                    label = { Text(stringResource(R.string.title_dialog_add_place_textinput_city)) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default,
                    singleLine = true,
                    enabled = !currentlySearching.value,
                    trailingIcon = {
                        IconButton(onClick = {searchPlace()}) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "",    //  TODO
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
            //  An error message if the search failed
            if (searchError.value != ErrorType.NONE) {
                Text(
                    text = when (searchError.value) {
                        ErrorType.QUERY_EMPTY -> stringResource(R.string.error_place_query_empty)
                        ErrorType.QUERY_TOO_SHORT -> stringResource(R.string.error_place_query_too_short)
                        ErrorType.ALREADY_PRESENT -> stringResource(R.string.error_place_already_added)
                        ErrorType.NO_RESULTS -> stringResource(R.string.error_no_results)
                        ErrorType.NO_CONNECTION -> stringResource(R.string.error_device_not_connected)
                        ErrorType.SERVER_ERROR -> stringResource(R.string.error_server_unreachable)
                        else -> stringResource(R.string.error_unknown_error)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    fontSize = 18.sp
                )

                return@Column
            }

            //  The lIST of results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                if (resultList.value.isNotEmpty()) {
                    //  Show the list of results
                    resultList.value.forEach { geolocation ->
                        GeolocationItem(
                            geolocation = geolocation,
                            onClick = {
                                //  Add the place to the list of places
                                placeViewModel.fetchAndAddNewPlaceFromWeb(
                                    geolocation,
                                    fetchDataCallback
                                )
                                onDismissRequest()
                            }
                        )
                    }
                }
            }
        }
    }
}

private enum class ErrorType {
    NONE,
    QUERY_EMPTY,
    QUERY_TOO_SHORT,
    ALREADY_PRESENT,
    NO_RESULTS,
    NO_CONNECTION,
    SERVER_ERROR,
    UNKNOWN_ERROR
}

@Preview
@Composable
fun AddPlaceDialogPreview() {
    AddPlaceDialog(
        placeViewModel = viewModel(factory = PlaceViewModelFactory(LocalContext.current)),
        onDismissRequest = {}
    )
}

@Composable
fun GeolocationItem(
    geolocation: Geolocation,
    onClick: (Geolocation) -> Unit
) {
    val latitudeFormatted = DecimalFormat("#.###").format(geolocation.coordinates.latitude)
    val longitudeFormatted = DecimalFormat("#.###").format(geolocation.coordinates.longitude)

    Box(
        modifier = Modifier
            .requiredHeight(80.dp)
            .requiredWidth(300.dp)
            .padding(8.dp)
            .clickable { onClick(geolocation) },
    ) {
        Text(
            text = geolocation.city,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            fontSize = 18.sp
        )
        Text(
            text = geolocation.countryCode,
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            fontSize = 18.sp
        )

        Text(
            text = "($latitudeFormatted, $longitudeFormatted)",
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomEnd),
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Start,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
fun GeolocationItemPreview() {
    val coordinates = Coordinates.newBuilder()
        .setLatitude(48.8566)
        .setLongitude(2.3522)
        .build()
    val geolocation = Geolocation.newBuilder()
        .setCity("Paris")
        .setCountryCode("FR")
        .setCoordinates(coordinates)
        .build()
    GeolocationItem(
        geolocation = geolocation,
        onClick = {}
    )
}