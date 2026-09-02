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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.graphicsLayer
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
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
import fr.qgdev.openweather.ui.common.components.dragToReorder
import fr.qgdev.openweather.ui.common.components.rememberDragToReorderState
import fr.qgdev.openweather.ui.components.dialogs.AddPlaceDialog
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import androidx.annotation.StringRes
import fr.qgdev.openweather.data.remote.RequestStatus
import fr.qgdev.openweather.ui.place.PendingPlaceCard
import fr.qgdev.openweather.ui.place.PlaceSkyCard
import fr.qgdev.openweather.ui.theme.LocalWeatherPalette
import fr.qgdev.openweather.ui.viewmodel.RefreshOutcome
import fr.qgdev.openweather.data.repositories.identityKey
import java.util.Date
import java.util.TimeZone
import fr.qgdev.openweather.ui.theme.AppTheme
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModel
import fr.qgdev.openweather.ui.viewmodel.PlaceViewModelFactory
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModel
import fr.qgdev.openweather.ui.viewmodel.SettingsViewModelFactory
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
    settingsViewModel: SettingsViewModel,
    onOpenPlace: (Place) -> Unit = {}
) {
    val data by placeViewModel.placesState.collectAsState()
    val isRefreshing by placeViewModel.isRefreshing.collectAsState(initial = false)
    val refreshOutcome by placeViewModel.lastRefreshOutcome.collectAsState()
    val pendingPlaceName by placeViewModel.pendingPlaceName.collectAsState()
    val addPlaceFailure by placeViewModel.addPlaceFailure.collectAsState()
    val settings by settingsViewModel.settingsState.collectAsState()
    //  Republished as a new instance on every settings change, which is what makes the cards
    //  below redraw when a unit changes.
    val formattingService by settingsViewModel.formattingServiceState.collectAsState()
    val isApiKeyRegistered = settings?.apiKey?.isNotEmpty() ?: false
    val isApiKeyValid = settings?.apiKey?.length == 32

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

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
                                    formattingService = formattingService,
                                    lazyListState = lazyListState,
                                    isRefreshing = isRefreshing,
                                    refreshOutcome = refreshOutcome,
                                    pendingPlaceName = pendingPlaceName,
                                    onRefreshOutcomeShown = { placeViewModel.acknowledgeRefreshOutcome() },
                                    onOpenPlace = onOpenPlace,
                                    onRefresh = { placeViewModel.refreshAllPlaces() },
                                    onMovePlace = { from, to ->
                                        placeViewModel.movePlace(from, to)
                                    },
                                    onDismiss = { place, index ->
                                        placeViewModel.deletePlace(place)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = String.format(deletedLabel, place.geolocation.city, place.geolocation.countryCode),
                                                actionLabel = undoLabel,
                                                duration = SnackbarDuration.Long
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                //  Restored where it was, not appended: addPlace put the
                                                //  place back at the end of the list, so undoing a delete
                                                //  silently reordered the list.
                                                placeViewModel.restorePlace(place, index)
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
                        //  Labelled while the list is at rest, icon-only once scrolled: the label
                        //  says what the button does, but it should not sit over the cards being read.
                        expanded = lazyListState.firstVisibleItemIndex == 0 &&
                                lazyListState.firstVisibleItemScrollOffset == 0
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

            //  A first download that fails leaves nothing behind - no half-built card in the list -
            //  so the failure has to be said out loud or the place would simply never appear.
            val addFailureMessage = addPlaceFailure?.let { stringResource(addPlaceErrorRes(it)) }
            LaunchedEffect(addPlaceFailure) {
                if (addFailureMessage != null) {
                    snackbarHostState.showSnackbar(addFailureMessage)
                    placeViewModel.acknowledgeAddPlaceFailure()
                }
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
    expanded: Boolean = true,
    onClick: () -> Unit = {},
) {
    val addPlaceDialogOpened = remember { mutableStateOf(false) }
    val palette = LocalWeatherPalette.current

    ExtendedFloatingActionButton(
        modifier = modifier,
        expanded = expanded,
        containerColor = palette.accent,
        contentColor = palette.onAccent,
        onClick = {
            onClick()
            addPlaceDialogOpened.value = true
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.title_dialog_add_place)
            )
        },
        text = { Text(text = stringResource(id = R.string.title_dialog_add_place)) }
    )

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
    formattingService: FormattingService,
    lazyListState: LazyListState = rememberLazyListState(),
    isRefreshing: Boolean = false,
    refreshOutcome: RefreshOutcome? = null,
    pendingPlaceName: String? = null,
    onRefreshOutcomeShown: () -> Unit = {},
    onOpenPlace: (Place) -> Unit = {},
    onRefresh: () -> Unit = {},
    onDismiss: (Place, Int) -> Unit = { _, _ -> },
    onMovePlace: (from: Int, to: Int) -> Unit = { _, _ -> }
) {
    //  formattingService is required rather than defaulted to FormattingService.getInstance().
    //  Reaching for the singleton here pinned one instance for the lifetime of the screen, and
    //  since Compose compares this unstable type by identity, changing a unit never redrew the
    //  cards. It now comes from SettingsViewModel, which republishes a new instance per change.
    // Dialog géré ici, hors du composable de l'item :
    // évite toute réutilisation d'état après un Undo qui remettrait le dialog au premier plan.
    var pendingDeletePlace by remember { mutableStateOf<Place?>(null) }
    var pendingDeleteIndex by remember { mutableStateOf(-1) }
    var pendingResetCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    //  The list reorders in memory while the finger is down and is persisted once on drop, so a
    //  drag across ten rows is one storage write rather than ten. Keyed on placeList so an update
    //  arriving from the repository - a refresh, a delete - replaces this copy rather than being
    //  overwritten by it.
    var orderedPlaces by remember(placeList) { mutableStateOf(placeList) }

    val reorderState = rememberDragToReorderState(
        lazyListState = lazyListState,
        itemCount = orderedPlaces.size,
        //  The title and the refresh outcome occupy the first lazy item, so the cards start at 1.
        leadingItemCount = 1,
        onMove = { from, to ->
            orderedPlaces = orderedPlaces.toMutableList().apply { add(to, removeAt(from)) }
        },
        onMoveCompleted = onMovePlace
    )
    

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

    //  PullToRefreshBox replaces roughly eighty lines of hand-rolled NestedScrollConnection that
    //  tracked cumulative drag, threshold crossing and gesture release by hand. It ships in
    //  material3, so this adds no dependency, and it frees the vertical drag gesture for the
    //  reordering below - the custom connection consumed scroll at the top of the list, which is
    //  exactly where a drag to reorder starts.
    //
    //  The drop-shaped indicator is kept rather than falling back to the Material default, driven
    //  by the state's distanceFraction instead of a hand-tracked pixel offset.
    val pullToRefreshState = rememberPullToRefreshState()

    //  PullToRefreshBox holds the indicator extended for the whole refresh and only animates
    //  distanceFraction back to zero once isRefreshing clears. Without this latch the drop would
    //  reappear for that retraction and be seen draining from full to empty after the refresh had
    //  already finished - the pull replayed backwards.
    //
    //  Armed when a refresh starts, disarmed once the indicator has actually come to rest, so the
    //  drop is only ever drawn for a pull the user is making.
    var retractingAfterRefresh by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing, pullToRefreshState.distanceFraction) {
        when {
            isRefreshing -> retractingAfterRefresh = true
            pullToRefreshState.distanceFraction == 0f -> retractingAfterRefresh = false
        }
    }

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        indicator = {
            if (!isRefreshing && !retractingAfterRefresh && pullToRefreshState.distanceFraction > 0f) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center
                ) {
                    PullToRefreshDropIndicator(
                        displayedDragY = pullToRefreshState.distanceFraction * PULL_THRESHOLD_PX,
                        pullThreshold = PULL_THRESHOLD_PX,
                        showReleaseText = pullToRefreshState.distanceFraction >= 1f
                    )
                }
            }
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        strokeWidth = 4.dp
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState
        ) {
            //  Keyed on the place, not its position. With the index as the key, deleting or moving
            //  a row renumbers every row below it, so Compose sees "everything changed" instead of
            //  "one item moved" - which loses per-item state (the expanded card, the swipe offset)
            //  to whichever row inherits the number, and makes reordering impossible to animate.
            //
            //  geolocation is unique now that addPlaceAt refuses a place already held, but it is
            //  flattened to a String: Lazy list keys have to survive being saved to a Bundle, and
            //  a protobuf message does not. Passing the message itself compiles and then throws
            //  when the list state is saved.
            item(key = "header") {
                PlacesHeader(refreshOutcome = refreshOutcome, onOutcomeShown = onRefreshOutcomeShown)
            }
            itemsIndexed(
                items = orderedPlaces,
                key = { _, place -> place.identityKey }
            ) { index, place ->
                val isDragging = reorderState.draggingItemIndex == index
                Box(
                    modifier = Modifier
                        //  Neighbours slide to their new position instead of jumping. Not applied
                        //  to the dragged row itself: that one is positioned by the finger through
                        //  translationY below, and a placement animation would fight it.
                        .then(
                            if (isDragging) Modifier
                            else Modifier.animateItem(
                                placementSpec = spring(
                                    stiffness = Spring.StiffnessMediumLow,
                                    visibilityThreshold = IntOffset.VisibilityThreshold
                                )
                            )
                        )
                        //  Lifted above its neighbours so it is drawn over them while travelling.
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = if (isDragging) reorderState.draggingItemOffset else 0f
                            //  A slight lift, so it reads as picked up rather than stuck.
                            scaleX = if (isDragging) 1.02f else 1f
                            scaleY = if (isDragging) 1.02f else 1f
                            shadowElevation = if (isDragging) 12f else 0f
                        }
                        .dragToReorder(reorderState, index)
                ) {
                    SwipeablePlaceItem(
                        place = place,
                        formattingService = formattingService,
                        onClick = { onOpenPlace(place) },
                        onSwipedPastThreshold = { resetCallback ->
                            pendingDeletePlace = place
                            pendingDeleteIndex = index
                            pendingResetCallback = resetCallback
                        }
                    )
                }
            }
            if (pendingPlaceName != null) {
                item(key = "pending") {
                    PendingPlaceCard(
                        cityName = pendingPlaceName,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
    }
}

/**
 * The screen's title, and the one line reporting how the last refresh went.
 *
 * A refresh used to end in silence: the indicator retracted whether four places had been updated or
 * none. The outcome is announced and then cleared, so it reads as the result of the gesture just
 * made rather than as a permanent state.
 */
@Composable
private fun PlacesHeader(
    refreshOutcome: RefreshOutcome?,
    onOutcomeShown: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val formattingService = FormattingService.getInstance(LocalContext.current)

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)) {
        Text(
            text = stringResource(R.string.title_places),
            color = palette.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (refreshOutcome != null) {
            val finishedAt = formattingService.getFormattedTime(
                Date(refreshOutcome.finishedAt),
                TimeZone.getDefault()
            )
            PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = if (refreshOutcome.failed > 0) {
                    pluralStringResource(
                        R.plurals.status_refresh_failed,
                        refreshOutcome.failed,
                        refreshOutcome.failed
                    )
                } else {
                    stringResource(R.string.status_refresh_up_to_date, finishedAt)
                },
                accent = if (refreshOutcome.failed > 0) palette.orange else palette.green
            )

            //  Shown, then gone: the banner reports a gesture, it is not a state of the list.
            LaunchedEffect(refreshOutcome) {
                delay(OUTCOME_VISIBLE_MILLIS)
                onOutcomeShown()
            }
        }
    }
}

