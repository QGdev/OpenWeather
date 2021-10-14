package fr.qgdev.openweather.fragment.places;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.dataplaces.DataPlaces;
import fr.qgdev.openweather.dialog.AddPlaceDialog;

public class PlacesFragment extends Fragment {

	private static Context mContext;
	private String API_KEY;

	private TextView noPlacesRegisteredTextView;
	private static SwipeRefreshLayout swipeRefreshLayout;
	private FloatingActionButton addPlacesFab;
	private RecyclerView placeRecyclerView;
	private PlaceRecyclerViewAdapter placeRecyclerViewAdapter;
	private static DataPlaces dataPlaces;
	private PlacesFragment.Interactions interactions;
	private static ArrayList<Place> placeArrayList;

	private WeatherService weatherService;
	private WeatherService.WeatherCallbackGetData getPlaceListCallback;

	private static AtomicInteger refreshCounter;

	private void showSnackbar(View view, String message) {
		Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
				.setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
				.setMaxInlineActionWidth(3)
				.show();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		weatherService.cancel();
		mContext = null;
	}


	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mContext = context;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		placeRecyclerView.destroyDrawingCache();
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
		placeArrayList = new ArrayList<Place>();

		try {
			placeArrayList.addAll(dataPlaces.getAllPlacesStored());
		} catch (Exception e) {
			e.printStackTrace();
		}


		//  Initialize the list and the ArrayList where all places registered
		placeRecyclerView = root.findViewById(R.id.place_list);
		refreshCounter = new AtomicInteger();
		refreshCounter.set(0);


		this.interactions = new Interactions() {
			@Override
			public void onPlaceDeletion(DataPlaces dataPlaces, int position) {
				try {
					if (dataPlaces.deletePlace(position)) {
						placeArrayList.remove(position);
						showSnackbar(container, mContext.getString(R.string.place_deletion_successful));
					} else {
						showSnackbar(container, mContext.getString(R.string.error_place_deletion));
					}

					if (placeArrayList.isEmpty()) {
						noPlacesRegisteredTextView.setVisibility(View.VISIBLE);
						swipeRefreshLayout.setVisibility(View.GONE);

					}

				} catch (ArrayIndexOutOfBoundsException e) {
					showSnackbar(container, mContext.getString(R.string.error_place_deletion));
				}
			}

			@Override
			public void onAddingPlace(DataPlaces dataPlaces, int position, Place place) {
				if (placeArrayList.isEmpty()) {
					noPlacesRegisteredTextView.setVisibility(View.GONE);
					swipeRefreshLayout.setVisibility(View.VISIBLE);
				}

				placeArrayList.add(place);
				placeRecyclerViewAdapter.add(position);
			}

			@Override
			public void onPlaceUpdate(DataPlaces dataPlaces, int position, Place place) {
				placeArrayList.set(position, place);
				placeRecyclerViewAdapter.notifyItemChanged(position);
			}
		};


		//  Initialisation of the RecycleView
		//________________________________________________________________
		//
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

		placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(mContext, this, this.interactions, dataPlaces);
		placeRecyclerView.setLayoutManager(layoutManager);
		placeRecyclerView.setAdapter(placeRecyclerViewAdapter);

		//  Get API key
		SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		API_KEY = apiKeyPref.getString("api_key", null);


		//  Initialize Weather Services and callbacks
		weatherService = new WeatherService(mContext, API_KEY, mContext.getResources().getConfiguration().getLocales().get(0).getLanguage(), dataPlaces);

		//  acquire data of the places callback
		getPlaceListCallback = new WeatherService.WeatherCallbackGetData() {
			@Override
			public void onWeatherData(final Place place, DataPlaces dataPlaces) {
				try {
					if (dataPlaces.updatePlace(place)) {
						interactions.onPlaceUpdate(dataPlaces, dataPlaces.getPlacePositionInRegister(place), place);

						if (noPlacesRegisteredTextView.getVisibility() == View.VISIBLE) {
							noPlacesRegisteredTextView.setVisibility(View.GONE);
							swipeRefreshLayout.setVisibility(View.VISIBLE);
						}

					} else {
						showSnackbar(container, mContext.getString(R.string.error_cannot_refresh_place_list));
					}
				} catch (Exception e) {
					e.printStackTrace();
					showSnackbar(container, mContext.getString(R.string.error_cannot_refresh_place_list));
				}
			}

			@Override
			public void onTreatmentError() {
				showSnackbar(container, mContext.getString(R.string.error_cannot_refresh_place_list_network));
			}

			@Override
			public void onNoResponseError() {
				showSnackbar(container, mContext.getString(R.string.error_server_unreachable));
			}

			@Override
			public void onTooManyRequestsError() {
				showSnackbar(container, mContext.getString(R.string.error_too_many_request_in_a_day));
			}

			@Override
			public void onPlaceNotFoundError() {
				showSnackbar(container, mContext.getString(R.string.error_place_not_found));
			}

			@Override
			public void onWrongOrUnknownApiKeyError() {
				showSnackbar(container, mContext.getString(R.string.error_wrong_api_key));
			}

			@Override
			public void onUnknownError() {
				showSnackbar(container, mContext.getString(R.string.error_unknow_error));
			}

			@Override
			public void onDeviceNotConnected() {
				showSnackbar(container, mContext.getString(R.string.error_device_not_connected));
			}

			@Override
			public void onTheEndOfTheRequest() {
				if (placeArrayList.size() == refreshCounter.incrementAndGet()) {
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
					final AddPlaceDialog addPlaceDialog = new AddPlaceDialog(mContext, placeFabView, this.API_KEY, this.weatherService, this.interactions);
					addPlaceDialog.build();
				});


		//  Swipe to refresh listener
		swipeRefreshLayout.setOnRefreshListener(
				() -> {
					swipeRefreshLayout.setRefreshing(true);
					if (API_KEY != null && !Objects.equals(API_KEY, "")) {
						try {
							for (Place place : placeArrayList) {
								weatherService.getWeatherDataOWM(place, dataPlaces, getPlaceListCallback);
							}

						} catch (Exception e) {
							showSnackbar(container, mContext.getString(R.string.error_cannot_refresh_place_list));
						}
					} else {
						swipeRefreshLayout.setRefreshing(false);
						showSnackbar(container, mContext.getString(R.string.error_no_api_key_registered));
					}
				}
		);


		try {
			if (!placeArrayList.isEmpty()) {
				noPlacesRegisteredTextView.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);

				for (Place place : placeArrayList) {
					weatherService.getWeatherDataOWM(place, dataPlaces, getPlaceListCallback);
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

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	public Place getPlace(int index) {
		return placeArrayList.get(index);
	}

	public int getPlaceArrayListSize() {
		return placeArrayList.size();
	}

	public TimeZone getTimeZonePlaceInArray(int index) {
		return placeArrayList.get(index).getTimeZone();
	}

	public interface Interactions {
		void onPlaceDeletion(DataPlaces dataPlaces, int position);

		void onAddingPlace(DataPlaces dataPlaces, int position, Place place);

		void onPlaceUpdate(DataPlaces dataPlaces, int position, Place place);
	}
}