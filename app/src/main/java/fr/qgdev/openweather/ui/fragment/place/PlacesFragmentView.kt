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

import fr.qgdev.openweather.data.repositories.RefreshProblem
import fr.qgdev.openweather.ui.onboarding.API_KEY_LENGTH
import fr.qgdev.openweather.ui.onboarding.isWellFormedApiKey
import fr.qgdev.openweather.ui.onboarding.FirstPlaceCard
import fr.qgdev.openweather.ui.onboarding.ApiKeyDialog
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import fr.qgdev.openweather.ui.utils.countryNameFromCode
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Arrangement
import fr.qgdev.openweather.ui.viewmodel.RefreshProgress
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import fr.qgdev.openweather.ui.viewmodel.identityKey
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
    onOpenPlace: (Place) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val data by placeViewModel.placesState.collectAsState()
    val isRefreshing by placeViewModel.isRefreshing.collectAsState(initial = false)
    val refreshOutcome by placeViewModel.lastRefreshOutcome.collectAsState()
    val refreshProgress by placeViewModel.refreshProgress.collectAsState()
    val pendingPlaceName by placeViewModel.pendingPlaceName.collectAsState()
    val addPlaceFailure by placeViewModel.addPlaceFailure.collectAsState()
    val refreshProblem by placeViewModel.refreshProblem.collectAsState()
    val placesRefreshing by placeViewModel.placesRefreshing.collectAsState()
    val settings by settingsViewModel.settingsState.collectAsState()
    //  Republished as a new instance on every settings change, which is what makes the cards
    //  below redraw when a unit changes.
    val formattingService by settingsViewModel.formattingServiceState.collectAsState()
    val apiKey = settings?.apiKey.orEmpty()
    val isApiKeyValid = isWellFormedApiKey(apiKey)
    var apiKeyDialogOpened by remember { mutableStateOf(false) }
    var addPlaceDialogOpened by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    //  The place just deleted, offered back at the top of the list for a while.
    var recentlyDeleted by remember { mutableStateOf<DeletedPlace?>(null) }
    val undoDelete: (DeletedPlace) -> Unit = { deleted ->
        //  Restored where it was, not appended: addPlace put the place back at the end of the
        //  list, so undoing a delete silently reordered the list.
        placeViewModel.restorePlace(deleted.place, deleted.index)
        recentlyDeleted = null
    }

    //  What the banners at the top of the list report, whether the list has places or not.
    //  A refused key is about the key it was refused with: changing the key clears it, rather
    //  than keeping the banner up until the next refresh proves the new one.
    var keyOfProblem by remember { mutableStateOf(apiKey) }
    LaunchedEffect(apiKey) {
        if (apiKey != keyOfProblem) {
            keyOfProblem = apiKey
            if (refreshProblem?.status == RequestStatus.AUTH_FAILED) placeViewModel.clearRefreshProblem()
        }
    }

    val banners = PlacesBannersState(
        apiKey = apiKey,
        onFixApiKey = { apiKeyDialogOpened = true },
        addFailure = addPlaceFailure,
        onAddFailureShown = { placeViewModel.acknowledgeAddPlaceFailure() },
        refreshProblem = refreshProblem,
        onOpenSettings = onOpenSettings,
        //  The oldest observation on screen: what "offline" leaves the user reading.
        dataTime = data.orEmpty().map { it.currentWeather.dt }.filter { it > 0 }.minOrNull(),
        nextPeriodicUpdate = refreshProblem?.takeIf { settings?.periodicUpdateEnabled == true }
            ?.let { it.at + settings!!.updatePeriod.durationMillis },
        //  A refresh the pull did not start - the periodic worker's - has no indicator of its own.
        backgroundRefreshCount = placesRefreshing.takeIf { !isRefreshing } ?: 0,
        refreshOutcome = refreshOutcome,
        onRefreshOutcomeShown = { placeViewModel.acknowledgeRefreshOutcome() },
        recentlyDeleted = recentlyDeleted,
        onUndoDelete = undoDelete,
        onDeletedShown = { recentlyDeleted = null }
    )

    AppTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            //  The onboarding asks for the key before the tabs are reached. A key missing or
            //  malformed afterwards - edited in the settings - is a banner over the places, which
            //  stay readable, rather than a screen in their place.
            when {
                data == null -> { /* chargement en cours : rien à afficher */ }
                data!!.isEmpty() -> {
                    EmptyPlacesScreen(
                        banners = banners,
                        onAddPlace = if (isApiKeyValid) ({ addPlaceDialogOpened = true }) else null
                    )
                }
                else -> {
                    LoadPlacesScreen {
                        PlacesList(
                            placeList = data!!,
                            formattingService = formattingService,
                            lazyListState = lazyListState,
                            isRefreshing = isRefreshing,
                            refreshProgress = refreshProgress,
                            banners = banners,
                            pendingPlaceName = pendingPlaceName,
                            onOpenPlace = onOpenPlace,
                            onRefresh = { placeViewModel.refreshAllPlaces() },
                            onMovePlace = { from, to ->
                                placeViewModel.movePlace(from, to)
                            },
                            onDismiss = { place, index ->
                                placeViewModel.deletePlace(place)
                                recentlyDeleted = DeletedPlace(place, index)
                            }
                        )
                    }
                }
            }
            //  Adding a place needs a key to fetch its weather with.
            if (isApiKeyValid) {
                AddPlaceFloatingActionButton(
                    modifier = Modifier
                        .padding(32.dp)
                        .align(Alignment.BottomEnd),
                    onClick = { addPlaceDialogOpened = true },
                    //  Labelled while the list is at rest, icon-only once scrolled: the label
                    //  says what the button does, but it should not sit over the cards being read.
                    expanded = lazyListState.firstVisibleItemIndex == 0 &&
                            lazyListState.firstVisibleItemScrollOffset == 0
                )
            }

            if (addPlaceDialogOpened) {
                AddPlaceDialog(
                    placeViewModel = placeViewModel,
                    onDismissRequest = { addPlaceDialogOpened = false }
                )
            }
            if (apiKeyDialogOpened) {
                ApiKeyDialog(
                    currentKey = apiKey,
                    onSave = { settingsViewModel.setApiKey(it) },
                    onDismissRequest = { apiKeyDialogOpened = false }
                )
            }

        }
    }
}

