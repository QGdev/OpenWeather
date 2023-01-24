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

public class CustomPreferenceFragment extends PreferenceFragmentCompat {
	
	@NonNull
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View root = super.onCreateView(inflater, container, savedInstanceState);
		RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
		
		recyclerView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.recycler_view_bottom_padding));
		recyclerView.setClipToPadding(false);
		
		return root;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		PreferenceManager preferenceManager = getPreferenceManager();
		preferenceManager.setPreferenceDataStore(new SecuredPreferenceDataStore(getContext()));
		addPreferencesFromResource(R.xml.settings_preferences);
	}
}
