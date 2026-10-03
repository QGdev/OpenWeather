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

package fr.qgdev.openweather.data.storage

import androidx.datastore.core.CorruptionException
import fr.qgdev.openweather.data.models.PlaceStorage
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Tests the serializer of [PlaceDataStore].
 *
 * A corrupted file must raise a CorruptionException, which is what makes the DataStore hand it to
 * its ReplaceFileCorruptionHandler.
 */
class PlaceDataStoreTest {

    @Test
    fun `an empty file reads as an empty storage`() = runBlocking {
        val storage = PlaceDataStore.PlaceSerializer.readFrom(ByteArrayInputStream(ByteArray(0)))

        assertEquals(PlaceStorage.getDefaultInstance(), storage)
    }

    @Test
    fun `a written storage reads back the same`() = runBlocking {
        val storage = PlaceStorage.newBuilder().setLastKeyUsed(3).addOrderedPlaceKeys(3).build()
        val output = ByteArrayOutputStream()
        PlaceDataStore.PlaceSerializer.writeTo(storage, output)

        assertEquals(storage, PlaceDataStore.PlaceSerializer.readFrom(ByteArrayInputStream(output.toByteArray())))
    }

    @Test(expected = CorruptionException::class)
    fun `a truncated file raises a corruption`() {
        runBlocking {
            //  Field 1, length-delimited, announcing 5 bytes but holding only 1
            PlaceDataStore.PlaceSerializer.readFrom(ByteArrayInputStream(byteArrayOf(0x0A, 0x05, 0x01)))
        }
    }
}
