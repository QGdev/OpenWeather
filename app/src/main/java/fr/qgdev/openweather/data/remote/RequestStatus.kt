/*
 *  Copyright (c) 2019 - 2025
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

package fr.qgdev.openweather.data.remote

/**
 * RequestStatus
 * <p>
 *    An enum to represent the request status.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
enum class RequestStatus {
    NO_ANSWER,
    TOO_MANY_REQUESTS,
    NOT_FOUND,
    AUTH_FAILED,
    NOT_CONNECTED,
    UNKNOWN_ERROR,
    ALREADY_PRESENT,
    TOO_SHORT;

    companion object {
        /** The status of a failed request from its HTTP status, null when no response came back. */
        fun fromHttpStatus(status: Int?): RequestStatus = when (status) {
            null -> NO_ANSWER
            429 -> TOO_MANY_REQUESTS
            404 -> NOT_FOUND
            401, 403 -> AUTH_FAILED
            else -> UNKNOWN_ERROR
        }
    }
}