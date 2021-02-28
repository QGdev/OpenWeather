package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

public class MinutelyWeatherForecast {

    public final long dt;
    public final double precipitation;

// --Commented out by Inspection START (28/02/21 17:50):
//    public MinutelyWeatherForecast() {
//        this.dt = 0;
//        this.precipitation = 0;
//    }
// --Commented out by Inspection STOP (28/02/21 17:50)

    public MinutelyWeatherForecast(long dt, double precipitation) {
        this.dt = dt;
        this.precipitation = precipitation;
    }

    @NonNull
    public MinutelyWeatherForecast clone() {
        return new MinutelyWeatherForecast(this.dt, this.precipitation);
    }
}

