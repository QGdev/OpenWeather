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
package fr.qgdev.openweather.ui.fragment.place

import androidx.collection.MutableObjectList
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import fr.qgdev.openweather.R
import fr.qgdev.openweather.data.models.AirQuality
import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.CurrentWeather
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.Properties
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.ui.components.dialogs.AddPlaceDialog
import fr.qgdev.openweather.ui.place.PlaceCardView
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * PlacesFragment
 *
 * Fragment to display all places
 * and manage them
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Fragment
 */

@Composable
fun PlacesScreenView(
    placeViewModel: PlaceViewModel,
    settingsViewModel: SettingsViewModel
) {
    val data by placeViewModel.placesState.collectAsState()
    val isRefreshing by placeViewModel.isRefreshing.collectAsState(initial = false)
    val settings by settingsViewModel.settingsState.collectAsState()
    val isApiKeyRegistered = settings?.apiKey?.isNotEmpty() ?: false
    val isApiKeyValid = settings?.apiKey?.length == 32

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val deletedLabel = stringResource(R.string.place_deleted)
    val undoLabel = stringResource(R.string.action_undo)

    AppTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (isApiKeyRegistered) {
                if (isApiKeyValid) {
                    when {
                        data == null -> { /* chargement en cours : rien à afficher */ }
                        data!!.isEmpty() -> {
                            NoPlacesRegisteredMessage(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }
                        else -> {
                            LoadPlacesScreen(
                                settingsViewModel = settingsViewModel
                            ) {
                                PlacesList(
                                    placeList = data!!,
                                    isRefreshing = isRefreshing,
                                    onRefresh = { placeViewModel.refreshAllPlaces() },
                                    onDismiss = { place, _ ->
                                        placeViewModel.deletePlace(place)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = String.format(deletedLabel, place.geolocation.city, place.geolocation.countryCode),
                                                actionLabel = undoLabel,
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                placeViewModel.addPlace(place)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                    AddPlaceFloatingActionButton(
                        modifier = Modifier
                            .padding(32.dp)
                            .align(Alignment.BottomEnd),
                        placeViewModel = placeViewModel,
                    )
                } else {
                    InvalidApiKeyMessage(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            } else {
                NoApiKeyMessage(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp) // au-dessus de la NavigationBar
            )
        }
    }
}

@Composable
private fun InvalidApiKeyMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.error_api_key_incorrectly_formed),
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}

@Composable
private fun NoApiKeyMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.error_no_api_key_registered),
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}

@Composable
private fun NoPlacesRegisteredMessage(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.error_no_places_registered),
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = colorResource(id = R.color.colorFirstText),
        fontSize = 18.sp
    )
}

@Composable
private fun AddPlaceFloatingActionButton(
    modifier: Modifier = Modifier,
    placeViewModel: PlaceViewModel,
    onClick: () -> Unit = {},
) {
    val addPlaceDialogOpened = remember { mutableStateOf(false) }

    FloatingActionButton(
        modifier = modifier,
        onClick = {
            onClick()
            addPlaceDialogOpened.value = true
        },
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.title_dialog_add_place)
        )
    }

    if (addPlaceDialogOpened.value) {
        AddPlaceDialog(
            placeViewModel = placeViewModel,
            onDismissRequest = {
                addPlaceDialogOpened.value = false
            }
        )
    }
}

@Composable
fun LoadPlacesScreen(
    settingsViewModel: SettingsViewModel,
    content: @Composable () -> Unit
) {
    MutableStateFlow(false)

    if (settingsViewModel.isApiKeyRegistered() && settingsViewModel.isApiKeyValid()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(id = R.color.colorBackground))
        ) {
            content()
        }
    }
}

