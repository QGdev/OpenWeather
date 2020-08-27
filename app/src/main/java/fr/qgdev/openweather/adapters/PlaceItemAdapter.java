package fr.qgdev.openweather.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;


public class PlaceItemAdapter extends BaseAdapter{

    private Context context;
    private List<Place> placeItemList;
    private LayoutInflater inflater;
    private View parentView;

    public PlaceItemAdapter(Context context, List<Place> placeItemList, View parentView) {
        this.context = context;
        this.placeItemList = placeItemList;
        this.inflater = LayoutInflater.from(context);
        this.parentView = parentView;
    }


    @Override
    public int getCount() {
        return placeItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return placeItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.adapter_places, null);

        LinearLayout adapterPlace = view.findViewById(R.id.adapter_place);
        MaterialCardView cardView = view.findViewById(R.id.card_place);

        TextView cityTextView = view.findViewById(R.id.city_adapter);
        TextView countryTextView = view.findViewById(R.id.country_adapter);

        TextView temperatureTextView = view.findViewById(R.id.temperature_adapter);
        TextView temperatureFeelsLikeTextView = view.findViewById(R.id.temperature_feelslike_adapter);

        TextView weatherDescriptionTextView = view.findViewById(R.id.weather_description_adapter);
        ImageView weatherIconImageView = view.findViewById(R.id.weather_icon_adapter);

        TextView windDirectionTextView = view.findViewById(R.id.wind_direction_value);
        TextView windSpeedTextView = view.findViewById(R.id.wind_speed_value);
        TextView windGustSpeedTextView = view.findViewById(R.id.wind_gust_speed_value);

        TextView humidityTextView = view.findViewById(R.id.humidity_value);
        TextView pressureTextView = view.findViewById(R.id.pressure_value);
        TextView visibilityTextView = view.findViewById(R.id.visibility_value);

        TextView sunriseTextView = view.findViewById(R.id.sunrise_value);
        TextView sunsetTextView = view.findViewById(R.id.sunset_value);
        TextView cloudinessTextView = view.findViewById(R.id.cloudiness_value);

        TextView rainTextView = view.findViewById(R.id.rain_precipitations_current_value);
        TextView snowTextView = view.findViewById(R.id.snow_precipitations_current_value);


        //  Get parent view elements
        final SwipeRefreshLayout swipeRefreshLayout = parentView.findViewById(R.id.swiperefresh);
        final TextView noPlacesRegisteredTextView = parentView.findViewById(R.id.no_places_registered);


        SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(context);
        String temperatureUnit = apiKeyPref.getString("temperature_unit", null);
        String measureUnit = apiKeyPref.getString("measure_unit", null);
        String pressureUnit = apiKeyPref.getString("pressure_unit", null);
        String timeOffset = apiKeyPref.getString("time_offset", null);
        String timeFormat = apiKeyPref.getString("time_format", null);

        Place currentItem = (Place) getItem(position);
        CurrentWeather currentWeather = currentItem.getCurrentWeather();

        //  Place settings
        final String cityName = currentItem.getCity();
        final String countryName = currentItem.getCountry();

        //  Temperature
        final String temperature;
        final String temperatureFeelsLike;
        
        if(temperatureUnit.contains("celsius")){
            temperature = String.format("%.1f°C", (currentWeather.temperature - 273.15));
            temperatureFeelsLike = String.format("%.1f°C", (currentWeather.temperatureFeelsLike - 273.15));
        }
        else {
            temperature = String.format("%.1f °F", ((currentWeather.temperature - 273.15) * (9/5)) + 32);
            temperatureFeelsLike = String.format("%.1f°F", ((currentWeather.temperatureFeelsLike - 273.15) * (9/5)) + 32);
        }

        //  Weather description and icon
        final String weatherDescription = currentWeather.weatherDescription;
        final int weatherIconId;

        switch (weatherDescription) {
            case "light thunderstorm":
            case "ragged thunderstorm":
            case "heavy thunderstorm":
            case "thunderstorm":
                weatherIconId = context.getResources().getIdentifier("thunderstorm_flat", "drawable", context.getPackageName());
                break;

            //Drizzle
            case "light intensity drizzle":
            case "drizzle rain":
            case "light intensity drizzle rain":
            case "heavy intensity drizzle":
            case "drizzle":
                weatherIconId = context.getResources().getIdentifier("hail_flat", "drawable", context.getPackageName());
                break;
            case "heavy intensity drizzle rain":
            case "shower rain and drizzle":
            case "heavy shower rain and drizzle":
            case "shower drizzle":
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("rainy_night_flat", "drawable", context.getPackageName());
                }
                break;

