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

package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.UiContext;

import java.util.List;

import fr.qgdev.openweather.R;


/**
 * AttributionItemAdapter
 * <p>
 * Used to generate attribution items in AboutUs section of the app.
 * But can be used like any other adapter.
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see android.widget.BaseAdapter
 */
public class AttributionItemAdapter extends BaseAdapter {
	
	private final List<String> attributionTitleList;
	private final List<String> attributionDescriptionList;
	private final LayoutInflater inflater;
	
	/**
	 * AttributionItemAdapter Constructor
	 * <p>
	 *     Just build an AttributionItemAdapter Object
	 * </p>
	 *
	 * @param context                       Current context, only used for LayoutInflater
	 * @param attributionTitleList          List of each attribution titles
	 * @param attributionDescriptionList    List of each attribution descriptions
	 */
	public AttributionItemAdapter(@UiContext Context context, List<String> attributionTitleList, List<String> attributionDescriptionList) {
		this.inflater = LayoutInflater.from(context);
		this.attributionTitleList = attributionTitleList;
		this.attributionDescriptionList = attributionDescriptionList;
	}
	
	/**
	 * getCount()
	 * <p>
	 *     UNIMPLEMENTED
	 * </p>
	 *
	 * @return Always return 0;
	 */
	@Override
	public int getCount() {
		return 0;
	}
	
	/**
	 * getItem(int position)
	 * <p>
	 * UNIMPLEMENTED
	 * </p>
	 *
	 * @param position Position of the item
	 * @return Always return null;
	 */
	@Override
	public Object getItem(int position) {
		return null;
	}
	
	/**
	 * getItemId()
	 * <p>
	 *     UNIMPLEMENTED
	 * </p>
	 *
	 * @param position Position of the item
	 *
	 * @return Always return 0;
	 */
	@Override
	public long getItemId(int position) {
		return 0;
	}
	
	
	/**
	 * getView(int position, View view, ViewGroup parent)
	 * <p>
	 *     Just build a view base on information of item at the position
	 * </p>
	 *
	 * @param position  Position of the item
	 * @param view      The view that will be inflated and completed with data
	 * @param parent    The parent view
	 *
	 * @return Built and completed view
	 */
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		view = inflater.inflate(R.layout.adapter_attribution_item, null);
		
		TextView attributionTitle = view.findViewById(R.id.attribution_title);
		TextView attributionContent = view.findViewById(R.id.attribution_content);
		
		attributionTitle.setText(this.attributionTitleList.get(position));
		attributionContent.setText(this.attributionDescriptionList.get(position));
		attributionContent.setClickable(true);
		
		return view;
	}
}
