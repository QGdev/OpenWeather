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
import java.util.Date;
import java.util.List;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;


public class HourlyColumnAdapter extends BaseAdapter{

    private Context context;
    private List<HourlyWeatherForecast> hourlyWeatherForecastList;
    private LayoutInflater inflater;

    public HourlyColumnAdapter(Context context, List<HourlyWeatherForecast> hourlyWeatherForecastList)
    {
        this.context = context;
        this.hourlyWeatherForecastList = hourlyWeatherForecastList;
        this.inflater = LayoutInflater.from(context);
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

        TextView dateTextView = (TextView) view.findViewById(R.id.hourly_date);

        ImageView weatherIconImageView = (ImageView) view.findViewById(R.id.hourly_weather);

        TextView temperatureTextView = (TextView) view.findViewById(R.id.hourly_temperature);
        TextView temperatureFeelsLikeTextView = (TextView) view.findViewById(R.id.hourly_temperature_feels_like);
        //TextView dewPointTextView = (TextView) view.findViewById(R.id.hourly_dewpoint);

        TextView pressureTextView = (TextView) view.findViewById(R.id.hourly_pressure);
        TextView humidityTextView = (TextView) view.findViewById(R.id.hourly_humidity);
        TextView cloudinessTextView = (TextView) view.findViewById(R.id.hourly_cloudiness);
        TextView visibilityTextView = (TextView) view.findViewById(R.id.hourly_visibility);

        TextView windDirectionTextView = (TextView) view.findViewById(R.id.hourly_wind_direction);
        TextView windSpeedTextView = (TextView) view.findViewById(R.id.hourly_wind_speed);
        TextView windGustSpeedTextView = (TextView) view.findViewById(R.id.hourly_wind_gust_speed);

        TextView popTextView = (TextView) view.findViewById(R.id.hourly_pop);

        SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(context);
        String temperatureUnit = apiKeyPref.getString("temperature_unit", null);
        String measureUnit = apiKeyPref.getString("measure_unit", null);
        String pressureUnit = apiKeyPref.getString("pressure_unit", null);

        HourlyWeatherForecast currentItem = getItem(position);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM\nHH:mm");

        //  Date
        final String date = simpleDateFormat.format(new Date(currentItem.dt));

        //  Icon
        final int weatherIconId;

        //  Temperature
        final String temperature;
        final String temperatureFeelsLike;
        //final String dewPoint;
        
        if(temperatureUnit.contains("celsius")){
            temperature = String.format("%.1f°C", (currentItem.temperature - 273.15));
            temperatureFeelsLike = String.format("%.1f°C", (currentItem.temperatureFeelsLike - 273.15));
            //dewPoint = String.format("%.1f°C", (currentItem.dewPoint - 273.15));
        }
        else {
            temperature = String.format("%.1f °F", ((currentItem.temperature - 273.15) * (9/5)) + 32);
            temperatureFeelsLike = String.format("%.1f°F", ((currentItem.temperatureFeelsLike - 273.15) * (9/5)) + 32);
            //dewPoint = String.format("%.1f°F", ((currentItem.dewPoint - 273.15) * (9/5)) + 32);
        }


        final String pressure;
        final String humidity;
        final String cloudiness;
        final String visibility;

        //  Pressure
        if(pressureUnit.contains("hpa")){
            pressure= String.format("%d hPa", (int) currentItem.pressure);
        }
        else{
            pressure = String.format("%d bar",currentItem.pressure);
        }

        //  Humidity
        humidity = currentItem.humidity + " %";

        cloudiness = currentItem.cloudiness + " %";

        //  Visibility
        if(measureUnit.contains("metric")){
            visibility = String.format("%d km", (int) (currentItem.visibility / 1000));
        }
        else{
            visibility = String.format("%d mile", (int) (currentItem.visibility * 0.000621371));
        }


        final String windDirection;
        final String windSpeed;
        final String windGustSpeed;

        //  Wind
        ////    Wind Direction
        windDirection = currentItem.getWindDirectionCardinalPoints();
        ////  Wind speed and Wind gust Speed
        if(measureUnit.contains("metric")){
            windSpeed = String.format("%d km/h", (int) (currentItem.windSpeed * 3.6));
            windGustSpeed = String.format("%d km/h", (int) (currentItem.windGustSpeed * 3.6));
        }
        else{
            windSpeed = String.format("%d mph", (int) (currentItem.windSpeed * 2.23694));
            windGustSpeed = String.format("%d mph", (int) (currentItem.windGustSpeed * 2.23694));
        }


        final String pop = String.valueOf((int)(currentItem.pop * 100)) + '%';



        dateTextView.setText(date);

        //weatherIconImageView.setImageResource(weatherIconId);

        temperatureTextView.setText(temperature);
        temperatureFeelsLikeTextView.setText(temperatureFeelsLike);
        //dewPointTextView.setText(dewPoint);

        humidityTextView.setText(humidity);
        pressureTextView.setText(pressure);
        cloudinessTextView.setText(cloudiness);
        visibilityTextView.setText(visibility);

        windDirectionTextView.setText(windDirection);
        windSpeedTextView.setText(windSpeed);
        windGustSpeedTextView.setText(windGustSpeed);

        popTextView.setText(pop);

        return view;
    }
}
