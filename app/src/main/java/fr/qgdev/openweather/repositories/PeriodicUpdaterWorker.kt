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

package fr.qgdev.openweather.repositories

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.settings.SettingsRepository
import fr.qgdev.openweather.widgets.WidgetsManager
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration

/**
 * PeriodicUpdaterWorker
 *
 * A CoroutineWorker that updates all places and widgets periodically.
 * Avoids PeriodicWorkRequest (15-minute minimum floor) by using a
 * OneTimeWorkRequest that reschedules itself at the next quarter-hour mark
 * (e.g. 13:00, 13:15, 13:30, 13:45, …).
 *
 * Flow:
 *  1. No places registered → success, nothing to do.
 *  2. Periodic update disabled → success, nothing to do.
 *  3. Update all places concurrently via [PlaceRepository.updateAllPlacesFromWeb].
 *  4. Broadcast a widget refresh via [WidgetsManager.updateWidgets].
 *  5. Every single update failed → retry (transient network issue).
 *  6. Otherwise → reschedule at the next quarter-hour mark and return success.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see CoroutineWorker
 */
class PeriodicUpdaterWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val context = applicationContext
            val placeRepository = PlaceRepository.getInstance(context)
            val settingsRepository = SettingsRepository.getInstance(context)
            val widgetsManager = WidgetsManager.getInstance(context)

            val placeCount = try {
                withTimeoutOrNull(10000) {
                    placeRepository.getPlaceCount()
                } ?: run {
                    return Result.retry()
                }
            } catch (e: Exception) {
                return Result.retry()
            }
            
            if (placeCount <= 0) {
                return Result.success()
            }

            val isPeriodicUpdateEnabled = settingsRepository.isPeriodicUpdateEnabled()
            if (!isPeriodicUpdateEnabled) {
                return Result.success()
            }

            val (successCount, errorCount) = try {
                withTimeoutOrNull(30000) {
                    placeRepository.updateAllPlacesFromWeb()
                } ?: run {
                    Pair(0, placeCount)
                }
            } catch (e: Exception) {
                Pair(0, placeCount)
            }
            
            widgetsManager.updateWidgets(context)

            if (errorCount == placeCount) {
                return Result.retry()
            }

            val periodMillis = settingsRepository.settingsFlow.value.updatePeriod.durationMillis
            val timeUntilNextMark = periodMillis - (System.currentTimeMillis() % periodMillis)
            widgetsManager.scheduleNextRun(context, Duration.ofMillis(timeUntilNextMark))

            return Result.success()
            
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}


