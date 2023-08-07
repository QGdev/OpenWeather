
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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.databinding.WidgetStandardPlaceConfigureBinding;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Geolocation;
import fr.qgdev.openweather.repositories.settings.SecuredPreferenceDataStore;

/**
 * The configuration screen for the {@link WidgetStandardPlace WidgetStandardPlaceInfo} AppWidget.
 */
public class WidgetStandardPlaceConfigureActivity extends Activity {
	private static final String PREF_PREFIX_KEY = "__appwidget_";
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private int[] placeIds;
	
	
	private WidgetStandardPlaceConfigureBinding binding;
	
	/**
	 * WidgetStandardPlaceConfigureActivity constructor
	 */
	public WidgetStandardPlaceConfigureActivity() {
		super();
	}
	
	/**
	 * Save widget settings
	 *
	 * @param context     The context of the application
	 * @param appWidgetId The id of the widget
	 * @param placeId     The id of the place
	 */
	static void saveWidgetSettings(Context context, int appWidgetId, int placeId) {
		SecuredPreferenceDataStore spds = new SecuredPreferenceDataStore(context);
		spds.putInt(PREF_PREFIX_KEY + appWidgetId, placeId);
	}
	
	/**
	 * Load widget settings
	 *
	 * @param context     The context of the application
	 * @param appWidgetId The id of the widget
	 * @return The id of the place
	 */
	static Integer loadWidgetSettings(Context context, int appWidgetId) {
		SecuredPreferenceDataStore spds = new SecuredPreferenceDataStore(context);
		String key = PREF_PREFIX_KEY + appWidgetId;
		if (!spds.contains(key)) return null;
		return spds.getInt(key, 0);
	}
	
	/**
	 * Delete widget settings
	 *
	 * @param context     The context of the application
	 * @param appWidgetId The id of the widget
	 */
	static void deleteWidgetSettings(Context context, int appWidgetId) {
		SecuredPreferenceDataStore spds = new SecuredPreferenceDataStore(context);
		spds.remove(PREF_PREFIX_KEY + appWidgetId);
	}
	
	/**
	 * Check if widget settings are present
	 *
	 * @param context     The context of the application
	 * @param appWidgetId The id of the widget
	 * @return True if settings are present, false otherwise
	 */
	static boolean isWidgetSettingsPresent(Context context, int appWidgetId) {
		SecuredPreferenceDataStore spds = new SecuredPreferenceDataStore(context);
		return spds.contains(PREF_PREFIX_KEY + appWidgetId);
	}
	
	/**
	 * Called when the activity is created
	 *
	 * @param icicle The saved instance state
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);
		
		final Context context = WidgetStandardPlaceConfigureActivity.this;
		
		AppRepository repository = new AppRepository(context);
		
		binding = WidgetStandardPlaceConfigureBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		binding.placeSpinner.setEnabled(false);
		binding.addButton.setEnabled(false);
		
		ArrayAdapter adapter = new ArrayAdapter(context, R.layout.dialog_country_list_item, new ArrayList<String>() {
		});
		binding.placeSpinner.setAdapter(adapter);
		
		repository.getBasicListingLiveData().observeForever(new Observer<List<Geolocation>>() {
			@Override
			public void onChanged(List<Geolocation> listings) {
				if (listings == null) return;
				
				repository.getBasicListingLiveData().removeObserver(this);
				
				if (listings.isEmpty()) {
					Log.d("WidgetStandardPlace", "No places found");
					return;
				}
				
				// Build data for expandable list view
				String[] placesNames = new String[listings.size()];
				placeIds = new int[listings.size()];
				
				for (int i = 0; i < listings.size(); i++) {
					placesNames[i] = String.format(repository.getSettingsManager().getDefaultLocale(),
							  "%s, %s (%.3f, %.3f)",
							  listings.get(i).getCity(),
							  listings.get(i).getCountryCode(),
							  listings.get(i).getCoordinates().getLatitude(),
							  listings.get(i).getCoordinates().getLongitude());
					placeIds[i] = listings.get(i).getPlaceId();
				}
				
				adapter.addAll(placesNames);
				
				binding.addButton.setOnClickListener(v -> {
					int placeId = placeIds[binding.placeSpinner.getSelectedItemPosition()];
					saveWidgetSettings(context, mAppWidgetId, placeId);
					
					// It is the responsibility of the configuration activity to update the app widget
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					WidgetStandardPlace.updateAppWidget(context, appWidgetManager, repository, mAppWidgetId);
					
					// Make sure we pass back the original appWidgetId
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
				});
				
				
				binding.placeSpinner.setEnabled(true);
				binding.addButton.setEnabled(true);
				
				// Find the widget id from the intent.
				Intent intent = getIntent();
				Bundle extras = intent.getExtras();
				if (extras != null) {
					mAppWidgetId = extras.getInt(
							  AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				
				// If this activity was started with an intent without an app widget ID, finish with an error.
				if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
					finish();
				}
			}
		});
	}
}