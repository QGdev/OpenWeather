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
 * @version 1.0.0
 * @see androidx.fragment.app.DialogFragment
 * @since 0.9.3
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