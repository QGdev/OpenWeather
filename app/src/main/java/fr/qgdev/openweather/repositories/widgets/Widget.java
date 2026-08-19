
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

package fr.qgdev.openweather.repositories.widgets;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Widget {
	
	@NonNull
	private final String placeId;
	private final int widgetId;
	
	public Widget(@NonNull String placeId, int widgetId) {
		Objects.requireNonNull(placeId, "placeId must not be null");
		if (widgetId == -1) throw new IllegalArgumentException("widgetId must be a valid widget id");
		
		this.placeId = placeId;
		this.widgetId = widgetId;
	}
	
	public String getPlaceId() {
		return placeId;
	}
	
	public int getWidgetId() {
		return widgetId;
	}
}
