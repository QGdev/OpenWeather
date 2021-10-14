package fr.qgdev.openweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;
import fr.qgdev.openweather.weather.MinutelyWeatherForecast;
import fr.qgdev.openweather.weather.WeatherAlert;

public class Place {

	private String city;
	private String countryCode;

	private double latitude;
	private double longitude;

	private Date lastUpdateDate;
	private long lastUpdate;

	private int timeOffset;

	private CurrentWeather currentWeather;
	private ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList;
	private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
	private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;
	private ArrayList<WeatherAlert> weatherAlertsArrayList;


	public Place(String city, String countryCode) {

		this.currentWeather = new CurrentWeather();
		this.minutelyWeatherForecastArrayList = new ArrayList<MinutelyWeatherForecast>();
		this.hourlyWeatherForecastArrayList = new ArrayList<HourlyWeatherForecast>();
		this.dailyWeatherForecastArrayList = new ArrayList<DailyWeatherForecast>();
		this.weatherAlertsArrayList = new ArrayList<WeatherAlert>();

		this.city = city;
		this.countryCode = countryCode;
	}

	public Place(JSONObject placeObjectJSON) throws JSONException {

		this.currentWeather = new CurrentWeather();
		this.minutelyWeatherForecastArrayList = new ArrayList<MinutelyWeatherForecast>();
		this.hourlyWeatherForecastArrayList = new ArrayList<HourlyWeatherForecast>();
		this.dailyWeatherForecastArrayList = new ArrayList<DailyWeatherForecast>();
		this.weatherAlertsArrayList = new ArrayList<WeatherAlert>();

		//  Place data set
		//________________________________________________________________
		//

		if (placeObjectJSON.has("place")) {
			JSONObject placeJSON = placeObjectJSON.optJSONObject("place");

			this.city = placeJSON.getString("city");
			this.countryCode = placeJSON.getString("country_code");
			this.latitude = placeJSON.getDouble("latitude");
			this.longitude = placeJSON.getDouble("longitude");
			//  Moving from a timeZone in a String form to a timeZoneOffset int since version 0.9
			if (placeJSON.has("timezone_offset"))
				this.timeOffset = placeJSON.getInt("timezone_offset");
			else this.timeOffset = 0;

		} else {
			throw new JSONException("Cannot find place data in PlaceObjectJSON");
		}


		//  Update data set
		//________________________________________________________________
		//

		if (placeObjectJSON.has("update")) {
			JSONObject updateJSON = placeObjectJSON.optJSONObject("update");

			this.lastUpdate = updateJSON.getLong("last_update");
			this.lastUpdateDate = new Date(lastUpdate);

		} else {
			throw new JSONException("Cannot find update data in PlaceObjectJSON");
		}


		//  Current Weather data set
		//________________________________________________________________
		//
		if (placeObjectJSON.has("current_weather")) {
			this.currentWeather = new CurrentWeather(placeObjectJSON.optJSONObject("current_weather"));
		} else {
			throw new JSONException("Cannot find current weather data in PlaceObjectJSON");
		}


		//  Minutely Weather Forecast
		//________________________________________________________________
		//
		//  Minutely weather forecast arraylist can be empty
		if (placeObjectJSON.has("minutely_weather_forecast")) {
			JSONArray minutelyWeatherJSON = placeObjectJSON.optJSONArray("minutely_weather_forecast");

			for (int i = 0; i < minutelyWeatherJSON.length(); i++) {
				this.minutelyWeatherForecastArrayList.add(i, new MinutelyWeatherForecast(minutelyWeatherJSON.getJSONObject(i)));
			}
		}


		//  Hourly Weather Forecast
		//________________________________________________________________
		//

		if (placeObjectJSON.has("hourly_weather_forecast")) {
			JSONArray hourlyWeatherJSON = placeObjectJSON.getJSONArray("hourly_weather_forecast");
			for (int i = 0; i < hourlyWeatherJSON.length(); i++) {
				this.hourlyWeatherForecastArrayList.add(i, new HourlyWeatherForecast(hourlyWeatherJSON.getJSONObject(i)));
			}
		} else {
			throw new JSONException("Cannot find hourly weather forecast data in PlaceObjectJSON");
		}


		//  Daily Weather Forecast
		//________________________________________________________________
		//

		if (placeObjectJSON.has("daily_weather_forecast")) {
			JSONArray dailyWeatherJSON = placeObjectJSON.getJSONArray("daily_weather_forecast");

			for (int i = 0; i < dailyWeatherJSON.length(); i++) {
				this.dailyWeatherForecastArrayList.add(i, new DailyWeatherForecast(dailyWeatherJSON.getJSONObject(i)));
			}
		} else {
			throw new JSONException("Cannot find daily weather forecast data in PlaceObjectJSON");
		}


		//  Weather Alerts
		//________________________________________________________________
		//

		if (placeObjectJSON.has("weather_alert")) {
			JSONArray weatherAlertJSON = placeObjectJSON.optJSONArray("weather_alert");

			for (int i = 0; i < weatherAlertJSON.length(); i++) {
				this.weatherAlertsArrayList.add(i, new WeatherAlert(weatherAlertJSON.getJSONObject(i)));
			}
		}
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getTimeZoneStringForm() {
		if (this.timeOffset == 0) return "UTC";
		else return String.format("UTC%+d", this.timeOffset / 3600);
	}

	public TimeZone getTimeZone() {
		return new SimpleTimeZone(this.timeOffset * 1000, "UTC");
	}

	public void setTimeZoneOffset(int timeZoneOffset) {
		this.timeOffset = timeZoneOffset;
	}

	public CurrentWeather getCurrentWeather() {
		return this.currentWeather.clone();
	}

	public void setCurrentWeather(CurrentWeather currentWeather) {
		this.currentWeather = currentWeather.clone();
	}

	public ArrayList<MinutelyWeatherForecast> getMinutelyWeatherForecastArrayList() {
		return (ArrayList<MinutelyWeatherForecast>) this.minutelyWeatherForecastArrayList.clone();
	}

	public void setMinutelyWeatherForecastArrayList(ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList) {
		this.minutelyWeatherForecastArrayList = (ArrayList<MinutelyWeatherForecast>) minutelyWeatherForecastArrayList.clone();
	}

	public ArrayList<HourlyWeatherForecast> getHourlyWeatherForecastArrayList() {
		return (ArrayList<HourlyWeatherForecast>) this.hourlyWeatherForecastArrayList.clone();
	}

	public void setHourlyWeatherForecastArrayList(ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList) {
		this.hourlyWeatherForecastArrayList = (ArrayList<HourlyWeatherForecast>) hourlyWeatherForecastArrayList.clone();
	}

	public ArrayList<DailyWeatherForecast> getDailyWeatherForecastArrayList() {
		return (ArrayList<DailyWeatherForecast>) this.dailyWeatherForecastArrayList.clone();
	}

	public void setDailyWeatherForecastArrayList(ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList) {
		this.dailyWeatherForecastArrayList = (ArrayList<DailyWeatherForecast>) dailyWeatherForecastArrayList.clone();
	}

	public ArrayList<WeatherAlert> getWeatherAlertsArrayList() {
		return (ArrayList<WeatherAlert>) this.weatherAlertsArrayList.clone();
	}

	public void setWeatherAlertsArrayList(ArrayList<WeatherAlert> weatherAlertsArrayList) {
		this.weatherAlertsArrayList = (ArrayList<WeatherAlert>) weatherAlertsArrayList.clone();
	}

	public MinutelyWeatherForecast getMinutelyWeatherForecast(int minute) {
		return minutelyWeatherForecastArrayList.get(minute).clone();
	}


	public HourlyWeatherForecast getHourlyWeatherForecast(int hour) {

		return hourlyWeatherForecastArrayList.get(hour).clone();
	}

	public DailyWeatherForecast getDailyWeatherForecast(int day) {
		return dailyWeatherForecastArrayList.get(day).clone();
	}

	public WeatherAlert getMWeatherAlert(int index) {
		return weatherAlertsArrayList.get(index).clone();
	}

	public int getMWeatherAlertCount() {
		return weatherAlertsArrayList.size();
	}

	public void setMinutelyWeatherForecast(int minute, MinutelyWeatherForecast minutelyWeatherForecast) {
		this.minutelyWeatherForecastArrayList.set(minute, minutelyWeatherForecast.clone());
	}

	public void setHourlyWeatherForecast(int hour, HourlyWeatherForecast hourlyWeatherForecast) {
		this.hourlyWeatherForecastArrayList.set(hour, hourlyWeatherForecast.clone());
	}

	public void setDailyWeatherForecast(int day, DailyWeatherForecast dailyWeatherForecast) {
		this.dailyWeatherForecastArrayList.set(day, dailyWeatherForecast.clone());
	}

	public void setWeatherAlert(int index, WeatherAlert weatherAlert) {
		this.weatherAlertsArrayList.set(index, weatherAlert.clone());
	}

	public void addMinutelyWeatherForecast(int minute, MinutelyWeatherForecast minutelyWeatherForecast) {
		this.minutelyWeatherForecastArrayList.add(minute, minutelyWeatherForecast.clone());
	}

	public void addHourlyWeatherForecast(int hour, HourlyWeatherForecast hourlyWeatherForecast) {
		this.hourlyWeatherForecastArrayList.add(hour, hourlyWeatherForecast.clone());
	}

	public void addDailyWeatherForecast(int day, DailyWeatherForecast dailyWeatherForecast) {
		this.dailyWeatherForecastArrayList.add(day, dailyWeatherForecast.clone());
	}


	public void addWeatherAlert(int index, WeatherAlert weatherAlert) {
		this.weatherAlertsArrayList.add(index, weatherAlert.clone());
	}


	//  getPlaceJSON()
	//________________________________________________________________
	//
	public JSONObject getPlaceJSON() throws JSONException {
		JSONObject placeJSON = new JSONObject();

		////    Place
		placeJSON.accumulate("city", city);
		placeJSON.accumulate("country_code", countryCode);
		placeJSON.accumulate("latitude", latitude);
		placeJSON.accumulate("longitude", longitude);
		placeJSON.accumulate("timezone_offset", timeOffset);

		return placeJSON;
	}


	//  getUpdateJSON()
	//________________________________________________________________
	//

	public JSONObject getUpdateJSON() throws JSONException {
		JSONObject updateJSON = new JSONObject();

		////    Update
		updateJSON.accumulate("last_update_date", lastUpdateDate);
		updateJSON.accumulate("last_update", lastUpdate);

		return updateJSON;
	}


	//  getAllMinutelyWeatherForecastJSON()
	//________________________________________________________________
	//

	public JSONArray getAllMinutelyWeatherForecastJSON() throws JSONException {
		JSONArray minutelyWeatherForecastJSON = new JSONArray();

		for (MinutelyWeatherForecast minutelyWeatherForecast : minutelyWeatherForecastArrayList) {
			minutelyWeatherForecastJSON.put(minutelyWeatherForecast.getJSONObject());
		}

		return minutelyWeatherForecastJSON;
	}


	//  getAllHourlyWeatherForecastJSON()
	//________________________________________________________________
	//

	public JSONArray getAllHourlyWeatherForecastJSON() throws JSONException {
		JSONArray hourlyForecastWeatherJSON = new JSONArray();

		for (HourlyWeatherForecast hourlyWeatherForecast : hourlyWeatherForecastArrayList) {
			hourlyForecastWeatherJSON.put(hourlyWeatherForecast.getJSONObject());
		}

		return hourlyForecastWeatherJSON;
	}

	//  getAllDailyWeatherForecastJSON()
	//________________________________________________________________
	//

	public JSONArray getAllDailyWeatherForecastJSON() throws JSONException {
		JSONArray dailyWeatherForecastJSON = new JSONArray();

		for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {
			dailyWeatherForecastJSON.put(dailyWeatherForecast.getJSONObject());
		}

		return dailyWeatherForecastJSON;
	}

	//  getAllMinutelyWeatherForecastJSON()
	//________________________________________________________________
	//

	public JSONArray getAllWeatherAlertJSON() throws JSONException {
		JSONArray weatherAlertJSON = new JSONArray();

		for (WeatherAlert weatherAlert : weatherAlertsArrayList) {
			weatherAlertJSON.put(weatherAlert.getJSONObject());
		}

		return weatherAlertJSON;
	}

	//  getPlaceObjectJSON()
	//________________________________________________________________
	//


	public JSONObject getPlaceObjectJSON() throws JSONException {
		JSONObject placeObjectJSON = new JSONObject();

		placeObjectJSON.accumulate("place", this.getPlaceJSON());
		placeObjectJSON.accumulate("update", this.getUpdateJSON());

		placeObjectJSON.accumulate("current_weather", this.currentWeather.getJSONObject());

		//  Minutely weather forecasts can be empty so it doesn't have to appear in the resulting JSON
		if (!this.minutelyWeatherForecastArrayList.isEmpty())
			placeObjectJSON.accumulate("minutely_weather_forecast", this.getAllMinutelyWeatherForecastJSON());

		placeObjectJSON.accumulate("hourly_weather_forecast", this.getAllHourlyWeatherForecastJSON());
		placeObjectJSON.accumulate("daily_weather_forecast", this.getAllDailyWeatherForecastJSON());
		placeObjectJSON.accumulate("weather_alert", this.getAllWeatherAlertJSON());

		return placeObjectJSON;
	}

	@Override
	public String toString() {
		return "Place{" +
				"city='" + city + '\'' +
				", countryCode='" + countryCode + '\'' +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", lastUpdateDate=" + lastUpdateDate +
				", lastUpdate=" + lastUpdate +
				", timeOffset=" + timeOffset +
				", currentWeather=" + currentWeather +
				", minutelyWeatherForecastArrayList=" + minutelyWeatherForecastArrayList +
				", hourlyWeatherForecastArrayList=" + hourlyWeatherForecastArrayList +
				", dailyWeatherForecastArrayList=" + dailyWeatherForecastArrayList +
				", weatherAlertsArrayList=" + weatherAlertsArrayList +
				'}';
	}
}