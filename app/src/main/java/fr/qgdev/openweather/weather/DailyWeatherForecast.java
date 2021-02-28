package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

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

