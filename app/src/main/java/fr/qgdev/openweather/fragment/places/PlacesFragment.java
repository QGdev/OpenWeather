package fr.qgdev.openweather.fragment.places;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.dataplaces.DataPlaces;
import fr.qgdev.openweather.dialog.AddPlaceDialog;

public class PlacesFragment extends Fragment {

	private Context mContext;
	private String API_KEY;

	private TextView noPlacesRegisteredTextView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private RecyclerView placeRecyclerView;
	private PlaceRecyclerViewAdapter placeRecyclerViewAdapter;
	private static DataPlaces dataPlaces;
	private PlacesFragment.Interactions interactions;
	private static ArrayList<Place> placeArrayList;

	private WeatherService weatherService;
	private WeatherService.CallbackGetData getDataPlaceListCallback;

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
		if (weatherService != null) weatherService.cancel();
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
		FloatingActionButton addPlacesFab = root.findViewById(R.id.add_places);

		//  Initialize places data storage
		dataPlaces = new DataPlaces(mContext);
		placeArrayList = new ArrayList<>();

		try {
			placeArrayList.addAll(dataPlaces.getAllPlacesStored());
		} catch (Exception e) {
			e.printStackTrace();
			placeArrayList.clear();
		}


		//  Initialize the list and the ArrayList where all places registered
		placeRecyclerView = root.findViewById(R.id.place_list);
		refreshCounter = new AtomicInteger();
		refreshCounter.set(0);


		//	Initialize Fragment Interactions
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

					placeRecyclerViewAdapter.remove(position);

					if (placeArrayList.isEmpty()) {
						noPlacesRegisteredTextView.setVisibility(View.VISIBLE);
						swipeRefreshLayout.setVisibility(View.GONE);
					}

				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
					showSnackbar(container, mContext.getString(R.string.error_place_deletion));
				}
			}

			@Override
			public void onAddingPlace(Place place) {
				if (placeArrayList.isEmpty()) {
					noPlacesRegisteredTextView.setVisibility(View.GONE);
					swipeRefreshLayout.setVisibility(View.VISIBLE);
				}

				placeArrayList.add(place);
				placeRecyclerViewAdapter.add(placeArrayList.size() - 1);
			}

			@Override
			public void onPlaceUpdate(DataPlaces dataPlaces, int position, Place place) {
				placeArrayList.set(position, place);
				placeRecyclerViewAdapter.notifyItemChanged(position);
			}

			@Override
			public void onMovedPlace(DataPlaces dataPlaces, int initialPosition, int finalPosition) {
				try {
					dataPlaces.movePlace(initialPosition, finalPosition);
				} catch (Exception e) {
					e.printStackTrace();
					showSnackbar(container, mContext.getString(R.string.error_place_move));
				}
			}
		};


		//  Initialisation of the RecyclerView
		//________________________________________________________________
		//
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

		placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(mContext, this);
		placeRecyclerView.setLayoutManager(layoutManager);
		placeRecyclerView.setAdapter(placeRecyclerViewAdapter);

		//	Used to manage drag & drop reorganisation of place cards and swipe to delete
		//
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {

			int _initialPosition;
			boolean itemAsBeenMoved = false;

			@Override
			public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
				//	If the holder is an other view type than COMPACT or if there is no items in the recyclerView, swipe and drag&drop are disabled
				if (recyclerView.getChildCount() == 0 || viewHolder.getItemViewType() != 0)
					return 0;
				//	When there is only one item, drag&drop will be disabled
				if (recyclerView.getChildCount() < 2)
					return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
				//	When it is in COMPACT view type, swipe and drag&drop are enabled
				return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
			}

			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				if (!itemAsBeenMoved) {
					_initialPosition = viewHolder.getAbsoluteAdapterPosition();
					itemAsBeenMoved = true;
				}
				int initialPosition = viewHolder.getAbsoluteAdapterPosition();
				int finalPosition = target.getAbsoluteAdapterPosition();

				Collections.swap(placeArrayList, initialPosition, finalPosition);
				placeRecyclerViewAdapter.notifyItemMoved(initialPosition, finalPosition);

				return false;
			}

			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
					int index = viewHolder.getLayoutPosition();
					Place place = placeArrayList.get(index);
					new AlertDialog.Builder(mContext)
							.setTitle(mContext.getString(R.string.dialog_confirmation_title_delete_place))
							.setMessage(String.format(mContext.getString(R.string.dialog_confirmation_message_delete_place), place.getCity(), place.getCountryCode()))
							.setPositiveButton(mContext.getString(R.string.dialog_confirmation_choice_yes), (dialog, which) -> interactions.onPlaceDeletion(dataPlaces, index))
							.setNegativeButton(mContext.getString(R.string.dialog_confirmation_choice_no), (dialogInterface, i) -> placeRecyclerViewAdapter.notifyItemChanged(index))
							.setCancelable(false)
							.show();
				}
			}

			//	This handles the ending of the drag & drop feature
			@Override
			public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
				super.clearView(recyclerView, viewHolder);
				if (itemAsBeenMoved)
					interactions.onMovedPlace(dataPlaces, _initialPosition, viewHolder.getAbsoluteAdapterPosition());
				itemAsBeenMoved = false;
			}
		});
		itemTouchHelper.attachToRecyclerView(placeRecyclerView);


		//  Get API key
		SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		API_KEY = apiKeyPref.getString("api_key", null);

		if (API_KEY != null) {
			//  Initialize Weather Services and callbacks
			weatherService = new WeatherService(mContext, API_KEY, mContext.getResources().getConfiguration().getLocales().get(0).getLanguage(), dataPlaces);
		}

		//  acquire data of the places callback
		getDataPlaceListCallback = new WeatherService.CallbackGetData() {

			@Override
			public void onTreatmentError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_cannot_refresh_place_list_network));
			}

			@Override
			public void onNoResponseError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_server_unreachable));
			}

			@Override
			public void onTooManyRequestsError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_too_many_request_in_a_day));
			}

			@Override
			public void onPlaceNotFoundError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_place_not_found));
			}

			@Override
			public void onWrongOrUnknownApiKeyError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_wrong_api_key));
			}

			@Override
			public void onUnknownError(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_unknow_error));
			}

			@Override
			public void onDeviceNotConnected(RequestStatus requestStatus) {
				showSnackbar(container, mContext.getString(R.string.error_device_not_connected));
			}

			@Override
			public void onTheEndOfTheRequest(Place place, DataPlaces dataPlaces, RequestStatus requestStatus) {
				if (requestStatus != RequestStatus.WEATHER_REQUEST_FAIL) {
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
								weatherService.getWeatherDataOWM(place, dataPlaces, getDataPlaceListCallback);
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


		//	Initialisation the UI part and refreshing data of each places
		try {
			if (!placeArrayList.isEmpty()) {
				noPlacesRegisteredTextView.setVisibility(View.GONE);
				swipeRefreshLayout.setVisibility(View.VISIBLE);

				for (Place place : placeArrayList) {
					weatherService.getWeatherDataOWM(place, dataPlaces, getDataPlaceListCallback);
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

	public Place getPlace(int index) {
		return placeArrayList.get(index);
	}

	public int getPlaceArrayListSize() {
		return placeArrayList.size();
	}

	public interface Interactions {
		void onPlaceDeletion(DataPlaces dataPlaces, int position);

		void onAddingPlace(Place place);

		void onPlaceUpdate(DataPlaces dataPlaces, int position, Place place);

		void onMovedPlace(DataPlaces dataPlaces, int initialPosition, int finalPosition);
	}
}