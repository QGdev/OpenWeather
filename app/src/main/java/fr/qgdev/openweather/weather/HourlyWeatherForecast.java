package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class HourlyWeatherForecast {

    public long dt;

    public String weather;
    public String weatherDescription;
    public int weatherCode;

    public double temperature;
    public double temperatureFeelsLike;

    public int pressure;
    public int humidity;
    public double dewPoint;

    public int cloudiness;
    public int visibility;

    public double windSpeed;
    public double windGustSpeed;
    public int windDirection;

    public double pop;
    public double rain;
    public double snow;

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

        this.windSpeed = 0;
        this.windGustSpeed = 0;
        this.windDirection = 0;

        this.pop = 0;
        this.rain = 0;
        this.snow = 0;
    }

    public HourlyWeatherForecast(JSONObject hourlyWeatherForecast) throws JSONException
    {
        //  Time
        this.dt = hourlyWeatherForecast.getLong("dt");

        //  Weather
        this.weather = hourlyWeatherForecast.getString("weather");
        this.weatherDescription = hourlyWeatherForecast.getString("weather_description");
        this.weatherCode = hourlyWeatherForecast.getInt("weather_code");

        //  Temperatures
        this.temperature = hourlyWeatherForecast.getDouble("temperature");
        this.temperatureFeelsLike = hourlyWeatherForecast.getDouble("temperature_feels_like");

        //  Pressure, Humidity, dew point, Cloudiness, Visibility
        this.pressure = hourlyWeatherForecast.getInt("pressure");
        this.humidity = hourlyWeatherForecast.getInt("humidity");
        this.dewPoint = hourlyWeatherForecast.getDouble("dew_point");
        this.cloudiness = hourlyWeatherForecast.getInt("cloudiness");
        this.visibility = hourlyWeatherForecast.getInt("visibility");

        //  Wind
        this.windSpeed = hourlyWeatherForecast.getDouble("wind_speed");
        this.windGustSpeed = hourlyWeatherForecast.getDouble("wind_gust_speed");
        this.windDirection = hourlyWeatherForecast.getInt("wind_direction");

        //  Precipitations
        ////    PoP -   Probability of Precipitations
        this.pop = hourlyWeatherForecast.getDouble("pop");
        ////    Rain
        this.rain = hourlyWeatherForecast.getDouble("rain");
        ////    Snow
        this.snow = hourlyWeatherForecast.getDouble("snow");
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
        if (windDirection >=101.25 && windDirection < 123.75) {
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

