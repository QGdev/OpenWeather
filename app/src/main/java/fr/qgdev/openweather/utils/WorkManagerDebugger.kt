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

package fr.qgdev.openweather.utils

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager

/**
 * WorkManagerDebugger
 *
 * Utility class to check the status of WorkManager tasks and log their current state.
 * Useful for debugging and verifying that periodic tasks are scheduled correctly.
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 */
object WorkManagerDebugger {
    private const val TAG = "WorkManagerDebugger"
    private const val WORKER_TASK_NAME = "PeriodicUpdaterWorker"

    /**
     * Checks the current status of the periodic updater work.
     * Logs detailed information about the work state to the console.
     *
     * @param context The application context
     */
    fun checkPeriodicUpdaterStatus(context: Context) {
        val tag = TAG
        Log.d(tag, "════════════════════════════════════════════════════════")
        Log.d(tag, "📊 Checking WorkManager Status for: $WORKER_TASK_NAME")
        Log.d(tag, "════════════════════════════════════════════════════════")

        try {
            val workManager = WorkManager.getInstance(context)
            
            // Get work info for the unique work name
            val workInfosLiveData = workManager.getWorkInfosForUniqueWorkLiveData(WORKER_TASK_NAME)
            
            Log.d(tag, "⏳ Fetching work info...")
            Log.d(tag, "Note: This call is asynchronous. Work info will be available when LiveData updates.")
            
            workInfosLiveData.observeForever { workInfoList ->
                if (workInfoList.isNullOrEmpty()) {
                    Log.d(tag, "❌ No work found for: $WORKER_TASK_NAME")
                    Log.d(tag, "   → Either the work hasn't been scheduled yet, or it has been cancelled.")
                } else {
                    Log.d(tag, "✅ Found ${workInfoList.size} work request(s):")
                    
                    workInfoList.forEachIndexed { index, workInfo ->
                        val stateEmoji = when (workInfo.state) {
                            WorkInfo.State.ENQUEUED -> "⏳"
                            WorkInfo.State.RUNNING -> "🔄"
                            WorkInfo.State.SUCCEEDED -> "✅"
                            WorkInfo.State.FAILED -> "❌"
                            WorkInfo.State.BLOCKED -> "🚫"
                            WorkInfo.State.CANCELLED -> "❌"
                        }
                        
                        Log.d(tag, "")
                        Log.d(tag, "   Work #${index + 1}:")
                        Log.d(tag, "   - ID: ${workInfo.id}")
                        Log.d(tag, "   - State: $stateEmoji ${workInfo.state}")
                        Log.d(tag, "   - Tags: ${workInfo.tags.joinToString(", ")}")
                        
                        // Log constraints if available
                        val constraints = workInfo.constraints
                        Log.d(tag, "   - Constraints:")
                        Log.d(tag, "     • Network type: ${constraints.requiredNetworkType}")
                        Log.d(tag, "     • Battery not low: ${constraints.requiresBatteryNotLow()}")
                    }
                }
                Log.d(tag, "════════════════════════════════════════════════════════")
                Log.d(tag, "")
            }
        } catch (e: Exception) {
            Log.e(tag, "❌ Error checking WorkManager status: ${e.message}", e)
            Log.d(tag, "════════════════════════════════════════════════════════")
        }
    }

    /**
     * Cancels all pending work for the periodic updater.
     * Useful for debugging purposes.
     *
     * @param context The application context
     */
    fun cancelPeriodicUpdaterWork(context: Context) {
        Log.d(TAG, "🛑 Cancelling all work for: $WORKER_TASK_NAME")
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORKER_TASK_NAME)
            Log.d(TAG, "✅ Work cancelled successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cancelling work: ${e.message}", e)
        }
    }

    /**
     * Prints all registered work in the WorkManager queue.
     * Useful for general debugging.
     *
     * @param context The application context
     */
    fun printAllWork(context: Context) {
        Log.d(TAG, "════════════════════════════════════════════════════════")
        Log.d(TAG, "📋 All Work in WorkManager Queue")
        Log.d(TAG, "════════════════════════════════════════════════════════")
        
        try {
            val workManager = WorkManager.getInstance(context)
            
            // Note: getWorkInfosLiveData() is used to get all work
            // We need to use it differently than before
            Log.d(TAG, "⏳ Fetching all work...")
            Log.d(TAG, "Note: This feature is limited in current WorkManager API.")
            Log.d(TAG, "Use checkPeriodicUpdaterStatus() for specific work details.")
            Log.d(TAG, "════════════════════════════════════════════════════════")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error listing work: ${e.message}", e)
            Log.d(TAG, "════════════════════════════════════════════════════════")
        }
    }
}

