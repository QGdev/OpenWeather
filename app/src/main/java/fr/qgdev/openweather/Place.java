package fr.qgdev.openweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import fr.qgdev.openweather.weather.CurrentWeather;
import fr.qgdev.openweather.weather.DailyWeatherForecast;
import fr.qgdev.openweather.weather.HourlyWeatherForecast;
import fr.qgdev.openweather.weather.MinutelyWeatherForecast;
import fr.qgdev.openweather.weather.WeatherAlert;

public class Place {

    private String city;
    private String country;
    private String countryCode;

    private double latitude;
    private double longitude;

    private Date lastUpdateDate;
    private long lastUpdate;

    private String timeZone;

    private boolean errorDuringDataAcquisition;
    private int errorCode;

    private CurrentWeather currentWeather;
    private ArrayList<MinutelyWeatherForecast> minutelyWeatherForecastArrayList;
    private ArrayList<HourlyWeatherForecast> hourlyWeatherForecastArrayList;
    private ArrayList<DailyWeatherForecast> dailyWeatherForecastArrayList;
    private ArrayList<WeatherAlert> weatherAlertsArrayList;


    public Place(String city, String country, String countryCode) {

        this.currentWeather = new CurrentWeather();
        this.minutelyWeatherForecastArrayList = new ArrayList<MinutelyWeatherForecast>();
        this.hourlyWeatherForecastArrayList = new ArrayList<HourlyWeatherForecast>();
        this.dailyWeatherForecastArrayList = new ArrayList<DailyWeatherForecast>();
        this.weatherAlertsArrayList = new ArrayList<WeatherAlert>();

        this.city = city;
        this.country = country;
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
            this.country = placeJSON.getString("country");
            this.countryCode = placeJSON.getString("country_code");
            this.latitude = placeJSON.getDouble("latitude");
            this.longitude = placeJSON.getDouble("longitude");
            this.timeZone = placeJSON.getString("timezone");

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


        //  Error data set
        //________________________________________________________________
        //

        if (placeObjectJSON.has("errors")) {
            JSONObject errorJSON = placeObjectJSON.optJSONObject("errors");

            this.errorDuringDataAcquisition = errorJSON.getBoolean("error_during_data_acquisition");
            this.errorCode = errorJSON.getInt("error_code");

        } else {
            throw new JSONException("Cannot find errors data in PlaceObjectJSON");
        }


        //  Current Weather data set
        //________________________________________________________________
        //
        if (placeObjectJSON.has("current_weather")) {
            this.currentWeather = new CurrentWeather(placeObjectJSON.optJSONObject("current_weather"));
        } else {
            throw new JSONException("Cannot find current weather data in PlaceObjectJSON");
        }


        //  Minutely Weather Forecast data set
        //________________________________________________________________
        //
        if (placeObjectJSON.has("minutely_weather_forecast")) {
            JSONArray minutelyForecastWeatherJSON = placeObjectJSON.optJSONArray("minutely_weather_forecast");
            JSONObject minutelyForecastWeatherJSON_tmp;

            for (int i = 0; i < minutelyForecastWeatherJSON.length(); i++) {

                minutelyForecastWeatherJSON_tmp = minutelyForecastWeatherJSON.getJSONObject(i);

                this.minutelyWeatherForecastArrayList.add(i, new MinutelyWeatherForecast(minutelyForecastWeatherJSON_tmp.getLong("dt"), minutelyForecastWeatherJSON_tmp.getDouble("precipitation")));
            }
        } else {
            throw new JSONException("Cannot find minutely weather forecast in PlaceObjectJSON");
        }


        //  Hourly Weather Forecast
        //________________________________________________________________
        //

        if (placeObjectJSON.has("hourly_weather_forecast")) {
            JSONArray hourlyForecastWeatherJSON = placeObjectJSON.getJSONArray("hourly_weather_forecast");
            for (int i = 0; i < hourlyForecastWeatherJSON.length(); i++) {
                this.hourlyWeatherForecastArrayList.add(i, new HourlyWeatherForecast(hourlyForecastWeatherJSON.getJSONObject(i)));
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

        if (placeObjectJSON.has("weather_alert")) {
            JSONArray weatherAlertJSON = placeObjectJSON.optJSONArray("weather_alert");
            JSONObject weatherAlertJSON_tmp;

            for (int i = 0; i < weatherAlertJSON.length(); i++) {
                weatherAlertJSON_tmp = weatherAlertJSON.getJSONObject(i);
                this.weatherAlertsArrayList.add(i, new WeatherAlert(weatherAlertJSON_tmp.getString("sender"), weatherAlertJSON_tmp.getString("event"), weatherAlertJSON_tmp.getLong("start_dt"), weatherAlertJSON_tmp.getLong("end_dt"), weatherAlertJSON_tmp.getString("description")));
            }
        }
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    //public long getLastUpdateWithTimeZoneOffset() {return lastUpdate + timeZoneOffset;}

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

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isErrorDuringDataAcquisition() {
        return errorDuringDataAcquisition;
    }

    public void setErrorDuringDataAcquisition(boolean errorDuringDataAcquisition) {
        this.errorDuringDataAcquisition = errorDuringDataAcquisition;
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
    public JSONObject getPlaceJSON() throws Exception {
        JSONObject placeJSON = new JSONObject();

        ////    Place
        placeJSON.accumulate("city", city);
        placeJSON.accumulate("country", country);
        placeJSON.accumulate("country_code", countryCode);
        placeJSON.accumulate("latitude", latitude);
        placeJSON.accumulate("longitude", longitude);
        placeJSON.accumulate("timezone", timeZone);

        return placeJSON;
    }


    //  getUpdateJSON()
    //________________________________________________________________
    //

    public JSONObject getUpdateJSON() throws Exception {
        JSONObject updateJSON = new JSONObject();

        ////    Update
        updateJSON.accumulate("last_update_date", lastUpdateDate);
        updateJSON.accumulate("last_update", lastUpdate);

        return updateJSON;
    }


    //  getErrorsJSON()
    //________________________________________________________________
    //

    public JSONObject getErrorsJSON() throws Exception {
        JSONObject errorsJSON = new JSONObject();

        ////    Errors during updates
        errorsJSON.accumulate("error_during_data_acquisition", errorDuringDataAcquisition);
        errorsJSON.accumulate("error_code", errorCode);


        return errorsJSON;
    }


    //  getMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONObject getMinutelyWeatherForecastJSON(int minute) throws Exception {
        JSONObject minutelyWeatherForecastJSON = new JSONObject();
        MinutelyWeatherForecast minutelyWeatherForecast = this.getMinutelyWeatherForecast(minute);

        minutelyWeatherForecastJSON.accumulate("dt", minutelyWeatherForecast.dt);
        minutelyWeatherForecastJSON.accumulate("precipitation", minutelyWeatherForecast.precipitation);


        return minutelyWeatherForecastJSON;
    }


    //  getAllMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllMinutelyWeatherForecastJSON() throws Exception {
        JSONArray minutelyWeatherForecastJSON = new JSONArray();

        for (int index = 0; index < minutelyWeatherForecastArrayList.size(); index++) {
            minutelyWeatherForecastJSON.put(this.getMinutelyWeatherForecastJSON(index));
        }

        return minutelyWeatherForecastJSON;
    }


    //  getAllHourlyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllHourlyWeatherForecastJSON() throws Exception {
        JSONArray hourlyForecastWeatherJSON = new JSONArray();

        for (HourlyWeatherForecast hourlyWeatherForecast : hourlyWeatherForecastArrayList) {
            hourlyForecastWeatherJSON.put(hourlyWeatherForecast.getJSONObject());
        }

        return hourlyForecastWeatherJSON;
    }

    //  getAllDailyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllDailyWeatherForecastJSON() throws Exception {
        JSONArray dailyWeatherForecastJSON = new JSONArray();

        for (DailyWeatherForecast dailyWeatherForecast : dailyWeatherForecastArrayList) {
            dailyWeatherForecastJSON.put(dailyWeatherForecast.getJSONObject());
        }

        return dailyWeatherForecastJSON;
    }