/**
 * No places yet: the title and banners as over a list, and in the middle the invitation to add
 * a first one - the add button alone is easy to miss. Without a key there is nothing to add with,
 * and the banner saying so is left alone.
 */
@Composable
private fun EmptyPlacesScreen(
    banners: PlacesBannersState,
    onAddPlace: (() -> Unit)?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalWeatherPalette.current.screen)
    ) {
        PlacesHeader(banners)
        if (onAddPlace != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                FirstPlaceCard(onAddPlace = onAddPlace)
            }
        }
    }
}

@Composable
private fun AddPlaceFloatingActionButton(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        expanded = expanded,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(id = R.string.title_dialog_add_place)
            )
        },
        text = { Text(text = stringResource(id = R.string.title_dialog_add_place)) }
    )
}

@Composable
fun LoadPlacesScreen(
    content: @Composable () -> Unit
) {
    //  No longer gated on the key: without one, the places stay on screen under a banner.
    Box(
        modifier = Modifier
            .fillMaxSize()
            //  The redesign's ground, as on the other screens: the old colorBackground resource is
            //  pure white in light mode, a shade apart from the bars around it.
            .background(color = LocalWeatherPalette.current.screen)
    ) {
        content()
    }
}

@Composable
private fun PlacesList(
    placeList: List<Place>,
    formattingService: FormattingService,
    lazyListState: LazyListState = rememberLazyListState(),
    isRefreshing: Boolean = false,
    refreshProgress: RefreshProgress? = null,
    banners: PlacesBannersState,
    pendingPlaceName: String? = null,
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
        DeletePlaceDialog(
            place = place,
            onKeep = {
                pendingDeletePlace = null
                pendingResetCallback?.invoke()
                pendingResetCallback = null
            },
            onDelete = {
                val placeToDelete = pendingDeletePlace
                val indexToDelete = pendingDeleteIndex

                pendingDeletePlace = null
                pendingDeleteIndex = -1
                pendingResetCallback = null  // Nettoyer sans appeler reset()

                if (placeToDelete != null && indexToDelete != -1) {
                    onDismiss(placeToDelete, indexToDelete)
                }
            }
        )
    }

    //  PullToRefreshBox replaces roughly eighty lines of hand-rolled NestedScrollConnection that
    //  tracked cumulative drag, threshold crossing and gesture release by hand. It ships in
    //  material3, so this adds no dependency, and it frees the vertical drag gesture for the
    //  reordering below - the custom connection consumed scroll at the top of the list, which is
    //  exactly where a drag to reorder starts.
    val pullToRefreshState = rememberPullToRefreshState()

    //  PullToRefreshBox holds the indicator extended for the whole refresh and only animates
    //  distanceFraction back to zero once isRefreshing clears. Without this latch the arc would
    //  reappear for that retraction and be seen draining from full to empty after the refresh had
    //  already finished - the pull replayed backwards.
    //
    //  Armed when a refresh starts, disarmed once the indicator has actually come to rest, so the
    //  arc is only ever drawn for a pull the user is making.
    var retractingAfterRefresh by remember { mutableStateOf(false) }
    LaunchedEffect(isRefreshing, pullToRefreshState.distanceFraction) {
        when {
            isRefreshing -> retractingAfterRefresh = true
            pullToRefreshState.distanceFraction == 0f -> retractingAfterRefresh = false
        }
    }

    //  Dimmed, not hidden, while the refresh runs: the old values stay readable, and the list does
    //  not blink when the new ones land.
    val cardsAlpha by animateFloatAsState(
        targetValue = if (isRefreshing) REFRESHING_CARDS_ALPHA else 1f,
        label = "cardsAlpha"
    )

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        indicator = {
            val pulling = !retractingAfterRefresh && pullToRefreshState.distanceFraction > 0f
            if (isRefreshing || pulling) {
                PullToRefreshIndicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    distanceFraction = pullToRefreshState.distanceFraction,
                    isRefreshing = isRefreshing,
                    refreshProgress = refreshProgress
                )
            }
        }
    ) {
        LazyColumn(
            //  The list follows the finger down, opening the band the indicator sits in, rather
            //  than having the indicator drawn over the title.
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = pullToRefreshState.distanceFraction * PULL_INDICATOR_HEIGHT.toPx()
                },
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
                PlacesHeader(banners)
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
                            alpha = cardsAlpha
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

/** Everything the banners at the top of the list may have to say, and what they act on. */
private class PlacesBannersState(
    val apiKey: String,
    val onFixApiKey: () -> Unit,
    val addFailure: RequestStatus?,
    val onAddFailureShown: () -> Unit,
    val refreshProblem: RefreshProblem?,
    val onOpenSettings: () -> Unit,
    val dataTime: Long?,
    val nextPeriodicUpdate: Long?,
    val backgroundRefreshCount: Int,
    val refreshOutcome: RefreshOutcome?,
    val onRefreshOutcomeShown: () -> Unit,
    val recentlyDeleted: DeletedPlace?,
    val onUndoDelete: (DeletedPlace) -> Unit,
    val onDeletedShown: () -> Unit
)

/**
 * The screen's title, and the one banner under it.
 *
 * One at a time. What answers a gesture just made comes first, since it will not wait: the undo
 * of a deletion, a place that could not be added. Then the canvas's order - a refused key, a
 * malformed one, no network, the quota, a refresh running in the background - and last how the
 * last pull went. A refresh used to end in silence, so its outcome is announced and then cleared,
 * reading as the result of the gesture rather than as a state of the list.
 */
@Composable
private fun PlacesHeader(banners: PlacesBannersState) {
    val palette = LocalWeatherPalette.current
    val formattingService = FormattingService.getInstance(LocalContext.current)
    val refreshOutcome = banners.refreshOutcome
    val recentlyDeleted = banners.recentlyDeleted
    val apiKey = banners.apiKey
    val addFailure = banners.addFailure
    val problem = banners.refreshProblem?.status

    //  Shown, then gone, even when another banner took its place meanwhile.
    if (refreshOutcome != null) {
        LaunchedEffect(refreshOutcome) {
            delay(OUTCOME_VISIBLE_MILLIS)
            banners.onRefreshOutcomeShown()
        }
    }
    //  A first download that fails leaves nothing behind - no half-built card in the list - so the
    //  failure has to be said, or the place would simply never appear.
    if (addFailure != null) {
        LaunchedEffect(addFailure) {
            delay(ADD_FAILURE_VISIBLE_MILLIS)
            banners.onAddFailureShown()
        }
    }
    fun time(millis: Long) = formattingService.getFormattedTime(Date(millis), TimeZone.getDefault())

    Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp)) {
        Text(
            text = stringResource(R.string.title_places),
            color = palette.textPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )

        when {
            recentlyDeleted != null -> DeletedPlaceBanner(
                deleted = recentlyDeleted,
                onUndo = { banners.onUndoDelete(recentlyDeleted) },
                onShown = banners.onDeletedShown,
                modifier = Modifier.padding(top = 12.dp)
            )
            addFailure != null -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(addPlaceErrorRes(addFailure)),
                accent = palette.orange
            )
            problem == RequestStatus.AUTH_FAILED -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = stringResource(R.string.banner_api_key_refused),
                accent = palette.red,
                actionLabel = stringResource(R.string.title_settings),
                onAction = banners.onOpenSettings
            )
            !isWellFormedApiKey(apiKey) -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = when {
                    apiKey.isEmpty() -> stringResource(R.string.banner_api_key_missing)
                    apiKey.length < API_KEY_LENGTH ->
                        stringResource(R.string.onboarding_key_too_short, apiKey.length, API_KEY_LENGTH)
                    apiKey.length > API_KEY_LENGTH ->
                        stringResource(R.string.onboarding_key_too_long, apiKey.length, API_KEY_LENGTH)
                    else -> stringResource(R.string.onboarding_key_not_alphanumeric)
                },
                accent = palette.amber,
                actionLabel = stringResource(
                    if (apiKey.isEmpty()) R.string.action_add_key else R.string.action_fix
                ),
                onAction = banners.onFixApiKey
            )
            //  No "Retry": it states a passing condition, and pulling the list is already the way
            //  to try again.
            problem == RequestStatus.NOT_CONNECTED -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = banners.dataTime?.let { stringResource(R.string.banner_offline_since, time(it)) }
                    ?: stringResource(R.string.banner_offline),
                accent = palette.orange
            )
            problem == RequestStatus.TOO_MANY_REQUESTS -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = banners.nextPeriodicUpdate
                    ?.let { stringResource(R.string.banner_quota_next_try, time(it)) }
                    ?: stringResource(R.string.banner_quota),
                accent = palette.amber
            )
            banners.backgroundRefreshCount > 0 -> PlacesBanner(
                modifier = Modifier.padding(top = 12.dp),
                text = pluralStringResource(
                    R.plurals.banner_refreshing,
                    banners.backgroundRefreshCount,
                    banners.backgroundRefreshCount
                ),
                accent = MaterialTheme.colorScheme.primary,
                tinted = false
            )
            refreshOutcome != null -> {
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
                    accent = if (refreshOutcome.failed > 0) palette.orange else palette.green,
                    icon = if (refreshOutcome.failed > 0) null else Icons.Default.Check
                )
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

