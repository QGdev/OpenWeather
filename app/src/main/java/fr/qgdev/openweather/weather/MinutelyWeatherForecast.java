package fr.qgdev.openweather.weather;

public class MinutelyWeatherForecast {

    public long dt;
    public double precipitation;

    public MinutelyWeatherForecast() {
        this.dt = 0;
        this.precipitation = 0;
    }

    public MinutelyWeatherForecast(long dt, double precipitation) {
        this.dt = dt;
        this.precipitation = precipitation;
    }

}

