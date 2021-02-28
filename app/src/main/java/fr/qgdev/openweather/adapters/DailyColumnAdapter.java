package fr.qgdev.openweather.adapters;

import android.annotation.SuppressLint;
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

    @SuppressLint("DefaultLocale")
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

        switch (currentItemDailyForecasts.weatherCode) {
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
                weatherIconId = context.getResources().getIdentifier("rain_and_sun_flat", "drawable", context.getPackageName());
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
            case 602:
            case 620:
            case 621:
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
                weatherIconId = context.getResources().getIdentifier("fog_flat", "drawable", context.getPackageName());
                break;

            //  Sky
            case 800:
                weatherIconId = context.getResources().getIdentifier("sun_flat", "drawable", context.getPackageName());
                break;

            case 801:
            case 802:
            case 803:
                weatherIconId = context.getResources().getIdentifier("clouds_and_sun_flat", "drawable", context.getPackageName());
                break;

            case 804:
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
