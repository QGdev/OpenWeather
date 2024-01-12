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

package fr.qgdev.openweather.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import fr.qgdev.openweather.MainActivity;
import fr.qgdev.openweather.R;

/**
 * SplashScreenActivity
 * <p>
 *    A splash screen to display the app logo.
 *    It will be displayed for a short time before the main activity.
 *    It is used to display the app logo and to load the app.
 *    Might be deleted in the future.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see AppCompatActivity
 */
public class SplashScreenActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        
        int splashTimeOut = 1000;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // This method will be executed once the timer is over
            // Start your app main activity
            Intent mainActivity = new Intent(SplashScreenActivity.this, MainActivity.class);
            startActivity(mainActivity);
            
            // close this activity
            finish();
        }, splashTimeOut);
    }
}