package fr.qgdev.openweather.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.dialog.WeatherAlertDialog;
import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;


public class PlaceRecyclerViewAdapter extends RecyclerView.Adapter<PlaceRecyclerViewAdapter.PlaceAdapterViewHolder> {

	private final Context context;
	private final ArrayList<PlaceView> placeViewArrayList;
	private final List<String> countryNames;
	private final List<String> countryCodes;
	private final ActionCallback actionCallback;

	public interface ActionCallback{
		void onPlaceDeletion(int position);
	}

	public static class PlaceView{

		public int viewType;
		public Place place;

		public static final int COMPACT = 0;
		public static final int EXTENDED = 1;

		public PlaceView(Place place, int viewType) {
			this.place = place;
			this.viewType = viewType;
		}
	}

	public ArrayList<PlaceView> generatePlaceViewArrayList(final ArrayList<Place> placeArrayList)
	{
		ArrayList<PlaceView> placeViewArrayList = new ArrayList<>();

		for(Place place : placeArrayList)
		{
			placeViewArrayList.add(new PlaceView(place, PlaceView.COMPACT));
		}

		return placeViewArrayList;
	}

	public void add(final Place place)
	{
		this.placeViewArrayList.add(new PlaceView(place, PlaceView.COMPACT));
		this.notifyItemInserted(this.placeViewArrayList.size() - 1);
	}

	public void add(int position, final Place place)
	{
		this.placeViewArrayList.add(position, new PlaceView(place, PlaceView.COMPACT));
		this.notifyItemInserted(position);
	}

	public void remove(int position)
	{
		this.placeViewArrayList.remove(position);
		this.notifyItemRemoved(position);
	}

	public void set(int position, final Place place)
	{
		this.placeViewArrayList.set(position, new PlaceView(place, PlaceView.COMPACT));
		this.notifyItemChanged(position);
	}

