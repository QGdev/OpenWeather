package fr.qgdev.openweather.weather;

public class CurrentWeather {

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
    public int uvIndex;
    public int visibility;

    public long sunrise;
    public long sunset;

    public double windSpeed;
    public double windGustSpeed;
    public boolean isWindDirectionReadable;
    public int windDirection;

    public double rain;
    public double snow;

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