            //  Rain
            case "light rain":
            case "heavy intensity rain":
            case "moderate rain":
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("rainy_night_flat", "drawable", context.getPackageName());
                }
                break;
            case "very heavy rain":
            case "shower rain":
            case "light intensity shower rain":
            case "freezing rain":
            case "extreme rain":
                weatherIconId = context.getResources().getIdentifier("rain_flat", "drawable", context.getPackageName());
                break;
            case "heavy intensity shower rain":
            case "ragged shower rain":
                weatherIconId = context.getResources().getIdentifier("heavy_rain_flat", "drawable", context.getPackageName());
                break;

            //  Snow
            case "light snow":
            case "Snow":
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("snow_and_night_flat", "drawable", context.getPackageName());
                }
                break;
            case "Heavy snow":
            case "Heavy shower snow":
            case "Shower snow":
            case "Light shower snow":
                weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
                break;
            case "Sleet":
            case "Rain and snow":
            case "Light rain and snow":
            case "Shower sleet":
            case "Light shower sleet":
                weatherIconId = context.getResources().getIdentifier("sleet_flat", "drawable", context.getPackageName());
                break;

            //  Atmosphere
            case "mist":
            case "Smoke":
            case "Haze":
            case "sand/ dust whirls":
            case "volcanic ash":
            case "squalls":
            case "dust":
            case "sand":
            case "fog":
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("fog_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("fog_and_night_flat", "drawable", context.getPackageName());
                }
                break;
            case "tornado":
                weatherIconId = context.getResources().getIdentifier("tornado_flat", "drawable", context.getPackageName());

                //  Sky
                break;
            case "clear sky":
                //  Day
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("sun_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("moon_phase_flat", "drawable", context.getPackageName());
                }
                break;
            case "few clouds":
            case "broken clouds":
            case "scattered clouds":
                if (currentWeather.dt >= currentWeather.sunrise && currentWeather.dt < currentWeather.sunset) {
                    weatherIconId = context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", context.getPackageName());
                }
                //  Night
                else {
                    weatherIconId = context.getResources().getIdentifier("cloudy_night_flat", "drawable", context.getPackageName());
                }
                break;
            case "overcast clouds":
                weatherIconId = context.getResources().getIdentifier("cloudy_flat", "drawable", context.getPackageName());
                break;
            case "thunderstorm with heavy drizzle":
            case "thunderstorm with drizzle":
            case "thunderstorm with light drizzle":
            case "thunderstorm with heavy rain":
            case "thunderstorm with rain":
                //  Thunderstorm Group
            case "thunderstorm with light rain":

                //  Default
            default:
                weatherIconId = context.getResources().getIdentifier("storm_flat", "drawable", context.getPackageName());
                break;
        }


        final String windDirection;
        final String windSpeed;
        final String windGustSpeed;

        //  Wind
        ////    Wind Direction
        if(currentWeather.isWindDirectionReadable){
            windDirection = currentWeather.getWindDirectionCardinalPoints();
        }
        else{
            windDirection = "N/A";
        }
        ////  Wind speed and Wind gust Speed
        if(measureUnit.contains("metric")){
            windSpeed = String.format("%d km/h", (int) (currentWeather.windSpeed * 3.6));
            windGustSpeed = String.format("%d km/h", (int) (currentWeather.windGustSpeed * 3.6));
        }
        else{
            windSpeed = String.format("%d mph", (int) (currentWeather.windSpeed * 2.23694));
            windGustSpeed = String.format("%d mph", (int) (currentWeather.windGustSpeed * 2.23694));
        }


        final String humidity;
        final String pressure;
        final String visibility;

        //  Humidity
        humidity = currentWeather.humidity + " %";

        //  Pressure
        if(pressureUnit.contains("hpa")){
            pressure = String.format("%d hPa", currentWeather.pressure);
        }
        else{
            pressure = String.format("%d bar",currentWeather.pressure);
        }

        //  Visibility
        if (measureUnit.contains("metric")) {
            visibility = String.format("%d km", currentWeather.visibility / 1000);
        } else {
            visibility = String.format("%d mile", (int) (currentWeather.visibility * 0.000621371));
        }

        final String sunrise;
        final String sunset;
        final String cloudiness;


        DateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

        if (timeOffset.contains("place"))
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(currentItem.getTimeZone())));

        sunrise = simpleDateFormat.format(new Date(currentWeather.sunrise));
        sunset = simpleDateFormat.format(new Date(currentWeather.sunset));


        cloudiness = currentWeather.cloudiness + " %";

        final String rain;
        final String snow;

        //  Precipitations
        if(measureUnit.contains("metric")){
            rain = String.format("%.1f mm", currentWeather.rain);
            snow = String.format("%.1f mm", currentWeather.snow);
        }
        else{
            rain = String.format("%.1f in", currentWeather.rain / 25.4);
            snow = String.format("%.1f in", currentWeather.snow / 25.4);
        }


        cityTextView.setText(cityName);
        countryTextView.setText(countryName);

        temperatureTextView.setText(temperature);
        temperatureFeelsLikeTextView.setText(temperatureFeelsLike);

        weatherDescriptionTextView.setText(weatherDescription);
        weatherIconImageView.setImageResource(weatherIconId);

        windDirectionTextView.setText(windDirection);
        windSpeedTextView.setText(windSpeed);
        windGustSpeedTextView.setText(windGustSpeed);

        humidityTextView.setText(humidity);
        pressureTextView.setText(pressure);
        visibilityTextView.setText(visibility);

        sunriseTextView.setText(sunrise);
        sunsetTextView.setText(sunset);
        cloudinessTextView.setText(cloudiness);

        rainTextView.setText(rain);
        snowTextView.setText(snow);

        LinearLayout detailedInformationsLinearLayout = view.findViewById(R.id.detailed_informations);
        LinearLayout windGustInfomationLinearLayout = view.findViewById(R.id.wind_gust_speed);
        LinearLayout visibilityInformationLinearLayout = view.findViewById(R.id.visibility);
        LinearLayout skyInformationsLinearLayout = view.findViewById(R.id.sky_informations);
        LinearLayout forecastInformationsLinearLayout = view.findViewById(R.id.forecast);

        LinearLayout hourlyForecastLinearLayout = view.findViewById(R.id.hourly_forecast);
        LinearLayout hourlyForecastScrollview = view.findViewById(R.id.hourly_forecast_scrollView);
        LinearLayout hourlyForecast = view.findViewById(R.id.hourly_forecast_layout);
        ImageView hourlyForecastExpandIcon = view.findViewById(R.id.hourly_forecast_expand_icon);

        ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList = currentItem.getHourlyWeatherForecastArrayList();

        HourlyColumnAdapter hourlyColumnAdapter = new HourlyColumnAdapter(context, currentItem);

        cardView.setOnClickListener(v -> {

            if (detailedInformationsLinearLayout.getVisibility() == View.GONE) {


                detailedInformationsLinearLayout.setVisibility(View.VISIBLE);
                windGustInfomationLinearLayout.setVisibility(View.VISIBLE);
                visibilityInformationLinearLayout.setVisibility(View.VISIBLE);
                skyInformationsLinearLayout.setVisibility(View.VISIBLE);
                forecastInformationsLinearLayout.setVisibility(View.VISIBLE);


                hourlyForecast.setOnClickListener(v1 -> {

                    if (hourlyForecastLinearLayout.getVisibility() == View.GONE) {
                        hourlyForecastExpandIcon.setRotation(180);

                        if (hourlyForecastScrollview.getChildCount() < 48) {

                            hourlyForecastScrollview.removeAllViewsInLayout();

                            for (int index = 0; index < hourlyWeatherForecastArrayList.size(); index++) {
                                hourlyForecastScrollview.addView(hourlyColumnAdapter.getView(index, null, null), index);
                            }
                        }
                        hourlyForecastLinearLayout.setVisibility(View.VISIBLE);
                    } else {
                        hourlyForecastExpandIcon.setRotation(0);
                        hourlyForecastLinearLayout.setVisibility(View.GONE);
                    }
                });


            } else {
                hourlyForecastScrollview.removeAllViewsInLayout();
                detailedInformationsLinearLayout.setVisibility(View.GONE);
                windGustInfomationLinearLayout.setVisibility(View.GONE);
                visibilityInformationLinearLayout.setVisibility(View.GONE);
                skyInformationsLinearLayout.setVisibility(View.GONE);
                forecastInformationsLinearLayout.setVisibility(View.GONE);
                hourlyForecastLinearLayout.setVisibility(View.GONE);
            }
        });

        //  Place deletion
        cardView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.dialog_confirmation_title_delete_place))
                    .setMessage(String.format(context.getString(R.string.dialog_confirmation_message_delete_place), currentItem.getCity(), currentItem.getCountryCode()))
                    .setPositiveButton(context.getString(R.string.dialog_confirmation_choice_yes), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataPlaces dataPlaces = new DataPlaces(context);
                            String dataPlaceName = currentItem.getCity().toUpperCase() + '/' + currentItem.getCountryCode();
                            dataPlaces.deletePlace(dataPlaceName);

                            try {
                                placeItemList = dataPlaces.getPlaces();

                                if (placeItemList.isEmpty()) {
                                    noPlacesRegisteredTextView.setVisibility(View.VISIBLE);
                                    swipeRefreshLayout.setVisibility(View.GONE);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            adapterPlace.setVisibility(View.GONE);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.dialog_confirmation_choice_no), null)
                    .show();
            return false;
        });

        return view;
    }
}
