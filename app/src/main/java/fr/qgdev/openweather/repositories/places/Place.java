/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.repositories.places;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Ignore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import fr.qgdev.openweather.metrics.AirQuality;
import fr.qgdev.openweather.metrics.CurrentWeather;
import fr.qgdev.openweather.metrics.DailyWeatherForecast;
import fr.qgdev.openweather.metrics.HourlyWeatherForecast;
import fr.qgdev.openweather.metrics.MinutelyWeatherForecast;
import fr.qgdev.openweather.metrics.WeatherAlert;

public class Place {
	
	@Embedded
	private final Geolocation geolocation;
	@Embedded
	private final Properties properties;
	
	private CurrentWeather currentWeather;
	private AirQuality airQuality;
	private List<MinutelyWeatherForecast> minutelyWeatherForecastList;
	private List<HourlyWeatherForecast> hourlyWeatherForecastList;
	private List<DailyWeatherForecast> dailyWeatherForecastList;
	private List<WeatherAlert> weatherAlertsList;
	
	
	public Place(@NonNull Geolocation geolocation,
					 @NonNull Properties properties,
					 @NonNull CurrentWeather currentWeather,
					 @NonNull AirQuality airQuality,
					 @NonNull List<MinutelyWeatherForecast> minutelyWeatherForecastList,
					 @NonNull List<HourlyWeatherForecast> hourlyWeatherForecastList,
					 @NonNull List<DailyWeatherForecast> dailyWeatherForecastList,
					 List<WeatherAlert> weatherAlertsList) {
		this.geolocation = geolocation;
		this.properties = properties;
		this.currentWeather = currentWeather;
		this.airQuality = airQuality;
		this.minutelyWeatherForecastList = minutelyWeatherForecastList;
		this.hourlyWeatherForecastList = hourlyWeatherForecastList;
		this.dailyWeatherForecastList = dailyWeatherForecastList;
		
		if (weatherAlertsList == null) this.weatherAlertsList = new ArrayList<>();
		else this.weatherAlertsList = weatherAlertsList;
	}
	
	@Ignore
	public Place(int placeID, @NonNull JSONObject placeJSONObject) throws JSONException {
		
		this.currentWeather = null;
		this.airQuality = null;
		this.minutelyWeatherForecastList = new ArrayList<>();
		this.hourlyWeatherForecastList = new ArrayList<>();
		this.dailyWeatherForecastList = new ArrayList<>();
		this.weatherAlertsList = new ArrayList<>();
		
		//  Retrieve and set place properties
		int timeOffset = 0;
		
		if (placeJSONObject.has("place")) {
			JSONObject placeJSON = placeJSONObject.optJSONObject("place");
			
			//	Like every properties of the place
			
			if (placeJSON.has("timezone_offset")) {
				timeOffset = placeJSON.getInt("timezone_offset");
			}
			
			this.properties = new Properties(new Date().getTime(), timeOffset, -1, placeID);
			
			//	Or geographical properties of it
			if (placeJSON.has("city") && placeJSON.has("country_code")) {
				this.geolocation = new Geolocation(
						  placeID,
						  new Coordinates(placeJSON.getDouble("lat"),
									 placeJSON.getDouble("lon")));
			} else {
				this.geolocation = new Geolocation(
						  placeID,
						  placeJSON.getString("city"),
						  placeJSON.getString("country_code"),
						  new Coordinates(placeJSON.getDouble("lat"),
									 placeJSON.getDouble("lon")));
			}
		} else {
			throw new JSONException("Cannot find place data in PlaceObjectJSON");
		}
	}
	