/** A place just deleted, and where it stood, so undoing puts it back there. */
private data class DeletedPlace(val place: Place, val index: Int)

/** How long a deleted place can be brought back, as long as the snackbar that used to offer it. */
private const val UNDO_VISIBLE_MILLIS = 10_000L

/**
 * "Venice, Italy deleted · Undo", at the top of the list where the refresh outcome is told, and in
 * the same banner: in the red of destructive actions, the undo as its action. Clears itself after
 * a while, the deletion then being final.
 */
@Composable
private fun DeletedPlaceBanner(
    deleted: DeletedPlace,
    onUndo: () -> Unit,
    onShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(deleted) {
        delay(UNDO_VISIBLE_MILLIS)
        onShown()
    }
    PlacesBanner(
        modifier = modifier,
        text = String.format(
            stringResource(R.string.place_deleted),
            deleted.place.geolocation.city,
            deleted.place.displayCountry()
        ),
        accent = LocalWeatherPalette.current.red,
        actionLabel = stringResource(R.string.action_undo),
        onAction = onUndo
    )
}

/** How long a place that could not be added is reported. */
private const val ADD_FAILURE_VISIBLE_MILLIS = 6000L

/** How long the refresh outcome stays on screen before clearing itself. */
private const val OUTCOME_VISIBLE_MILLIS = 2000L

