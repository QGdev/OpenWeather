package fr.qgdev.openweather.fragment.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.dialog.AboutAppDialog;


public class SettingsFragment extends Fragment {

    private FragmentManager fragmentManager;
    private Button aboutUsButton;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        aboutUsButton = root.findViewById(R.id.about_us);

        fragmentManager = getChildFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, new CustomPreferenceFragment())
                .commit();

        this.aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AboutAppDialog aboutAppDialog = new AboutAppDialog(getContext());
                aboutAppDialog.build();
            }
        });

        return root;
    }
}

