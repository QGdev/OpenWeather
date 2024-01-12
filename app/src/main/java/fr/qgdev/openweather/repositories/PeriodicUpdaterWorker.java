
/*
 *  Copyright (c) 2019 - 2024
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

package fr.qgdev.openweather.repositories;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import fr.qgdev.openweather.repositories.weather.FetchCallback;
import fr.qgdev.openweather.repositories.weather.RequestStatus;

/**
 * PeriodicUpdaterWorker
 * <p>
 *    A worker to update all places and widgets periodically.
 *    Did not use a PeriodicWorkRequest due to 15 minutes minimum interval.
 *    Uses a OneTimeWorkRequest that will be rescheduled at the end of the work.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Worker
 */
public class PeriodicUpdaterWorker extends Worker {
	
	private static final String TAG = PeriodicUpdaterWorker.class.getSimpleName();
	private final Context mContext;
	private final AppRepository mRepository;
	
	/**
	 * PeriodicUpdaterWorker constructor
	 */
	public PeriodicUpdaterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		mContext = context.getApplicationContext();
		mRepository = AppRepository.getInstance(mContext);
	}
	
	/**
	 * Result doWork()
	 *
	 * <p>
	 * Will be called periodically to update all places and widgets.
	 * If there is no place, nothing has to be done and a success is returned.
	 * Otherwise, all places are updated and widgets will be updated.
	 * </p>
	 *
	 * @return Result
	 */
	@NonNull
	@Override
	public Result doWork() {
		int numberOfPlaces = mRepository.countPlaces();
		if (numberOfPlaces <= 0) return Result.success();
		
		//  If periodic update is disabled, return success and no need to update widgets or data
		if (!mRepository.getSettingsManager().isPeriodicUpdateEnabled()) return Result.success();
		
		AtomicInteger remainingPlaces = new AtomicInteger(numberOfPlaces);
		AtomicInteger numberOfErrors = new AtomicInteger(0);
		
		mRepository.updateAllPlacesSynchronized(new FetchCallback() {
			@Override
			public void onSuccess() {
				remainingPlaces.decrementAndGet();
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				numberOfErrors.incrementAndGet();
			}
		});
		
		//	Wait for all places to be updated in order to update widgets
		while (remainingPlaces.compareAndSet(0, 0)) ;
		mRepository.getWidgetsManager().updateWidgets(mContext);
		
		// In case of errors, return failure
		// If every updates did fail, it will return failure
		if (numberOfErrors.compareAndSet(numberOfPlaces, 0)) return Result.retry();
		
		
		//  Do some maths to know how many time there is until the next quarter of an hour like is 12:50,
		//  the next given hour will be 13:00 and the next quarter of an hour will be 13:15, etc...
		//  Don't want the raw next hour but the exact next quarter of an hour like 13:00, 13:15, 13:30, etc...
		long now = System.currentTimeMillis();
		//  Time until next quarter of an hour
		long timeUntilNextQuarter = 900000 - (now % 900000);
		
		mRepository.getWidgetsManager().scheduleWorkRequest(mContext, Duration.ofMillis(timeUntilNextQuarter));
		
		return Result.success();
	}
}