/** Height of the band the pull opens above the list, where the indicator sits. */
private val PULL_INDICATOR_HEIGHT = 84.dp

/** How far the cards fade while a refresh runs. */
private const val REFRESHING_CARDS_ALPHA = 0.55f

/**
 * The pull-to-refresh indicator, in three steps: an arc filling with the distance pulled, a full
 * badge once letting go will refresh, and that badge spinning while the places are fetched, with
 * how many are done - each place is a request of its own, so there is a real wait to count down.
 */
@Composable
private fun PullToRefreshIndicator(
    distanceFraction: Float,
    isRefreshing: Boolean,
    refreshProgress: RefreshProgress?,
    modifier: Modifier = Modifier
) {
    val palette = LocalWeatherPalette.current
    val accent = MaterialTheme.colorScheme.primary
    val readyToRelease = !isRefreshing && distanceFraction >= 1f
    val armed = isRefreshing || readyToRelease

    Column(
        modifier = modifier
            .height(PULL_INDICATOR_HEIGHT)
            .padding(top = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
            if (armed) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
                )
            }
            when {
                isRefreshing -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = accent,
                    trackColor = accent.copy(alpha = 0.25f),
                    strokeWidth = 2.dp
                )
                readyToRelease -> Canvas(modifier = Modifier.size(20.dp)) {
                    //  A clock face: the data's time is what letting go will change.
                    val unit = size.minDimension / 26f
                    val stroke = Stroke(width = 2.2f * unit, cap = StrokeCap.Round)
                    drawCircle(color = accent, radius = 9.8f * unit, style = stroke)
                    drawLine(accent, Offset(13f * unit, 7.5f * unit), Offset(13f * unit, 13.5f * unit),
                        strokeWidth = stroke.width, cap = StrokeCap.Round)
                    drawLine(accent, Offset(13f * unit, 13.5f * unit), Offset(17f * unit, 15.9f * unit),
                        strokeWidth = stroke.width, cap = StrokeCap.Round)
                }
                else -> Canvas(modifier = Modifier.size(22.dp)) {
                    //  Filled clockwise from the top as the pull deepens, over a faint full turn.
                    val unit = size.minDimension / 26f
                    val stroke = Stroke(width = 2.2f * unit, cap = StrokeCap.Round)
                    val radius = 9.8f * unit
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2, radius * 2)
                    drawCircle(color = palette.textQuiet.copy(alpha = 0.25f), radius = radius, style = stroke)
                    drawArc(
                        color = palette.textQuiet,
                        startAngle = -90f,
                        sweepAngle = 360f * distanceFraction.coerceIn(0f, 1f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                }
            }
        }

        Text(
            text = when {
                isRefreshing && refreshProgress != null -> stringResource(
                    R.string.status_refreshing_progress,
                    refreshProgress.done,
                    refreshProgress.total
                )
                armed -> stringResource(R.string.action_release_to_refresh)
                else -> stringResource(R.string.action_pull_to_refresh)
            },
            color = if (armed) accent else palette.textQuiet,
            fontSize = 10.5.sp,
            fontWeight = if (readyToRelease) FontWeight.Medium else FontWeight.Normal
        )
    }
}

