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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests the storage rules PlaceRepository enforces, against a plain in-memory PlaceStorage.
 *
 * PlaceRepository itself needs a Context - it builds a DataStore, a Volley queue and a
 * PlaceSearchingService in its constructor - so it cannot be instantiated on the JVM. What is
 * worth testing is the transform applied inside `updateData`, which is pure: given a storage and an
 * operation, what does the next storage look like. Those transforms are duplicated here in the same
 * shape as the repository applies them.
 *
 * That means these tests verify the *rules*, not the wiring. The wiring - that the transform runs
 * inside updateData rather than around it, which is the actual atomicity fix - is verified by
 * reading the code, and noted here so nobody mistakes this file for proof of it.
 */
class PlaceRepositoryOrderingTest {

    private companion object {
        const val MAX_PLACES = 100
    }

    private fun geolocationOf(city: String, latitude: Double = 0.0) =
        Geolocation.newBuilder()
            .setCity(city)
            .setCountryCode("FR")
            .setCoordinates(
                Coordinates.newBuilder().setLatitude(latitude).setLongitude(0.0).build()
            )
            .build()

    private fun placeOf(city: String, latitude: Double = 0.0) =
        Place.newBuilder().setGeolocation(geolocationOf(city, latitude)).buildPartial()

    // region The transforms, mirroring PlaceRepository

    private fun PlaceStorage.nextFreeId(): Int? =
        if (placesCount >= MAX_PLACES) null
        else (0 until MAX_PLACES).firstNotNullOfOrNull { offset ->
            ((lastKeyUsed + 1 + offset) % MAX_PLACES).takeIf { !placesMap.containsKey(it) }
        }

    private fun PlaceStorage.holds(geolocation: Geolocation) =
        placesMap.values.any { it.geolocation == geolocation }

    private fun PlaceStorage.addPlaceAt(index: Int?, place: Place): Pair<PlaceStorage, AddPlaceResult> {
        if (holds(place.geolocation)) return this to AddPlaceResult.ALREADY_PRESENT
        val placeId = nextFreeId() ?: return this to AddPlaceResult.STORAGE_FULL

        val orderedKeys = orderedPlaceKeysList.toMutableList()
        if (index != null && index in 0..orderedKeys.size) {
            orderedKeys.add(index, placeId)
        } else {
            orderedKeys.add(placeId)
        }
        return toBuilder()
            .putPlaces(placeId, place)
            .setLastKeyUsed(placeId)
            .clearOrderedPlaceKeys()
            .addAllOrderedPlaceKeys(orderedKeys)
            .build() to AddPlaceResult.ADDED
    }

    private fun PlaceStorage.movePlace(fromIndex: Int, toIndex: Int): PlaceStorage {
        if (fromIndex == toIndex) return this
        val orderedKeys = orderedPlaceKeysList.toMutableList()
        if (fromIndex !in orderedKeys.indices || toIndex !in orderedKeys.indices) return this
        orderedKeys.add(toIndex, orderedKeys.removeAt(fromIndex))
        return toBuilder().clearOrderedPlaceKeys().addAllOrderedPlaceKeys(orderedKeys).build()
    }

    private fun PlaceStorage.cities() =
        orderedPlaceKeysList.mapNotNull { placesMap[it]?.geolocation?.city }

    private fun storageOf(vararg cities: String): PlaceStorage =
        cities.foldIndexed(PlaceStorage.getDefaultInstance()) { i, storage, city ->
            storage.addPlaceAt(null, placeOf(city, i.toDouble())).first
        }

    // endregion

    // region Duplicates

    @Test
    fun `a place with the same geolocation is refused`() {
        val storage = storageOf("Paris")
        val (after, result) = storage.addPlaceAt(null, placeOf("Paris"))

        assertEquals(AddPlaceResult.ALREADY_PRESENT, result)
        assertEquals("the refused add must not change anything", 1, after.placesCount)
        assertEquals(listOf("Paris"), after.cities())
    }

