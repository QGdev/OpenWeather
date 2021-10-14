package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class MinutelyWeatherForecast {

	public long dt;
	public float precipitation;

	public MinutelyWeatherForecast() {
		this.dt = 0;
		this.precipitation = 0;
	}

	public MinutelyWeatherForecast(long dt, float precipitation) {
		this.dt = dt;
		this.precipitation = precipitation;
	}

	public MinutelyWeatherForecast(JSONObject minutelyWeatherForecast) throws JSONException {
		this.dt = minutelyWeatherForecast.getLong("dt");
		this.precipitation = BigDecimal.valueOf(minutelyWeatherForecast.getDouble("precipitation")).floatValue();
	}

	public void fillWithOWMData(JSONObject minutelyWeather) throws JSONException {
		this.dt = minutelyWeather.getLong("dt") * 1000;
		this.precipitation = BigDecimal.valueOf(minutelyWeather.getDouble("precipitation")).floatValue();
	}

    public JSONObject getJSONObject() throws JSONException
    {
        JSONObject minutelyWeatherForecastJSON = new JSONObject();

        minutelyWeatherForecastJSON.accumulate("dt", this.dt);
        minutelyWeatherForecastJSON.accumulate("precipitation", this.precipitation);

        return minutelyWeatherForecastJSON;
    }

    @NonNull
    public MinutelyWeatherForecast clone()
    {
        return new MinutelyWeatherForecast(this.dt, this.precipitation);
    }
}

