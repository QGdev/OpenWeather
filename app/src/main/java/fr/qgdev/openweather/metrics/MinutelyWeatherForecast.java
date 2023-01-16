package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

@Entity(tableName = "minutely_weather_forecasts",
		  primaryKeys = {"placeId", "dt"})

public class MinutelyWeatherForecast {
	private int placeId;
	private long dt;
	private float precipitation;
	
	@Ignore
	public MinutelyWeatherForecast() {
		this.dt = 0;
		this.precipitation = 0;
	}
	
	public MinutelyWeatherForecast(int placeId, long dt, float precipitation) {
		this.placeId = placeId;
		this.dt = dt;
		this.precipitation = precipitation;
	}
	
	@Ignore
	public MinutelyWeatherForecast(JSONObject minutelyWeather) throws JSONException {
		this.dt = minutelyWeather.getLong("dt") * 1000;
		this.precipitation = BigDecimal.valueOf(minutelyWeather.getDouble("precipitation")).floatValue();
	}
	
	//	Getter
	public int getPlaceId() {
		return placeId;
	}
	
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	public long getDt() {
		return dt;
	}
	
	//	Setter
	
	public void setDt(long dt) {
		this.dt = dt;
	}
	
	public float getPrecipitation() {
		return precipitation;
	}
	
	public void setPrecipitation(float precipitation) {
		this.precipitation = precipitation;
	}
	
	@NonNull
	public MinutelyWeatherForecast clone() {
		return new MinutelyWeatherForecast(placeId, dt, precipitation);
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", MinutelyWeatherForecast.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("dt=" + dt)
				  .add("precipitation=" + precipitation)
				  .toString();
	}
}

