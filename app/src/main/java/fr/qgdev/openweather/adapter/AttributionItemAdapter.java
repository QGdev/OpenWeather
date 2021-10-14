package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fr.qgdev.openweather.R;

public class AttributionItemAdapter extends BaseAdapter {

	private final Context context;
	private final List<String> attributionTitleList;
	private final List<String> attributionContentList;

	private final LayoutInflater inflater;

	public AttributionItemAdapter(Context context, List<String> attributionTitleList, List<String> attributionContentList) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.attributionTitleList = attributionTitleList;
		this.attributionContentList = attributionContentList;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		view = inflater.inflate(R.layout.adapter_attribution_item, null);

		TextView attributionTitle = view.findViewById(R.id.attribution_title);
		TextView attributionContent = view.findViewById(R.id.attribution_content);

		attributionTitle.setText(this.attributionTitleList.get(position));
		attributionContent.setText(this.attributionContentList.get(position));
		attributionContent.setClickable(true);

		return view;
	}
}
