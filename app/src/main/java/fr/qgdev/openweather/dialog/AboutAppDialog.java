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

package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import fr.qgdev.openweather.BuildConfig;
import fr.qgdev.openweather.R;


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
public class AboutAppDialog extends DialogFragment {
	
	private static final String TAG = "AboutAppDialog";
	private TextView versionTextView;
	private ImageView exitButton;
	private ImageView devLogoImageView;
	
	
	/**
	 * public static void display(FragmentManager fragmentManager)
	 * <p>
	 * Display the dialog
	 * </p>
	 *
	 * @param fragmentManager FragmentManager
	 */
	public static void display(FragmentManager fragmentManager) {
		AboutAppDialog dialog = new AboutAppDialog();
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
			if (dialog.getWindow() != null) {
				dialog.getWindow().setLayout(width, height);
			}
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
		LinearLayout attributionLinearLayout;
		super.onCreateView(inflater, container, savedInstanceState);
		
		View view = inflater.inflate(R.layout.dialog_about_us, container, false);
		Context context = view.getContext();
		
		versionTextView = view.findViewById(R.id.application_version);
		attributionLinearLayout = view.findViewById(R.id.attribution_section);
		exitButton = view.findViewById(R.id.exit_button);
		devLogoImageView = view.findViewById(R.id.dev_logo);
		
		//  Get string arrays stored in XML resources files
		List<String> attributionTitleList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_title));
		List<String> attributionContentList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_content));
		//  Needs to be the same size
		if (attributionTitleList.size() != attributionContentList.size()) {
			throw new IllegalArgumentException("Attribution title and content must have the same size");
		}
		
		
		//  Initialize variables containing application characteristics
		String versionName = BuildConfig.VERSION_NAME;
		int versionCode = BuildConfig.VERSION_CODE;
		
		//  Show application characteristics
		versionTextView.setText(String.format(Locale.US, "%s - %d", versionName, versionCode));
		
		//  Get margins from dimens.xml
		Resources resources = context.getResources();
		
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		titleParams.setMargins(resources.getDimensionPixelSize(R.dimen.text_marginStart_dialog_title),
				  resources.getDimensionPixelSize(R.dimen.text_marginTop_dialog_title),
				  resources.getDimensionPixelSize(R.dimen.text_marginEnd_dialog_title),
				  resources.getDimensionPixelSize(R.dimen.text_marginBottom_dialog_title));
		
		LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		contentParams.setMargins(resources.getDimensionPixelSize(R.dimen.text_marginStart_dialog_content),
				  resources.getDimensionPixelSize(R.dimen.text_marginTop_dialog_content),
				  resources.getDimensionPixelSize(R.dimen.text_marginEnd_dialog_content),
				  resources.getDimensionPixelSize(R.dimen.text_marginBottom_dialog_content));
		
		//  Fill attribution section
		for (int index = 0; index < attributionTitleList.size(); index++) {
			
			// Add margins to each text view in the view
			TextView titleTextView = new TextView(context);
			titleTextView.setLayoutParams(titleParams);
			titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_dialog_subtitle));
			titleTextView.setText(attributionTitleList.get(index));
			
			attributionLinearLayout.addView(titleTextView);
			
			TextView contentTextView = new TextView(context);
			contentTextView.setLayoutParams(contentParams);
			contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_dialog_content));
			contentTextView.setText(attributionContentList.get(index));
			contentTextView.setLinksClickable(true);
			contentTextView.setClickable(true);
			Linkify.addLinks(contentTextView, Linkify.WEB_URLS);
			
			attributionLinearLayout.addView(contentTextView);
		}
		
		//  Warn attributionLinearLayout that it has changed
		attributionLinearLayout.requestLayout();
		attributionLinearLayout.invalidate();
		
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
		
		Context context = view.getContext();
		String versionType = BuildConfig.BUILD_TYPE;
		
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

