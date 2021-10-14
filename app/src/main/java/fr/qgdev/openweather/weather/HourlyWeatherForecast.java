package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class HourlyWeatherForecast {

    public long dt;

    public String weather;
    public String weatherDescription;
    public int weatherCode;

    public float temperature;
    public float temperatureFeelsLike;

    public int pressure;
    public int humidity;
    public float dewPoint;

    public int cloudiness;
    public int visibility;
    public int uvIndex;

    public float windSpeed;
    public float windGustSpeed;
    public short windDirection;

    public float pop;
    public float rain;
    public float snow;

    public HourlyWeatherForecast() {
        this.dt = 0;

        this.weather = "";
        this.weatherDescription = "";
        this.weatherCode = 0;

        this.temperature = 0;
        this.temperatureFeelsLike = 0;

        this.pressure = 0;
        this.humidity = 0;
        this.dewPoint = 0;

        this.cloudiness = 0;
        this.visibility = 0;
        this.uvIndex = 0;

        this.windSpeed = 0;
        this.windGustSpeed = 0;
        this.windDirection = 0;

        this.pop = 0;
        this.rain = 0;
        this.snow = 0;
    }

    public HourlyWeatherForecast(JSONObject hourlyWeatherForecast) throws JSONException {
        //  Time
        this.dt = hourlyWeatherForecast.getLong("dt");

        //  Weather
        this.weather = hourlyWeatherForecast.getString("weather");
        this.weatherDescription = hourlyWeatherForecast.getString("weather_description");
        this.weatherCode = hourlyWeatherForecast.getInt("weather_code");

        //  Temperatures
        this.temperature = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("temperature")).floatValue();
        this.temperatureFeelsLike = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("temperature_feels_like")).floatValue();

        //  Pressure, Humidity, dew point, Cloudiness, Visibility
        this.pressure = hourlyWeatherForecast.getInt("pressure");
        this.humidity = hourlyWeatherForecast.getInt("humidity");
        this.dewPoint = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("dew_point")).floatValue();
        this.cloudiness = hourlyWeatherForecast.getInt("cloudiness");
        this.visibility = hourlyWeatherForecast.getInt("visibility");
        //  To assure retrocompatibility with older versions
        if (hourlyWeatherForecast.has("uvi")) this.uvIndex = hourlyWeatherForecast.getInt("uvi");
        else this.uvIndex = 0;

        //  Wind
        this.windSpeed = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("wind_speed")).floatValue();
        this.windGustSpeed = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("wind_gust_speed")).floatValue();
        this.windDirection = BigDecimal.valueOf(hourlyWeatherForecast.getInt("wind_direction")).shortValue();

        //  Precipitations
        ////    PoP -   Probability of Precipitations
        this.pop = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("pop")).floatValue();
        ////    Rain
        this.rain = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("rain")).floatValue();
        ////    Snow
        this.snow = BigDecimal.valueOf(hourlyWeatherForecast.getDouble("snow")).floatValue();
    }

    public void fillWithOWMData(JSONObject hourlyWeather) throws JSONException
    {
        //  Time
        this.dt = hourlyWeather.getLong("dt") * 1000;

        //    Weather descriptions
        JSONObject hourlyForecastWeatherDescriptionsJSON = hourlyWeather.getJSONArray("weather").getJSONObject(0);
        this.weather = hourlyForecastWeatherDescriptionsJSON.getString("main");
        this.weatherDescription = hourlyForecastWeatherDescriptionsJSON.getString("description");
        this.weatherCode = hourlyForecastWeatherDescriptionsJSON.getInt("id");

        //  Temperatures
        this.temperature = BigDecimal.valueOf(hourlyWeather.getDouble("temp")).floatValue();
        this.temperatureFeelsLike = BigDecimal.valueOf(hourlyWeather.getDouble("feels_like")).floatValue();

        //  Pressure, Humidity, Visibility, cloudiness, dewPoint and uvIndex
        this.pressure = hourlyWeather.getInt("pressure");
        this.humidity = hourlyWeather.getInt("humidity");
        this.dewPoint = BigDecimal.valueOf(hourlyWeather.getDouble("dew_point")).floatValue();
        this.visibility = hourlyWeather.getInt("visibility");
        this.cloudiness = hourlyWeather.getInt("clouds");
        this.uvIndex = BigDecimal.valueOf(hourlyWeather.getDouble("uvi")).intValue();

        //  Wind
        this.windSpeed = BigDecimal.valueOf(hourlyWeather.getDouble("wind_speed")).floatValue();
        this.windDirection = BigDecimal.valueOf(hourlyWeather.getInt("wind_deg")).shortValue();
        ////    Wind Gusts
        if (hourlyWeather.has("wind_gust")) {
            this.windGustSpeed = BigDecimal.valueOf(hourlyWeather.getDouble("wind_gust")).floatValue();
        } else {
            this.windGustSpeed = 0;
        }

        //  Precipitations
        ////    PoP -   Probability of Precipitations
        this.pop = BigDecimal.valueOf(hourlyWeather.getDouble("pop")).floatValue();
        ////    Rain
        if (hourlyWeather.has("rain") && hourlyWeather.getJSONObject("rain").has("1h")) {
            this.rain = BigDecimal.valueOf(hourlyWeather.getJSONObject("rain").getDouble("1h")).floatValue();
        } else {
            this.rain = 0;
        }
        ////    Snow
        if (hourlyWeather.has("snow") && hourlyWeather.getJSONObject("snow").has("1h")) {
            this.snow = BigDecimal.valueOf(hourlyWeather.getJSONObject("snow").getDouble("1h")).floatValue();
        } else {
            this.snow = 0;
        }
    }

    @NonNull
    public HourlyWeatherForecast clone() {
        HourlyWeatherForecast returnedHourlyWeatherForecast = new HourlyWeatherForecast();
        returnedHourlyWeatherForecast.dt = this.dt;

        returnedHourlyWeatherForecast.weather = this.weather;
        returnedHourlyWeatherForecast.weatherDescription = this.weatherDescription;
        returnedHourlyWeatherForecast.weatherCode = this.weatherCode;

        returnedHourlyWeatherForecast.temperature = this.temperature;
        returnedHourlyWeatherForecast.temperatureFeelsLike = this.temperatureFeelsLike;

        returnedHourlyWeatherForecast.pressure = this.pressure;
        returnedHourlyWeatherForecast.humidity = this.humidity;
        returnedHourlyWeatherForecast.dewPoint = this.dewPoint;

        returnedHourlyWeatherForecast.cloudiness = this.cloudiness;
        returnedHourlyWeatherForecast.visibility = this.visibility;
        returnedHourlyWeatherForecast.uvIndex = this.uvIndex;

        returnedHourlyWeatherForecast.windSpeed = this.windSpeed;
        returnedHourlyWeatherForecast.windGustSpeed = this.windGustSpeed;
        returnedHourlyWeatherForecast.windDirection = this.windDirection;

        returnedHourlyWeatherForecast.pop = this.pop;
        returnedHourlyWeatherForecast.rain = this.rain;
        returnedHourlyWeatherForecast.snow = this.snow;

        return returnedHourlyWeatherForecast;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject hourlyWeatherForecastJSON = new JSONObject();

        //  Hourly Weather Forecast
        ////    Time
        hourlyWeatherForecastJSON.accumulate("dt", this.dt);

        ////    Weather
        hourlyWeatherForecastJSON.accumulate("weather", this.weather);
        hourlyWeatherForecastJSON.accumulate("weather_description", this.weatherDescription);
        hourlyWeatherForecastJSON.accumulate("weather_code", this.weatherCode);

        ////    Temperatures
        hourlyWeatherForecastJSON.accumulate("temperature", this.temperature);
        hourlyWeatherForecastJSON.accumulate("temperature_feels_like", this.temperatureFeelsLike);

        ////    Environmental Variables
        hourlyWeatherForecastJSON.accumulate("pressure", this.pressure);
        hourlyWeatherForecastJSON.accumulate("humidity", this.humidity);
        hourlyWeatherForecastJSON.accumulate("dew_point", this.dewPoint);

        ////    Sky
        hourlyWeatherForecastJSON.accumulate("cloudiness", this.cloudiness);
        hourlyWeatherForecastJSON.accumulate("visibility", this.visibility);
        hourlyWeatherForecastJSON.accumulate("uvi", this.uvIndex);

        ////    Wind
        hourlyWeatherForecastJSON.accumulate("wind_speed", this.windSpeed);
        hourlyWeatherForecastJSON.accumulate("wind_gust_speed", this.windGustSpeed);
        hourlyWeatherForecastJSON.accumulate("wind_direction", this.windDirection);

        ////    Precipitations
        hourlyWeatherForecastJSON.accumulate("pop", this.pop);
        hourlyWeatherForecastJSON.accumulate("rain", this.rain);
        hourlyWeatherForecastJSON.accumulate("snow", this.snow);


        return hourlyWeatherForecastJSON;
    }
}