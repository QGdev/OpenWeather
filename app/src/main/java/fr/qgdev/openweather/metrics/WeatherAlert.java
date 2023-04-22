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

/**
 * The type Weather alert.
 */
@Entity(tableName = "weather_alerts",
		  primaryKeys = {"placeId", "startDt", "event",})
public class WeatherAlert {
	@NonNull
	private final String sender;
	@NonNull
	private final String event;
	private final long startDt;
	private final long endDt;
	@NonNull
	private final String description;
	private final List<String> tags;
	private int placeId;
	
	/**
	 * Instantiates a new Weather alert.
	 */
	public WeatherAlert(@NonNull String sender, @NonNull String event, long startDt, long endDt, @NonNull String description, List<String> tags) {
		this.sender = sender;
		this.event = event;
		this.startDt = startDt;
		this.endDt = endDt;
		this.description = description;
		this.tags = Collections.unmodifiableList(tags);
	}
	
	/**
	 * Instantiates a new Weather alert with JSON from OpenWeatherMap.
	 *
	 * @param weatherAlert the weather alert JSON from OpenWeatherMap
	 * @throws JSONException
	 */
	@Ignore
	public WeatherAlert(JSONObject weatherAlert) throws JSONException {
		this.sender = weatherAlert.getString("sender_name");
		this.event = weatherAlert.getString("event");
		this.startDt = weatherAlert.getLong("start") * 1000;
		this.endDt = weatherAlert.getLong("end") * 1000;
		this.description = weatherAlert.getString("description");
		this.tags = new ArrayList<>();
		
		JSONArray tagsJSON = weatherAlert.getJSONArray("tags");
		
		for (int idx = 0; idx < tagsJSON.length(); idx++) {
			tags.add(tagsJSON.getString(idx));
		}
	}
	
	//  Getter
	
	/**
	 * Gets place id.
	 *
	 * @return the place id
	 */
	public int getPlaceId() {
		return placeId;
	}
	
	/**
	 * Sets place id.
	 *
	 * @param placeId the place id
	 */
	public void setPlaceId(int placeId) {
		this.placeId = placeId;
	}
	
	/**
	 * Gets sender.
	 *
	 * @return the sender
	 */
	public String getSender() {
		return sender;
	}
	
	/**
	 * Gets event.
	 *
	 * @return the event
	 */
	public String getEvent() {
		return event;
	}
	
	/**
	 * Gets start dt.
	 *
	 * @return the start dt
	 */
	public long getStartDt() {
		return startDt;
	}
	
	/**
	 * Gets end dt.
	 *
	 * @return the end dt
	 */
	public long getEndDt() {
		return endDt;
	}
	
	//  Setter
	
	/**
	 * Gets description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Gets tags.
	 *
	 * @return the tags
	 */
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}
	
	/**
	 * Clone weather alert.
	 *
	 * @return the weather alert
	 */
	@NonNull
	public WeatherAlert clone() {
		return new WeatherAlert(this.sender, this.event, this.startDt, this.endDt, this.description, this.tags);
	}
	
	/**
	 * To string method for debugging.
	 *
	 * @return the string
	 */
	@NonNull
	@Override
	public String toString() {
		return new StringJoiner(", ", WeatherAlert.class.getSimpleName() + "[", "]")
				  .add("placeId=" + placeId)
				  .add("sender='" + sender + "'")
				  .add("event='" + event + "'")
				  .add("start_dt=" + startDt)
				  .add("end_dt=" + endDt)
				  .add("description='" + description + "'")
				  .add("tags=" + tags)
				  .toString();
	}
}
