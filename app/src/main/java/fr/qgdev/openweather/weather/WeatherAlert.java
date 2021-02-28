package fr.qgdev.openweather.weather;

import androidx.annotation.NonNull;

import java.util.Date;

public class WeatherAlert {
    private final String sender;
    private final String event;
    private final long start_dt;
    private final long end_dt;
    private final String description;

// --Commented out by Inspection START (28/02/21 17:50):
//    public WeatherAlert(){
//
//    }
// --Commented out by Inspection STOP (28/02/21 17:50)

    public WeatherAlert(String sender, String event, long start_dt, long end_dt, String description) {
        this.sender = sender;
        this.event = event;
        this.start_dt = start_dt;
        this.end_dt = end_dt;
        this.description = description;
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

    @NonNull
    public WeatherAlert clone() {
        return new WeatherAlert(this.sender, this.event, this.start_dt, this.end_dt, this.description);
    }
}
