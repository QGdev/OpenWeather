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

package fr.qgdev.openweather.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.repositories.settings.SecuredPreferenceDataStore;

/**
 * CustomPreferenceFragment
 * <p>
 * 	Custom Preference Fragment to add a bottom padding to the recycler view
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see PreferenceFragmentCompat
 */
public class CustomPreferenceFragment extends PreferenceFragmentCompat {
	
	/**
	 * onCreateView()
	 * <p>
	 * Override the default onCreateView method to add a bottom padding to the recycler view
	 */
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		RecyclerView recyclerView = root.findViewById(androidx.preference.R.id.recycler_view);
		
		recyclerView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.recycler_view_bottom_padding));
		recyclerView.setClipToPadding(false);
		
		return root;
	}
	
	/**
	 * onCreatePreferences()
	 * <p>
	 * Override the default onCreatePreferences method to add a custom data store
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setPreferenceDataStore(new SecuredPreferenceDataStore(getContext(),
				  "fr.qgdev.openweather_preferences"));
		addPreferencesFromResource(R.xml.settings_preferences);
	}
}
