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

import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.Geolocation
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Two places of the same name closer than this are taken for the same town. */
const val SAME_PLACE_DISTANCE_KM = 20.0

/**
 * A search result, with whether OpenStreetMap describes it as the town's own point ("place=city"
 * and the like) rather than, say, its administrative boundary.
 */
internal data class SearchCandidate(val geolocation: Geolocation, val isPlacePoint: Boolean)

/**
 * Nominatim often returns one town twice: the point marking its centre and its administrative
 * boundary, whose computed centre lies elsewhere - Nantes' two come out 12 km apart. Its own dedupe
 * does not merge them, being different objects. To a reader they are one town, so results with
 * the same name, country and region within [SAME_PLACE_DISTANCE_KM] are merged: the first keeps
 * its rank, and the town's own point replaces a boundary, its coordinates being the centre, where
 * the weather is most representative.
 */
internal fun mergeNearDuplicates(candidates: List<SearchCandidate>): List<Geolocation> {
    val kept = mutableListOf<SearchCandidate>()
    for (candidate in candidates) {
        val twin = kept.indexOfFirst { it.geolocation.isSamePlaceAs(candidate.geolocation) }
        when {
            twin < 0 -> kept.add(candidate)
            candidate.isPlacePoint && !kept[twin].isPlacePoint -> kept[twin] = candidate
        }
    }
    return kept.map { it.geolocation }
}

/** Same name, country and region, and within [SAME_PLACE_DISTANCE_KM] of each other. */
fun Geolocation.isSamePlaceAs(other: Geolocation): Boolean =
    city.equals(other.city, ignoreCase = true) &&
            countryCode.equals(other.countryCode, ignoreCase = true) &&
            (region.isEmpty() || other.region.isEmpty() || region.equals(other.region, ignoreCase = true)) &&
            distanceKm(coordinates, other.coordinates) < SAME_PLACE_DISTANCE_KM

/** Great-circle distance between two points, in kilometres. */
fun distanceKm(a: Coordinates, b: Coordinates): Double {
    val earthRadiusKm = 6371.0
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val dLat = lat2 - lat1
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val h = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(h))
}