/**
 * What to tell the user when a request fails.
 *
 * Shared by the add dialog and the list, so the same failure never gets two different wordings.
 */
@StringRes
internal fun addPlaceErrorRes(status: RequestStatus): Int = when (status) {
    RequestStatus.TOO_SHORT -> R.string.error_place_query_too_short
    RequestStatus.NOT_FOUND -> R.string.error_no_results
    RequestStatus.ALREADY_PRESENT -> R.string.error_place_already_added
    RequestStatus.NOT_CONNECTED -> R.string.error_device_not_connected
    RequestStatus.NO_ANSWER,
    RequestStatus.AUTH_FAILED,
    RequestStatus.TOO_MANY_REQUESTS -> R.string.error_server_unreachable
    RequestStatus.UNKNOWN_ERROR -> R.string.error_unknown_error
}

/** How long the refresh outcome stays on screen before clearing itself. */
private const val OUTCOME_VISIBLE_MILLIS = 4000L

/** Drag distance, in pixels, at which the indicator switches to "release to refresh". */
private const val PULL_THRESHOLD_PX = 80f

@Composable
private fun PullToRefreshDropIndicator(
    displayedDragY: Float,
    pullThreshold: Float,
    showReleaseText: Boolean
) {
    val progress = (displayedDragY / pullThreshold).coerceIn(0f, 1f)
    val outline = MaterialTheme.colorScheme.primary
    val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)

    //  The drop settles to full size once the threshold is reached, so the moment it is ready to
    //  release is felt as much as read.
    val scale by animateFloatAsState(
        targetValue = if (showReleaseText) 1f else 0.7f + progress * 0.25f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dropScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .width(30.dp)
                .height(38.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
        ) {
            //  A teardrop: apex at the top, circular bowl at the bottom, the sides curving between
            //  the two. The previous version drew a full-width rectangle, which is why pulling the
            //  list painted a slab across the screen.
            val radius = size.width / 2f
            val centreX = size.width / 2f
            val centreY = size.height - radius

            val drop = Path().apply {
                moveTo(centreX, 0f)
                cubicTo(
                    centreX + radius * 0.6f, radius * 0.75f,
                    centreX + radius, centreY - radius * 0.75f,
                    centreX + radius, centreY
                )
                arcTo(
                    rect = Rect(
                        left = centreX - radius,
                        top = centreY - radius,
                        right = centreX + radius,
                        bottom = centreY + radius
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                cubicTo(
                    centreX - radius, centreY - radius * 0.75f,
                    centreX - radius * 0.6f, radius * 0.75f,
                    centreX, 0f
                )
                close()
            }

            //  Water rising inside the outline as the pull deepens.
            clipPath(drop) {
                drawRect(
                    color = fill,
                    topLeft = Offset(0f, size.height * (1f - progress)),
                    size = Size(size.width, size.height * progress)
                )
            }

            drawPath(
                path = drop,
                color = outline,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        if (showReleaseText) {
            Text(
                text = stringResource(R.string.action_release_to_refresh),
                style = MaterialTheme.typography.labelSmall,
                color = outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
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
    onClick: () -> Unit = {},
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
        PlaceSkyCard(
            place = place,
            formattingService = formattingService,
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp)
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

    //  viewModel(factory = ...) rather than constructing directly: a ViewModel built inside a
    //  composable is rebuilt on every recomposition and never reaches the ViewModelStore.
    PlacesScreenView(
        placeViewModel = viewModel(factory = PlaceViewModelFactory(LocalContext.current)),
        settingsViewModel = viewModel(factory = SettingsViewModelFactory(LocalContext.current))
    )
}

