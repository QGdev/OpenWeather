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

package fr.qgdev.openweather.data.storage

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import fr.qgdev.openweather.data.models.PlaceStorage
import java.io.InputStream
import java.io.OutputStream

object PlaceDataStore {

    private const val TAG: String = "PlaceDataStore"
    private const val FILENAME = "placeStorage.pb"
    private val Context.placeDatastore by dataStore(
        fileName = FILENAME,
        serializer = PlaceSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { PlaceStorage.getDefaultInstance() }
    )

    fun getDataStore(context: Context): DataStore<PlaceStorage> {
        return context.placeDatastore
    }

    private object PlaceSerializer : Serializer<PlaceStorage> {
        override val defaultValue: PlaceStorage = PlaceStorage.getDefaultInstance()

        override suspend fun readFrom(input: InputStream): PlaceStorage {
            try {
                return PlaceStorage.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

        override suspend fun writeTo(t: PlaceStorage, output: OutputStream) {
            t.writeTo(output)
        }
    }
}