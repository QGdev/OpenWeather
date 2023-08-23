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

package fr.qgdev.openweather.fragment.places;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import fr.qgdev.openweather.R;
import fr.qgdev.openweather.adapter.PlaceRecyclerViewAdapter;
import fr.qgdev.openweather.dialog.AddPlaceDialog;
import fr.qgdev.openweather.repositories.AppRepository;
import fr.qgdev.openweather.repositories.places.Place;
import fr.qgdev.openweather.repositories.weather.FetchCallback;
import fr.qgdev.openweather.repositories.weather.RequestStatus;

/**
 * The type Places fragment.
 */
public class PlacesFragment extends Fragment {
	
	private static final String TAG = PlacesFragment.class.getSimpleName();
	private final Logger logger = Logger.getLogger(TAG);
	
	private Context mContext;
	
	private Vibrator vibrator;
	
	private AppRepository appRepository;
	private PlacesViewModel placesViewModel;
	
	private TextView informationTextView;
	private SwipeRefreshLayout swipeRefreshLayout;
	private FloatingActionButton addPlacesFab;
	private RecyclerView placeRecyclerView;
	private PlaceRecyclerViewAdapter placeRecyclerViewAdapter;
	
	private FetchCallback fetchUpdateCallback;
	
	private AtomicInteger refreshCounter;
	
	/**
	 * Show snackbar.
	 *
	 * @param view    the view
	 * @param message the message
	 */
	public void showSnackbar(View view, String message) {
		Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
				  .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
				  .setMaxInlineActionWidth(3)
				  .show();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mContext = null;
	}
	
	/**
	 * Called when the fragment is visible to the user and actively running.
	 * This is generally
	 * tied to {@link Activity#onResume() Activity.onResume} of the containing
	 * Activity's lifecycle.
	 */
	@Override
	public void onResume() {
		super.onResume();
		appRepository.getFormattingService().update();
		placeRecyclerViewAdapter.notifyDataSetChanged();
	}
	
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		mContext = context;
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		appRepository = AppRepository.getInstance(context.getApplicationContext());
		placesViewModel = PlacesViewModelFactory.getInstance();
		placeRecyclerViewAdapter = new PlaceRecyclerViewAdapter(mContext, placesViewModel, appRepository.getFormattingService());
		
