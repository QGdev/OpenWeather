package fr.qgdev.openweather.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import fr.qgdev.openweather.MainActivity;
import fr.qgdev.openweather.R;


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