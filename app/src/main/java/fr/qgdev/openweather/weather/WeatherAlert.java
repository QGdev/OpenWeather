package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class WeatherAlert {
    private String sender;
    private String event;
    private long start_dt;
    private long end_dt;
    private String description;

    public WeatherAlert(){}

    public WeatherAlert(String sender, String event, long start_dt, long end_dt, String description) {
        this.sender = sender;
        this.event = event;
        this.start_dt = start_dt;
        this.end_dt = end_dt;
        this.description = description;
    }

    public WeatherAlert(JSONObject weatherAlert) throws JSONException
    {
        this.sender = weatherAlert.getString("sender");
        this.event = weatherAlert.getString("event");
        this.start_dt = weatherAlert.getLong("start_dt");
        this.end_dt = weatherAlert.getLong("end_dt");
        this.description = weatherAlert.getString("description");
    }

    public void fillWithOWMData(JSONObject weatherAlert) throws JSONException
    {
        this.sender = weatherAlert.getString("sender_name");
        this.event = weatherAlert.getString("event");
        this.start_dt = weatherAlert.getLong("start") * 1000;
        this.end_dt = weatherAlert.getLong("end") * 1000;
        this.description = weatherAlert.getString("description");
    }

    public String getSender() {
        return sender;
    }

    public String getEvent() {
        return event;
    }

    public long getStart_dt() {
        return start_dt;
    }

    public Date getStart_dtDate() {
        return new Date(this.start_dt);
    }

    public long getEnd_dt() {
        return end_dt;
    }

    public Date getEnd_dtDate() {
        return new Date(this.end_dt);
    }

    public String getDescription() {
        return description;
    }

    public JSONObject getJSONObject() throws JSONException
    {
        JSONObject weatherAlertJSON = new JSONObject();

        weatherAlertJSON.accumulate("sender", this.sender);
        weatherAlertJSON.accumulate("event", this.event);
        weatherAlertJSON.accumulate("start_dt", this.start_dt);
        weatherAlertJSON.accumulate("end_dt", this.end_dt);
        weatherAlertJSON.accumulate("description", this.description);

        return weatherAlertJSON;
    }

    @NonNull
    public WeatherAlert clone() {
        return new WeatherAlert(this.sender, this.event, this.start_dt, this.end_dt, this.description);
    }
}
