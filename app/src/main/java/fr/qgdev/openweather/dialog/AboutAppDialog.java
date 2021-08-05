package fr.qgdev.openweather.dialog;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.qgdev.openweather.BuildConfig;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapters.AttributionItemAdapter;

public class AboutAppDialog extends Dialog {

	private final TextView versionTextView;
	private final LinearLayout attributionLinearLayout;
	private final List<String> attributionTitleList;
	private final List <String> attributionContentList;
	private AttributionItemAdapter attributionItemAdapter;
	private final Button exitButton;

	public AboutAppDialog(Context context) {
		super(context);
		setContentView(R.layout.dialog_about_us);

		this.versionTextView = findViewById(R.id.application_version);
		this.attributionLinearLayout = findViewById(R.id.attribution_section);
		this.exitButton = findViewById(R.id.exit_button);

		//  Get string arrays stored in XML resources files
		this.attributionTitleList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_title));
		this.attributionContentList = Arrays.asList(context.getResources().getStringArray(R.array.attribution_content));

		//  Initialize variables containing application characteristics
		String versionName = BuildConfig.VERSION_NAME;
		String versionType = BuildConfig.BUILD_TYPE;
		int versionCode = BuildConfig.VERSION_CODE;

		//  Show application characteristics
		this.versionTextView.setText(String.format("%s - %d", versionName, versionCode));
		this.versionTextView.setOnLongClickListener(v -> {
			Toast.makeText(context, versionType, Toast.LENGTH_LONG).show();
			return false;
		});

		//  Fill attribution section
		AttributionItemAdapter attributionItemAdapter = new AttributionItemAdapter(context, this.attributionTitleList, this.attributionContentList);
		for(int index = 0; index < this.attributionTitleList.size(); index++)
		{
			this.attributionLinearLayout.addView(attributionItemAdapter.getView(index, null, null), index);
		}


		this.exitButton.setOnClickListener(v -> dismiss());
	}

	public void build() {
		show();
	}

}
