/*
 *  Copyright (c) 2019 - 2026
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultsTest {

    private fun place(city: String, country: String, region: String, lat: Double, lon: Double) =
        Geolocation.newBuilder()
            .setCity(city).setCountryCode(country).setRegion(region)
            .setCoordinates(Coordinates.newBuilder().setLatitude(lat).setLongitude(lon))
            .build()

    //  The two Nantes Nominatim returns: the commune's boundary and the city's own point, 12 km apart.
    private val nantesBoundary = place("Nantes", "fr", "Pays de la Loire", 47.106, -1.532)
    private val nantesPoint = place("Nantes", "fr", "Pays de la Loire", 47.219, -1.554)
    private val nantesBrazil = place("Nantes", "br", "São Paulo", -22.619, -51.238)

    @Test
    fun `the distance between the two Nantes is about 12 km`() {
        assertEquals(12.6, distanceKm(nantesBoundary.coordinates, nantesPoint.coordinates), 0.5)
    }

    @Test
    fun `near duplicates merge, and the town's own point wins over its boundary`() {
        val merged = mergeNearDuplicates(
            listOf(
                SearchCandidate(nantesBoundary, isPlacePoint = false),
                SearchCandidate(nantesBrazil, isPlacePoint = true),
                SearchCandidate(nantesPoint, isPlacePoint = true)
            )
        )
        assertEquals(listOf(nantesPoint, nantesBrazil), merged)
    }

    @Test
    fun `a boundary after the point does not replace it`() {
        val merged = mergeNearDuplicates(
            listOf(SearchCandidate(nantesPoint, true), SearchCandidate(nantesBoundary, false))
        )
        assertEquals(listOf(nantesPoint), merged)
    }

    @Test
    fun `same name in another region or far away is another place`() {
        assertFalse(nantesPoint.isSamePlaceAs(nantesBrazil))
        assertFalse(nantesPoint.isSamePlaceAs(place("Nantes", "fr", "Bretagne", 47.22, -1.55)))
        assertTrue(nantesPoint.isSamePlaceAs(nantesBoundary))
    }

    @Test
    fun `a place stored before regions existed still matches`() {
        assertTrue(place("Nantes", "fr", "", 47.21, -1.55).isSamePlaceAs(nantesPoint))
    }
}
