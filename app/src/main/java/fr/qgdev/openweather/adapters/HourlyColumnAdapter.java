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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;


public class HourlyColumnAdapter extends BaseAdapter {

    private final Context context;
    private final Place place;
    private final List<HourlyWeatherForecast> hourlyWeatherForecastList;
    private final LayoutInflater inflater;

    public HourlyColumnAdapter(Context context, Place place) {
        this.context = context;
        this.place = place;
        this.inflater = LayoutInflater.from(context);
        this.hourlyWeatherForecastList = place.getHourlyWeatherForecastArrayList();
    }


    @Override
    public int getCount() {
        return hourlyWeatherForecastList.size();
    }

    @Override
    public HourlyWeatherForecast getItem(int position) {
        return hourlyWeatherForecastList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.adapter_hourly_column, null);

        TextView dateTextView = view.findViewById(R.id.hourly_date);

        ImageView weatherIconImageView = view.findViewById(R.id.hourly_weather);

        TextView temperatureTextView = view.findViewById(R.id.hourly_temperature);
        TextView temperatureFeelsLikeTextView = view.findViewById(R.id.hourly_temperature_feels_like);

        TextView pressureTextView = view.findViewById(R.id.hourly_pressure);
        TextView humidityTextView = view.findViewById(R.id.hourly_humidity);
        TextView cloudinessTextView = view.findViewById(R.id.hourly_cloudiness);

        TextView windDirectionTextView = view.findViewById(R.id.hourly_wind_direction);
        TextView windSpeedTextView = view.findViewById(R.id.hourly_wind_speed);

        TextView popTextView = view.findViewById(R.id.hourly_pop);

        SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(context);
        String temperatureUnit = apiKeyPref.getString("temperature_unit", null);
        String measureUnit = apiKeyPref.getString("measure_unit", null);
        String pressureUnit = apiKeyPref.getString("pressure_unit", null);
        String timeOffset = apiKeyPref.getString("time_offset", null);
        String timeFormat = apiKeyPref.getString("time_format", null);

        HourlyWeatherForecast currentItemHourlyForecasts = getItem(position);
        CurrentWeather currentItemCurrentWeather = place.getCurrentWeather();

        SimpleDateFormat simpleDateFormat;

        if (timeFormat.contains("24")) {
            simpleDateFormat = new SimpleDateFormat("dd/MM\nHH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("dd/MM\nKK:mm a");
        }

        if (timeOffset.contains("place"))
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(place.getTimeZone())));

        //  Date
        final String date = simpleDateFormat.format(new Date(currentItemHourlyForecasts.dt));

        //  Icon
        final int weatherIconId;

        //  Temperature
        final String temperature;
        final String temperatureFeelsLike;

        if (temperatureUnit.contains("celsius")) {
            temperature = String.format("%.1f°C", (currentItemHourlyForecasts.temperature - 273.15));
            temperatureFeelsLike = String.format("%.1f°C", (currentItemHourlyForecasts.temperatureFeelsLike - 273.15));
        } else {
            temperature = String.format("%.1f °F", ((currentItemHourlyForecasts.temperature - 273.15) * (9 / 5)) + 32);
            temperatureFeelsLike = String.format("%.1f°F", ((currentItemHourlyForecasts.temperatureFeelsLike - 273.15) * (9 / 5)) + 32);
        }


        final String pressure;
        final String humidity;
        final String cloudiness;

        //  Pressure
        if (pressureUnit.contains("hpa")) {
            pressure = String.format("%d hPa", currentItemHourlyForecasts.pressure);
        } else {
            pressure = String.format("%d bar", currentItemHourlyForecasts.pressure);
        }

        //  Humidity
        humidity = currentItemHourlyForecasts.humidity + " %";

        cloudiness = currentItemHourlyForecasts.cloudiness + " %";

        final String windDirection;
        final String windSpeed;

        //  Wind
        ////    Wind Direction
        windDirection = currentItemHourlyForecasts.getWindDirectionCardinalPoints();
        ////  Wind speed and Wind gust Speed
        if (measureUnit.contains("metric")) {
            windSpeed = String.format("%d km/h", (int) (currentItemHourlyForecasts.windSpeed * 3.6));
        } else {
            windSpeed = String.format("%d mph", (int) (currentItemHourlyForecasts.windSpeed * 2.23694));
        }


        final String pop = String.valueOf((int) (currentItemHourlyForecasts.pop * 100)) + '%';


        final String currentDt;
        final String currentSunrise;
        final String currentSunset;
        boolean isDayTime = true;

        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(place.getTimeZone())));

        currentDt = dateFormat.format(new Date(currentItemHourlyForecasts.dt));
        currentSunrise = dateFormat.format(new Date(currentItemCurrentWeather.sunrise));
        currentSunset = dateFormat.format(new Date(currentItemCurrentWeather.sunset));

        try {
            isDayTime = dateFormat.parse(currentDt).after(dateFormat.parse(currentSunrise)) && dateFormat.parse(currentDt).before(dateFormat.parse(currentSunset));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        switch (currentItemHourlyForecasts.weatherCode) {

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
                if (isDayTime) {
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
                if (isDayTime) {
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
                if (isDayTime) {
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
                if (isDayTime) {
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
                if (isDayTime) {
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


        dateTextView.setText(date);

        weatherIconImageView.setImageResource(weatherIconId);

        temperatureTextView.setText(temperature);
        temperatureFeelsLikeTextView.setText(temperatureFeelsLike);

        humidityTextView.setText(humidity);
        pressureTextView.setText(pressure);
        cloudinessTextView.setText(cloudiness);

        windDirectionTextView.setText(windDirection);
        windSpeedTextView.setText(windSpeed);

        popTextView.setText(pop);

        return view;
    }
}