/** "France" rather than "FR", as on the cards; the code itself when it has no name (§12.13). */
private fun Place.displayCountry(): String =
    countryNameFromCode(geolocation.countryCode).ifEmpty { geolocation.countryCode.uppercase() }

/**
 * Asks before a swiped place goes: its name in full, keeping on the left, deleting on the right in
 * the red of destructive actions.
 */
@Composable
private fun DeletePlaceDialog(
    place: Place,
    onKeep: () -> Unit,
    onDelete: () -> Unit
) {
    val palette = LocalWeatherPalette.current
    val shape = RoundedCornerShape(16.dp)
    Dialog(onDismissRequest = onKeep) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(palette.screen)
                .border(1.dp, palette.outlineStrong, shape)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.dialog_confirmation_title_delete_place),
                color = palette.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format(
                    stringResource(R.string.dialog_confirmation_message_delete_place),
                    place.geolocation.city,
                    place.displayCountry()
                ),
                color = palette.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .border(1.dp, palette.outlineStrong, CircleShape)
                        .clickable(onClick = onKeep)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dialog_confirmation_choice_no),
                        color = palette.textPrimary,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(palette.red)
                        .clickable(onClick = onDelete)
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dialog_confirmation_choice_yes),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@Composable
private fun SwipeablePlaceItem(
    place: Place,
    formattingService: FormattingService,
    hero: Boolean = false,
    onClick: () -> Unit = {},
    onSwipedPastThreshold: ((resetCallback: () -> Unit) -> Unit)
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState()

    //  The swipe state is saved under the place's key, so a place brought back by Undo comes back
    //  still swiped away. That inherited state is reset silently, without asking again. It used
    //  to be reset by an effect of its own, which ran alongside the one below and lost the race:
    //  the confirmation reopened for the place just restored.
    var inheritedSwipe by remember {
        mutableStateOf(dismissState.settledValue != SwipeToDismissBoxValue.Settled)
    }

    //  Keyed on where the card has come to rest, not on currentValue: currentValue follows the
    //  finger and turns to "dismissed" as soon as the threshold is crossed, so the confirmation
    //  opened while the card was still held - under a background reading "Release to delete".
    LaunchedEffect(dismissState.settledValue) {
        when {
            dismissState.settledValue == SwipeToDismissBoxValue.Settled -> inheritedSwipe = false
            //  Snapped, not animated: the card is only just back in the list, and an animated reset
            //  started there stopped halfway, leaving the card half swiped.
            inheritedSwipe -> dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            else -> {
                onSwipedPastThreshold { scope.launch { dismissState.reset() } }
                scope.launch { dismissState.reset() }
            }
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
            val palette = LocalWeatherPalette.current
            val red = palette.red
            //  Past the threshold, letting go will ask to delete: the banner deepens and says so.
            val armed = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            val fill by animateFloatAsState(
                targetValue = if (armed) 0.18f else 0.09f,
                label = "swipeFill"
            )
            val shape = RoundedCornerShape(if (hero) 24.dp else 22.dp)

            //  The banners' look - a faint red ground under a red outline - rather than a slab of
            //  solid red, faded in with the distance swiped.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    //  Behind the card exactly: its margins and its corners.
                    .padding(horizontal = 16.dp, vertical = 5.dp)
                    .graphicsLayer { this.alpha = alpha }
                    .clip(shape)
                    .background(red.copy(alpha = fill))
                    .border(1.dp, red.copy(alpha = 0.36f), shape),
                contentAlignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.description_delete_icon),
                        tint = red,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(
                            if (armed) R.string.action_release_to_delete else R.string.action_delete
                        ),
                        color = palette.textSecondary,
                        fontSize = 12.5.sp,
                        fontWeight = if (armed) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }
    ) {
        PlaceSkyCard(
            place = place,
            formattingService = formattingService,
            hero = hero,
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

