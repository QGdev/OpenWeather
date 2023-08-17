
/*
 *  Copyright (c) 2019 - 2023
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

package fr.qgdev.openweather.widgets;

import static fr.qgdev.openweather.widgets.WidgetsBinder.bindWidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.util.TypedValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.Map;

import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.widgets.WidgetsBinder.WidgetType;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetsConfigurationActivity WidgetStandardPlaceInfoConfigureActivity}
 */
public class WidgetsProvider extends AppWidgetProvider {
	
	/**
	 * Provides widgets for each sizes
	 *
	 * @param context           the context
	 * @param place             the place to display
	 * @param formattingService the formatting service used to format the data
	 * @param sizes             the list of sizes to display
	 * @return the map of remote views
	 */
	private static Map<SizeF, RemoteViews> getRemoteViewsMap(@NonNull Context context, @NonNull Place place, @NonNull FormattingService formattingService, @NonNull List<SizeF> sizes) {
		if (sizes.isEmpty()) throw new IllegalArgumentException("sizes must not be empty");
		Map<SizeF, RemoteViews> views = new ArrayMap<>();
		
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
			for (SizeF size : sizes) {
				WidgetType type = WidgetType.fromSizeF(new SizeF(size.getWidth(), size.getHeight()));
				if (type == null) continue;
				Log.d("WidgetsProvider", "getRemoteViewsMap: " + size + " " + type);
				views.put(size, bindWidget(context, type, place, formattingService));
			}
		} else {
			for (WidgetType type : WidgetType.values()) {
				views.put(new SizeF(type.getWidth(), type.getHeight()),
						  bindWidget(context, type, place, formattingService));
			}
		}
		
		return views;
	}
	
	/**
	 * Update a specific widget
	 *
	 * @param context          the context of the application
	 * @param appWidgetManager the widget manager to update the widget
	 * @param repository       the repository to get the data
	 * @param appWidgetId      the widget id of the widget to update
	 */
	protected static void updateAppWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager,
													  @NonNull AppRepository repository, int appWidgetId) {
		
		// Get widgetsSettings
		WidgetsSettings widgetsSettings = repository.getWidgetsManager().loadWidgetSettings(appWidgetId, null);
		if (widgetsSettings == null) return;
		
		// Get actual widget size
		Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
		List<SizeF> sizes = appWidgetOptions.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
		
		repository.getPlaceFromPlaceIdLiveData(widgetsSettings.getPlaceId()).observeForever(new Observer<Place>() {
			@Override
			public void onChanged(Place place) {
				if (place == null) return;
				
				// Construct the RemoteViews object
				Map<SizeF, RemoteViews> views = getRemoteViewsMap(context, place, repository.getFormattingService(), sizes);
				
				// Instruct the widget manager to update the widget
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					appWidgetManager.updateAppWidget(appWidgetId, views.get(sizes.get(0)));
				} else {
					appWidgetManager.updateAppWidget(appWidgetId, views.get(sizes.get(0)));
				}
				
				repository.getPlaceFromPlaceIdLiveData(widgetsSettings.getPlaceId()).removeObserver(this);
			}
		});
	}
	
	/**
	 * dpToPx(@NonNull Context context, float dip)
	 * <p>
	 * Just a DP to PX converter method
	 * </p>
	 *
	 * @param context Application context in order to access to resources
	 * @param dip     DP value that you want to convert
	 * @return The DP converted value into PX
	 */
	private static int dpToPx(@NonNull Context context, float dip) {
		return (int) TypedValue.applyDimension(
				  TypedValue.COMPLEX_UNIT_DIP,
				  dip,
				  context.getResources().getDisplayMetrics());
	}
	
	/**
	 * Called in response to the {@link AppWidgetManager#ACTION_APPWIDGET_OPTIONS_CHANGED}
	 * broadcast when this widget has been layed out at a new size.
	 * <p>
	 * {@more}
	 *
	 * @param context          The {@link Context Context} in which this receiver is
	 *                         running.
	 * @param appWidgetManager A {@link AppWidgetManager} object you can call {@link
	 *                         AppWidgetManager#updateAppWidget} on.
	 * @param appWidgetId      The appWidgetId of the widget whose size changed.
	 * @param newOptions       The appWidgetId of the widget whose size changed.
	 * @see AppWidgetManager#ACTION_APPWIDGET_OPTIONS_CHANGED
	 */
	@Override
	public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		AppRepository repository = new AppRepository(context);
		
		// FOR TEST AND DEBUG
		int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
		int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
		int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
		
		Log.d("WidgetProvider", "onAppWidgetOptionsChanged: maxWidth = " + maxWidth + ", maxHeight = " + maxHeight + ", minWidth = " + minWidth + ", minHeight = " + minHeight);
		
		List<SizeF> sizes = newOptions.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
		if (sizes == null || sizes.isEmpty()) return;
		
		// Get WidgetsSettings and specially the placeId
		WidgetsSettings widgetsSettings = repository.getWidgetsManager().loadWidgetSettings(appWidgetId, null);
		if (widgetsSettings == null) return;   // Invalid widgetsSettings or not found
		
		int placeId = widgetsSettings.getPlaceId();
		if (placeId == 0) return;   // Invalid placeId
		
		// Get the place data from the placeId
		repository.getPlaceFromPlaceIdLiveData(placeId).observeForever(place -> {
			if (place == null) return;   // Invalid place or not retrieved yet
			
			Map<SizeF, RemoteViews> viewMapping = new ArrayMap<>();
			RemoteViews remoteViews;
			
			//	For Android 12 and above
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				for (SizeF size : sizes) {
				}
				for (SizeF size : sizes) {
					WidgetType widgetType = WidgetType.fromSizeF(size);
					Log.d("Widget", "onAppWidgetOptionsChanged: " + size.toString());
					if (widgetType == null) continue;
					Log.d("Widget", "onAppWidgetOptionsChanged: " + widgetType);
					viewMapping.put(size, bindWidget(context, widgetType, place, repository.getFormattingService()));
				}
				remoteViews = new RemoteViews(viewMapping);
			}
			//	For Android 11 and below
			else {
				WidgetType widgetType = WidgetType.fromSizeF(sizes.get(0));
				remoteViews = bindWidget(context, widgetType, place, repository.getFormattingService());
			}
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
			
		});
	}
	
	/**
	 * Update the widget with the given data
	 *
	 * @param context          The context
	 * @param appWidgetManager The widget manager
	 * @param appWidgetIds     All the widget ids
	 */
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		AppRepository repository = new AppRepository(context);
		
		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, repository, appWidgetId);
		}
	}
	
	/**
	 * Will handle receiving broadcast intents sent
	 *
	 * @param context The Context in which the receiver is running.
	 * @param intent  The Intent being received.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, WidgetsProvider.class);
		int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		if (appWidgetIds != null && appWidgetIds.length > 0) {
			onUpdate(context, appWidgetManager, appWidgetIds);
		}
	}
	
	
	/**
	 * Called when the widget is deleted from the home screen.
	 *
	 * @param context      The context
	 * @param appWidgetIds The app widget id
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		
		AppRepository repository = new AppRepository(context);
		
		// When the user deletes the widget, delete the preference associated with it.
		for (int appWidgetId : appWidgetIds) {
			repository.getWidgetsManager().deleteWidgetSettings(appWidgetId);
		}
	}
}