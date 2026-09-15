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

import fr.qgdev.openweather.data.models.Coordinates
import fr.qgdev.openweather.data.models.Geolocation
import fr.qgdev.openweather.data.models.Place
import fr.qgdev.openweather.data.models.PlaceStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class PlaceReplaceTest {

    private fun geolocationOf(city: String, latitude: Double) =
        Geolocation.newBuilder()
            .setCity(city)
            .setCountryCode("FR")
            .setCoordinates(Coordinates.newBuilder().setLatitude(latitude).setLongitude(0.0))
            .build()

    private fun placeOf(city: String, latitude: Double) =
        Place.newBuilder().setGeolocation(geolocationOf(city, latitude)).buildPartial()

    private fun storageOf(vararg places: Place): PlaceStorage =
        places.foldIndexed(PlaceStorage.newBuilder()) { key, builder, place ->
            builder.putPlaces(key, place).addOrderedPlaceKeys(key)
        }.build()

    /**
     * Test swapping a place for another location.
     * Test will not pass if the new place does not take the position of the old one.
     */
    @Test
    fun `the new place takes the position of the old one`() {
        val storage = storageOf(placeOf("Paris", 1.0), placeOf("Lyon", 2.0), placeOf("Nice", 3.0))

        val (replaced, result) = storage.withPlaceReplaced(geolocationOf("Lyon", 2.0), placeOf("Lyon centre", 2.5))

        assertEquals(ReplaceResult.REPLACED, result)
        assertEquals(listOf(0, 1, 2), replaced.orderedPlaceKeysList)
        assertEquals("Lyon centre", replaced.getPlacesOrThrow(1).geolocation.city)
    }

    /**
     * Test swapping a place for a location another stored place already is.
     * Test will not pass if two stored places end up being the same location.
     */
    @Test
    fun `a location already stored elsewhere is refused`() {
        val storage = storageOf(placeOf("Paris", 1.0), placeOf("Lyon", 2.0))

        val (unchanged, result) = storage.withPlaceReplaced(geolocationOf("Lyon", 2.0), placeOf("Paris", 1.0))

        assertEquals(ReplaceResult.ALREADY_PRESENT, result)
        assertSame(storage, unchanged)
    }

    /**
     * Test swapping a place for its own location, to refresh it for instance.
     * Test will not pass if a place is refused for clashing with itself.
     */
    @Test
    fun `a place can be replaced by its own location`() {
        val storage = storageOf(placeOf("Paris", 1.0))

        val (_, result) = storage.withPlaceReplaced(geolocationOf("Paris", 1.0), placeOf("Paris", 1.0))

        assertEquals(ReplaceResult.REPLACED, result)
    }

    /**
     * Test swapping a place that was deleted meanwhile.
     * Test will not pass if the new place is stored anyway, unlisted.
     */
    @Test
    fun `a place deleted meanwhile is not replaced`() {
        val storage = storageOf(placeOf("Paris", 1.0))

        val (unchanged, result) = storage.withPlaceReplaced(geolocationOf("Lyon", 2.0), placeOf("Lyon centre", 2.5))

        assertEquals(ReplaceResult.NOT_FOUND, result)
        assertSame(storage, unchanged)
    }
}
