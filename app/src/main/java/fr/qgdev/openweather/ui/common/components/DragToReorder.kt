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

package fr.qgdev.openweather.ui.common.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Long-press-and-drag reordering for a [androidx.compose.foundation.lazy.LazyColumn].
 *
 * Compose has no first-party reorderable list, and this is deliberately the smallest thing that
 * does the job rather than a general-purpose one: vertical lists only, no nested scrolling, no
 * drag handles.
 *
 * [onMove] is called for each row the dragged item crosses, so the list can reorder underneath the
 * finger. It is meant to update in-memory state only. [onMoveCompleted] fires once on drop with the
 * original and final positions, which is where the move should be persisted - writing on every
 * crossing would put a storage write behind every few pixels of travel.
 */
class DragToReorderState internal constructor(
    private val lazyListState: LazyListState,
    private val scope: CoroutineScope,
    private val haptics: HapticFeedback,
    private val onMove: (from: Int, to: Int) -> Unit,
    private val onMoveCompleted: (from: Int, to: Int) -> Unit
) {
    /** Index of the row currently being dragged, or null when no drag is in progress. */
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    /** How far the dragged row has travelled from its resting position, in pixels. */
    var draggingItemOffset by mutableFloatStateOf(0f)
        private set

    /** Where the drag began, so a single move can be reported on drop. */
    private var dragStartIndex: Int? = null

    /** Size of the reorderable region. Rows beyond it - trailing spacers - are not valid targets. */
    internal var reorderableItemCount: Int = 0

    private val draggingItemLayoutInfo: LazyListItemInfo?
        get() = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == draggingItemIndex }

    internal fun onDragStart(index: Int) {
        if (index !in 0 until reorderableItemCount) return
        draggingItemIndex = index
        dragStartIndex = index
        draggingItemOffset = 0f
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    internal fun onDrag(deltaY: Float) {
        val currentIndex = draggingItemIndex ?: return
        draggingItemOffset += deltaY

        val draggingItem = draggingItemLayoutInfo ?: return
        //  Where the dragged row now sits visually, rather than where the layout thinks it is.
        val startOffset = draggingItem.offset + draggingItemOffset
        val endOffset = startOffset + draggingItem.size
        val middleOffset = startOffset + (endOffset - startOffset) / 2f

        //  Swap only once the dragged row's centre has passed the *centre* of its neighbour, not
        //  merely entered its bounds. Triggering on entry makes a row flip as soon as the drag
        //  begins to overlap it, and with tall cards that reads as the list snapping about under
        //  the finger. Requiring the midpoint means the swap lands where the eye expects it.
        val target = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            if (item.index == currentIndex || item.index !in 0 until reorderableItemCount) {
                false
            } else {
                val itemCentre = item.offset + item.size / 2f
                if (item.index > currentIndex) middleOffset > itemCentre
                else middleOffset < itemCentre
            }
        }

        if (target != null) {
            onMove(currentIndex, target.index)
            //  The rows swap underneath, so the dragged row is now where the target was. Its
            //  resting position moved with it, so the visual offset has to be reduced by the same
            //  amount or the row would jump.
            draggingItemOffset += draggingItem.offset - target.offset
            draggingItemIndex = target.index
        } else {
            autoScrollIfNearEdge(startOffset, endOffset)
        }
    }

    /**
     * Scrolls the list when the dragged row is held against the top or bottom edge, so items
     * outside the viewport can be reached without letting go.
     */
    private fun autoScrollIfNearEdge(startOffset: Float, endOffset: Float) {
        val viewportStart = lazyListState.layoutInfo.viewportStartOffset
        val viewportEnd = lazyListState.layoutInfo.viewportEndOffset

        val scrollAmount = when {
            draggingItemOffset > 0 -> (endOffset - viewportEnd).coerceAtLeast(0f)
            draggingItemOffset < 0 -> (startOffset - viewportStart).coerceAtMost(0f)
            else -> 0f
        }

        if (scrollAmount != 0f) {
            scope.launch { lazyListState.scrollBy(scrollAmount) }
        }
    }

    internal fun onDragEnd() {
        val from = dragStartIndex
        val to = draggingItemIndex
        if (from != null && to != null && from != to) {
            onMoveCompleted(from, to)
        }
        reset()
    }

    internal fun reset() {
        draggingItemIndex = null
        dragStartIndex = null
        draggingItemOffset = 0f
    }
}

/**
 * @param lazyListState the state of the list being reordered
 * @param itemCount number of reorderable rows; rows at or beyond this index cannot be dragged to
 * @param onMove called per crossed row, to reorder in-memory state
 * @param onMoveCompleted called once on drop with the original and final index, to persist
 */
@Composable
fun rememberDragToReorderState(
    lazyListState: LazyListState,
    itemCount: Int,
    onMove: (from: Int, to: Int) -> Unit,
    onMoveCompleted: (from: Int, to: Int) -> Unit
): DragToReorderState {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    //  Both callbacks are read through rememberUpdatedState rather than captured by remember.
    //  remember(lazyListState) would pin whichever lambdas existed at first composition, so a
    //  later one - closing over different state - would silently never be called.
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnMoveCompleted by rememberUpdatedState(onMoveCompleted)

    val state = remember(lazyListState) {
        DragToReorderState(
            lazyListState = lazyListState,
            scope = scope,
            haptics = haptics,
            onMove = { from, to -> currentOnMove(from, to) },
            onMoveCompleted = { from, to -> currentOnMoveCompleted(from, to) }
        )
    }
    state.reorderableItemCount = itemCount
    return state
}

/**
 * Starts a reorder drag on this row after a long press.
 *
 * Only vertical drag is consumed, so a horizontal swipe still reaches an enclosing
 * `SwipeToDismissBox`. The long press is what separates the two: a swipe begins immediately, a
 * reorder only after the press is held.
 */
@Composable
fun Modifier.dragToReorder(state: DragToReorderState, index: Int): Modifier {
    //  The index must NOT be a pointerInput key. Reordering changes the dragged row's index, which
    //  would tear down and restart this pointerInput mid-gesture. The coroutine is cancelled
    //  outright in that case, so onDragCancel never runs and draggingItemIndex keeps a stale value
    //  - leaving the lift attached to a position rather than to a card, so it appears to jump to
    //  whichever place the reorder moved into that slot.
    //
    //  Reading it through rememberUpdatedState keeps the gesture alive across reorders while still
    //  seeing the current index when a new drag starts.
    val currentIndex by rememberUpdatedState(index)

    return this.pointerInput(state) {
        detectDragGesturesAfterLongPress(
            onDragStart = { state.onDragStart(currentIndex) },
            onDrag = { change, dragAmount ->
                change.consume()
                state.onDrag(dragAmount.y)
            },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.reset() }
        )
    }
}
