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

package fr.qgdev.openweather.data.repositories

import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.PlaceStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaceUpdateTest {

    private fun placeOf(city: String) =
        Place.newBuilder().setGeolocation(Geolocation.newBuilder().setCity(city)).buildPartial()

    /**
     * Test a refresh landing on a place that is still stored.
     * Test will not pass if the refreshed place does not replace the stored one.
     */
    @Test
    fun `a stored place is replaced by its refresh`() {
        val storage = PlaceStorage.newBuilder().putPlaces(3, placeOf("Paris")).build()

        val updated = storage.withPlaceUpdated(3, placeOf("Paris refreshed"))

        assertEquals("Paris refreshed", updated?.getPlacesOrThrow(3)?.geolocation?.city)
    }

    /**
     * Test a refresh landing after its place was deleted.
     * Test will not pass if the late result puts the deleted place back into the storage.
     */
    @Test
    fun `a place deleted during its refresh is not stored again`() {
        val storage = PlaceStorage.newBuilder().putPlaces(3, placeOf("Paris")).build()

        assertNull(storage.withPlaceUpdated(4, placeOf("Lyon")))
    }
}
