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

package fr.qgdev.openweather.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import fr.qgdev.openweather.data.repositories.PlaceRepository
import fr.qgdev.openweather.data.repositories.SettingsRepository
import fr.qgdev.openweather.widgets.WidgetsManager
import kotlinx.coroutines.withTimeoutOrNull

/**
 * PeriodicUpdaterWorker
 *
 * A CoroutineWorker that updates all places and widgets periodically.
 * Avoids PeriodicWorkRequest (15-minute minimum floor) by using a
 * OneTimeWorkRequest that reschedules itself at the next mark of the chosen
 * interval (e.g. every 30 minutes: 13:00, 13:30, 14:00, …).
 *
 * Flow:
 *  1. No places registered → success, nothing to do.
 *  2. Periodic update disabled → success, nothing to do.
 *  3. Update all places concurrently via [PlaceRepository.updateAllPlacesFromWeb].
 *  4. Broadcast a widget refresh via [WidgetsManager.updateWidgets].
 *  5. Every single update failed → retry (transient network issue).
 *  6. Otherwise → reschedule at the interval's next mark and return success.
 *
 * Until the rescheduling was written, this list described it but the code returned without
 * doing it: the work ran once after each app launch or settings change, then stopped.
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
            
            val isPeriodicUpdateEnabled = settingsRepository.isPeriodicUpdateEnabled()
            if (!isPeriodicUpdateEnabled) {
                return Result.success()
            }

            if (placeCount <= 0) {
                //  Nothing to update yet, but the chain goes on: a place may be added in between.
                scheduleNextRun(settingsRepository, widgetsManager)
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
            
            //  Each place's widgets were redrawn as its data was written. This redraws them all once
            //  more, so that a widget whose place could not be refreshed still shows its values
            //  ageing.
            widgetsManager.updateWidgets(context)

            if (errorCount == placeCount) {
                //  Retried with WorkManager's backoff; the next mark is scheduled once it succeeds.
                return Result.retry()
            }

            scheduleNextRun(settingsRepository, widgetsManager)
            return Result.success()
            
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    /** Queues the next run at the chosen interval's next mark, after this one. */
    private fun scheduleNextRun(settingsRepository: SettingsRepository, widgetsManager: WidgetsManager) {
        val periodMillis = settingsRepository.getUpdatePeriodSetting().durationMillis
        widgetsManager.scheduleNextRun(applicationContext, WidgetsManager.timeUntilNextMark(periodMillis))
    }
}


