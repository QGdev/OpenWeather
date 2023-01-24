package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.customview.DailyForecastGraphView;
import fr.qgdev.openweather.customview.HourlyForecastGraphView;
import fr.qgdev.openweather.dialog.WeatherAlertDialog;
import fr.qgdev.openweather.fragment.places.PlacesViewModel;
import fr.qgdev.openweather.metrics.AirQuality;
import fr.qgdev.openweather.metrics.CurrentWeather;
import fr.qgdev.openweather.repositories.FormattingService;
import fr.qgdev.openweather.repositories.places.Place;

/**
 * PlaceRecyclerViewAdapter
 * <p>
 * Used to generate places items in main section of the app.
 * But can be used like any other adapter.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see android.widget.BaseAdapter
 */
public class PlaceRecyclerViewAdapter extends RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceViewHolder> {
	
	private final Context context;
	private final FormattingService formattingService;
	private final PlacesViewModel placesViewModel;
	private final List<String> countryNames;
	private final List<String> countryCodes;
	
	/**
	 * PlaceRecyclerViewAdapter Constructor
	 * <p>
	 * Just build an PlaceRecyclerViewAdapter Object
	 * </p>
	 *
	 * @param context         Current context, only used for LayoutInflater
	 * @param placesViewModel Reference of the parent PlacesFragment
	 */
	public PlaceRecyclerViewAdapter(Context context, PlacesViewModel placesViewModel, FormattingService formattingService) {
		this.context = context;
		this.placesViewModel = placesViewModel;
		this.formattingService = formattingService;
		this.countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		this.countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));
	}


	/**
	 * add(int position)
	 * <p>
	 *     Need to be called when a new place is added.
	 * </p>
	 *
	 * @param position Position of the new place
	 */
	public void add(int position) {
		this.notifyItemInserted(position);
	}

	/**
	 * remove(int position)
	 * <p>
	 *     Need to be called when a place is deleted or removed.
	 * </p>
	 *
	 * @param position Position of the deleted place
	 */
	public void remove(int position) {
		notifyItemRemoved(position);
	}

	/**
	 * setListeners(Place currentPlace, PlaceViewHolder holder)
	 * <p>
	 *     Will initialize all listeners in the PlaceViewHolder
	 * </p>
	 *
	 * @param currentPlace  The place
	 * @param holder        The place viewHolder where all listeners will be initialized
	 */
	private void setListeners(Place currentPlace, PlaceViewHolder holder) {
		int placeViewType = holder.getItemViewType();
		Integer placeID = currentPlace.getProperties().getPlaceId();
		
		//  Set listener for the whole place card item
		holder.cardView.setOnClickListener(v -> {
			if (ViewType.fromInt(placeViewType) == ViewType.COMPACT) {
				placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED);
			} else {
				placesViewModel.setPlaceViewType(placeID, ViewType.COMPACT);
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		//  Set listener for the hourly weather forecast tab
		holder.hourlyForecast.setOnClickListener(v -> {
			switch (ViewType.fromInt(placeViewType)) {
				case EXTENDED:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_HOURLY);
					break;
				case EXTENDED_HOURLY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED);
					break;
				case EXTENDED_DAILY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_FULLY);
					break;
				case EXTENDED_FULLY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_DAILY);
					break;
				default:
					placesViewModel.setPlaceViewType(placeID, ViewType.COMPACT);
					break;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		//  Set listener for the daily weather forecast tab
		holder.dailyForecast.setOnClickListener(v -> {
			switch (ViewType.fromInt(placeViewType)) {
				case EXTENDED:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_DAILY);
					break;
				case EXTENDED_DAILY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED);
					break;
				case EXTENDED_HOURLY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_FULLY);
					break;
				case EXTENDED_FULLY:
					placesViewModel.setPlaceViewType(placeID, ViewType.EXTENDED_HOURLY);
					break;
				default:
					placesViewModel.setPlaceViewType(placeID, ViewType.COMPACT);
					break;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		//  Set listener for the weather alerts tab
		holder.weatherAlertLayout.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context,
					  currentPlace,
					  formattingService);
			weatherAlertDialog.build();
		});

		//  Set listener for the weather alert icon
		holder.weatherAlertIcon.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context,
					  currentPlace,
					  formattingService);
			weatherAlertDialog.build();
		});
	}

	/**
	 * getCountryName(String countryCode)
	 * <p>
	 * Will find country name related to a country code
	 * </p>
	 *
	 * @param countryCode String Country code of a place
	 * @return Will return the corresponding country name
	 * @apiNote Country code must be present in string file resources
	 */
	private String getCountryName(String countryCode) {
		int index = this.countryCodes.indexOf(countryCode);
		return this.countryNames.get(index);
	}


	/**
	 * onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	 * <p>
	 * Will inflate created viewHolder
	 * </p>
	 *
	 * @param parent   ViewHolder parent
	 * @param viewType ViewHolder view type
	 * @return Will return the inflated placeViewHolder
	 * @see RecyclerView.Adapter<>
	 */
	@NonNull
	@Override
	public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.adapter_places, parent, false);
		return new PlaceViewHolder(context, view);
	}


	/**
	 * onBindViewHolder(@NonNull PlaceViewHolder holder, final int position)
	 * <p>
	 * Will fill PlaceViewHolder with data
	 * </p>
	 *
	 * @param holder   PlaceViewHolder which will be filled with place data
	 * @param position Place position in placeViewArrayList
	 * @see RecyclerView.Adapter<>
	 */
	@Override
	public void onBindViewHolder(@NonNull PlaceViewHolder holder, final int position) {
		
		Place currentPlace = placesViewModel.getPlaces().get(position);
		
		switch (placesViewModel.getPlaceViewType(currentPlace.getProperties().getPlaceId())) {
			
			//  Extended view
			case EXTENDED: {
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.VISIBLE);
				else holder.windGustSpeedTextView.setVisibility(View.VISIBLE);
				
				holder.visibilityTextView.setVisibility(View.VISIBLE);
				holder.sunriseTextView.setVisibility(View.VISIBLE);
				holder.sunsetTextView.setVisibility(View.VISIBLE);
				holder.cloudinessTextView.setVisibility(View.VISIBLE);
				
				holder.detailedInformationsLayout.setVisibility(View.VISIBLE);
				holder.weatherAlertIcon.setVisibility(View.GONE);

				holder.forecastInformationsLayout.setVisibility(View.VISIBLE);
				holder.hourlyForecastExpandIcon.setRotation(0);
				holder.hourlyForecastLayout.setVisibility(View.GONE);
				holder.dailyForecastExpandIcon.setRotation(0);
				holder.dailyForecastLayout.setVisibility(View.GONE);
				
				holder.lastUpdateAvailableLayout.setVisibility(View.VISIBLE);
				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertLayout.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertLayout.setVisibility(View.GONE);
				}
				break;
			}
			
			//  Hourly extended view
			case EXTENDED_HOURLY: {
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.VISIBLE);
				else holder.windGustSpeedTextView.setVisibility(View.VISIBLE);
				
				holder.visibilityTextView.setVisibility(View.VISIBLE);
				holder.sunriseTextView.setVisibility(View.VISIBLE);
				holder.sunsetTextView.setVisibility(View.VISIBLE);
				holder.cloudinessTextView.setVisibility(View.VISIBLE);
				
				holder.detailedInformationsLayout.setVisibility(View.VISIBLE);
				holder.weatherAlertIcon.setVisibility(View.GONE);

				holder.forecastInformationsLayout.setVisibility(View.VISIBLE);
				holder.hourlyForecastExpandIcon.setRotation(180);
				holder.hourlyForecastLayout.setVisibility(View.VISIBLE);
				holder.dailyForecastExpandIcon.setRotation(0);
				holder.lastUpdateAvailableLayout.setVisibility(View.VISIBLE);
				
				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertLayout.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertLayout.setVisibility(View.GONE);
				}
				break;
			}
			
			//  Daily extended view
			case EXTENDED_DAILY: {
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.VISIBLE);
				else holder.windGustSpeedTextView.setVisibility(View.VISIBLE);
				
				holder.visibilityTextView.setVisibility(View.VISIBLE);
				holder.sunriseTextView.setVisibility(View.VISIBLE);
				holder.sunsetTextView.setVisibility(View.VISIBLE);
				holder.cloudinessTextView.setVisibility(View.VISIBLE);
				
				holder.detailedInformationsLayout.setVisibility(View.VISIBLE);
				holder.weatherAlertIcon.setVisibility(View.GONE);

				holder.forecastInformationsLayout.setVisibility(View.VISIBLE);
				holder.hourlyForecastExpandIcon.setRotation(0);
				holder.hourlyForecastLayout.setVisibility(View.GONE);
				holder.dailyForecastExpandIcon.setRotation(180);
				holder.dailyForecastLayout.setVisibility(View.VISIBLE);
				holder.lastUpdateAvailableLayout.setVisibility(View.VISIBLE);
				
				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertLayout.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertLayout.setVisibility(View.GONE);
				}
				break;
			}
			//  Fully extended view
			case EXTENDED_FULLY: {
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.VISIBLE);
				else holder.windGustSpeedTextView.setVisibility(View.VISIBLE);
				
				holder.visibilityTextView.setVisibility(View.VISIBLE);
				holder.sunriseTextView.setVisibility(View.VISIBLE);
				holder.sunsetTextView.setVisibility(View.VISIBLE);
				holder.cloudinessTextView.setVisibility(View.VISIBLE);
				
				holder.detailedInformationsLayout.setVisibility(View.VISIBLE);
				holder.weatherAlertIcon.setVisibility(View.GONE);
				
				holder.forecastInformationsLayout.setVisibility(View.VISIBLE);
				holder.hourlyForecastExpandIcon.setRotation(180);
				holder.hourlyForecastLayout.setVisibility(View.VISIBLE);
				holder.dailyForecastExpandIcon.setRotation(180);
				holder.dailyForecastLayout.setVisibility(View.VISIBLE);
				holder.lastUpdateAvailableLayout.setVisibility(View.VISIBLE);
				
				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertLayout.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertLayout.setVisibility(View.GONE);
				}
				break;
			}
			case COMPACT:
			default: {
				//  Compact view
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.GONE);
				else holder.windGustSpeedTextView.setVisibility(View.GONE);
				
				holder.visibilityTextView.setVisibility(View.GONE);
				holder.sunriseTextView.setVisibility(View.GONE);
				holder.sunsetTextView.setVisibility(View.GONE);
				holder.cloudinessTextView.setVisibility(View.GONE);
				
				holder.detailedInformationsLayout.setVisibility(View.GONE);
				holder.weatherAlertIcon.setVisibility(View.GONE);
				
				holder.forecastInformationsLayout.setVisibility(View.GONE);
				holder.hourlyForecastExpandIcon.setRotation(0);
				holder.hourlyForecastLayout.setVisibility(View.GONE);
				holder.dailyForecastExpandIcon.setRotation(0);
				holder.dailyForecastLayout.setVisibility(View.GONE);
				holder.lastUpdateAvailableLayout.setVisibility(View.GONE);
				
				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertIcon.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertIcon.setVisibility(View.GONE);
				}
				
				break;
			}
		}
		
		setListeners(currentPlace, holder);
		
		holder.cityNameTextView.setText(currentPlace.getGeolocation().getCity());
		holder.countryNameTextVIew.setText(this.getCountryName(currentPlace.getGeolocation().getCountryCode()));
		
		CurrentWeather currentWeather = currentPlace.getCurrentWeather();
		AirQuality airQuality = currentPlace.getAirQuality();
		
		holder.temperatureTextView.setText(formattingService.getFloatFormattedTemperature(currentWeather.getTemperature(), true));
		holder.temperatureFeelsLikeTextView.setText(formattingService.getFloatFormattedTemperature(currentWeather.getTemperatureFeelsLike(), true));
		
		holder.weatherDescription.setText(currentWeather.getWeatherDescription());
		
		final int weatherIconId;
		
		switch (currentWeather.getWeatherCode()) {
			
			//  Thunderstorm Group
			case 210:
			case 211:
			case 212:
			case 221:
				weatherIconId = context.getResources().getIdentifier("thunderstorm_flat", "drawable", context.getPackageName());
				break;
			
			case 200:
			case 201:
			case 202:
			case 230:
			case 231:
			case 232:
				weatherIconId = context.getResources().getIdentifier("storm_flat", "drawable", context.getPackageName());
				break;

			//  Drizzle and Rain (Light)
			case 300:
			case 310:
			case 500:
			case 501:
			case 520:
				if (currentWeather.getDt() >= currentWeather.getSunrise() && currentWeather.getDt() < currentWeather.getSunset()) {
					weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = context.getResources().getIdentifier("rainy_night_flat", "drawable", context.getPackageName());
				}
				break;

			//Drizzle and Rain (Moderate)
			case 301:
			case 302:
			case 311:
			case 313:
			case 321:
			case 511:
			case 521:
			case 531:
				weatherIconId = context.getResources().getIdentifier("rain_flat", "drawable", context.getPackageName());
				break;

			//Drizzle and Rain (Heavy)
			case 312:
			case 314:
			case 502:
			case 503:
			case 504:
			case 522:
				weatherIconId = context.getResources().getIdentifier("heavy_rain_flat", "drawable", context.getPackageName());
				break;

			//  Snow
			case 600:
			case 601:
			case 620:
			case 621:
				if (currentWeather.getDt() >= currentWeather.getSunrise() && currentWeather.getDt() < currentWeather.getSunset()) {
					weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = context.getResources().getIdentifier("snow_and_night_flat", "drawable", context.getPackageName());
				}
				break;

			case 602:
			case 622:
				weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
				break;

			case 611:
			case 612:
			case 613:
			case 615:
			case 616:
				weatherIconId = context.getResources().getIdentifier("sleet_flat", "drawable", context.getPackageName());
				break;

			//  Atmosphere
			case 701:
			case 711:
			case 721:
			case 731:
			case 741:
			case 751:
			case 761:
			case 762:
			case 771:
			case 781:
				if (currentWeather.getDt() >= currentWeather.getSunrise() && currentWeather.getDt() < currentWeather.getSunset()) {
					weatherIconId = context.getResources().getIdentifier("fog_flat", "drawable", context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = context.getResources().getIdentifier("fog_and_night_flat", "drawable", context.getPackageName());
				}
				break;

			//  Sky
			case 800:
				//  Day
				if (currentWeather.getDt() >= currentWeather.getSunrise() && currentWeather.getDt() < currentWeather.getSunset()) {
					weatherIconId = context.getResources().getIdentifier("sun_flat", "drawable", context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = context.getResources().getIdentifier("moon_phase_flat", "drawable", context.getPackageName());
				}
				break;

			case 801:
			case 802:
			case 803:
				if (currentWeather.getDt() >= currentWeather.getSunrise() && currentWeather.getDt() < currentWeather.getSunset()) {
					weatherIconId = context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", context.getPackageName());
				}
				//  Night
				else {
					weatherIconId = context.getResources().getIdentifier("cloudy_night_flat", "drawable", context.getPackageName());
				}
				break;

			case 804:
				weatherIconId = context.getResources().getIdentifier("cloudy_flat", "drawable", context.getPackageName());
				break;
			
			//  Default
			default:
				weatherIconId = context.getResources().getIdentifier("storm_flat", "drawable", context.getPackageName());
				break;
		}
		
		holder.weatherIcon.setImageDrawable(context.getDrawable(weatherIconId));
		
		//  Wind
		////    Wind Direction
		holder.windDirectionTextView.setText(formattingService.getFormattedDirection(currentWeather.getWindDirection(), currentWeather.isWindDirectionReadable()));
		
		////  Wind speed and Wind gust Speed
		holder.windSpeedTextView.setText(formattingService.getIntFormattedSpeed(currentWeather.getWindSpeed(), true));
		holder.windGustSpeedTextView.setText(formattingService.getIntFormattedSpeed(currentWeather.getWindGustSpeed(), true));
		
		//  Humidity
		holder.humidityTextView.setText(String.format("%d %%", currentWeather.getHumidity()));
		
		//  Pressure
		holder.pressureTextView.setText(formattingService.getFormattedPressure(currentWeather.getPressure(), true));
		
		//  Visibility
		holder.visibilityTextView.setText(formattingService.getIntFormattedDistance(currentWeather.getVisibility(), true));
		
		//  Sunrise and sunset
		holder.sunriseTextView.setText(formattingService.getFormattedTime(new Date(currentWeather.getSunrise()), currentPlace.getProperties().getTimeZone()));
		holder.sunsetTextView.setText(formattingService.getFormattedTime(new Date(currentWeather.getSunset()), currentPlace.getProperties().getTimeZone()));
		
		holder.cloudinessTextView.setText(String.format("%d%%", currentWeather.getCloudiness()));
		
		//	Air quality
		holder.airQualityIndex.setText(String.valueOf(airQuality.getAqi()));
		
		switch (airQuality.getAqi()) {
			
			case 5:
				holder.airQualityCircle.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorUvExtreme)));
				holder.airQualityMessage.setText(context.getText(R.string.air_quality_5));
				break;
			
			case 4:
				holder.airQualityCircle.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorUvVeryHigh)));
				holder.airQualityMessage.setText(context.getText(R.string.air_quality_4));
				break;
			
			case 3:
				holder.airQualityCircle.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorUvHigh)));
				holder.airQualityMessage.setText(context.getText(R.string.air_quality_3));
				break;
			
			case 2:
				holder.airQualityCircle.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorUvModerate)));
				holder.airQualityMessage.setText(context.getText(R.string.air_quality_2));
				break;
			
			case 1:
			default:
				holder.airQualityCircle.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorUvLow)));
				holder.airQualityMessage.setText(context.getText(R.string.air_quality_1));
				break;
		}
		
		holder.airQualityCO.setText(String.format("%.3f", airQuality.getCo()));
		holder.airQualityNO.setText(String.format("%.3f", airQuality.getNo()));
		holder.airQualityNO2.setText(String.format("%.3f", airQuality.getNo2()));
		holder.airQualityO3.setText(String.format("%.3f", airQuality.getO3()));
		holder.airQualitySO2.setText(String.format("%.3f", airQuality.getSo2()));
		holder.airQualityNH3.setText(String.format("%.3f", airQuality.getNh3()));
		holder.airQualityPM25.setText(String.format("%.3f", airQuality.getPm2_5()));
		holder.airQualityPM10.setText(String.format("%.3f", airQuality.getPm10()));
		
		
		//  Precipitations
		//  If There is no rain or snow, so there is nothing to show about precipitations
		if (currentWeather.getRain() > 0 || currentWeather.getSnow() > 0) {
			holder.precipitationLayout.setVisibility(View.VISIBLE);
			
			holder.rainTextView.setText(formattingService.getFloatFormattedShortDistance(currentWeather.getRain(), true));
			holder.snowTextView.setText(formattingService.getFloatFormattedShortDistance(currentWeather.getSnow(), true));
			
			//  If There is no rain, so there is nothing to show about rain
			if (currentWeather.getRain() > 0)
				holder.precipitationLayout.findViewById(R.id.rain_precipitations).setVisibility(View.VISIBLE);
			else
				holder.precipitationLayout.findViewById(R.id.rain_precipitations).setVisibility(View.GONE);
			//  If There is no snow, so there is nothing to show about snow
			if (currentWeather.getSnow() > 0)
				holder.precipitationLayout.findViewById(R.id.snow_precipitations).setVisibility(View.VISIBLE);
			else
				holder.precipitationLayout.findViewById(R.id.snow_precipitations).setVisibility(View.GONE);
		} else {
			holder.precipitationLayout.setVisibility(View.GONE);
		}
		
		holder.hourlyForecastGraphView.initialization(currentPlace.getHourlyWeatherForecastList(), currentPlace.getDailyWeatherForecastList(), formattingService, currentPlace.getProperties().getTimeZone());
		holder.dailyForecastGraphView.initialization(currentPlace.getDailyWeatherForecastList(), currentPlace.getProperties().getTimeZone(), formattingService);
		
		holder.hourlyForecastScrollview.scrollTo(0, 0);
		holder.dailyForecastScrollview.scrollTo(0, 0);
		
		
		holder.lastUpdateAvailableTextView.setText(String.format("%s %s", formattingService.getFormattedFullTimeHour(new Date(currentWeather.getDt()), currentPlace.getProperties().getTimeZone()), currentPlace.getProperties().getTimeZoneStringForm()));
	}
	
	
	/**
	 * getItemViewType(int position)
	 * <p>
	 * Will return view type of the corresponding item to position
	 * </p>
	 *
	 * @param position Item position
	 * @return Will return the ViewType corresponding to the item at the given position
	 */
	@Override
	public int getItemViewType(int position) {
		return placesViewModel.getPlaceViewTypeFromPosition(position).toInt();
	}

	/**
	 * getItemCount()
	 * <p>
	 * Call PlacesFragment methods to know item count in array
	 * </p>
	 *
	 * @return Will return the number of displayed places
	 */
	@Override
	public int getItemCount() {
		return placesViewModel.getPlaces().size();
	}
	
	/**
	 * ViewType
	 * <p>
	 * Describes the state of a place card which can be:
	 * Compact, Extended, Extended with Hourly or Daily tabs extended or Fully Extended where all tabs are extended
	 * </p>
	 */
	public enum ViewType {
		COMPACT(0),
		EXTENDED(1),
		EXTENDED_HOURLY(2),
		EXTENDED_DAILY(3),
		EXTENDED_FULLY(4);
		
		private final int viewTypeID;
		
		ViewType(int id) {
			viewTypeID = id;
		}
		
		public static ViewType fromInt(int id) {
			switch (id) {
				default:
				case 0:
					return COMPACT;
				case 1:
					return EXTENDED;
				case 2:
					return EXTENDED_HOURLY;
				case 3:
					return EXTENDED_DAILY;
				case 4:
					return EXTENDED_FULLY;
			}
		}
		
		public int toInt() {
			return this.viewTypeID;
		}
	}
	
	
	/**
	 * PlaceViewHolder
	 * <p>
	 * Used to contain the card of a place
	 * </p>
	 *
	 * @author Quentin GOMES DOS REIS
	 * @version 1
	 * @see RecyclerView.ViewHolder
	 */
	public static class PlaceViewHolder extends RecyclerView.ViewHolder {
		
		public final MaterialCardView cardView;
		public final LinearLayout adapterPlaceLayout;
		public final TextView cityNameTextView;
		public final TextView countryNameTextVIew;
		
		public final TextView temperatureTextView;
		public final TextView temperatureFeelsLikeTextView;
		
		public final TextView weatherDescription;
		public final ImageView weatherIcon;
		
		public final LinearLayout weatherAlertLayout;
		public final ImageView weatherAlertIcon;
		
		public final TextView windDirectionTextView;
		public final TextView windSpeedTextView;
		public final TextView windGustSpeedTextView;
		
		public final TextView humidityTextView;
		public final TextView pressureTextView;
		public final TextView visibilityTextView;
		
		public final TextView sunriseTextView;
		public final TextView sunsetTextView;
		public final TextView cloudinessTextView;
		
		public final ImageView airQualityCircle;
		public final TextView airQualityIndex;
		public final TextView airQualityMessage;
		public final TextView airQualityCO;
		public final TextView airQualityNO;
		public final TextView airQualityNO2;
		public final TextView airQualityO3;
		public final TextView airQualitySO2;
		public final TextView airQualityNH3;
		public final TextView airQualityPM25;
		public final TextView airQualityPM10;
		
		public final LinearLayout precipitationLayout;
		public final TextView rainTextView;
		public final TextView snowTextView;
		
		public final LinearLayout lastUpdateAvailableLayout;
		public final TextView lastUpdateAvailableTextView;
		
		public final LinearLayout detailedInformationsLayout;
		public final LinearLayout forecastInformationsLayout;
		
		public final LinearLayout hourlyForecastLayout;
		public final HorizontalScrollView hourlyForecastScrollview;
		public final LinearLayout hourlyForecast;
		public final ImageView hourlyForecastExpandIcon;
		
		public final HourlyForecastGraphView hourlyForecastGraphView;
		
		public final LinearLayout dailyForecastLayout;
		public final HorizontalScrollView dailyForecastScrollview;
		public final LinearLayout dailyForecast;
		
		public final DailyForecastGraphView dailyForecastGraphView;
		
		public final ImageView dailyForecastExpandIcon;
		
		
		/**
		 * PlaceViewHolder(@NonNull Context context, @NonNull View itemView)
		 * <p>
		 * Place ViewHolder constructor
		 * </p>
		 *
		 * @param context  Application context in order to access to resources
		 * @param itemView The inflated view of place
		 * @apiNote context and itemView shouldn't be null !
		 */
		public PlaceViewHolder(@NonNull Context context, @NonNull View itemView) {
			super(itemView);

			this.cardView = itemView.findViewById(R.id.card_place);
			this.adapterPlaceLayout = itemView.findViewById(R.id.adapter_place);
			this.cityNameTextView = itemView.findViewById(R.id.city_adapter);
			this.countryNameTextVIew = itemView.findViewById(R.id.country_adapter);
			this.temperatureTextView = itemView.findViewById(R.id.temperature_value);
			this.temperatureFeelsLikeTextView = itemView.findViewById(R.id.temperature_feelslike_value);
			this.weatherDescription = itemView.findViewById(R.id.weather_description_adapter);
			this.weatherIcon = itemView.findViewById(R.id.weather_icon_adapter);
			this.weatherAlertLayout = itemView.findViewById(R.id.weather_alert);
			this.weatherAlertIcon = itemView.findViewById(R.id.warning_icon);

			this.windDirectionTextView = itemView.findViewById(R.id.wind_direction_value);
			this.windSpeedTextView = itemView.findViewById(R.id.wind_speed_value);
			this.windGustSpeedTextView = itemView.findViewById(R.id.wind_gust_speed_value);
			this.humidityTextView = itemView.findViewById(R.id.humidity_value);
			this.pressureTextView = itemView.findViewById(R.id.pressure_value);
			this.visibilityTextView = itemView.findViewById(R.id.visibility_value);
			this.sunriseTextView = itemView.findViewById(R.id.sunrise_value);
			this.sunsetTextView = itemView.findViewById(R.id.sunset_value);
			this.cloudinessTextView = itemView.findViewById(R.id.cloudiness_value);

			this.airQualityCircle = itemView.findViewById(R.id.airquality_circle);
			this.airQualityIndex = itemView.findViewById(R.id.airquality_number);
			this.airQualityMessage = itemView.findViewById(R.id.airquality_text);
			this.airQualityCO = itemView.findViewById(R.id.textview_co);
			this.airQualityNO = itemView.findViewById(R.id.textview_no);
			this.airQualityNO2 = itemView.findViewById(R.id.textview_no2);
			this.airQualityO3 = itemView.findViewById(R.id.textview_o3);
			this.airQualitySO2 = itemView.findViewById(R.id.textview_so2);
			this.airQualityNH3 = itemView.findViewById(R.id.textview_nh3);
			this.airQualityPM25 = itemView.findViewById(R.id.textview_pm2_5);
			this.airQualityPM10 = itemView.findViewById(R.id.textview_pm10);

			this.precipitationLayout = itemView.findViewById(R.id.precipitations);
			this.rainTextView = itemView.findViewById(R.id.rain_precipitations_current_value);
			this.snowTextView = itemView.findViewById(R.id.snow_precipitations_current_value);
			this.lastUpdateAvailableLayout = itemView.findViewById(R.id.last_update_available);
			this.lastUpdateAvailableTextView = itemView.findViewById(R.id.last_update_value);

			this.detailedInformationsLayout = itemView.findViewById(R.id.detailed_informations);
			this.forecastInformationsLayout = itemView.findViewById(R.id.forecast);

			this.hourlyForecastLayout = itemView.findViewById(R.id.hourly_forecast);
			this.hourlyForecastScrollview = itemView.findViewById(R.id.hourly_forecast_scroll_view);
			this.hourlyForecast = itemView.findViewById(R.id.hourly_forecast_layout);
			this.hourlyForecastExpandIcon = itemView.findViewById(R.id.hourly_forecast_expand_icon);
			this.hourlyForecastGraphView = itemView.findViewById(R.id.hourly_graphview);

			this.dailyForecastLayout = itemView.findViewById(R.id.daily_forecast);
			this.dailyForecastScrollview = itemView.findViewById(R.id.daily_forecast_scroll_view);
			this.dailyForecast = itemView.findViewById(R.id.daily_forecast_layout);
			this.dailyForecastExpandIcon = itemView.findViewById(R.id.daily_forecast_expand_icon);
			this.dailyForecastGraphView = itemView.findViewById(R.id.daily_forecast_graphView);

			setDrawableCompoundTextView(context, this.windDirectionTextView, R.drawable.wind_vane_material);
			setDrawableCompoundTextView(context, this.windSpeedTextView, R.drawable.windsock_material);
			setDrawableCompoundTextView(context, this.windGustSpeedTextView, R.drawable.wind_material);
			setDrawableCompoundTextView(context, this.humidityTextView, R.drawable.humidity_material);
			setDrawableCompoundTextView(context, this.pressureTextView, R.drawable.barometer_material);
			setDrawableCompoundTextView(context, this.visibilityTextView, R.drawable.visibility_24dp);
			setDrawableCompoundTextView(context, this.sunriseTextView, R.drawable.sunrise_material);
			setDrawableCompoundTextView(context, this.sunsetTextView, R.drawable.sunset_material);
			setDrawableCompoundTextView(context, this.cloudinessTextView, R.drawable.cloudy_material);

			//  Compact view
			if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
				this.pressureTextView.setVisibility(View.GONE);
			else this.windGustSpeedTextView.setVisibility(View.GONE);

			this.visibilityTextView.setVisibility(View.GONE);
			this.sunriseTextView.setVisibility(View.GONE);
			this.sunsetTextView.setVisibility(View.GONE);
			this.cloudinessTextView.setVisibility(View.GONE);

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
		private int dpToPx(@NonNull Context context, float dip) {
			return (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					dip,
					context.getResources().getDisplayMetrics());
		}

		/**
		 * setDrawableCompoundTextView(Context context, @NonNull TextView textView, @DrawableRes int id)
		 * <p>
		 * A method to set a drawable to a compound drawable of a textView
		 * </p>
		 *
		 * @param context  Application context in order to access to resources
		 * @param textView The textView where the compound drawable will be set
		 * @param id       The id of the drawable that will be set as a compound drawable
		 */
		private void setDrawableCompoundTextView(@NonNull Context context, @NonNull TextView textView, @DrawableRes int id) {
			int compoundDrawableSideSize = dpToPx(context, 20);
			Drawable drawable = context.getDrawable(id);
			assert drawable != null;
			drawable.setBounds(0, 0, compoundDrawableSideSize, compoundDrawableSideSize);
			textView.setCompoundDrawables(drawable, null, null, null);
		}
	}
}