	@Ignore
	public Place(int placeID, int placeOrder, JSONObject placeJSON) throws JSONException {
		
		this.currentWeather = null;
		this.airQuality = null;
		this.minutelyWeatherForecastList = new ArrayList<>();
		this.hourlyWeatherForecastList = new ArrayList<>();
		this.dailyWeatherForecastList = new ArrayList<>();
		this.weatherAlertsList = new ArrayList<>();
		
		//	Retrieve timezone data and define properties
		//________________________________________________________________
		//
		int timeOffset;
		if (!placeJSON.has("timezone"))
			throw new JSONException("Cannot find timezone data in PlaceObjectJSON");
		timeOffset = placeJSON.getInt("timezone");
		this.properties = new Properties(new Date().getTime(), timeOffset, placeOrder, placeID);
		
		//	Retrieve coordinates and see if there is city and country info
		//________________________________________________________________
		//
		JSONObject coordinatesJSON;
		if (!placeJSON.has("coord"))
			throw new JSONException("Cannot find geolocation data in PlaceObjectJSON");
		coordinatesJSON = placeJSON.getJSONObject("coord");
		
		//	Or geographical properties of it
		if (placeJSON.has("sys") && placeJSON.getJSONObject("sys").has("country") && placeJSON.has("name")) {
			this.geolocation = new Geolocation(
					  placeID,
					  placeJSON.getString("name"),
					  placeJSON.getJSONObject("sys").getString("country"),
					  new Coordinates(coordinatesJSON.getDouble("lat"),
								 coordinatesJSON.getDouble("lon")));
			
			
		} else {
			this.geolocation = new Geolocation(
					  placeID,
					  new Coordinates(coordinatesJSON.getDouble("lat"),
								 coordinatesJSON.getDouble("lon")));
		}
	}
	
