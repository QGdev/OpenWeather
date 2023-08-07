
/*
 *  Copyright (c) 2019 - 2023
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

import java.util.concurrent.atomic.AtomicInteger;

import fr.qgdev.openweather.repositories.weather.FetchCallback;
import fr.qgdev.openweather.repositories.weather.RequestStatus;

public class PeriodicUpdaterWorker extends Worker {
	
	private static final String TAG = PeriodicUpdaterWorker.class.getSimpleName();
	
	private final AppRepository mRepository;
	
	public PeriodicUpdaterWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		mRepository = new AppRepository(context);
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
		
		Context context = getApplicationContext();
		
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
		mRepository.updateWidgets(context);
		
		// In case of errors, return failure
		// If every updates did fail, it will return failure
		if (numberOfErrors.compareAndSet(numberOfPlaces, 0)) return Result.retry();
		
		return Result.success();
	}
}
