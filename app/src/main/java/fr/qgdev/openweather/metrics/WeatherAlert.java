package fr.qgdev.openweather.metrics;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@Entity(tableName = "weather_alerts",
		  primaryKeys = {"placeId", "start_dt", "event",})

public class WeatherAlert {
	@NonNull
	private final String sender;
	@NonNull
	private final String event;
	private final long start_dt;
	private final long end_dt;
	@NonNull
	private final String description;
	private final List<String> tags;
	private int placeId;
	
	
	public WeatherAlert(@NonNull String sender, @NonNull String event, long start_dt, long end_dt, @NonNull String description, List<String> tags) {
		this.sender = sender;
		this.event = event;
		this.start_dt = start_dt;
		this.end_dt = end_dt;
		this.description = description;
		this.tags = Collections.unmodifiableList(tags);
	}
	
	@Ignore
	public WeatherAlert(JSONObject weatherAlert) throws JSONException {
		this.sender = weatherAlert.getString("sender_name");
		this.event = weatherAlert.getString("event");
		this.start_dt = weatherAlert.getLong("start") * 1000;
		this.end_dt = weatherAlert.getLong("end") * 1000;
		this.description = weatherAlert.getString("description");
		this.tags = new ArrayList<String>();
		
		JSONArray tags = weatherAlert.getJSONArray("tags");
		
		for (int idx = 0; idx < tags.length(); idx++) {
			this.tags.add(tags.getString(idx));
		}
	}
	
	
	//  Getter
	public int getPlaceId() {
		return placeId;
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
	
	public long getEnd_dt() {
		return end_dt;
	}
	
	public String getDescription() {
		return description;
	}
	
	//  Setter
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}
	
	@NonNull
	public WeatherAlert clone() {
		return new WeatherAlert(this.sender, this.event, this.start_dt, this.end_dt, this.description, this.tags);
	}
	
	
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", WeatherAlert.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("sender='" + sender + "'")
				  .add("event='" + event + "'")
				  .add("start_dt=" + start_dt)
				  .add("end_dt=" + end_dt)
				  .add("description='" + description + "'")
				  .add("tags=" + tags)
				  .toString();
	}
}
