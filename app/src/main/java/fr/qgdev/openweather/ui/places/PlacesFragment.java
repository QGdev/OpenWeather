package fr.qgdev.openweather.ui.places;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import java.util.concurrent.atomic.AtomicInteger;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.adapters.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.dialog.AddPlaceDialog;

public class PlacesFragment extends Fragment {

	private Context mContext;
	private String API_KEY;

	private TextView noPlacesRegisteredTextView;
	private static SwipeRefreshLayout swipeRefreshLayout;
	private FloatingActionButton addPlacesFab;
	private RecyclerView placeRecyclerView;
	private PlaceRecyclerViewAdapter placeRecyclerViewAdapter;

	private static ArrayList<Place> placeArrayList;
	private DataPlaces dataPlaces;

	private WeatherService weatherService;
	private WeatherService.WeatherCallback getPlaceListCallback;

	private static AtomicInteger refreshCounter;


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
		noPlacesRegisteredTextView = root.findViewById(R.id.no_places_registered);
		swipeRefreshLayout = root.findViewById(R.id.swiperefresh);
		addPlacesFab = root.findViewById(R.id.add_places);

		//  Initialize places data storage
		dataPlaces = new DataPlaces(mContext);

		//  Initialize the list and the ArrayList where all places registered
		placeArrayList = new ArrayList<>();
		placeRecyclerView = root.findViewById(R.id.place_list);
		refreshCounter = new AtomicInteger();
		refreshCounter.set(0);


		//  Initialisation of the RecycleView
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

		placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(mContext, placeArrayList, new PlaceRecyclerViewAdapter.ActionCallback() {
			@Override
			public void onPlaceDeletion(int position) {
				String dataPlaceName = placeArrayList.get(position).getCity().toUpperCase() + '/' + placeArrayList.get(position).getCountryCode();
				dataPlaces.deletePlace(dataPlaceName);
				placeArrayList.remove(position);

				if (!placeArrayList.isEmpty()) {
					noPlacesRegisteredTextView.setVisibility(View.GONE);
					swipeRefreshLayout.setVisibility(View.VISIBLE);

				} else {
					noPlacesRegisteredTextView.setVisibility(View.VISIBLE);
					swipeRefreshLayout.setVisibility(View.GONE);
				}
			}
		});

		placeRecyclerView.setLayoutManager(layoutManager);
		placeRecyclerView.setAdapter(placeRecyclerViewAdapter);

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
						int nbPlacesBefore = placeRecyclerViewAdapter.getItemCount();

						//  New Place has been added
						if (nbPlacesBefore < index + 1) {
							placeRecyclerViewAdapter.add(place);
							placeArrayList.add(place);
						}
						//  Nothing new
						else {
							placeRecyclerViewAdapter.set(index, place);
							placeArrayList.set(index, place);
						}

						noPlacesRegisteredTextView.setVisibility(View.GONE);
						swipeRefreshLayout.setVisibility(View.VISIBLE);

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

				// TEST
				if(placeArrayList.size() == refreshCounter.incrementAndGet()){
					swipeRefreshLayout.setRefreshing(false);
					refreshCounter.set(0);
				}

			}

			@Override
			public void onError(Exception exception, Place place, DataPlaces dataPlaces) {
				Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list_network), Snackbar.LENGTH_LONG)
						.setAnimationMode(Snackbar.ANIMATION_MODE_FADE).setMaxInlineActionWidth(3)
						.show();
				exception.printStackTrace();

				// TEST
				if(placeArrayList.size() == refreshCounter.incrementAndGet()){
					swipeRefreshLayout.setRefreshing(false);
					refreshCounter.set(0);
				}
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

				// TEST
				if(placeArrayList.size() == refreshCounter.incrementAndGet()){
					swipeRefreshLayout.setRefreshing(false);
					refreshCounter.set(0);
				}

			}
		};


		//  Button and action Listeners
		//________________________________________________________________
		//
		//  Initialize buttons and actions behavior


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
				() -> {
					swipeRefreshLayout.setRefreshing(true);
					if (API_KEY != null && !Objects.equals(API_KEY, "")) {
						try {
							placeArrayList.clear();
							placeArrayList.addAll(dataPlaces.getPlaces());
							placeRecyclerViewAdapter.remplaceSet(placeArrayList);

							for (Place place : placeArrayList) {
								weatherService.getWeatherDataOWM(place, getPlaceListCallback);
							}
							//swipeRefreshLayout.setRefreshing(false);
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
		);

		//  Display data about places
		try {
			//  Show current saved data
			placeArrayList.clear();
			placeArrayList.addAll(dataPlaces.getPlaces());
			placeRecyclerViewAdapter.remplaceSet(placeArrayList);

			if (!placeArrayList.isEmpty()) {
				noPlacesRegisteredTextView.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);

				//  Refresh data
				for (Place place : placeArrayList) {
					weatherService.getWeatherDataOWM(place, getPlaceListCallback);
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