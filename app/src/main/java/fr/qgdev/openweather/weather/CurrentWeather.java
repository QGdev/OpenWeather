package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class CurrentWeather {

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
    public int uvIndex;
    public int visibility;

    public long sunrise;
    public long sunset;

    public float windSpeed;
    public float windGustSpeed;
    public boolean isWindDirectionReadable;
    public short windDirection;

    public float rain;
    public float snow;

    public CurrentWeather() {
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
        this.uvIndex = 0;
        this.visibility = 0;

        this.sunrise = 0;
        this.sunset = 0;

        this.windSpeed = 0;
        this.windGustSpeed = 0;
        this.isWindDirectionReadable = false;
        this.windDirection = 0;

        this.rain = 0;
        this.snow = 0;
    }

    public CurrentWeather(JSONObject currentWeather) throws JSONException {
        //  The time of this update
        this.dt = currentWeather.getLong("dt");

        //  Weather
        this.weather = currentWeather.getString("weather");
        this.weatherDescription = currentWeather.getString("weather_description");
        this.weatherCode = currentWeather.getInt("weather_code");

        //  Temperatures
        this.temperature = BigDecimal.valueOf(currentWeather.getDouble("temperature")).floatValue();
        this.temperatureFeelsLike = BigDecimal.valueOf(currentWeather.getDouble("temperature_feels_like")).floatValue();

        //  Pressure, Humidity, dewPoint
        this.pressure = currentWeather.getInt("pressure");
        this.humidity = currentWeather.getInt("humidity");
        this.dewPoint = BigDecimal.valueOf(currentWeather.getDouble("dew_point")).floatValue();

        //  Sky informations
        this.cloudiness = currentWeather.getInt("cloudiness");
        this.uvIndex = currentWeather.getInt("uvi");
        this.visibility = currentWeather.getInt("visibility");
        this.sunrise = currentWeather.getLong("sunrise");
        this.sunset = currentWeather.getLong("sunset");

        //    Wind informations
        this.windSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_speed")).floatValue();
        this.windGustSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_gust_speed")).floatValue();
        this.isWindDirectionReadable = currentWeather.getBoolean("wind_readable_direction");
        this.windDirection = BigDecimal.valueOf(currentWeather.getInt("wind_direction")).shortValue();

        //  Precipitations
        this.rain = currentWeather.getInt("rain");
        this.snow = currentWeather.getInt("snow");
    }


    public void fillWithOWMData(JSONObject currentWeather) throws JSONException
    {
        //  The time of this update
        this.dt = currentWeather.getLong("dt") * 1000;

        //    Weather descriptions
        JSONObject currentWeatherDescriptionsJSON = currentWeather.getJSONArray("weather").getJSONObject(0);    //  Get only the first station
        this.weather = currentWeatherDescriptionsJSON.getString("main");
        this.weatherDescription = currentWeatherDescriptionsJSON.getString("description");
        this.weatherCode = currentWeatherDescriptionsJSON.getInt("id");

        //    Temperatures
        this.temperature = BigDecimal.valueOf(currentWeather.getDouble("temp")).floatValue();
        this.temperatureFeelsLike = BigDecimal.valueOf(currentWeather.getDouble("feels_like")).floatValue();

        //    Pressure, Humidity, dewPoint, uvIndex
        this.pressure = currentWeather.getInt("pressure");
        this.humidity = currentWeather.getInt("humidity");
        this.dewPoint = BigDecimal.valueOf(currentWeather.getDouble("dew_point")).floatValue();

        if (currentWeather.has("uvi")) {
            this.uvIndex = currentWeather.getInt("uvi");
        } else {
            this.uvIndex = 0;
        }

        //    Sky informations
        this.cloudiness = currentWeather.getInt("clouds");
        this.visibility = currentWeather.getInt("visibility");
        this.sunrise = currentWeather.getLong("sunrise") * 1000;
        this.sunset = currentWeather.getLong("sunset") * 1000;

        //    Wind informations
        this.windSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_speed")).floatValue();

        ////  Enough wind for a viable wind direction information
        this.isWindDirectionReadable = currentWeather.has("wind_deg");
        if (this.isWindDirectionReadable) {
            this.windDirection = BigDecimal.valueOf(currentWeather.getInt("wind_deg")).shortValue();
        }
        ////    Wind Gusts
        if (currentWeather.has("wind_gust")) {
            this.windGustSpeed = BigDecimal.valueOf(currentWeather.getDouble("wind_gust")).floatValue();
        } else {
            this.windGustSpeed = 0;
        }

        //  Precipitations
        ////    Rain
        if (currentWeather.has("rain") && currentWeather.getJSONObject("rain").has("1h")) {
            this.rain = BigDecimal.valueOf(currentWeather.getJSONObject("rain").getDouble("1h")).floatValue();
        } else {
            this.rain = 0;
        }
        ////    Snow
        if (currentWeather.has("snow") && currentWeather.getJSONObject("snow").has("1h")) {
            this.snow = BigDecimal.valueOf(currentWeather.getJSONObject("snow").getDouble("1h")).floatValue();
        } else {
            this.snow = 0;
        }
    }


    @NonNull
    public CurrentWeather clone() {
        CurrentWeather returnedCurrentWeather = new CurrentWeather();
        returnedCurrentWeather.dt = this.dt;

        returnedCurrentWeather.weather = this.weather;
        returnedCurrentWeather.weatherDescription = this.weatherDescription;
        returnedCurrentWeather.weatherCode = this.weatherCode;

        returnedCurrentWeather.temperature = this.temperature;
        returnedCurrentWeather.temperatureFeelsLike = this.temperatureFeelsLike;

        returnedCurrentWeather.pressure = this.pressure;
        returnedCurrentWeather.humidity = this.humidity;
        returnedCurrentWeather.dewPoint = this.dewPoint;

        returnedCurrentWeather.cloudiness = this.cloudiness;
        returnedCurrentWeather.uvIndex = this.uvIndex;
        returnedCurrentWeather.visibility = this.visibility;

        returnedCurrentWeather.sunrise = this.sunrise;
        returnedCurrentWeather.sunset = this.sunset;

        returnedCurrentWeather.windSpeed = this.windSpeed;
        returnedCurrentWeather.windGustSpeed = this.windGustSpeed;
        returnedCurrentWeather.isWindDirectionReadable = this.isWindDirectionReadable;
        returnedCurrentWeather.windDirection = this.windDirection;

        returnedCurrentWeather.rain = this.rain;
        returnedCurrentWeather.snow = this.snow;

        return returnedCurrentWeather;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject currentWeatherJSON = new JSONObject();

        //  Weather content
        ////    Time
        currentWeatherJSON.put("dt", this.dt);

        ////    Weather
        currentWeatherJSON.accumulate("weather", this.weather);
        currentWeatherJSON.accumulate("weather_description", this.weatherDescription);
        currentWeatherJSON.accumulate("weather_code", this.weatherCode);

        ////    Temperatures
        currentWeatherJSON.accumulate("temperature", this.temperature);
        currentWeatherJSON.accumulate("temperature_feels_like", this.temperatureFeelsLike);

        ////    Environmental Variables
        currentWeatherJSON.accumulate("pressure", this.pressure);
        currentWeatherJSON.accumulate("humidity", this.humidity);
        currentWeatherJSON.accumulate("dew_point", this.dewPoint);

        ////    Sky
        currentWeatherJSON.accumulate("cloudiness", this.cloudiness);
        currentWeatherJSON.accumulate("uvi", this.uvIndex);
        currentWeatherJSON.accumulate("visibility", this.visibility);
        currentWeatherJSON.accumulate("sunrise", this.sunrise);
        currentWeatherJSON.accumulate("sunset", this.sunset);

        ////    Wind
        currentWeatherJSON.accumulate("wind_speed", this.windSpeed);
        currentWeatherJSON.accumulate("wind_gust_speed", this.windGustSpeed);
        currentWeatherJSON.accumulate("wind_readable_direction", this.isWindDirectionReadable);
        currentWeatherJSON.accumulate("wind_direction", this.windDirection);

        ////    Precipitations
        currentWeatherJSON.accumulate("rain", this.rain);
        currentWeatherJSON.accumulate("snow", this.snow);

        return currentWeatherJSON;
    }


    public String getWindDirectionCardinalPoints() {

        //  N
        if (windDirection > 348.75 || windDirection < 11.25) {
            return "N";
        }

        //  NNE
        if (windDirection >= 11.25 && windDirection < 33.75) {
            return "NNE";
        }
        //  NE
        if (windDirection >= 33.75 && windDirection <= 56.25) {
            return "NE";
        }
        //  ENE
        if (windDirection > 56.25 && windDirection <= 78.75) {
            return "ENE";
        }
        //  E
        if (windDirection > 78.75 && windDirection < 101.25) {
            return "E";
        }
        //  ESE
        if (windDirection >= 101.25 && windDirection < 123.75) {
            return "ESE";
        }
        //  SE
        if (windDirection >= 123.75 && windDirection <= 146.25) {
            return "SE";
        }
        // SSE
        if (windDirection > 146.25 && windDirection <= 168.75) {
            return "SSE";
        }
        //  S
        if (windDirection > 168.75 && windDirection < 191.25) {
            return "S";
        }
        //  SSW
        if (windDirection >= 191.25 && windDirection < 213.75) {
            return "SSW";
        }
        //  SW
        if (windDirection >= 213.75 && windDirection <= 236.25) {
            return "SW";
        }
        //  WSW
        if (windDirection > 236.25 && windDirection <= 258.75) {
            return "WSW";
        }
        //  W
        if (windDirection > 258.75 && windDirection < 281.25) {
            return "W";
        }
        //  WNW
        if (windDirection >= 281.25 && windDirection < 303.75) {
            return "WNW";
        }
        //  NW
        if (windDirection >= 303.75 && windDirection <= 326.25) {
            return "NW";
        }
        //  NNW
        if (windDirection > 326.25 && windDirection <= 348.75) {
            return "NNW";
        }

        return "";
    }
}