	public void updateWithOWMWeatherData(JSONObject placeJSON) throws JSONException {
		
		//	Smart way to update weather data, will not affect object state if error occurs
		long tmpLastUpdateAttemptTime = new Date().getTime();
		long tmpLastAvailableDataTime;
		
		CurrentWeather tmpCurrentWeather;
		List<MinutelyWeatherForecast> tmpMinutelyWeatherForecasts = new ArrayList<>();
		List<HourlyWeatherForecast> tmpHourlyWeatherForecasts = new ArrayList<>();
		List<DailyWeatherForecast> tmpDailyWeatherForecasts = new ArrayList<>();
		List<WeatherAlert> tmpWeatherAlerts = new ArrayList<>();
		
		//  Properties data set
		//________________________________________________________________
		//
		properties.setLastWeatherUpdateAttemptTime(tmpLastUpdateAttemptTime);
		
		
		//  Current Weather data set
		//________________________________________________________________
		//	Current weather also contains lastAvailableDataTime value
		if (!placeJSON.has("current"))
			throw new JSONException("Cannot find current weather data in PlaceObjectJSON");
		
		JSONObject crtWeatherJSON = placeJSON.optJSONObject("current");
		tmpLastAvailableDataTime = crtWeatherJSON.getLong("dt");
		tmpCurrentWeather = new CurrentWeather(crtWeatherJSON);
		tmpCurrentWeather.setPlaceId(properties.getPlaceId());
		
		
		//  Minutely Weather Forecast
		//________________________________________________________________
		//
		//  Minutely weather forecast arraylist can be empty
		if (placeJSON.has("minutely")) {
			JSONArray minutelyWeatherJSON = placeJSON.optJSONArray("minutely");
			MinutelyWeatherForecast minutelyWeatherForecast;
			
			for (int i = 0; i < minutelyWeatherJSON.length(); i++) {
				minutelyWeatherForecast = new MinutelyWeatherForecast(minutelyWeatherJSON.getJSONObject(i));
				minutelyWeatherForecast.setPlaceId(properties.getPlaceId());
				tmpMinutelyWeatherForecasts.add(i, minutelyWeatherForecast);
			}
		}
		
		
		//  Hourly Weather Forecast
		//________________________________________________________________
		//
		
		if (!placeJSON.has("hourly"))
			throw new JSONException("Cannot find hourly weather forecast data in PlaceObjectJSON");
		
		JSONArray hourlyWeatherJSON = placeJSON.getJSONArray("hourly");
		HourlyWeatherForecast hourlyWeatherForecast;
		
		for (int i = 0; i < hourlyWeatherJSON.length(); i++) {
			hourlyWeatherForecast = new HourlyWeatherForecast(hourlyWeatherJSON.getJSONObject(i));
			hourlyWeatherForecast.setPlaceId(properties.getPlaceId());
			tmpHourlyWeatherForecasts.add(i, hourlyWeatherForecast);
		}
		
		
		//  Daily Weather Forecast
		//________________________________________________________________
		//
		
		if (!placeJSON.has("daily"))
			throw new JSONException("Cannot find daily weather forecast data in PlaceObjectJSON");
		
		JSONArray dailyWeatherJSON = placeJSON.getJSONArray("daily");
		DailyWeatherForecast dailyWeatherForecast;
		
		for (int i = 0; i < dailyWeatherJSON.length(); i++) {
			dailyWeatherForecast = new DailyWeatherForecast(dailyWeatherJSON.getJSONObject(i));
			dailyWeatherForecast.setPlaceId(properties.getPlaceId());
			tmpDailyWeatherForecasts.add(i, dailyWeatherForecast);
		}
		
		
		//  Weather Alerts
		//________________________________________________________________
		//
		
		if (placeJSON.has("alerts")) {
			JSONArray weatherAlertJSON = placeJSON.optJSONArray("alerts");
			WeatherAlert weatherAlert;
			for (int i = 0; i < weatherAlertJSON.length(); i++) {
				weatherAlert = new WeatherAlert(weatherAlertJSON.getJSONObject(i));
				weatherAlert.setPlaceId(properties.getPlaceId());
				tmpWeatherAlerts.add(i, weatherAlert);
			}
		}
		
		
		//  Commit phase - Every new values will be set in the object
		//________________________________________________________________
		//
		
		//	Save weather information
		currentWeather = tmpCurrentWeather;
		minutelyWeatherForecastList = tmpMinutelyWeatherForecasts;
		hourlyWeatherForecastList = tmpHourlyWeatherForecasts;
		dailyWeatherForecastList = tmpDailyWeatherForecasts;
		weatherAlertsList = tmpWeatherAlerts;
		
		//	Save statistics or properties
		properties.setLastAvailableWeatherDataTime(tmpLastAvailableDataTime);
		properties.setLastSuccessfulWeatherUpdateTime(tmpLastUpdateAttemptTime);
	}
	
	
	public void updateWithOWMAirQualityData(JSONObject aqJsonObject) throws JSONException {
		
		//	Smart way to update weather data, will not affect object state if error occurs
		long tmpLastUpdateAttemptTime = new Date().getTime();
		long tmpLastAvailableDataTime;
		
		AirQuality tmpAirQuality;
		
		//	Register new attempt timestamp in object
		properties.setLastAirQualityUpdateAttemptTime(tmpLastUpdateAttemptTime);
		
		//  Properties data set
		//________________________________________________________________
		//
		if (aqJsonObject.has("list") && aqJsonObject.getJSONArray("list").length() == 1) {
			JSONObject subObject = aqJsonObject.getJSONArray("list").getJSONObject(0);
			
			//	Extract temporal information
			if (subObject.has("dt")) {
				tmpLastAvailableDataTime = aqJsonObject.getJSONArray("list")
						  .getJSONObject(0)
						  .getLong("dt");
			} else throw new JSONException("Cannot find update data in Air Quality JSON Object");
			
			//	Extract Air Quality data
			if (subObject.has("main") && subObject.has("components")) {
				tmpAirQuality = new AirQuality(aqJsonObject);
				tmpAirQuality.setPlaceId(properties.getPlaceId());
			} else throw new JSONException("Cannot find air quality data in Air Quality JSON Object");
		} else throw new JSONException("Cannot find data in in Air Quality JSON Object");
		
		//  Commit phase - Every new values will be set in the object
		//________________________________________________________________
		//
		
		//	Save air quality information
		airQuality = tmpAirQuality;
		
		//	Save statistics or properties
		properties.setLastAvailableAirQualityDataTime(tmpLastAvailableDataTime);
		properties.setLastSuccessfulAirQualityUpdateTime(tmpLastUpdateAttemptTime);
	}
	