	public void remplaceSet(final ArrayList<Place> placeArrayList)
	{
		this.placeViewArrayList.clear();
		for(Place place : placeArrayList)
		{
			this.placeViewArrayList.add(new PlaceView(place, PlaceView.COMPACT));
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position)
	{
		return this.placeViewArrayList.get(position).viewType;
	}

	public PlaceRecyclerViewAdapter(Context context, ArrayList<Place> placeViewArrayList, ActionCallback actionCallback) {
		this.context = context;
		this.placeViewArrayList = generatePlaceViewArrayList(placeViewArrayList);
		this.actionCallback = actionCallback;

		this.countryNames = Arrays.asList(context.getResources().getStringArray(R.array.countries_names));
		this.countryCodes = Arrays.asList(context.getResources().getStringArray(R.array.countries_codes));
	}

	private String getCountryName(String countryCode)
	{
		int index = this.countryCodes.indexOf(countryCode);
		return this.countryNames.get(index);
	}

	private void setListeners(PlaceView placeView, PlaceAdapterViewHolder holder) {

		holder.cardView.setOnClickListener(v -> {
			if (placeView.viewType == PlaceView.COMPACT) {

				placeView.viewType = PlaceView.EXTENDED;

			} else {
				placeView.viewType = PlaceView.COMPACT;
			}
			this.notifyItemChanged(holder.getAbsoluteAdapterPosition());
		});

		ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList = placeView.place.getHourlyWeatherForecastArrayList();
		HourlyColumnAdapter hourlyColumnAdapter = new HourlyColumnAdapter(context, placeView.place);

		ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList = placeView.place.getDailyWeatherForecastArrayList();
		DailyColumnAdapter dailyColumnAdapter = new DailyColumnAdapter(context, placeView.place);


		holder.hourlyForecast.setOnClickListener(hourlyView -> {

			if (holder.hourlyForecastLayout.getVisibility() == View.GONE) {
				holder.hourlyForecastExpandIcon.setRotation(180);
				if (holder.hourlyForecastScrollview.getChildCount() < 48) {

					holder.hourlyForecastScrollview.removeAllViewsInLayout();

					for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {
						holder.hourlyForecastScrollview.addView(hourlyColumnAdapter.getView(index, null, null), index);
					}
				}
				holder.hourlyForecastLayout.setVisibility(View.VISIBLE);
			} else {
				holder.hourlyForecastExpandIcon.setRotation(0);
				holder.hourlyForecastLayout.setVisibility(View.GONE);
			}
		});

		holder.dailyForecast.setOnClickListener(hourlyView -> {

			if (holder.dailyForecastLayout.getVisibility() == View.GONE) {
				holder.dailyForecastExpandIcon.setRotation(180);
				if (holder.dailyForecastScrollview.getChildCount() < 48) {

					holder.dailyForecastScrollview.removeAllViewsInLayout();

					for (int index = 0; index < dailyWeatherForecastArrayList.size(); index++) {
						holder.dailyForecastScrollview.addView(dailyColumnAdapter.getView(index, null, null), index);
					}
				}
				holder.dailyForecastLayout.setVisibility(View.VISIBLE);

			} else {
				holder.dailyForecastExpandIcon.setRotation(0);
				holder.dailyForecastLayout.setVisibility(View.GONE);
			}
		});

		holder.weatherAlertLayout.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context, placeView.place);
			weatherAlertDialog.build();
		});

		holder.weatherAlertIcon.setOnClickListener(v -> {
			final WeatherAlertDialog weatherAlertDialog = new WeatherAlertDialog(context, placeView.place);
			weatherAlertDialog.build();
		});
	}

	@NonNull
	@Override
	public PlaceAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.adapter_places, parent, false);

		return new PlaceAdapterViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull PlaceAdapterViewHolder holder, final int position) {

		SharedPreferences userPref = PreferenceManager.getDefaultSharedPreferences(context);
		String temperatureUnit = userPref.getString("temperature_unit", null);
		String measureUnit = userPref.getString("measure_unit", null);
		String pressureUnit = userPref.getString("pressure_unit", null);
		String timeOffset = userPref.getString("time_offset", null);
		String timeFormat = userPref.getString("time_format", null);

		Place currentPlace = this.placeViewArrayList.get(position).place;

		switch (this.placeViewArrayList.get(position).viewType)
		{
			case PlaceView.COMPACT:
				holder.detailedInformationsLayout.setVisibility(View.GONE);
				holder.windGustInfomationLayout.setVisibility(View.GONE);
				holder.visibilityInformationLayout.setVisibility(View.GONE);
				holder.skyInformationsLayout.setVisibility(View.GONE);
				holder.forecastInformationsLayout.setVisibility(View.GONE);
				holder.hourlyForecastLayout.setVisibility(View.GONE);
				holder.dailyForecastLayout.setVisibility(View.GONE);
				holder.lastUpdateAvailableLayout.setVisibility(View.GONE);
				if(currentPlace.getMWeatherAlertCount() > 0)
				{
					holder.weatherAlertIcon.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.weatherAlertIcon.setVisibility(View.GONE);
				}

				break;
			case PlaceView.EXTENDED:
				holder.detailedInformationsLayout.setVisibility(View.VISIBLE);
				holder.weatherAlertIcon.setVisibility(View.GONE);
				holder.windGustInfomationLayout.setVisibility(View.VISIBLE);
				holder.visibilityInformationLayout.setVisibility(View.VISIBLE);
				holder.skyInformationsLayout.setVisibility(View.VISIBLE);
				holder.forecastInformationsLayout.setVisibility(View.VISIBLE);
				holder.hourlyForecastExpandIcon.setRotation(0);
				holder.dailyForecastExpandIcon.setRotation(0);
				holder.lastUpdateAvailableLayout.setVisibility(View.VISIBLE);
				if(currentPlace.getMWeatherAlertCount() > 0)
				{
					holder.weatherAlertLayout.setVisibility(View.VISIBLE);
				}
				else
				{
					holder.weatherAlertLayout.setVisibility(View.GONE);
				}
				break;
		}

		setListeners(this.placeViewArrayList.get(position), holder);

		holder.cityNameTextView.setText(currentPlace.getCity());
		holder.countryNameTextVIew.setText(this.getCountryName(currentPlace.getCountryCode()));

		CurrentWeather currentWeather = currentPlace.getCurrentWeather();

		if (temperatureUnit.contains("celsius")) {
			holder.temperatureTextView.setText(String.format("%.1f°C", (currentWeather.temperature - 273.15)));
			holder.temperatureFeelsLikeTextView.setText(String.format("%.1f°C", (currentWeather.temperatureFeelsLike - 273.15)));
		} else {
			holder.temperatureTextView.setText(String.format("%.1f °F", ((currentWeather.temperature - 273.15) * (9 / 5)) + 32));
			holder.temperatureFeelsLikeTextView.setText(String.format("%.1f°F", ((currentWeather.temperatureFeelsLike - 273.15) * (9 / 5)) + 32));
		}

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

		holder.weatherIcon.setImageResource(weatherIconId);

		//  Wind
		////    Wind Direction
		if (currentWeather.isWindDirectionReadable) {
			holder.windDirectionTextView.setText(currentWeather.getWindDirectionCardinalPoints());
		} else {
			holder.windDirectionTextView.setText("N/A");
		}
		////  Wind speed and Wind gust Speed
		if (measureUnit.contains("metric")) {
			holder.windSpeedTextView.setText(String.format("%d km/h", (int) (currentWeather.windSpeed * 3.6)));
			holder.windGustSpeedTextView.setText(String.format("%d km/h", (int) (currentWeather.windGustSpeed * 3.6)));
		} else {
			holder.windSpeedTextView.setText(String.format("%d mph", (int) (currentWeather.windSpeed * 2.23694)));
			holder.windGustSpeedTextView.setText(String.format("%d mph", (int) (currentWeather.windGustSpeed * 2.23694)));
		}

		//  Humidity
		holder.humidityTextView.setText(String.format("%d %%", currentWeather.humidity));

		//  Pressure
		if (pressureUnit.contains("hpa")) {
			holder.pressureTextView.setText(String.format("%d hPa", currentWeather.pressure));
		} else {
			holder.pressureTextView.setText(String.format("%d bar", currentWeather.pressure));
		}

		//  Visibility
		if (measureUnit.contains("metric")) {
			holder.visibilityTextView.setText(String.format("%d km", currentWeather.visibility / 1000));
		} else {
			if (currentWeather.visibility * 0.000621371 > 1) {
				holder.visibilityTextView.setText(String.format("%d miles", (int) (currentWeather.visibility * 0.000621371)));
			} else {
				holder.visibilityTextView.setText(String.format("%d mile", (int) (currentWeather.visibility * 0.000621371)));
			}
		}

		DateFormat simpleDateFormat;

		if (timeFormat.contains("24")) {
			simpleDateFormat = new SimpleDateFormat("HH:mm");
		} else {
			simpleDateFormat = new SimpleDateFormat("KK:mm a");
		}

		if (timeOffset.contains("place"))
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(currentPlace.getTimeZone())));

		holder.sunriseTextView.setText(simpleDateFormat.format(new Date(currentWeather.sunrise)));
		holder.sunsetTextView.setText(simpleDateFormat.format(new Date(currentWeather.sunset)));

		holder.cloudinessTextView.setText(currentWeather.cloudiness + " %");

		//  Precipitations
		if (currentWeather.rain > 0 || currentWeather.snow > 0) {
			holder.precipitationLayout.setVisibility(View.VISIBLE);

			if (measureUnit.contains("metric")) {
				holder.rainTextView.setText(String.format("%.1f mm", currentWeather.rain));
				holder.snowTextView.setText(String.format("%.1f mm", currentWeather.snow));
			} else {
				holder.rainTextView.setText(String.format("%.1f in", currentWeather.rain / 25.4));
				holder.snowTextView.setText(String.format("%.1f in", currentWeather.snow / 25.4));
			}

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

		DateFormat lastUpdateDateFormat;

		if (timeFormat.contains("24")) {
			lastUpdateDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
		} else {
			lastUpdateDateFormat = new SimpleDateFormat("dd/MM/yy KK:mm a");
		}

		if (timeOffset.contains("place")) {
			lastUpdateDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(currentPlace.getTimeZone())));

			if (currentPlace.getTimeZone().equals("UTC")) {
				holder.lastUpdateAvailableTextView.setText(String.format("%s %s+0", lastUpdateDateFormat.format(new Date(currentWeather.dt)), currentPlace.getTimeZone()));
			} else {
				holder.lastUpdateAvailableTextView.setText(String.format("%s UTC%s", lastUpdateDateFormat.format(new Date(currentWeather.dt)), currentPlace.getTimeZone()));
			}
		} else {
			holder.lastUpdateAvailableTextView.setText(lastUpdateDateFormat.format(new Date(currentWeather.dt)));
		}

		holder.cardView.setOnLongClickListener(v -> {
			new AlertDialog.Builder(context)
					.setTitle(context.getString(R.string.dialog_confirmation_title_delete_place))
					.setMessage(String.format(context.getString(R.string.dialog_confirmation_message_delete_place), currentPlace.getCity(), currentPlace.getCountryCode()))
					.setPositiveButton(context.getString(R.string.dialog_confirmation_choice_yes), (dialog, which) -> {

						int index = holder.getAbsoluteAdapterPosition();
						actionCallback.onPlaceDeletion(index);
						remove(index);
					})
					.setNegativeButton(context.getString(R.string.dialog_confirmation_choice_no), null)
					.show();
			return false;
		});
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
		public LinearLayout windGustInfomationLayout;
		public LinearLayout visibilityInformationLayout;
		public LinearLayout skyInformationsLayout;
		public LinearLayout forecastInformationsLayout;

		public LinearLayout hourlyForecastLayout;
		public LinearLayout hourlyForecastScrollview;
		public LinearLayout hourlyForecast;
		public ImageView hourlyForecastExpandIcon;

		public LinearLayout dailyForecastLayout;
		public LinearLayout dailyForecastScrollview;
		public LinearLayout dailyForecast;
		public ImageView dailyForecastExpandIcon;


		public PlaceAdapterViewHolder(@NonNull View itemView) {
			super(itemView);

			this.cardView = itemView.findViewById(R.id.card_place);
			this.adapterPlaceLayout = itemView.findViewById(R.id.adapter_place);
			this.cityNameTextView = itemView.findViewById(R.id.city_adapter);
			this.countryNameTextVIew = itemView.findViewById(R.id.country_adapter);
			this.temperatureTextView = itemView.findViewById(R.id.temperature_adapter);
			this.temperatureFeelsLikeTextView = itemView.findViewById(R.id.temperature_feelslike_adapter);
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
			this.windGustInfomationLayout = itemView.findViewById(R.id.wind_gust_speed);
			this.visibilityInformationLayout = itemView.findViewById(R.id.visibility);
			this.skyInformationsLayout = itemView.findViewById(R.id.sky_informations);
			this.forecastInformationsLayout = itemView.findViewById(R.id.forecast);

			this.hourlyForecastLayout = itemView.findViewById(R.id.hourly_forecast);
			this.hourlyForecastScrollview = itemView.findViewById(R.id.hourly_forecast_scrollView);
			this.hourlyForecast = itemView.findViewById(R.id.hourly_forecast_layout);
			this.hourlyForecastExpandIcon = itemView.findViewById(R.id.hourly_forecast_expand_icon);

			this.dailyForecastLayout = itemView.findViewById(R.id.daily_forecast);
			this.dailyForecastScrollview = itemView.findViewById(R.id.daily_forecast_scrollView);
			this.dailyForecast = itemView.findViewById(R.id.daily_forecast_layout);
			this.dailyForecastExpandIcon = itemView.findViewById(R.id.daily_forecast_expand_icon);
		}
	}
}
