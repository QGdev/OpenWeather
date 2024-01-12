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

package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import fr.qgdev.openweather.R;


/**
 * AirQualityInfoDialog
 * <p>
 * Dialog to display information about air quality
 * and how to interpret the data.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see androidx.fragment.app.DialogFragment
 */
public class AirQualityInfoDialog extends DialogFragment {
	
	private static final String TAG = "AirQualityInfoDialog";
	private ImageView exitButton;
	
	/**
	 * public static void display(FragmentManager fragmentManager)
	 * <p>
	 * Display the dialog
	 * </p>
	 *
	 * @param fragmentManager FragmentManager
	 */
	public static void display(FragmentManager fragmentManager) {
		AirQualityInfoDialog dialog = new AirQualityInfoDialog();
		dialog.show(fragmentManager, TAG);
	}
	
	/**
	 * public void onCreate(Bundle savedInstanceState)
	 * <p>
	 * Called to do initial creation of a fragment.
	 * </p>
	 *
	 * @param savedInstanceState Bundle
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.MaterialTheme);
		
	}
	
	/**
	 * public void onStart()
	 * <p>
	 * Called when the Fragment is visible to the user.
	 * </p>
	 */
	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog != null) {
			int width = ViewGroup.LayoutParams.MATCH_PARENT;
			int height = ViewGroup.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setLayout(width, height);
		}
	}
	
	/**
	 * public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	 * <p>
	 * Called to have the fragment instantiate its user interface view.
	 * This is optional, and non-graphical fragments can return null (which
	 * is the default implementation).  This will be called between
	 * onCreate(Bundle) and onActivityCreated(Bundle).
	 * </p>
	 *
	 * @param inflater           The LayoutInflater object that can be used to inflate
	 *                           any views in the fragment,
	 * @param container          If non-null, this is the parent view that the fragment's
	 *                           UI should be attached to.  The fragment should not add the view itself,
	 *                           but this can be used to generate the LayoutParams of the view.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 *                           from a previous saved state as given here.
	 * @return Inflated view
	 */
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.dialog_air_quality_information, container, false);
		
		exitButton = view.findViewById(R.id.exit_button);
		return view;
	}
	
	/**
	 * public void onViewCreated(View view, Bundle savedInstanceState)
	 * <p>
	 * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * has returned, but before any saved state has been restored in to the view.
	 * </p>
	 *
	 * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
	 * @param savedInstanceState If non-null, this fragment is being re-constructed
	 *                           from a previous saved state as given here.
	 */
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		exitButton.setOnClickListener(v -> dismiss());
	}
}