@Composable
private fun PlacesList(
    placeList: List<Place>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    formattingService: FormattingService = FormattingService.getInstance(LocalContext.current),
    onDismiss: (Place, Int) -> Unit = { _, _ -> }
) {
    // Dialog géré ici, hors du composable de l'item :
    // évite toute réutilisation d'état après un Undo qui remettrait le dialog au premier plan.
    var pendingDeletePlace by remember { mutableStateOf<Place?>(null) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }
    var pendingResetCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    // État pour la LazyColumn
    val lazyListState = rememberLazyListState()
    
    // Track si on a déclenché le refresh avec ce geste spécifique
    var refreshTriggeredInThisGesture by remember { mutableStateOf(false) }
    var displayedDragY by remember { mutableStateOf(0f) }
    var hasPassedThreshold by remember { mutableStateOf(false) }  // Track si on a passé le seuil
    val pullThreshold = 80f  // Seuil pour afficher "Relâcher pour rafraîchir"

    // Réinitialiser quand le refresh est terminé
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            refreshTriggeredInThisGesture = false
            displayedDragY = 0f
            hasPassedThreshold = false
        }
    }

    // Créer un NestedScrollConnection pour détecter le pull-down sans bloquer la LazyColumn
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            private var cumulativeDragY = 0f
            
             override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                  // Vérifier si on est au sommet (index 0 et offset 0)
                  val isAtTop = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
                  
                  if (isAtTop) {
                      // Si l'utilisateur scroll vers le haut, réinitialiser le compteur
                      if (available.y < 0) {
                          cumulativeDragY = 0f
                          displayedDragY = 0f
                          hasPassedThreshold = false
                          return Offset.Zero
                      }
                      
                      // Si l'utilisateur scroll vers le bas (available.y > 0)
                      if (available.y > 0) {
                          cumulativeDragY += available.y
                          displayedDragY = cumulativeDragY  // Mettre à jour la position visuelle
                          
                          // Vérifier si on a passé le seuil
                          if (cumulativeDragY > pullThreshold) {
                              hasPassedThreshold = true
                          }
                          
                          // Consommer le scroll au top pour montrer l'effet de pull
                          return available
                      }
                  } else {
                      // Si on n'est pas au top, réinitialiser le compteur
                      cumulativeDragY = 0f
                      displayedDragY = 0f
                      hasPassedThreshold = false
                  }
                  
                  return Offset.Zero
              }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // Détecter le relâchement : quand available est proche de 0 et qu'on était en pull
                if (displayedDragY > 0) {
                    // Détecter la fin du geste (scroll qui décélère)
                    if (available.y <= 0 && source == NestedScrollSource.Fling) {
                        // L'utilisateur a relâché
                        if (hasPassedThreshold && !isRefreshing && !refreshTriggeredInThisGesture) {
                            // Déclencher le refresh seulement si le seuil a été passé
                            refreshTriggeredInThisGesture = true
                            onRefresh()
                        }
                        // Réinitialiser le drag
                        displayedDragY = 0f
                    }
                }
                
                return Offset.Zero
            }
        }
    }

    pendingDeletePlace?.let { place ->
        AlertDialog(
            onDismissRequest = {
                pendingDeletePlace = null
                pendingResetCallback?.invoke()
                pendingResetCallback = null
            },
            title = { Text(stringResource(R.string.dialog_confirmation_title_delete_place)) },
            text = {
                Text(
                    String.format(
                        stringResource(R.string.dialog_confirmation_message_delete_place),
                        place.geolocation.city,
                        place.geolocation.countryCode
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val placeToDelete = pendingDeletePlace
                    val indexToDelete = pendingDeleteIndex

                    pendingDeletePlace = null
                    pendingDeleteIndex = -1
                    pendingResetCallback = null  // Nettoyer sans appeler reset()

                    if (placeToDelete != null && indexToDelete != -1) {
                        onDismiss(placeToDelete, indexToDelete)
                    }
                }) {
                    Text(stringResource(R.string.dialog_confirmation_choice_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingDeletePlace = null
                    pendingResetCallback?.invoke()
                    pendingResetCallback = null
                }) {
                    Text(stringResource(R.string.dialog_confirmation_choice_no))
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            state = lazyListState
        ) {
            itemsIndexed(items = placeList, key = { index, _ -> index }) { index, place ->
                SwipeablePlaceItem(
                    place = place,
                    formattingService = formattingService,
                    onSwipedPastThreshold = { resetCallback ->
                        pendingDeletePlace = place
                        pendingDeleteIndex = index
                        pendingResetCallback = resetCallback
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Indicateur de pull progressif avec texte en forme de goutte
        if (!isRefreshing && displayedDragY > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                PullToRefreshDropIndicator(
                    displayedDragY = displayedDragY,
                    pullThreshold = pullThreshold,
                    showReleaseText = displayedDragY >= pullThreshold
                )
            }
        }

        // Indicateur de refresh en haut de la liste (pendant le refresh)
        if (isRefreshing) {
            val infiniteTransition = rememberInfiniteTransition(label = "refresh")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "rotation"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .rotate(rotation),
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

@Composable
private fun PullToRefreshDropIndicator(
    displayedDragY: Float,
    pullThreshold: Float,
    showReleaseText: Boolean
) {
    val progress = (displayedDragY / pullThreshold).coerceIn(0f, 1f)
    val dropScale = (0.55f + progress * 0.45f)
    val primaryColor = MaterialTheme.colorScheme.primary
    val dropFillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)

    Box(
        modifier = Modifier
            .height((132 * dropScale).dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = androidx.compose.ui.graphics.Path().apply {
                lineTo(0f, h)
                lineTo(w, h)
                lineTo(w, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                path = path,
                color = dropFillColor
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showReleaseText) {
                Text(
                    text = stringResource(R.string.action_release_to_refresh),
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            CircularProgressIndicator(
                modifier = Modifier
                    .width((22 + progress * 16).dp)
                    .height((22 + progress * 16).dp),
                strokeWidth = 2.2.dp,
                progress = { progress.coerceAtLeast(0.05f) },
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.22f)
            )
        }
    }
}

// Preview of PullToRefreshDropIndicator
@Preview
@Composable
fun PullToRefreshDropIndicatorPreview() {
    PullToRefreshDropIndicator(
        displayedDragY = 120f,
        pullThreshold = 80f,
        showReleaseText = true
    )
    Spacer(modifier = Modifier.height(96.dp))
    Spacer(modifier = Modifier.height(96.dp))
    
}

@Composable
private fun SwipeablePlaceItem(
    place: Place,
    formattingService: FormattingService,
    onSwipedPastThreshold: ((resetCallback: () -> Unit) -> Unit)
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState()

    // Détecte une seule fois si on hérite d'un état non-Settled (après Undo)
    // et le reset silencieusement sans appeler onSwipedPastThreshold()
    LaunchedEffect(Unit) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            scope.launch { dismissState.reset() }
        }
    }

    // Logique normale du swipe : appelle onSwipedPastThreshold() avec le callback reset
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            onSwipedPastThreshold { scope.launch { dismissState.reset() } }
            scope.launch { dismissState.reset() }
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val density = LocalDensity.current
            // 96 dp = distance à partir de laquelle l'alpha atteint 1.0
            val fullAlphaPx = with(density) { 96.dp.toPx() }
            val offsetPx = runCatching<Float> { dismissState.requireOffset() }.getOrDefault(0f)
            val alpha = (kotlin.math.abs(offsetPx) / fullAlphaPx).coerceIn(0f, 1f)
            val isStart = offsetPx > 0f

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE01C15).copy(alpha = alpha)),
                contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.description_delete_icon),
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    ) {
        PlaceCardView(
            place = place,
            formattingService = formattingService,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun PlacesScreenPreview() {
    val places = MutableObjectList<Place>()

    val coordinates = Coordinates.newBuilder()
        .setLatitude(0.0)
        .setLongitude(0.0)
        .build()

    val geolocation = Geolocation.newBuilder()
        .setCity("Paris")
        .setCountryCode("FR")
        .setCoordinates(coordinates)
        .build()

    val properties = Properties.newBuilder()
        .setTimeOffset(0)
        .setCreationTime(0L)
        .setLastAvailableWeatherDataTime(0L)
        .setLastSuccessfulWeatherUpdateTime(0L)
        .setLastWeatherUpdateAttemptTime(0L)
        .setLastAirQualityUpdateAttemptTime(0L)
        .setLastAvailableAirQualityDataTime(0L)
        .setLastSuccessfulAirQualityUpdateTime(0L)
        .build()

    val currentWeather = CurrentWeather.newBuilder()
        .setWeather("Clear")
        .setWeatherCode(1)
        .setTemperature(0.0F)
        .setTemperatureFeelsLike(0.0F)
        .setCloudiness(0)
        .setHumidity(0)
        .setPressure(0)
        .setWindSpeed(0.0F)
        .setWindDirection(0)
        .setWindGustSpeed(0.0F)
        .setSunrise(0L)
        .setSunset(0L)
        .setUvIndex(0)
        .setVisibility(0)
        .setRain(0.0F)
        .setSnow(0.0F)
        .build()

    val airQuality = AirQuality.newBuilder()
        .setAqi(0)
        .setCo(0.0F)
        .setNo(0.0F)
        .setO3(0.0F)
        .setNo2(0.0F)
        .setPm25(0.0F)
        .setPm10(0.0F)
        .setNh3(0.0F)
        .setSo2(0.0F)
        .build()


    val place = Place.newBuilder()
        .setGeolocation(geolocation)
        .setProperties(properties)
        .setCurrentWeather(currentWeather)
        .setAirQuality(airQuality)
        .addAllMinutelyForecastList(emptyList())
        .addAllHourlyForecastList(emptyList())
        .addAllDailyForecastList(emptyList())
        .addAllWeatherAlertsList(emptyList())
        .build()

    // Add a place to the list a bunch of times
    for (i in 0..10) {
        places.add(place)
    }

    PlacesScreenView(
        placeViewModel = PlaceViewModel(PlaceRepository.getInstance(LocalContext.current)),
        settingsViewModel = SettingsViewModel(
            settingsRepository = SettingsRepository.getInstance(LocalContext.current),
            formattingService = FormattingService.getInstance(LocalContext.current)
        )
    )
}

