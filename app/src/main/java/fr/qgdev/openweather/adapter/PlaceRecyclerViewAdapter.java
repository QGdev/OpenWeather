package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.dataplaces.DataPlaces;
import fr.qgdev.openweather.dialog.WeatherAlertDialog;
import fr.qgdev.openweather.forecastView.DailyForecastGraphView;
import fr.qgdev.openweather.forecastView.HourlyForecastGraphView;
import fr.qgdev.openweather.fragment.places.PlacesFragment;
import fr.qgdev.openweather.weather.CurrentWeather;


public class PlaceRecyclerViewAdapter extends RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceAdapterViewHolder> {

	private final Context context;
	private static PlacesFragment placesFragment;
	private static ArrayList<PlaceView> placeViewArrayList;
	private static DataPlaces dataPlaces;
	private final List<String> countryNames;
	private final List<String> countryCodes;
	private static FormattingService formattingService;
	private final PlacesFragment.Interactions placesFragmentInteractions;

	public PlaceRecyclerViewAdapter(Context context, PlacesFragment placesFragment, PlacesFragment.Interactions placesFragmentInteractions, DataPlaces dataPlaces) {
		this.context = context;
		this.placesFragment = placesFragment;
		this.placesFragmentInteractions = placesFragmentInteractions;
		this.placeViewArrayList = generatePlaceViewArray();
		this.dataPlaces = dataPlaces;

		this.countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		this.countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));

		formattingService = new FormattingService(context);
	}

	public ArrayList<PlaceView> generatePlaceViewArray() {
		ArrayList<PlaceView> placeViewArray = new ArrayList<>();

		for (int index = 0; index < placesFragment.getPlaceArrayListSize(); index++) {
			placeViewArray.add(new PlaceView(PlaceView.COMPACT));
		}

		return placeViewArray;
	}


	public void add(int position) {
		placeViewArrayList.add(position, new PlaceView(PlaceView.COMPACT));
		this.notifyItemInserted(position);
	}


	public void remove(int position) {
		this.placesFragmentInteractions.onPlaceDeletion(dataPlaces, position);
		placeViewArrayList.remove(position);
		this.notifyItemRemoved(position);
	}


	@Override
	public int getItemViewType(int position) {
		return this.placeViewArrayList.get(position).viewType;
	}

	private void setListeners(int position, Place currentPlace, PlaceAdapterViewHolder holder) {

		holder.cardView.setOnClickListener(v -> {
			switch (this.placeViewArrayList.get(position).viewType) {
				case PlaceView.COMPACT:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED;
					break;
				default:
					this.placeViewArrayList.get(position).viewType = PlaceView.COMPACT;
					break;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		holder.hourlyForecast.setOnClickListener(v -> {
			switch (this.placeViewArrayList.get(position).viewType) {
				case PlaceView.EXTENDED:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_HOURLY;
					break;
				case PlaceView.EXTENDED_HOURLY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED;
					break;
				case PlaceView.EXTENDED_DAILY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_FULLY;
					break;
				case PlaceView.EXTENDED_FULLY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_DAILY;
					break;
				default:
					this.placeViewArrayList.get(position).viewType = PlaceView.COMPACT;
					break;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		holder.dailyForecast.setOnClickListener(v -> {
			switch (this.placeViewArrayList.get(position).viewType) {
				case PlaceView.EXTENDED:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_DAILY;
					break;
				case PlaceView.EXTENDED_DAILY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED;
					break;
				case PlaceView.EXTENDED_HOURLY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_FULLY;
					break;
				case PlaceView.EXTENDED_FULLY:
					this.placeViewArrayList.get(position).viewType = PlaceView.EXTENDED_HOURLY;
					break;
				default:
					this.placeViewArrayList.get(position).viewType = PlaceView.COMPACT;
					break;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		holder.weatherAlertLayout.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context, currentPlace);
			weatherAlertDialog.build();
		});

		holder.weatherAlertIcon.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context, currentPlace);
			weatherAlertDialog.build();
		});
	}

	private String getCountryName(String countryCode) {
		int index = this.countryCodes.indexOf(countryCode);
		return this.countryNames.get(index);
	}

	@NonNull
	@Override
	public PlaceAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.adapter_places, parent, false);

		return new PlaceAdapterViewHolder(context, view);
	}

	@Override
	public void onBindViewHolder(@NonNull PlaceAdapterViewHolder holder, final int position) {

		Place currentPlace = this.placesFragment.getPlace(position);

		switch (this.placeViewArrayList.get(position).viewType) {
			case PlaceView.COMPACT:
			default: {
				//  Compact view
				if (context.getResources().getInteger(R.integer.env_variables_column_count) == 2)
					holder.pressureTextView.setVisibility(View.GONE);
				else holder.windGustSpeedTextView.setVisibility(View.GONE);

				holder.visibilityTextView.setVisibility(View.GONE);
				holder.sunriseTextView.setVisibility(View.GONE);
				holder.sunsetTextView.setVisibility(View.GONE);
				holder.cloudinessTextView.setVisibility(View.GONE);

				if (currentPlace.getMWeatherAlertCount() > 0) {
					holder.weatherAlertIcon.setVisibility(View.VISIBLE);
				} else {
					holder.weatherAlertIcon.setVisibility(View.GONE);
				}

				break;
			}
			case PlaceView.EXTENDED: {
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

			case PlaceView.EXTENDED_HOURLY: {
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

			case PlaceView.EXTENDED_DAILY: {
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
			case PlaceView.EXTENDED_FULLY: {
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
		}

		setListeners(position, currentPlace, holder);

		holder.cityNameTextView.setText(currentPlace.getCity());
		holder.countryNameTextVIew.setText(this.getCountryName(currentPlace.getCountryCode()));

		CurrentWeather currentWeather = currentPlace.getCurrentWeather();

		holder.temperatureTextView.setText(formattingService.getFloatFormattedTemperature(currentWeather.temperature, true));
		holder.temperatureFeelsLikeTextView.setText(formattingService.getFloatFormattedTemperature(currentWeather.temperatureFeelsLike, true));


		holder.weatherDescription.setText(currentWeather.weatherDescription);

		final int weatherIconId;

		switch (currentWeather.weatherCode) {

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
				if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
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
				if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
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
				if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
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
				if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
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
				if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
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
		holder.windDirectionTextView.setText(formattingService.getFormattedDirection(currentWeather.windDirection, currentWeather.isWindDirectionReadable));

		////  Wind speed and Wind gust Speed
		holder.windSpeedTextView.setText(formattingService.getIntFormattedSpeed(currentWeather.windSpeed, true));
		holder.windGustSpeedTextView.setText(formattingService.getIntFormattedSpeed(currentWeather.windGustSpeed, true));

		//  Humidity
		holder.humidityTextView.setText(String.format("%d %%", currentWeather.humidity));

		//  Pressure
		holder.pressureTextView.setText(formattingService.getFormattedPressure(currentWeather.pressure, true));

		//  Visibility
		holder.visibilityTextView.setText(formattingService.getIntFormattedDistance(currentWeather.visibility, true));

		//  Sunrise and sunset
		holder.sunriseTextView.setText(formattingService.getFormattedTime(new Date(currentWeather.sunrise), currentPlace.getTimeZone()));
		holder.sunsetTextView.setText(formattingService.getFormattedTime(new Date(currentWeather.sunset), currentPlace.getTimeZone()));

		holder.cloudinessTextView.setText(currentWeather.cloudiness + " %");

		//  Precipitations
		if (currentWeather.rain > 0 || currentWeather.snow > 0) {
			holder.precipitationLayout.setVisibility(View.VISIBLE);

			holder.rainTextView.setText(formattingService.getFloatFormattedShortDistance(currentWeather.rain, true));
			holder.snowTextView.setText(formattingService.getFloatFormattedShortDistance(currentWeather.snow, true));


			if (currentWeather.rain > 0) {
				holder.precipitationLayout.findViewById(R.id.rain_precipitations).setVisibility(View.VISIBLE);
			} else {
				holder.precipitationLayout.findViewById(R.id.rain_precipitations).setVisibility(View.GONE);
			}
			if (currentWeather.snow > 0) {
				holder.precipitationLayout.findViewById(R.id.snow_precipitations).setVisibility(View.VISIBLE);
			} else {
				holder.precipitationLayout.findViewById(R.id.snow_precipitations).setVisibility(View.GONE);
			}
		} else {
			holder.precipitationLayout.setVisibility(View.GONE);
		}

		holder.hourlyForecastGraphView.initialisation(currentPlace.getHourlyWeatherForecastArrayList(), currentPlace.getDailyWeatherForecastArrayList(), formattingService, currentPlace.getTimeZone());
		holder.dailyForecastGraphView.initialisation(currentPlace.getDailyWeatherForecastArrayList(), currentPlace.getTimeZone(), formattingService);

		holder.lastUpdateAvailableTextView.setText(String.format("%s %s", formattingService.getFormattedFullTimeHour(new Date(currentWeather.dt), currentPlace.getTimeZone()), currentPlace.getTimeZoneStringForm()));

		Place finalCurrentPlace = currentPlace;
		holder.cardView.setOnLongClickListener(v -> {
			new AlertDialog.Builder(context)
					.setTitle(context.getString(R.string.dialog_confirmation_title_delete_place))
					.setMessage(String.format(context.getString(R.string.dialog_confirmation_message_delete_place), finalCurrentPlace.getCity(), finalCurrentPlace.getCountryCode()))
					.setPositiveButton(context.getString(R.string.dialog_confirmation_choice_yes), (dialog, which) -> {

						int index = holder.getAbsoluteAdapterPosition();
						remove(index);
					})
					.setNegativeButton(context.getString(R.string.dialog_confirmation_choice_no), null)
					.show();
			return false;
		});
	}

	public static class PlaceView {
		public static final byte COMPACT = 0;
		public static final byte EXTENDED = 1;
		public static final byte EXTENDED_HOURLY = 2;
		public static final byte EXTENDED_DAILY = 3;
		public static final byte EXTENDED_FULLY = 4;
		public byte viewType;

		public PlaceView(byte viewType) {
			this.viewType = viewType;
		}
	}

	@Override
	public int getItemCount() {
		return this.placeViewArrayList.size();
	}

	public static class PlaceAdapterViewHolder extends RecyclerView.ViewHolder {

		public MaterialCardView cardView;
		public LinearLayout adapterPlaceLayout;
		public TextView cityNameTextView;
		public TextView countryNameTextVIew;

		public TextView temperatureTextView;
		public TextView temperatureFeelsLikeTextView;

		public TextView weatherDescription;
		public ImageView weatherIcon;

		public LinearLayout weatherAlertLayout;
		public ImageView weatherAlertIcon;

		public TextView windDirectionTextView;
		public TextView windSpeedTextView;
		public TextView windGustSpeedTextView;

		public TextView humidityTextView;
		public TextView pressureTextView;
		public TextView visibilityTextView;

		public TextView sunriseTextView;
		public TextView sunsetTextView;
		public TextView cloudinessTextView;

		public LinearLayout precipitationLayout;
		public TextView rainTextView;
		public TextView snowTextView;

		public LinearLayout lastUpdateAvailableLayout;
		public TextView lastUpdateAvailableTextView;

		public LinearLayout detailedInformationsLayout;
		public LinearLayout forecastInformationsLayout;

		public LinearLayout hourlyForecastLayout;
		//public LinearLayout hourlyForecastScrollview;
		public LinearLayout hourlyForecast;
		public ImageView hourlyForecastExpandIcon;

		public HourlyForecastGraphView hourlyForecastGraphView;

		public LinearLayout dailyForecastLayout;
		//public LinearLayout dailyForecastScrollview;
		public LinearLayout dailyForecast;

		public DailyForecastGraphView dailyForecastGraphView;

		public ImageView dailyForecastExpandIcon;

		public PlaceAdapterViewHolder(@NonNull Context context, @NonNull View itemView) {
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

			this.precipitationLayout = itemView.findViewById(R.id.precipitations);
			this.rainTextView = itemView.findViewById(R.id.rain_precipitations_current_value);
			this.snowTextView = itemView.findViewById(R.id.snow_precipitations_current_value);
			this.lastUpdateAvailableLayout = itemView.findViewById(R.id.last_update_available);
			this.lastUpdateAvailableTextView = itemView.findViewById(R.id.last_update_value);

			this.detailedInformationsLayout = itemView.findViewById(R.id.detailed_informations);
			this.forecastInformationsLayout = itemView.findViewById(R.id.forecast);

			this.hourlyForecastLayout = itemView.findViewById(R.id.hourly_forecast);
			this.hourlyForecast = itemView.findViewById(R.id.hourly_forecast_layout);
			this.hourlyForecastExpandIcon = itemView.findViewById(R.id.hourly_forecast_expand_icon);
			this.hourlyForecastGraphView = itemView.findViewById(R.id.hourly_graphview);

			this.dailyForecastLayout = itemView.findViewById(R.id.daily_forecast);
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

		// dp --> px
		private int dpToPx(Context context, float dip) {
			return (int) TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP,
					dip,
					context.getResources().getDisplayMetrics());
		}

		private void setDrawableCompoundTextView(Context context, TextView textView, @DrawableRes int id) {
			int compoundDrawableSideSize = dpToPx(context, 20);
			Drawable drawable = context.getDrawable(id);
			drawable.setBounds(0, 0, compoundDrawableSideSize, compoundDrawableSideSize);
			textView.setCompoundDrawables(drawable, null, null, null);
		}
	}
}
