package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class DailyWeatherForecast {

    public long dt;

    public String weather;
    public String weatherDescription;
    public int weatherCode;

    public double temperatureMorning;
    public double temperatureDay;
    public double temperatureEvening;
    public double temperatureNight;
    public double temperatureMinimum;
    public double temperatureMaximum;

    public double temperatureMorningFeelsLike;
    public double temperatureDayFeelsLike;
    public double temperatureEveningFeelsLike;
    public double temperatureNightFeelsLike;

    public int pressure;
    public int humidity;
    public double dewPoint;

    public int cloudiness;
    public long sunrise;
    public long sunset;


    public double windSpeed;
    public double windGustSpeed;
    public int windDirection;

    public double pop;
    public double rain;
    public double snow;

    public DailyWeatherForecast() {
        this.dt = 0;

        this.weather = "";
        this.weatherDescription = "";
        this.weatherCode = 0;

        this.temperatureMorning = 0;
        this.temperatureDay = 0;
        this.temperatureEvening = 0;
        this.temperatureNight = 0;
        this.temperatureMinimum = 0;
        this.temperatureMaximum = 0;

        this.temperatureMorningFeelsLike = 0;
        this.temperatureDayFeelsLike = 0;
        this.temperatureEveningFeelsLike = 0;
        this.temperatureNightFeelsLike = 0;

        this.pressure = 0;
        this.humidity = 0;
        this.dewPoint = 0;

        this.cloudiness = 0;
        this.sunrise = 0;
        this.sunset = 0;

        this.windSpeed = 0;
        this.windGustSpeed = 0;
        this.windDirection = 0;

        this.pop = 0;
        this.rain = 0;
        this.snow = 0;
    }

    public DailyWeatherForecast(JSONObject dailyWeatherForecast) throws JSONException {
        //  Time
        this.dt = dailyWeatherForecast.getLong("dt");

        //  Weather
        this.weather = dailyWeatherForecast.getString("weather");
        this.weatherDescription = dailyWeatherForecast.getString("weather_description");
        this.weatherCode = dailyWeatherForecast.getInt("weather_code");

        //  Temperatures
        this.temperatureMorning = dailyWeatherForecast.getDouble("temperature_morning");
        this.temperatureDay = dailyWeatherForecast.getDouble("temperature_day");
        this.temperatureEvening = dailyWeatherForecast.getDouble("temperature_evening");
        this.temperatureNight = dailyWeatherForecast.getDouble("temperature_night");
        this.temperatureMinimum = dailyWeatherForecast.getDouble("temperature_minimum");
        this.temperatureMaximum = dailyWeatherForecast.getDouble("temperature_maximum");

        //  Feels Like Temperatures
        this.temperatureMorningFeelsLike = dailyWeatherForecast.getDouble("temperature_feelslike_morning");
        this.temperatureDayFeelsLike = dailyWeatherForecast.getDouble("temperature_feelslike_day");
        this.temperatureEveningFeelsLike = dailyWeatherForecast.getDouble("temperature_feelslike_evening");
        this.temperatureNightFeelsLike = dailyWeatherForecast.getDouble("temperature_feelslike_night");

        //  Pressure, Humidity, dewPoint
        this.pressure = dailyWeatherForecast.getInt("pressure");
        this.humidity = dailyWeatherForecast.getInt("humidity");
        this.dewPoint = dailyWeatherForecast.getDouble("dew_point");

        //  Sky
        this.cloudiness = dailyWeatherForecast.getInt("cloudiness");
        this.sunrise = dailyWeatherForecast.getLong("sunrise");
        this.sunset = dailyWeatherForecast.getLong("sunset");

        //  Wind
        this.windSpeed = dailyWeatherForecast.getDouble("wind_speed");
        this.windDirection = dailyWeatherForecast.getInt("wind_direction");
        this.windGustSpeed = dailyWeatherForecast.getDouble("wind_gust_speed");

        //  Precipitations
        ////    PoP -   Probability of Precipitations
        this.pop = dailyWeatherForecast.getDouble("pop");
        ////    Rain
        this.rain = dailyWeatherForecast.getDouble("rain");
        ////    Snow
        this.snow = dailyWeatherForecast.getDouble("snow");
    }

    public void fillWithOWMData(JSONObject dailyWeather) throws JSONException
    {
        //  Time
        this.dt = dailyWeather.getLong("dt") * 1000;

        //    Weather descriptions
        JSONObject dailyWeatherDescriptionsJSON = dailyWeather.getJSONArray("weather").getJSONObject(0);
        this.weather = dailyWeatherDescriptionsJSON.getString("main");
        this.weatherDescription = dailyWeatherDescriptionsJSON.getString("description");
        this.weatherCode = dailyWeatherDescriptionsJSON.getInt("id");

        //  Temperatures
        JSONObject dailyWeatherTemperaturesJSON = dailyWeather.getJSONObject("temp");
        this.temperatureMorning = dailyWeatherTemperaturesJSON.getDouble("morn");
        this.temperatureDay = dailyWeatherTemperaturesJSON.getDouble("day");
        this.temperatureEvening = dailyWeatherTemperaturesJSON.getDouble("eve");
        this.temperatureNight = dailyWeatherTemperaturesJSON.getDouble("night");
        this.temperatureMinimum = dailyWeatherTemperaturesJSON.getDouble("min");
        this.temperatureMaximum = dailyWeatherTemperaturesJSON.getDouble("max");

        //  Feels Like Temperatures
        JSONObject dailyWeatherTemperaturesFeelsLikeJSON = dailyWeather.getJSONObject("feels_like");
        this.temperatureMorningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("morn");
        this.temperatureDayFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("day");
        this.temperatureEveningFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("eve");
        this.temperatureNightFeelsLike = dailyWeatherTemperaturesFeelsLikeJSON.getDouble("night");

        //  Pressure, Humidity, dewPoint
        this.pressure = dailyWeather.getInt("pressure");
        this.humidity = dailyWeather.getInt("humidity");
        this.dewPoint = dailyWeather.getDouble("dew_point");

        //  Sky
        this.cloudiness = dailyWeather.getInt("clouds");
        this.sunrise = dailyWeather.getLong("sunrise") * 1000;
        this.sunset = dailyWeather.getLong("sunset") * 1000;

        //  Wind
        this.windSpeed = dailyWeather.getDouble("wind_speed");
        this.windDirection = dailyWeather.getInt("wind_deg");
        ////    Wind Gusts
        if (dailyWeather.has("wind_gust")) {
            this.windGustSpeed = dailyWeather.getDouble("wind_gust");
        } else {
            this.windGustSpeed = 0;
        }

        //  Precipitations
        ////    PoP -   Probability of Precipitations
        this.pop = dailyWeather.getDouble("pop");
        ////    Rain
        if (dailyWeather.has("rain")) {
            this.rain = dailyWeather.getDouble("rain");
        } else {
            this.rain = 0;
        }
        ////    Snow
        if (dailyWeather.has("snow")) {
            this.snow = dailyWeather.getDouble("snow");
        } else {
            this.snow = 0;
        }
    }

    public JSONObject getJSONObject() throws JSONException
    {
        JSONObject dailyWeatherForecastJSON = new JSONObject();

        //  Daily Weather Forecast
        ////    Time
        dailyWeatherForecastJSON.accumulate("dt", this.dt);

        ////    Weather
        dailyWeatherForecastJSON.accumulate("weather", this.weather);
        dailyWeatherForecastJSON.accumulate("weather_description", this.weatherDescription);
        dailyWeatherForecastJSON.accumulate("weather_code", this.weatherCode);

        ////    Temperatures
        dailyWeatherForecastJSON.accumulate("temperature_morning", this.temperatureMorning);
        dailyWeatherForecastJSON.accumulate("temperature_day", this.temperatureDay);
        dailyWeatherForecastJSON.accumulate("temperature_evening", this.temperatureEvening);
        dailyWeatherForecastJSON.accumulate("temperature_night", this.temperatureNight);
        dailyWeatherForecastJSON.accumulate("temperature_minimum", this.temperatureMinimum);
        dailyWeatherForecastJSON.accumulate("temperature_maximum", this.temperatureMaximum);

        ////    Feels Like
        dailyWeatherForecastJSON.accumulate("temperature_feelslike_morning", this.temperatureMorningFeelsLike);
        dailyWeatherForecastJSON.accumulate("temperature_feelslike_day", this.temperatureDayFeelsLike);
        dailyWeatherForecastJSON.accumulate("temperature_feelslike_evening", this.temperatureEveningFeelsLike);
        dailyWeatherForecastJSON.accumulate("temperature_feelslike_night", this.temperatureNightFeelsLike);

        ////    Environmental Variables
        dailyWeatherForecastJSON.accumulate("pressure", this.pressure);
        dailyWeatherForecastJSON.accumulate("humidity", this.humidity);
        dailyWeatherForecastJSON.accumulate("dew_point", this.dewPoint);

        ////    Sky
        dailyWeatherForecastJSON.accumulate("cloudiness", this.cloudiness);
        dailyWeatherForecastJSON.accumulate("sunrise", this.sunrise);
        dailyWeatherForecastJSON.accumulate("sunset", this.sunset);

        ////    Wind
        dailyWeatherForecastJSON.accumulate("wind_speed", this.windSpeed);
        dailyWeatherForecastJSON.accumulate("wind_gust_speed", this.windGustSpeed);
        dailyWeatherForecastJSON.accumulate("wind_direction", this.windDirection);

        ////    Precipitations
        dailyWeatherForecastJSON.accumulate("pop", this.pop);
        dailyWeatherForecastJSON.accumulate("rain", this.rain);
        dailyWeatherForecastJSON.accumulate("snow", this.snow);

        return dailyWeatherForecastJSON;
    }

    @NonNull
    public DailyWeatherForecast clone() {
        DailyWeatherForecast returnedDailyWeatherForecast = new DailyWeatherForecast();
        returnedDailyWeatherForecast.dt = this.dt;

        returnedDailyWeatherForecast.weather = this.weather;
        returnedDailyWeatherForecast.weatherDescription = this.weatherDescription;
        returnedDailyWeatherForecast.weatherCode = this.weatherCode;

        returnedDailyWeatherForecast.temperatureMorning = this.temperatureMorning;
        returnedDailyWeatherForecast.temperatureDay = this.temperatureDay;
        returnedDailyWeatherForecast.temperatureEvening = this.temperatureEvening;
        returnedDailyWeatherForecast.temperatureNight = this.temperatureNight;
        returnedDailyWeatherForecast.temperatureMinimum = this.temperatureMinimum;
        returnedDailyWeatherForecast.temperatureMaximum = this.temperatureMaximum;

        returnedDailyWeatherForecast.temperatureMorningFeelsLike = this.temperatureMorningFeelsLike;
        returnedDailyWeatherForecast.temperatureDayFeelsLike = this.temperatureDayFeelsLike;
        returnedDailyWeatherForecast.temperatureEveningFeelsLike = this.temperatureEveningFeelsLike;
        returnedDailyWeatherForecast.temperatureNightFeelsLike = this.temperatureNightFeelsLike;

        returnedDailyWeatherForecast.pressure = this.pressure;
        returnedDailyWeatherForecast.humidity = this.humidity;
        returnedDailyWeatherForecast.dewPoint = this.dewPoint;

        returnedDailyWeatherForecast.cloudiness = this.cloudiness;
        returnedDailyWeatherForecast.sunrise = this.sunrise;
        returnedDailyWeatherForecast.sunset = this.sunset;

        returnedDailyWeatherForecast.windSpeed = this.windSpeed;
        returnedDailyWeatherForecast.windGustSpeed = this.windGustSpeed;
        returnedDailyWeatherForecast.windDirection = this.windDirection;

        returnedDailyWeatherForecast.pop = this.pop;
        returnedDailyWeatherForecast.rain = this.rain;
        returnedDailyWeatherForecast.snow = this.snow;

        return returnedDailyWeatherForecast;
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

