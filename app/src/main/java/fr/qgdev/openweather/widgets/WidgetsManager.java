
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

package fr.qgdev.openweather.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import fr.qgdev.openweather.data.storage.SecuredPreferenceDataStore;
import fr.qgdev.openweather.repositories.PeriodicUpdaterWorker;

/**
 * WidgetsManager
 * <p>
 * 	A class to manage widgets settings.
 * 	Uses a SecuredPreferenceDataStore to store widgets settings.
 * 	Also uses a WorkManager to schedule a work request to update all places and widgets periodically.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
public final class WidgetsManager {
	
	private static final String FILE_NAME = "fr.qgdev.openweather_widget";
	private static final String PREFIX_WIDGET = "widget_";
	private static final String WORKER_TASK_NAME = "PeriodicUpdaterWorker";
	private static final AtomicReference<WidgetsManager> instance = new AtomicReference<>(null);
	private final SecuredPreferenceDataStore securedPreferenceDataStore;
	
	
	public static WidgetsManager getInstance(@NonNull Context context) {
		Context applicationContext = context.getApplicationContext();
		
		if (instance.get() == null) {
			synchronized (WidgetsManager.class) {
				instance.compareAndSet(null, new WidgetsManager(applicationContext));
			}
		}
		return instance.get();
	}
	
	private WidgetsManager(@NonNull Context context) {
		this.securedPreferenceDataStore = new SecuredPreferenceDataStore(context,
				  FILE_NAME);
	}
	
	/**
	 * Generate the storage key name for a widget
	 *
	 * @param appWidgetId The id of the widget
	 * @return The key name
	 */
	private static String getKeyName(int appWidgetId) {
		return PREFIX_WIDGET + appWidgetId;
	}
	
	/**
	 * Save widget settings
	 *
	 * @param widgetsSettings The id of the widget
	 */
	public boolean saveWidgetSettings(@NonNull WidgetsSettings widgetsSettings) {
		int appWidgetId = widgetsSettings.getWidgetId();
		String key = getKeyName(appWidgetId);
		String json;
		
		try {
			json = widgetsSettings.toJsonString();
		} catch (JSONException e) {
			return false;
		}
		
		securedPreferenceDataStore.putString(key, json);
		
		//	Checks if the saving process was successful
		return widgetsSettings.equals(loadWidgetSettings(appWidgetId, null));
	}
	
	/**
	 * Load widget settings
	 *
	 * @param appWidgetId The id of the widget
	 * @return An object containing the settings from WidgetsSettings class
	 */
	public WidgetsSettings loadWidgetSettings(int appWidgetId, Object ifNotFound) {
		String key = getKeyName(appWidgetId);
		if (!securedPreferenceDataStore.contains(key)) return (WidgetsSettings) ifNotFound;
		String json = securedPreferenceDataStore.getString(key, null);
		if (json == null) return (WidgetsSettings) ifNotFound;
		
		try {
			JSONObject jsonObject = new JSONObject(json);
			WidgetsSettings widgetsSettings = WidgetsSettings.fromJson(jsonObject);
			
			if (widgetsSettings == null) return (WidgetsSettings) ifNotFound;
			return widgetsSettings;
		} catch (JSONException e) {
			return (WidgetsSettings) ifNotFound;
		}
	}
	
	/**
	 * Delete widget settings
	 *
	 * @param appWidgetId The id of the widget
	 */
	public void deleteWidgetSettings(int appWidgetId) {
		securedPreferenceDataStore.remove(getKeyName(appWidgetId));
	}
	
	/**
	 * Check if widget settings are present
	 *
	 * @param appWidgetId The id of the widget
	 * @return True if settings are present, false otherwise
	 */
	public boolean isWidgetSettingsPresent(int appWidgetId) {
		return securedPreferenceDataStore.contains(getKeyName(appWidgetId));
	}
	
	/**
	 * Will send a broadcast to all widgets to update them.
	 *
	 * @param context the context used to send the broadcast
	 */
	public void updateWidgets(@NonNull Context context) {
		Intent updateIntent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
		updateIntent.setPackage("fr.qgdev.openweather");
		context.sendBroadcast(updateIntent, "fr.qgdev.openweather.permission.UPDATE_WIDGET");
	}
	
	/**
	 * Points the widgets that show one place at another, keeping their other settings.
	 * <p>
	 * Used when a place is replaced by a nearby one: a widget refers to its place by identity key,
	 * which the new coordinates change, and would otherwise lose it.
	 * </p>
	 *
	 * @param context     used to list the widgets placed
	 * @param oldPlaceKey the identity key of the place being replaced
	 * @param newPlaceKey the identity key of the place replacing it
	 */
	public void repointWidgets(@NonNull Context context, @NonNull String oldPlaceKey, @NonNull String newPlaceKey) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] allIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetsProvider.class));
		for (int appWidgetId : allIds) {
			WidgetsSettings settings = loadWidgetSettings(appWidgetId, null);
			if (settings == null || !oldPlaceKey.equals(settings.getPlaceId())) continue;
			saveWidgetSettings(new WidgetsSettings(newPlaceKey, appWidgetId,
					  settings.getBackgroundTransparency(), settings.getShowDetails()));
		}
	}
	
	/**
	 * Redraws the widgets showing one place, and only them.
	 * <p>
	 * Called whenever that place's data is written, so a widget changes when its data does,
	 * whichever path refreshed it: the background work, a pull-to-refresh, a retry.
	 * </p>
	 *
	 * @param context  the context used to send the broadcast
	 * @param placeKey the identity key of the place that changed
	 */
	public void updateWidgetsForPlace(@NonNull Context context, @NonNull String placeKey) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		int[] allIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetsProvider.class));
		
		List<Integer> matching = new ArrayList<>();
		for (int appWidgetId : allIds) {
			WidgetsSettings settings = loadWidgetSettings(appWidgetId, null);
			if (settings != null && placeKey.equals(settings.getPlaceId())) matching.add(appWidgetId);
		}
		if (matching.isEmpty()) return;
		
		int[] ids = new int[matching.size()];
		for (int i = 0; i < ids.length; i++) ids[i] = matching.get(i);
		
		// Explicit, so it reaches the provider only, carrying the ids it should redraw.
		Intent updateIntent = new Intent(context, WidgetsProvider.class);
		updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
		context.sendBroadcast(updateIntent);
	}
	
	/**
	 * Will remove scheduled work request that have been set by the app.
	 *
	 * @param context Use to get the WorkManager instance
	 */
	public void unscheduleWorkRequest(@NonNull Context context) {
		android.util.Log.d("WidgetsManager", "🛑 Cancelling work request: " + WORKER_TASK_NAME);
		try {
			WorkManager.getInstance(context).cancelUniqueWork(WORKER_TASK_NAME);
			android.util.Log.d("WidgetsManager", "✅ Work request cancelled successfully");
		} catch (Exception e) {
			android.util.Log.e("WidgetsManager", "❌ Error cancelling work: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Schedules the next run from within the running one.
	 * <p>
	 * {@link #scheduleWorkRequest} replaces the unique work, which is what a settings change wants,
	 * but called from the worker itself it would cancel the run in progress. This appends the next
	 * run after the current one instead, so the chain carries on at the chosen interval.
	 * </p>
	 *
	 * @param context              Use to get the WorkManager instance
	 * @param timeBeforeNextUpdate Delay before the next run
	 */
	public void scheduleNextRun(@NonNull Context context, @NonNull Duration timeBeforeNextUpdate) {
		Constraints constraints = new Constraints.Builder()
				  .setRequiresBatteryNotLow(true)
				  .build();
		
		OneTimeWorkRequest next = new OneTimeWorkRequest.Builder(PeriodicUpdaterWorker.class)
				  .setConstraints(constraints)
				  .setInitialDelay(timeBeforeNextUpdate)
				  .build();
		
		WorkManager.getInstance(context).enqueueUniqueWork(WORKER_TASK_NAME,
				  ExistingWorkPolicy.APPEND_OR_REPLACE,
				  next);
	}
	
	/**
	 * Will schedule a work request to update all places and widgets periodically.
	 *
	 * @param context Use to get the WorkManager instance
	 */
	public void scheduleWorkRequest(@NonNull Context context, @NonNull Duration timeBeforeNextUpdate) {
		android.util.Log.d("WidgetsManager", "📅 Scheduling work request: " + WORKER_TASK_NAME);
		android.util.Log.d("WidgetsManager", "   - Delay: " + timeBeforeNextUpdate.toMillis() + "ms (" + (timeBeforeNextUpdate.toMillis() / 60000) + " min)");
		
		try {
			Constraints constraints = new Constraints.Builder()
					  //.setRequiredNetworkType(NetworkType.CONNECTED)
					  .setRequiresBatteryNotLow(true)
					  .build();
			
			android.util.Log.d("WidgetsManager", "   - Constraints: Network=CONNECTED, BatteryNotLow=true");
			
			OneTimeWorkRequest oneTimeWorkRequest =
					  new OneTimeWorkRequest.Builder(PeriodicUpdaterWorker.class)
								 .setConstraints(constraints)
								 .setInitialDelay(timeBeforeNextUpdate)
								 .build();
			
			android.util.Log.d("WidgetsManager", "   - Work request built with ID: " + oneTimeWorkRequest.getId());
			
			WorkManager.getInstance(context).enqueueUniqueWork(WORKER_TASK_NAME,
					  ExistingWorkPolicy.REPLACE,
					  oneTimeWorkRequest);
			
			android.util.Log.d("WidgetsManager", "✅ Work request scheduled successfully");
		} catch (Exception e) {
			android.util.Log.e("WidgetsManager", "❌ Error scheduling work: " + e.getMessage(), e);
		}
	}
}
