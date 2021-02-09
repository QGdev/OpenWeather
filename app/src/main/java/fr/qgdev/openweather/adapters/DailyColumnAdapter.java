package fr.qgdev.openweather.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.DailyWeatherForecast;


public class DailyColumnAdapter extends BaseAdapter {

    private final Context context;
    private final Place place;
    private final List<DailyWeatherForecast> dailyWeatherForecastList;
    private final LayoutInflater inflater;

    public DailyColumnAdapter(Context context, Place place) {
        this.context = context;
        this.place = place;
        this.inflater = LayoutInflater.from(context);
        this.dailyWeatherForecastList = place.getDailyWeatherForecastArrayList();
    }


    @Override
    public int getCount() {
        return dailyWeatherForecastList.size();
    }

    @Override
    public DailyWeatherForecast getItem(int position) {
        return dailyWeatherForecastList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.adapter_daily_column, null);

        TextView dateTextView = view.findViewById(R.id.daily_date);

        ImageView weatherIconImageView = view.findViewById(R.id.daily_weather);

        TextView temperatureMaxTextView = view.findViewById(R.id.daily_temperature_maximum);
        TextView temperatureMinTextView = view.findViewById(R.id.daily_temperature_minimum);
        TextView temperatureMornTextView = view.findViewById(R.id.daily_temperature_morning);
        TextView temperatureDayTextView = view.findViewById(R.id.daily_temperature_day);

        TextView pressureTextView = view.findViewById(R.id.daily_pressure);
        TextView humidityTextView = view.findViewById(R.id.daily_humidity);
        TextView cloudinessTextView = view.findViewById(R.id.daily_cloudiness);
        TextView sunriseTextView = view.findViewById(R.id.daily_sunrise);
        TextView sunsetTextView = view.findViewById(R.id.daily_sunset);

        TextView windDirectionTextView = view.findViewById(R.id.daily_wind_direction);
        TextView windSpeedTextView = view.findViewById(R.id.daily_wind_speed);

        TextView popTextView = view.findViewById(R.id.daily_pop);

        SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(context);
        String temperatureUnit = apiKeyPref.getString("temperature_unit", null);
        String measureUnit = apiKeyPref.getString("measure_unit", null);
        String pressureUnit = apiKeyPref.getString("pressure_unit", null);
        String timeOffset = apiKeyPref.getString("time_offset", null);
        String timeFormat = apiKeyPref.getString("time_format", null);

        DailyWeatherForecast currentItemDailyForecasts = getItem(position);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM");

