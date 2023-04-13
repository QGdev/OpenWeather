package fr.qgdev.openweather;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
		
		BottomNavigationView navView = findViewById(R.id.nav_view);
		
		//  NavigationBar for Live data, Forecasts or settings
		NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
		if (navHostFragment == null) throw new NullPointerException("NavHostFragment is null");
		NavController navController = navHostFragment.getNavController();
		NavigationUI.setupWithNavController(navView, navController);
	}

}
