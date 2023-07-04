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

package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import fr.qgdev.openweather.BuildConfig;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapter.AttributionItemAdapter;


/**
 * AboutAppDialog
 * <p>
 * About us dialog box where all application information are presented<br>
 * Check string arrays in resource file to fill the dialog with data
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see Dialog
 */
public class AboutAppDialog extends Dialog {
	
	/**
	 * AboutAppDialog Constructor
	 * <p>
	 * Just the constructor of About app dialog class
	 * </p>
	 *
	 * @param context Context of the application in order to get resources
	 * @apiNote None of the parameters can be null
	 */
	public AboutAppDialog(@NonNull Context context) {
		super(context);
		setContentView(R.layout.dialog_about_us);
		
		TextView versionTextView = findViewById(R.id.application_version);
		LinearLayout attributionLinearLayout = findViewById(R.id.attribution_section);
		Button exitButton = findViewById(R.id.exit_button);
		ImageView devLogoImageView = findViewById(R.id.dev_logo);
		
		//  Get string arrays stored in XML resources files
		List<String> attributionTitleList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_title));
		List<String> attributionContentList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_content));
		
		//  Initialize variables containing application characteristics
		String versionName = BuildConfig.VERSION_NAME;
		String versionType = BuildConfig.BUILD_TYPE;
		int versionCode = BuildConfig.VERSION_CODE;
		
		//  Show application characteristics
		versionTextView.setText(String.format("%s - %d", versionName, versionCode));
		
		//  Fill attribution section
		AttributionItemAdapter attributionItemAdapter = new AttributionItemAdapter(context, attributionTitleList, attributionContentList);
		for (int index = 0; index < attributionTitleList.size(); index++) {
			attributionLinearLayout.addView(attributionItemAdapter.getView(index, null, null), index);
		}
		
		//	Set listeners
		versionTextView.setOnLongClickListener(v -> {
			Toast.makeText(context, versionType, Toast.LENGTH_LONG).show();
			return false;
		});
		
		devLogoImageView.setOnClickListener((v -> {
			Uri uri = Uri.parse("https://github.com/QGdev");
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(intent);
		}));
		
		exitButton.setOnClickListener(v -> dismiss());
	}
}