    //  getMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONObject getWeatherAlertJSON(int index) throws Exception {
        JSONObject weatherAlertJSON = new JSONObject();
        WeatherAlert weatherAlert = this.getMWeatherAlert(index);

        weatherAlertJSON.accumulate("sender", weatherAlert.getSender());
        weatherAlertJSON.accumulate("event", weatherAlert.getEvent());
        weatherAlertJSON.accumulate("start_dt", weatherAlert.getStart_dt());
        weatherAlertJSON.accumulate("end_dt", weatherAlert.getEnd_dt());
        weatherAlertJSON.accumulate("description", weatherAlert.getDescription());

        return weatherAlertJSON;
    }


    //  getAllMinutelyWeatherForecastJSON()
    //________________________________________________________________
    //

    public JSONArray getAllWeatherAlertJSON() throws Exception {
        JSONArray weatherAlertJSON = new JSONArray();

        for (int index = 0; index < weatherAlertsArrayList.size(); index++) {
            weatherAlertJSON.put(this.getWeatherAlertJSON(index));
        }

        return weatherAlertJSON;
    }

    //  getPlaceObjectJSON()
    //________________________________________________________________
    //


    public JSONObject getPlaceObjectJSON() throws Exception {
        JSONObject placeObjectJSON = new JSONObject();

        try {
            placeObjectJSON.accumulate("place", this.getPlaceJSON());
            placeObjectJSON.accumulate("update", this.getUpdateJSON());
            placeObjectJSON.accumulate("errors", this.getErrorsJSON());

            placeObjectJSON.accumulate("current_weather", this.currentWeather.getJSONObject());
            placeObjectJSON.accumulate("minutely_weather_forecast", this.getAllMinutelyWeatherForecastJSON());
            placeObjectJSON.accumulate("hourly_weather_forecast", this.getAllHourlyWeatherForecastJSON());
            placeObjectJSON.accumulate("daily_weather_forecast", this.getAllDailyWeatherForecastJSON());
            placeObjectJSON.accumulate("weather_alert", this.getAllWeatherAlertJSON());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return placeObjectJSON;
    }
}