/*
 *  Copyright (c) 2019 - 2024
 *  QGdev - Quentin GOMES DOS REIS
 *
 *  This file is part of OpenWeather.
 *
 *  OpenWeather is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenWeather is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * MinutelyWeatherForecast
 * <p>
 *    A data holder class for Minutely Weather Forecast data.
 *    Unlike other weather data, it's only holds the precipitation.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
@Entity(tableName = "minutely_weather_forecasts",
		  primaryKeys = {"placeId", "dt"})
public class MinutelyWeatherForecast {
	@NonNull
	private String placeId;
	private long dt;
	private float precipitation;
	
	@Ignore
	public MinutelyWeatherForecast() {
		this.placeId = "";
		this.dt = 0;
		this.precipitation = 0;
	}
	
	public MinutelyWeatherForecast(String placeId, long dt, float precipitation) {
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
	public String getPlaceId() {
		return placeId;
	}
	
	public void setPlaceId(String placeId) {
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