        if (timeOffset.contains("place"))
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(place.getTimeZone())));

        //  Date
        final String date = simpleDateFormat.format(new Date(currentItemDailyForecasts.dt));

        //  Icon
        final int weatherIconId;

        //  Temperature
        final String temperatureMaximum;
        final String temperatureMinimum;
        final String temperatureMorning;
        final String temperatureDay;

        if (temperatureUnit.contains("celsius")) {
            temperatureMaximum = String.format("%.1f°C", (currentItemDailyForecasts.temperatureMaximum - 273.15));
            temperatureMinimum = String.format("%.1f°C", (currentItemDailyForecasts.temperatureMinimum - 273.15));
            temperatureMorning = String.format("%.1f°C", (currentItemDailyForecasts.temperatureMorning - 273.15));
            temperatureDay = String.format("%.1f°C", (currentItemDailyForecasts.temperatureDay - 273.15));

        } else {
            temperatureMaximum = String.format("%.1f °F", ((currentItemDailyForecasts.temperatureMaximum - 273.15) * (9 / 5)) + 32);
            temperatureMinimum = String.format("%.1f°F", ((currentItemDailyForecasts.temperatureMinimum - 273.15) * (9 / 5)) + 32);
            temperatureMorning = String.format("%.1f °F", ((currentItemDailyForecasts.temperatureMorning - 273.15) * (9 / 5)) + 32);
            temperatureDay = String.format("%.1f°F", ((currentItemDailyForecasts.temperatureDay - 273.15) * (9 / 5)) + 32);

        }


        final String pressure;
        final String humidity;
        final String cloudiness;
        final String sunrise;
        final String sunset;

        //  Pressure
        if (pressureUnit.contains("hpa")) {
            pressure = String.format("%d hPa", currentItemDailyForecasts.pressure);
        } else {
            pressure = String.format("%d bar", currentItemDailyForecasts.pressure);
        }

        //  Humidity
        humidity = currentItemDailyForecasts.humidity + " %";

        //  Cloudiness
        cloudiness = currentItemDailyForecasts.cloudiness + " %";

        //  Sunrise, Sunset
        SimpleDateFormat hourFormat;

        if (timeFormat.contains("24")) {
            hourFormat = new SimpleDateFormat("HH:mm");
        } else {
            hourFormat = new SimpleDateFormat("KK:mm a");
        }

        if (timeOffset.contains("place"))
            hourFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(place.getTimeZone())));

        sunrise = hourFormat.format(new Date(currentItemDailyForecasts.sunrise));
        sunset = hourFormat.format(new Date(currentItemDailyForecasts.sunset));


        final String windDirection;
        final String windSpeed;

        //  Wind
        ////    Wind Direction
        windDirection = currentItemDailyForecasts.getWindDirectionCardinalPoints();
        ////  Wind speed and Wind gust Speed
        if (measureUnit.contains("metric")) {
            windSpeed = String.format("%d km/h", (int) (currentItemDailyForecasts.windSpeed * 3.6));
        } else {
            windSpeed = String.format("%d mph", (int) (currentItemDailyForecasts.windSpeed * 2.23694));
        }

        final String pop = String.valueOf((int) (currentItemDailyForecasts.pop * 100)) + '%';

        switch (currentItemDailyForecasts.weatherDescription) {
            //  Thunderstorm Group
            case "light thunderstorm":
            case "ragged thunderstorm":
            case "heavy thunderstorm":
            case "thunderstorm":
            case "thunderstorm with heavy drizzle":
            case "thunderstorm with drizzle":
            case "thunderstorm with light drizzle":
            case "thunderstorm with heavy rain":
            case "thunderstorm with rain":
            case "thunderstorm with light rain":
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
                weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
                break;

            //  Rain
            case "light rain":
            case "heavy intensity rain":
            case "moderate rain":
                weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
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
            case "snow":
                weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
                break;

            case "heavy snow":
            case "heavy shower snow":
            case "shower snow":
            case "light shower snow":
                weatherIconId = context.getResources().getIdentifier("snow_flat", "drawable", context.getPackageName());
                break;
            case "sleet":
            case "rain and snow":
            case "light rain and snow":
            case "shower sleet":
            case "light shower sleet":
                weatherIconId = context.getResources().getIdentifier("sleet_flat", "drawable", context.getPackageName());
                break;

            //  Atmosphere
            case "mist":
            case "smoke":
            case "haze":
            case "sand/ dust whirls":
            case "volcanic ash":
            case "squalls":
            case "dust":
            case "sand":
            case "fog":
                weatherIconId = context.getResources().getIdentifier("fog_flat", "drawable", context.getPackageName());
                break;

            case "tornado":
                weatherIconId = context.getResources().getIdentifier("tornado_flat", "drawable", context.getPackageName());

                //  Sky
                break;
            case "clear sky":
                weatherIconId = context.getResources().getIdentifier("sun_flat", "drawable", context.getPackageName());
                break;

            case "few clouds":
            case "broken clouds":
            case "scattered clouds":
                weatherIconId = context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", context.getPackageName());
                break;

            case "overcast clouds":
                weatherIconId = context.getResources().getIdentifier("cloudy_flat", "drawable", context.getPackageName());
                break;

            //  Default
            default:
                weatherIconId = context.getResources().getIdentifier("storm_flat", "drawable", context.getPackageName());
                break;
        }

        dateTextView.setText(date);

        weatherIconImageView.setImageResource(weatherIconId);

        temperatureMaxTextView.setText(temperatureMaximum);
        temperatureMinTextView.setText(temperatureMinimum);
        temperatureMornTextView.setText(temperatureMorning);
        temperatureDayTextView.setText(temperatureDay);

        humidityTextView.setText(humidity);
        pressureTextView.setText(pressure);
        cloudinessTextView.setText(cloudiness);
        sunriseTextView.setText(sunrise);
        sunsetTextView.setText(sunset);

        windDirectionTextView.setText(windDirection);
        windSpeedTextView.setText(windSpeed);

        popTextView.setText(pop);

        return view;
    }
}
