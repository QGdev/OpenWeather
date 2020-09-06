package fr.qgdev.openweather.weather;

public class CurrentWeather {

    public long dt = 0;

    public String weather = "";
    public String weatherDescription = "";

    public double temperature = 0;
    public double temperatureFeelsLike = 0;

    public int pressure = 0;
    public int humidity = 0;
    public double dewPoint = 0;

    public int cloudiness = 0;
    public int uvIndex = 0;
    public int visibility = 0;

    public long sunrise = 0;
    public long sunset = 0;

    public double windSpeed = 0;
    public double windGustSpeed = 0;
    public boolean isWindDirectionReadable = false;
    public int windDirection = 0;

    public double rain = 0;
    public double snow = 0;

    public CurrentWeather() {

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

