package fr.qgdev.openweather.ui.places;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.adapters.PlaceAdapter;
import fr.qgdev.openweather.dialog.AddPlaceDialog;

public class PlacesFragment extends Fragment {

	private Context mContext;
	private String API_KEY;
	private RecyclerView placeList;
	private PlaceAdapter placeAdapter;

	private ArrayList<Place> placeArrayList;
	private DataPlaces dataPlaces;

	private WeatherService weatherService;
	private WeatherService.WeatherCallback getPlaceListCallback;


	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mContext = null;
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	@SuppressLint("WrongThread")
	@UiThread
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         ViewGroup container, Bundle savedInstanceState) {

		View root = inflater.inflate(R.layout.fragment_places, container, false);

		//  Initialize places data storage
		dataPlaces = new DataPlaces(mContext);


		//  Initialize the list and the ArrayList where all places registered
		placeArrayList = new ArrayList<>();
		placeList = root.findViewById(R.id.place_list);


		//  Initialisation of the RecycleView
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		placeAdapter = new PlaceAdapter(mContext, placeArrayList);
		placeList.setLayoutManager(layoutManager);
		placeList.setAdapter(placeAdapter);


		//  Get API key
		SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		API_KEY = apiKeyPref.getString("api_key", null);


		//  Initialize Weather Services and callbacks
		weatherService = new WeatherService(mContext, API_KEY, mContext.getResources().getConfiguration().getLocales().get(0).getLanguage(), dataPlaces);

		//  acquire data of the places callback
		getPlaceListCallback = new WeatherService.WeatherCallback() {
			@Override
			public void onWeatherData(final Place place, DataPlaces dataPlaces) {
				try {
					if (dataPlaces.savePlace(place)) {
						int index = dataPlaces.getPlacePositionInRegister(place);
						int nbPlacesBefore = placeArrayList.size();

						//  New Place has been added
						if (nbPlacesBefore < index + 1) {
							placeArrayList.add(place);
							placeAdapter.notifyItemInserted(index);
							placeAdapter.notifyItemRangeChanged(0, index + 1);
						}
						//  Nothing new
						else {
							placeArrayList.set(index, place);
							placeAdapter.notifyItemChanged(index);
						}
					} else {
						Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG)
								.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
								.show();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG)
							.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
							.show();
				}
			}

			@Override
			public void onError(Exception exception, Place place, DataPlaces dataPlaces) {
				Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list_network), Snackbar.LENGTH_LONG)
						.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
						.show();
				exception.printStackTrace();
			}

			@Override
			public void onConnectionError(VolleyError error, Place place, DataPlaces dataPlaces) {

				//  no server response (NO INTERNET or SERVER DOWN)
				if (error.networkResponse == null) {
					Snackbar.make(container, mContext.getString(R.string.error_server_unreachable), Snackbar.LENGTH_LONG)
							.setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
							.show();
				}
				//  Server response
				else {
					place.setErrorDuringDataAcquisition(true);
					place.setErrorCode(error.networkResponse.statusCode);
					switch (place.getErrorCode()) {
						case 429:   //  Too many requests
							Snackbar.make(container, mContext.getString(R.string.error_too_many_request_in_a_day), Snackbar.LENGTH_LONG)
									.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
									.show();
							break;
						case 404:   //  Place not found
							Snackbar.make(container, mContext.getString(R.string.error_place_not_found), Snackbar.LENGTH_LONG)
									.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
									.show();
							break;
						case 401:   //  Unknown or wrong API key
							Snackbar.make(container, mContext.getString(R.string.error_wrong_api_key), Snackbar.LENGTH_LONG)
									.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
									.show();
							break;
						default:    //  Unknown error
							Snackbar.make(container, mContext.getString(R.string.error_unknow_error), Snackbar.LENGTH_LONG)
									.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
									.show();
							break;
					}
				}
			}
		};


		//  Button and action Listeners
		//________________________________________________________________
		//
		//  Initialize buttons and actions behavior
		SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swiperefresh);
		TextView noPlacesRegisteredTextView = root.findViewById(R.id.no_places_registered);
		FloatingActionButton addPlacesFab = getActivity().findViewById(R.id.add_places);

		//  No API key registered
		if (API_KEY == null || API_KEY.length() != 32) {
			//  Hide the Floating Action Button to add Places
			addPlacesFab.setVisibility(View.GONE);
		} else {
			//  Show the Floating Action Button to add Places
			addPlacesFab.setVisibility(View.VISIBLE);
		}

		//  A simple click to add a place
		addPlacesFab.setOnClickListener(
				placeFabView -> {
					final AddPlaceDialog addPlaceDialog = new AddPlaceDialog(mContext, placeFabView, API_KEY, weatherService, getPlaceListCallback);
					addPlaceDialog.build();
				});

		//  Swipe to refresh listener
		swipeRefreshLayout.setOnRefreshListener(
				new SwipeRefreshLayout.OnRefreshListener() {
					@Override
					public void onRefresh() {
						swipeRefreshLayout.setRefreshing(true);
						if (API_KEY != null && !Objects.equals(API_KEY, "")) {
							try {
								placeArrayList.clear();
								placeArrayList.addAll(dataPlaces.getPlaces());

								for (Place place : placeArrayList) {
									weatherService.getWeatherDataOWM(place, getPlaceListCallback);
								}
								swipeRefreshLayout.setRefreshing(false);
							} catch (Exception e) {
								Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
										.show();
								e.printStackTrace();
							}
						} else {
							swipeRefreshLayout.setRefreshing(false);
							Snackbar.make(container, mContext.getString(R.string.error_no_api_key_registered), Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
									.show();
						}
					}
				}
		);

		//  Display data about places
		try {
			//  Show current saved data
			placeArrayList.clear();
			placeArrayList.addAll(dataPlaces.getPlaces());
			placeAdapter.notifyDataSetChanged();

			if (!placeArrayList.isEmpty()) {
				noPlacesRegisteredTextView.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);

				//  Refresh data
				for (Place place : placeArrayList) {
					weatherService.getWeatherDataOWM(place, getPlaceListCallback);
					placeAdapter.notifyDataSetChanged();
				}

			} else {
				noPlacesRegisteredTextView.setVisibility(View.VISIBLE);
				swipeRefreshLayout.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return root;
	}
}