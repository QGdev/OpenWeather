package fr.qgdev.openweather.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.logging.Logger;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.dialog.AboutAppDialog;


public class SettingsFragment extends Fragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private final Logger logger = Logger.getLogger(TAG);

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

