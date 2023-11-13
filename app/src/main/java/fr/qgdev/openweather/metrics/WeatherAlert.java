/*
 *  Copyright (c) 2019 - 2023
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
		  primaryKeys = {"placeId", "sender", "startDt", "event"})
public class WeatherAlert {
	@NonNull
	private String sender;
	@NonNull
	private String event;
	private long startDt;
	private long endDt;
	private String description;
	private List<String> tags;
	@NonNull
	private String placeId;
	
	/**
	 * Instantiates a new Weather alert.
	 */
	public WeatherAlert() {
		this.placeId = "";
		this.sender = "";
		this.event = "";
		this.startDt = 0;
		this.endDt = 0;
		this.description = "";
		this.tags = new ArrayList<>();
	}
	
	/**
	 * Instantiates a new Weather alert with JSON from OpenWeatherMap.
	 *
	 * @param weatherAlert the weather alert JSON from OpenWeatherMap
	 * @throws JSONException the json exception if the JSON is not correct
	 */
	@Ignore
	public WeatherAlert(JSONObject weatherAlert) throws JSONException {
		this.placeId = null;
		
		this.sender = weatherAlert.getString("sender_name");
		this.event = weatherAlert.getString("event");
		
		setStartDt(weatherAlert.getLong("start") * 1000);
		setEndDt(weatherAlert.getLong("end") * 1000);
		setDescription(weatherAlert.getString("description"));
		this.tags = new ArrayList<>();
		
		List<String> tmp = new ArrayList<>();
		JSONArray tagsJSON = weatherAlert.getJSONArray("tags");
		if (tagsJSON.length() > 0) {
			for (int idx = 0; idx < tagsJSON.length(); idx++) {
				tmp.add(tagsJSON.getString(idx));
			}
			setTags(tmp);
		}
	}
	
	//  Getter
	
	/**
	 * Gets place id.
	 *
	 * @return the place id
	 */
	public String getPlaceId() {
		return placeId;
	}
	
	/**
	 * Sets place id.
	 *
	 * @param placeId the place id
	 */
	public void setPlaceId(String placeId) {
		this.placeId = placeId;
	}
	
	/**
	 * Gets sender.
	 *
	 * @return the sender
	 */
	@NonNull
	public String getSender() {
		return sender;
	}
	
	/**
	 * Sets sender.
	 *
	 * @param sender the sender to set
	 */
	public void setSender(@NonNull String sender) {
		this.sender = sender;
	}
	
	/**
	 * Gets event.
	 *
	 * @return the event
	 */
	@NonNull
	public String getEvent() {
		return event;
	}
	
	/**
	 * Sets event.
	 *
	 * @param event the event to set
	 */
	public void setEvent(@NonNull String event) {
		this.event = event;
	}
	
	/**
	 * Gets start dt.
	 *
	 * @return the start dt (in milliseconds)
	 */
	public long getStartDt() {
		return startDt;
	}
	
	/**
	 * Sets start dt.
	 *
	 * @param startDt the start dt to set (in milliseconds)
	 *                Must be positive or null
	 */
	public void setStartDt(long startDt) {
		if (startDt < 0)
			throw new IllegalArgumentException("startDt must be positive or null");
		this.startDt = startDt;
	}
	
	/**
	 * Gets end dt.
	 *
	 * @return the end dt (in milliseconds)
	 */
	public long getEndDt() {
		return endDt;
	}
	
	/**
	 * Sets end dt.
	 *
	 * @param endDt the end dt to set (in milliseconds)
	 *              Must be positive or null
	 */
	public void setEndDt(long endDt) {
		if (endDt < 0)
			throw new IllegalArgumentException("endDt must be positive or null");
		this.endDt = endDt;
	}
	
	/**
	 * Gets description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets description.
	 *
	 * @param description the description to set
	 */
	public void setDescription(@NonNull String description) {
		this.description = description;
	}
	
	/**
	 * Gets tags list.
	 *
	 * @return the list of tags
	 */
	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}
	
	/**
	 * Sets tags list.
	 *
	 * @param tags the list of tags to set
	 */
	public void setTags(@NonNull List<String> tags) {
		//	Remove all tags and add the new ones
		this.tags.clear();
		
		for (String tag : tags) {
			if (!addTag(tag)) {
				break;
			}
		}
	}
	
	/**
	 * Remove tag.
	 *
	 * @param tag the tag to remove
	 * @return true if the tag was removed
	 */
	public boolean removeTag(@NonNull String tag) {
		return tags.remove(tag);
	}
	
	/**
	 * Add tag.
	 *
	 * @param tag the tag to add
	 * @return true if the tag was added
	 */
	public boolean addTag(@NonNull String tag) {
		if (tags.contains(tag))
			return false;
		return tags.add(tag);
	}
	
	/**
	 * Clone weather alert.
	 *
	 * @return the weather alert
	 */
	@NonNull
	public WeatherAlert clone() {
		WeatherAlert clone = new WeatherAlert();
		clone.setPlaceId(this.placeId);
		clone.setSender(this.sender);
		clone.setEvent(this.event);
		clone.setStartDt(this.startDt);
		clone.setEndDt(this.endDt);
		clone.setDescription(this.description);
		clone.tags = new ArrayList<>(this.tags);
		return clone;
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
