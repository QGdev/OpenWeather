
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

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SizeF;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.databinding.WidgetConfigurationBinding;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Geolocation;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.widgets.WidgetsBinder.WidgetType;

/**
 * The configuration screen for the {@link WidgetsProvider WidgetStandardPlaceInfo} AppWidget.
 */
public class WidgetsConfigurationActivity extends Activity {
	private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private int[] placeIds;
	private WidgetConfigurationBinding binding;
	
	/**
	 * WidgetsConfigurationActivity constructor
	 */
	public WidgetsConfigurationActivity() {
		super();
	}
	
	
	/**
	 * Will generate a view that will be used as a preview for the widget.
	 *
	 * @param context           The context needed to generate the view
	 * @param widgetType        The widget type in order to generate the correct view
	 * @param place             The place to use to generate and fill the view
	 * @param formattingService The formatting service to use to format data
	 * @return The generated view
	 */
	private static View generateNewWidgetPreview(@NonNull Context context, @NonNull WidgetType widgetType, @NonNull Place place, @NonNull FormattingService formattingService) {
		RemoteViews widget = bindWidget(context, widgetType, place, formattingService);
		return widget.apply(context, null);
	}
	
	/**
	 * Will update the preview of the widget with the place selected by the user.
	 *
	 * @param context    The context needed to generate the view
	 * @param widgetType The widget type in order to generate the correct view
	 * @param placeId    The place id to use to generate and fill the view
	 * @param repository The repository to use to get the place
	 * @param binding    The binding to use to update the activity
	 */
	private static void updateWidgetPreview(@NonNull Context context, @NonNull WidgetType widgetType, int placeId, @NonNull AppRepository repository, @NonNull WidgetConfigurationBinding binding) {
		repository.getPlaceFromPlaceIdLiveData(placeId).observeForever(new Observer<Place>() {
			@Override
			public void onChanged(Place place) {
				if (place == null) return;
				
				// Generate preview
				View preview = generateNewWidgetPreview(context,
						  widgetType,
						  place,
						  repository.getFormattingService());
				
				// Add preview to the layout with the correct size
				binding.widgetPreview.removeAllViews();
				binding.widgetPreview.addView(preview,
						  widgetType.getPxWidth(context),
						  widgetType.getPxHeight(context));
				
				
				// Enable confirm button
				binding.confirmButton.setEnabled(true);
				
				// Don't observe anymore, we have the data
				repository.getPlaceFromPlaceIdLiveData(placeId).removeObserver(this);
			}
		});
	}
	
	/**
	 * Called when the activity is created
	 *
	 * @param icicle The saved instance state
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		final Context context = WidgetsConfigurationActivity.this;
		final AppRepository repository = AppRepository.getInstance(context.getApplicationContext());
		
		// Set the result to CANCELED.  This will cause the widget host to cancel
		// out of the widget placement if the user presses the back button.
		setResult(RESULT_CANCELED);
		
		// Find the widget id from the intent.
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(
					  AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		// If this activity was started with an intent without an app widget ID, finish with an error.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
		
		// Get size of the widget like width and height
		Bundle options = AppWidgetManager.getInstance(context).getAppWidgetOptions(mAppWidgetId);
		List<SizeF> sizes = options.getParcelableArrayList(AppWidgetManager.OPTION_APPWIDGET_SIZES);
		if (sizes == null || sizes.size() == 0) {
			finish();
			return;
		}
		
		// Get minimal width and height
		int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
		int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
		
		// Check if the size is supported
		WidgetType widgetType = WidgetType.fromSizeF(new SizeF(width, height));
		// If not supported, finish
		if (widgetType == null) {
			finish();
			return;
		}
		
		// Setup the view
		binding = WidgetConfigurationBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		// Setup close button
		binding.exitButton.setOnClickListener(v -> {
			setResult(RESULT_CANCELED);
			finish();
		});
		
		// Disable spinner and confirm button
		// They will be enabled when the list
		// of places will be loaded
		binding.placeSpinner.setEnabled(false);
		binding.confirmButton.setEnabled(false);
		
		// Setup spinner
		ArrayAdapter adapter = new ArrayAdapter(context, R.layout.dialog_country_list_item, new ArrayList<String>() {
		});
		binding.placeSpinner.setAdapter(adapter);
		
		// Load places in the spinner
		repository.getBasicListingLiveData().observeForever(new Observer<List<Geolocation>>() {
			@Override
			public void onChanged(List<Geolocation> listings) {
				if (listings == null) return;
				
				// Don't observe anymore, we have the data
				repository.getBasicListingLiveData().removeObserver(this);
				
				if (listings.isEmpty()) {
					Toast.makeText(context, R.string.error_no_places_registered, Toast.LENGTH_LONG).show();
					return;
				}
				
				//	The app widget ID is valid.
				// Retrieve his configuration
				WidgetsSettings widgetsSettings = repository.getWidgetsManager().loadWidgetSettings(mAppWidgetId, null);
				
				// Build data for spinner
				String[] placesNames = new String[listings.size()];
				placeIds = new int[listings.size()];
				
				for (int i = 0; i < listings.size(); i++) {
					placesNames[i] = String.format(repository.getSettingsManager().getDefaultLocale(),
							  "%s, %s (%.1f, %.1f)",
							  listings.get(i).getCity(),
							  listings.get(i).getCountryCode(),
							  listings.get(i).getCoordinates().getLatitude(),
							  listings.get(i).getCoordinates().getLongitude());
					placeIds[i] = listings.get(i).getPlaceId();
				}
				
				adapter.addAll(placesNames);
				
				// Set the spinner to the current place if possible
				if (widgetsSettings != null) {
					for (int i = 0; i < placeIds.length; i++) {
						if (placeIds[i] == widgetsSettings.getPlaceId()) {
							binding.placeSpinner.setSelection(i);
							break;
						}
					}
				}
				
				binding.placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						updateWidgetPreview(context, widgetType, placeIds[position], repository, binding);
					}
					
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
				
				adapter.setNotifyOnChange(true);
				// Setup preview
				// If the widget is already configured, show the preview with the current configuration
				// Otherwise, show the preview with the first place in the list
				updateWidgetPreview(context,
						  widgetType,
						  widgetsSettings == null ? placeIds[0] : widgetsSettings.getPlaceId(),
						  repository,
						  binding);
				
				// Setup confirm button
				binding.confirmButton.setOnClickListener(v -> {
					int placeId = placeIds[binding.placeSpinner.getSelectedItemPosition()];
					repository.getWidgetsManager().saveWidgetSettings(new WidgetsSettings(placeId, mAppWidgetId));
					
					// It is the responsibility of the configuration activity to update the app widget
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					WidgetsProvider.updateAppWidget(context, appWidgetManager, repository, mAppWidgetId);
					
					// Make sure we pass back the original appWidgetId
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
				});
				
				// The view is now ready, enable the spinner and confirm button
				binding.placeSpinner.setEnabled(true);
				binding.confirmButton.setEnabled(true);
			}
		});
	}
}