	public Geolocation getGeolocation() {
		return geolocation;
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public CurrentWeather getCurrentWeather() {
		return this.currentWeather.clone();
	}
	
	public void setCurrentWeather(CurrentWeather currentWeather) {
		this.currentWeather = currentWeather.clone();
		this.currentWeather.setPlaceId(properties.getPlaceId());
	}
	
	public AirQuality getAirQuality() {
		return this.airQuality.clone();
	}
	
	public void setAirQuality(AirQuality airQuality) {
		this.airQuality = airQuality.clone();
		this.airQuality.setPlaceId(properties.getPlaceId());
	}
	
	public List<MinutelyWeatherForecast> getMinutelyWeatherForecastList() {
		return Collections.unmodifiableList(this.minutelyWeatherForecastList);
	}
	
	public void setMinutelyWeatherForecastList(List<MinutelyWeatherForecast> minutelyWeatherForecastList) {
		this.minutelyWeatherForecastList = Collections.unmodifiableList(minutelyWeatherForecastList);
		for (MinutelyWeatherForecast minutelyWeatherForecast : this.minutelyWeatherForecastList) {
			minutelyWeatherForecast.setPlaceId(properties.getPlaceId());
		}
	}
	
	public List<HourlyWeatherForecast> getHourlyWeatherForecastList() {
		return Collections.unmodifiableList(this.hourlyWeatherForecastList);
	}
	
	public void setHourlyWeatherForecastList(List<HourlyWeatherForecast> hourlyWeatherForecastList) {
		this.hourlyWeatherForecastList = Collections.unmodifiableList(hourlyWeatherForecastList);
		for (HourlyWeatherForecast hourlyWeatherForecast : this.hourlyWeatherForecastList) {
			hourlyWeatherForecast.setPlaceId(properties.getPlaceId());
		}
	}
	
	public List<DailyWeatherForecast> getDailyWeatherForecastList() {
		return Collections.unmodifiableList(this.dailyWeatherForecastList);
	}
	
	public void setDailyWeatherForecastList(List<DailyWeatherForecast> dailyWeatherForecastList) {
		this.dailyWeatherForecastList = Collections.unmodifiableList(dailyWeatherForecastList);
		for (DailyWeatherForecast dailyWeatherForecast : this.dailyWeatherForecastList) {
			dailyWeatherForecast.setPlaceId(properties.getPlaceId());
		}
	}
	
	public List<WeatherAlert> getWeatherAlertsList() {
		return Collections.unmodifiableList(this.weatherAlertsList);
	}
	
	public void setWeatherAlertsList(List<WeatherAlert> weatherAlertsList) {
		this.weatherAlertsList = Collections.unmodifiableList(weatherAlertsList);
		for (WeatherAlert weatherAlert : this.weatherAlertsList) {
			weatherAlert.setPlaceId(properties.getPlaceId());
		}
	}
	
	public MinutelyWeatherForecast getMinutelyWeatherForecast(int minute) {
		return minutelyWeatherForecastList.get(minute).clone();
	}
	
	
	public HourlyWeatherForecast getHourlyWeatherForecast(int hour) {
		
		return hourlyWeatherForecastList.get(hour).clone();
	}
	
	public DailyWeatherForecast getDailyWeatherForecast(int day) {
		return dailyWeatherForecastList.get(day).clone();
	}
	
	public WeatherAlert getWeatherAlert(int index) {
		return weatherAlertsList.get(index).clone();
	}
	
	public int getWeatherAlertCount() {
		return weatherAlertsList.size();
	}
	
	public boolean thereIsWeatherAlerts() {
		return !weatherAlertsList.isEmpty();
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", Place.class.getSimpleName() + "[", "]")
				  .add("geolocation=" + geolocation)
				  .add("properties=" + properties)
				  .add("currentWeather=" + currentWeather)
				  .add("airQuality=" + airQuality)
				  .add("minutelyWeatherForecastList=" + minutelyWeatherForecastList)
				  .add("hourlyWeatherForecastList=" + hourlyWeatherForecastList)
				  .add("dailyWeatherForecastList=" + dailyWeatherForecastList)
				  .add("weatherAlertsList=" + weatherAlertsList)
				  .toString();
	}
}