		//
		appRepository.getPlacesLiveData().observeForever(places -> {
			if (places == null) return;
			placesViewModel.setPlaces(places);
		});
	}
	
	
	@Override
	@UiThread
	public View onCreateView(@NonNull LayoutInflater inflater,
									 ViewGroup container,
									 Bundle savedInstanceState) {
		
		View root = inflater.inflate(R.layout.fragment_places, container, false);
		
		informationTextView = root.findViewById(R.id.information_textview);
		swipeRefreshLayout = root.findViewById(R.id.swipe_refresh);
		addPlacesFab = root.findViewById(R.id.add_places);
		
		placeRecyclerView = root.findViewById(R.id.place_list);
		
		refreshCounter = new AtomicInteger();
		refreshCounter.set(0);
		
		//  Initialisation of the RecyclerView
		//________________________________________________________________
		//
		
		LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		placeRecyclerView.setLayoutManager(layoutManager);
		placeRecyclerView.setAdapter(placeRecyclerViewAdapter);
		
		
		placesViewModel.getPlacesLiveData().observe(getViewLifecycleOwner(), places -> {
			if (places == null) return;
			if (places.isEmpty()) {
				setNoPlacesViewState();
				return;
			}
			
			if (!placesViewModel.hasDataAlreadyBeenUpdated() && appRepository.isApiKeyValid()) {
				appRepository.updateAllPlaces(fetchUpdateCallback);
				placesViewModel.dataHasBeenUpdated();
			} else {
				placeRecyclerViewAdapter.notifyDataSetChanged();
			}
			
			setExistingPlacesViewState(container);
		});
		
		
		//	Used to manage drag & drop reorganisation of place cards and swipe to delete
		//
		ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
			
			int initialPosition;
			boolean itemAsBeenMoved = false;
			
			@Override
			public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
				//	If the holder is an other view type than COMPACT or if there is no items in the recyclerView, swipe and drag&drop are disabled
				if (recyclerView.getChildCount() == 0 || viewHolder.getItemViewType() != 0) {
					return makeMovementFlags(0, 0);
				}
				//	When there is only one item, drag&drop will be disabled
				if (recyclerView.getChildCount() < 2) {
					return makeMovementFlags(0, ItemTouchHelper.START | ItemTouchHelper.END);
				}
				//	When it is in COMPACT view type, swipe and drag&drop are enabled
				return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
			}
			
			@Override
			public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
				if (!itemAsBeenMoved) {
					initialPosition = viewHolder.getAbsoluteAdapterPosition();
					itemAsBeenMoved = true;
				}
				int initialPos = viewHolder.getAbsoluteAdapterPosition();
				int finalPos = target.getAbsoluteAdapterPosition();
				
				appRepository.movePlace(initialPos,
						  finalPos,
						  null);
				placeRecyclerViewAdapter.notifyItemMoved(initialPos, finalPos);
				return true;
			}
			
			@Override
			public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
				if (direction == ItemTouchHelper.START || direction == ItemTouchHelper.END) {
					int index = viewHolder.getLayoutPosition();
					Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
					
					if (vibrator != null) {
						vibrator.vibrate(VibrationEffect.createOneShot(100, 25));
					}
					
					if (index >= placesViewModel.getPlaces().size()) {
						placeRecyclerViewAdapter.notifyItemRemoved(index);
						return;
					}
					Place place = placesViewModel.getPlaces().get(index);
					new AlertDialog.Builder(mContext)
							  .setTitle(mContext.getString(R.string.dialog_confirmation_title_delete_place))
							  .setMessage(String.format(mContext.getString(R.string.dialog_confirmation_message_delete_place), place.getGeolocation().getCity(), place.getGeolocation().getCountryCode()))
							  .setPositiveButton(mContext.getString(R.string.dialog_confirmation_choice_yes), (dialogInterface, i) -> {
								  appRepository.delete(place,
											 () -> placeRecyclerViewAdapter.notifyItemRemoved(index));
							  })
							  .setNegativeButton(mContext.getString(R.string.dialog_confirmation_choice_no), (dialogInterface, i) -> placeRecyclerViewAdapter.notifyItemChanged(index))
							  .setCancelable(false)
							  .show();
				}
			}
			
			@Override
			public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
				
				if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
					Paint paintSwipeDelete = new Paint();
					Rect backgroundRect;
					int halfScreenWidth = recyclerView.getMeasuredWidth();
					paintSwipeDelete.setColor(getResources().getColor(R.color.colorRedError, null));
					
					Drawable binDrawable = AppCompatResources.getDrawable(mContext, R.drawable.ic_trash_can);
					//  Set Color and Dimensions of the drawable and print it on canvas
					binDrawable.setTint(paintSwipeDelete.getColor());
					
					if (Math.abs(dX) > 0.1) {
						if (dX > 0) {
							backgroundRect = new Rect(viewHolder.itemView.getLeft(), viewHolder.itemView.getTop(), viewHolder.itemView.getLeft() + (int) dX, viewHolder.itemView.getBottom());
						} else {
							backgroundRect = new Rect(viewHolder.itemView.getRight(), viewHolder.itemView.getTop(), viewHolder.itemView.getRight() + (int) dX, viewHolder.itemView.getBottom());
						}
						
						binDrawable.setBounds(backgroundRect.centerX() - 40, backgroundRect.centerY() - 40,
								  backgroundRect.centerX() + 40, backgroundRect.centerY() + 40);
						
						binDrawable.setAlpha((int) (Math.abs(dX) * 0.75F));
						viewHolder.itemView.setAlpha((halfScreenWidth - Math.abs(dX)) / halfScreenWidth);
						
						binDrawable.draw(c);
					}
				}
			}
			
			
			//	This handles the ending of the drag & drop feature
			@Override
			public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
				super.clearView(recyclerView, viewHolder);
				itemAsBeenMoved = false;
				viewHolder.itemView.setAlpha(1);
			}
		});
		
		itemTouchHelper.attachToRecyclerView(placeRecyclerView);
		
		//  acquire data of the places callback
		fetchUpdateCallback = new FetchCallback() {
			
			@Override
			public void onSuccess() {
				if (placesViewModel.getPlaces().size() == refreshCounter.incrementAndGet()) {
					swipeRefreshLayout.setRefreshing(false);
					refreshCounter.set(0);
					appRepository.getWidgetsManager().updateWidgets(mContext);
					
					vibrator.vibrate(VibrationEffect.createOneShot(100, 25));
				}
			}
			
			@Override
			public void onError(RequestStatus requestStatus) {
				switch (requestStatus) {
					case NO_ANSWER:
						showSnackbar(container, mContext.getString(R.string.error_server_unreachable));
						break;
					case NOT_CONNECTED:
						showSnackbar(container, mContext.getString(R.string.error_device_not_connected));
						break;
					case TOO_MANY_REQUESTS:
						showSnackbar(container, mContext.getString(R.string.error_too_many_request_in_a_day));
						break;
					case AUTH_FAILED:
						showSnackbar(container, mContext.getString(R.string.error_wrong_api_key));
						break;
					case NOT_FOUND:
						showSnackbar(container, mContext.getString(R.string.error_place_not_found));
						break;
					default:
						showSnackbar(container, mContext.getString(R.string.error_unknown_error));
						break;
				}
				if (placesViewModel.getPlaces().size() == refreshCounter.incrementAndGet()) {
					swipeRefreshLayout.setRefreshing(false);
					refreshCounter.set(0);
					appRepository.getWidgetsManager().updateWidgets(mContext);
				}
			}
		};
		
		
		//  Button and action Listeners
		//________________________________________________________________
		//
		//  Initialize buttons and actions behavior
		
		//  A simple click to add a place
		addPlacesFab.setOnClickListener(
				  placeFabView -> {
					  final AddPlaceDialog addPlaceDialog = new AddPlaceDialog(mContext, appRepository);
					  addPlaceDialog.build();
				  });
		
		
		//  Swipe to refresh listener
		swipeRefreshLayout.setOnRefreshListener(
				  () -> {
					  swipeRefreshLayout.setRefreshing(true);
					  vibrator.vibrate(VibrationEffect.createOneShot(100, 50));
					  if (appRepository.isApiKeyValid()) {
						  appRepository.updateAllPlaces(fetchUpdateCallback);
					  } else {
						  swipeRefreshLayout.setRefreshing(false);
						  showSnackbar(container, mContext.getString(R.string.error_no_api_key_registered_short));
					  }
				  }
		);
		
		return root;
	}
	
	
	private void setNoPlacesViewState() {
		if (!appRepository.isApiKeyRegistered()) {    //	An API key must be set
			informationTextView.setText(R.string.error_no_api_key_registered);
			addPlacesFab.setVisibility(View.GONE);
		} else if (!appRepository.isApiKeyValid()) {    //	Must have 32 alphanumerical characters
			informationTextView.setText(R.string.error_api_key_incorrectly_formed);
			addPlacesFab.setVisibility(View.GONE);
		} else {    //	No place as been registered
			informationTextView.setText(R.string.error_no_places_registered);
		}
		
		informationTextView.setVisibility(View.VISIBLE);
		swipeRefreshLayout.setVisibility(View.GONE);
	}
	
	private void setExistingPlacesViewState(View container) {
		if (!appRepository.isApiKeyRegistered()) {   //	No API key is registered
			showSnackbar(container, mContext.getString(R.string.error_no_api_key_registered));
		} else {
			if (!appRepository.isApiKeyValid())    //	API key is registered but malformed
				showSnackbar(container, mContext.getString(R.string.error_api_key_incorrectly_formed));
		}
		informationTextView.setVisibility(View.GONE);
		swipeRefreshLayout.setVisibility(View.VISIBLE);
	}
}