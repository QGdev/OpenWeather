package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
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

		//  Get string arrays stored in XML resources files
		List<String> attributionTitleList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_title));
		List<String> attributionContentList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_content));

		//  Initialize variables containing application characteristics
		String versionName = BuildConfig.VERSION_NAME;
		String versionType = BuildConfig.BUILD_TYPE;
		int versionCode = BuildConfig.VERSION_CODE;

		//  Show application characteristics
		versionTextView.setText(String.format("%s - %d", versionName, versionCode));
		versionTextView.setOnLongClickListener(v -> {
			Toast.makeText(context, versionType, Toast.LENGTH_LONG).show();
			return false;
		});

		//  Fill attribution section
		AttributionItemAdapter attributionItemAdapter = new AttributionItemAdapter(context, attributionTitleList, attributionContentList);
		for (int index = 0; index < attributionTitleList.size(); index++) {
			attributionLinearLayout.addView(attributionItemAdapter.getView(index, null, null), index);
		}
		exitButton.setOnClickListener(v -> dismiss());
	}
}
