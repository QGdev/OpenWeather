/*
 *  Copyright (c) 2019 - 2024
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

package fr.qgdev.openweather.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import androidx.core.os.BundleCompat
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.repositories.FormattingService
import fr.qgdev.openweather.ui.viewmodel.identityKey
import fr.qgdev.openweather.widgets.WidgetsBinder.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * WidgetsProvider
 *
 * Provides widgets to the system and updates them. Uses a [WidgetsBinder] to bind the data to the
 * widget layout and a [FormattingService] to format the data.
 *
 * Ported from the Java version whose update paths were commented out when AppRepository and its
 * LiveData went away: it now reads places from [PlaceRepository], and does so off the main thread
 * through [BroadcastReceiver.goAsync], since the DataStore read is a suspending call.
 *
 * @author Quentin GOMES DOS REIS
 * @version 2
 * @see AppWidgetProvider
 */
class WidgetsProvider : AppWidgetProvider() {

    /**
     * Will handle receiving broadcast intents sent.
     *
     * The app's own refresh broadcast (see [WidgetsManager.updateWidgets]) carries no widget ids,
     * so every widget of this provider is updated. The system's carries the ids it wants updated.
     * Either way the work happens in one asynchronous block: [goAsync] may be called only once per
     * broadcast.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            super.onReceive(context, intent)
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            ?: appWidgetManager.getAppWidgetIds(ComponentName(context, WidgetsProvider::class.java))
        if (appWidgetIds.isEmpty()) return

        doAsync {
            appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
        }
    }

    /**
     * Called when a widget has been laid out at a new size: the layout is picked by size, so it is
     * rebuilt.
     */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        doAsync { updateAppWidget(context, appWidgetManager, appWidgetId) }
    }

    /** When the user deletes a widget, its settings go with it. */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val widgetsManager = WidgetsManager.getInstance(context)
        appWidgetIds.forEach { widgetsManager.deleteWidgetSettings(it) }
    }

    private fun doAsync(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {

        /**
         * Update a specific widget.
         *
         * Does nothing when the widget has no settings yet (its configuration is still open) or
         * when its place is gone or has no forecast to show.
         */
        suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val widgetsSettings = WidgetsManager.getInstance(context)
                .loadWidgetSettings(appWidgetId, null) ?: return

            val place = PlaceRepository.getInstance(context).getPlaces()
                .firstOrNull { it.identityKey == widgetsSettings.placeId }
                ?.takeIf { it.canBeShownInWidget() }
                ?: return

            val formattingService = FormattingService.getInstance(context)
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val views = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RemoteViews(getRemoteViewsMap(context, place, formattingService, widgetsSettings, getSizes(options)))
            } else {
                getRemoteViewsFor(context, place, formattingService, widgetsSettings, getPortraitSize(options))
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Provides the one layout Android 11 can take, having no size-mapped RemoteViews: the
         * largest fitting the size, or the one-row layout when none does - as before the launcher
         * has reported any size.
         */
        private fun getRemoteViewsFor(
            context: Context,
            place: Place,
            formattingService: FormattingService,
            settings: WidgetsSettings,
            size: SizeF
        ): RemoteViews = WidgetsBinder.bindWidget(
            context, WidgetType.fromSizeF(size) ?: WidgetType.MINIMAL, place, formattingService,
            settings.backgroundTransparency, size.width, settings.showDetails
        )

        /**
         * The size, in dp, a widget is shown at in portrait on Android 11: launchers of that
         * version report its narrowest width with its tallest height for it.
         */
        private fun getPortraitSize(options: Bundle): SizeF = SizeF(
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).toFloat(),
            options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).toFloat()
        )

        /**
         * Provides a layout for each size the launcher may show the widget at.
         *
         * Right after placement the launcher may not have reported any size yet; every widget type
         * is then offered at its own size, and the launcher picks.
         */
        private fun getRemoteViewsMap(
            context: Context,
            place: Place,
            formattingService: FormattingService,
            settings: WidgetsSettings,
            sizes: List<SizeF>
        ): Map<SizeF, RemoteViews> {
            fun bind(type: WidgetType, width: Float) = WidgetsBinder.bindWidget(
                context, type, place, formattingService,
                settings.backgroundTransparency, width, settings.showDetails
            )

            //  Bound per reported size, not per type: the one-row widget shows more the wider it is.
            val views = sizes.mapNotNull { size ->
                WidgetType.fromSizeF(size)?.let { type -> size to bind(type, size.width) }
            }.toMap()
            if (views.isNotEmpty()) return views

            return WidgetType.entries.associate { type -> type.sizeF to bind(type, type.width.toFloat()) }
        }

        /** The sizes the launcher reported for the widget, in dp. */
        private fun getSizes(options: Bundle): List<SizeF> =
            BundleCompat.getParcelableArrayList(options, AppWidgetManager.OPTION_APPWIDGET_SIZES, SizeF::class.java)
                .orEmpty()
    }
}

/**
 * Whether a place has what the widget layouts read: a current observation, today's forecast and
 * the next hours. A place still downloading has none of them, and the binder would fail on it.
 */
internal fun Place.canBeShownInWidget(): Boolean =
    currentWeather.dt != 0L && dailyForecastListCount > 0 && hourlyForecastListCount > 4