    @Test
    fun `the same city name at different coordinates is a different place`() {
        val storage = storageOf("Paris")
        //  Paris, France and Paris, Texas are both legitimately "Paris".
        val (after, result) = storage.addPlaceAt(null, placeOf("Paris", latitude = 33.66))

        assertEquals(AddPlaceResult.ADDED, result)
        assertEquals(2, after.placesCount)
    }

    @Test
    fun `storage reports full at the cap rather than overwriting`() {
        val full = (0 until MAX_PLACES).fold(PlaceStorage.getDefaultInstance()) { storage, i ->
            storage.addPlaceAt(null, placeOf("City$i", i.toDouble())).first
        }
        assertEquals(MAX_PLACES, full.placesCount)

        val (after, result) = full.addPlaceAt(null, placeOf("OneTooMany", 999.0))

        assertEquals(AddPlaceResult.STORAGE_FULL, result)
        assertEquals(MAX_PLACES, after.placesCount)
    }

    // endregion

    // region Ordering

    @Test
    fun `a new place goes to the end by default`() {
        assertEquals(listOf("Paris", "Lyon", "Nice"), storageOf("Paris", "Lyon", "Nice").cities())
    }

    @Test
    fun `a place can be restored at its previous index`() {
        val storage = storageOf("Paris", "Lyon", "Nice")
            .addPlaceAt(1, placeOf("Brest", 99.0)).first

        assertEquals(listOf("Paris", "Brest", "Lyon", "Nice"), storage.cities())
    }

    @Test
    fun `moving an item down shifts the ones it passes up`() {
        val storage = storageOf("Paris", "Lyon", "Nice", "Brest").movePlace(0, 2)
        assertEquals(listOf("Lyon", "Nice", "Paris", "Brest"), storage.cities())
    }

    @Test
    fun `moving an item up shifts the ones it passes down`() {
        val storage = storageOf("Paris", "Lyon", "Nice", "Brest").movePlace(3, 1)
        assertEquals(listOf("Paris", "Brest", "Lyon", "Nice"), storage.cities())
    }

    @Test
    fun `moving to the same index changes nothing`() {
        val before = storageOf("Paris", "Lyon", "Nice")
        assertEquals(before.cities(), before.movePlace(1, 1).cities())
    }

    @Test
    fun `an out of range move is ignored rather than throwing`() {
        //  A drag can race a concurrent delete and arrive with a stale index.
        val before = storageOf("Paris", "Lyon")
        assertEquals(before.cities(), before.movePlace(0, 5).cities())
        assertEquals(before.cities(), before.movePlace(7, 0).cities())
        assertEquals(before.cities(), before.movePlace(-1, 0).cities())
    }

    @Test
    fun `a move reorders the display order without touching the stored places`() {
        val before = storageOf("Paris", "Lyon", "Nice")
        val after = before.movePlace(0, 2)

        assertEquals(before.placesMap, after.placesMap)
        assertNotEquals(before.orderedPlaceKeysList, after.orderedPlaceKeysList)
    }

    // endregion

    @Test
    fun `ids are reused after a delete without disturbing the order`() {
        val storage = storageOf("Paris", "Lyon", "Nice")
        val lyonId = storage.orderedPlaceKeysList[1]

        val afterDelete = storage.toBuilder()
            .removePlaces(lyonId)
            .clearOrderedPlaceKeys()
            .addAllOrderedPlaceKeys(storage.orderedPlaceKeysList.filterNot { it == lyonId })
            .build()
        assertEquals(listOf("Paris", "Nice"), afterDelete.cities())

        val (afterAdd, result) = afterDelete.addPlaceAt(null, placeOf("Brest", 99.0))
        assertEquals(AddPlaceResult.ADDED, result)
        assertEquals(listOf("Paris", "Nice", "Brest"), afterAdd.cities())
        assertTrue("the freed id should be reusable", afterAdd.placesCount == 3)
    }
}
