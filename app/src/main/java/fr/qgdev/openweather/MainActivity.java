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

package fr.qgdev.openweather;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.TimeUnit;

import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.PeriodicUpdaterWorker;
import fr.qgdev.openweather.repositories.settings.SettingsManager;

/**
 * MainActivity
 * <p>
 * Main Activity
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see AppCompatActivity
 */
public class MainActivity extends AppCompatActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Context context = getApplicationContext();
		AppRepository appRepository = new AppRepository(context);
		SettingsManager settingsManager = appRepository.getSettingsManager();
		
		BottomNavigationView navView = findViewById(R.id.nav_view);
		
		//  NavigationBar for Live data, Forecasts or settings
		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
		if (navHostFragment == null) throw new NullPointerException("NavHostFragment is null");
		NavController navController = navHostFragment.getNavController();
		NavigationUI.setupWithNavController(navView, navController);
		
		//  Periodic update
		//  If periodic update is enabled, we schedule a periodic work request
		if (settingsManager.isPeriodicUpdateEnabled()) {
			
			//  Do some maths to know how many time there is until the next quarter of an hour like is 12:50,
			//  the next given hour will be 13:00 and the next quarter of an hour will be 13:15, etc...
			//  Don't want the raw next hour but the exact next quarter of an hour like 13:00, 13:15, 13:30, etc...
			long now = System.currentTimeMillis();
			//  Time until next quarter of an hour
			long timeUntilNextQuarter = 900000 - (now % 900000);
			
			Constraints constraints = new Constraints.Builder()
					  .setRequiredNetworkType(NetworkType.CONNECTED)
					  .setRequiresBatteryNotLow(true)
					  .build();
			
			PeriodicWorkRequest periodicWorkRequest =
					  new PeriodicWorkRequest.Builder(PeriodicUpdaterWorker.class, 15, TimeUnit.MINUTES, 5, TimeUnit.MINUTES)
								 .setConstraints(constraints)
								 .setInitialDelay(timeUntilNextQuarter + 60000, TimeUnit.MILLISECONDS)
								 .build();
			
			WorkManager.getInstance(this.getApplicationContext()).enqueueUniquePeriodicWork("PeriodicUpdaterWorker",
					  ExistingPeriodicWorkPolicy.UPDATE,
					  periodicWorkRequest);
			
			appRepository.getWidgetsManager().updateWidgets(context);
		} else {
			//  Cancel all periodic work
			WorkManager.getInstance(this.getApplicationContext()).cancelAllWork();
		}
	}
}
