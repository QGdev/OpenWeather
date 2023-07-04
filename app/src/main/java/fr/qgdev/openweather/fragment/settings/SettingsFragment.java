/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

/*
 *   Copyright (c) 2023 QGdev - Quentin GOMES DOS REIS
 *
 *   This file is part of OpenWeather.
 *
 *   OpenWeather is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenWeather is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenWeather. If not, see <http://www.gnu.org/licenses/>
 */

package fr.qgdev.openweather.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.dialog.AboutAppDialog;


/**
 * SettingsFragment
 * <p>
 * Settings Fragment
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Fragment
 */
public class SettingsFragment extends Fragment {
    
    /**
     * onCreateView()
     * <p>
     * Override the default onCreateView method to add a bottom padding to the recycler view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        Button aboutUsButton = root.findViewById(R.id.about_us);
        
        getChildFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new CustomPreferenceFragment())
                .commit();
        
        aboutUsButton.setOnClickListener(v -> {
            Context context = getContext();
            if (context == null) return;
            
            final AboutAppDialog aboutAppDialog = new AboutAppDialog(context);
            aboutAppDialog.show();
        });
        
        return root;
    }
}

