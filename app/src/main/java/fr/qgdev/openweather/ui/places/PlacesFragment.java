package fr.qgdev.openweather.ui.places;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.VolleyError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import fr.qgdev.openweather.DataPlaces;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.WeatherService;
import fr.qgdev.openweather.adapters.PlaceItemAdapter;
import fr.qgdev.openweather.dialog.AddPlaceDialog;

public class PlacesFragment extends Fragment {

    PlaceItemAdapter placeItemAdapter;
    private Context mContext;
    private String API_KEY;
    private LinearLayout placeList;
    private DataPlaces dataPlaces;

    private ArrayList<Place> placeArrayList;

    private WeatherService weatherService;
    private WeatherService.WeatherCallback initializePlaceListCallback;
    private WeatherService.WeatherCallback refreshPlaceListCallback;



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

    @UiThread
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View root = inflater.inflate(R.layout.fragment_places, container, false);

        //  Initialize the list of all places registered
        placeList = root.findViewById(R.id.place_list);
        placeList.removeAllViews();

        //  Get API key
        SharedPreferences apiKeyPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        API_KEY = apiKeyPref.getString("api_key", null);


        placeItemAdapter = new PlaceItemAdapter(mContext, placeArrayList, root);

        //  Find SwipeRefreshLayout and no places registered message textview
        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.swiperefresh);
        final TextView noPlacesRegisteredTextView = root.findViewById(R.id.no_places_registered);

        //  Initialize places data storage
        dataPlaces = new DataPlaces(mContext);

        //  Initialize Weather Services and callbacks
        weatherService = new WeatherService(mContext, API_KEY, dataPlaces);


        ////    Refreshing places list callback
        refreshPlaceListCallback = new WeatherService.WeatherCallback() {

            @Override
            public void onWeatherData(final Place place, DataPlaces dataPlaces) {
                try {
                    if(dataPlaces.savePlace(place)) {

                        placeArrayList = dataPlaces.getPlaces();
                        if (!placeArrayList.isEmpty()) {
                            noPlacesRegisteredTextView.setVisibility(View.GONE);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                        }

                        int index = dataPlaces.getPlacePositionInRegister(place);
                        placeArrayList.set(index, place);
                        placeItemAdapter = new PlaceItemAdapter(mContext, placeArrayList, root);

                        if (placeList.getChildCount() - 1 < index) {
                            placeList.addView(placeItemAdapter.getView(index, null, null), placeList.getChildCount());
                        } else {
                            placeList.removeViewAt(index);
                            placeList.addView(placeItemAdapter.getView(index, null, null), index);
                        }

                    }
                    else {
                        Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG)
                                .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                .setMaxInlineActionWidth(3)
                                .show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                            .setMaxInlineActionWidth(3)
                            .show();
                }
            }
            @Override
            public void onError(Exception exception, Place place, DataPlaces dataPlaces) {
                try {
                    int index = dataPlaces.getPlacePositionInRegister(place);
                    placeList.addView(placeItemAdapter.getView(index, null, null));
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list_network), Snackbar.LENGTH_LONG)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                            .setMaxInlineActionWidth(3)
                            .show();
                }
            }

            @Override
            public void onConnectionError(VolleyError error, Place place, DataPlaces dataPlaces) {

                //  no server response (NO INTERNET or SERVER DOWN)
                if(error.networkResponse == null){
                    Snackbar.make(container, mContext.getString(R.string.error_server_unreachable), Snackbar.LENGTH_LONG)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                            //.setMaxInlineActionWidth(3)
                            .show();
                }
                //  Server response
                else {

                    place.setErrorDuringDataAcquisition(true);
                    place.setErrorCode(error.networkResponse.statusCode);

                    switch (place.getErrorCode()) {
                        case 429:
                            Snackbar.make(container, mContext.getString(R.string.error_too_many_request_in_a_day), Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                    .setMaxInlineActionWidth(3)
                                    .show();
                            break;

                        case 404:
                            Snackbar.make(container, mContext.getString(R.string.error_place_not_found), Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                    .setMaxInlineActionWidth(3)
                                    .show();
                            break;

                        case 401:
                            Snackbar.make(container, mContext.getString(R.string.error_wrong_api_key), Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                    .setMaxInlineActionWidth(3)
                                    .show();
                            break;

                        default:
                            Snackbar.make(container, mContext.getString(R.string.error_unknow_error), Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                    .setMaxInlineActionWidth(3)
                                    .show();
                            break;
                    }
                }
            }
        };


        //  Floating Action Button to add a place
        FloatingActionButton addPlacesFab = getActivity().findViewById(R.id.add_places);

        addPlacesFab.setOnClickListener(
                placeFabView -> {
                    final AddPlaceDialog addPlaceDialog = new AddPlaceDialog(mContext, placeFabView, refreshPlaceListCallback);
                    addPlaceDialog.build();
                });


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(true);
                        if(API_KEY != null && API_KEY != "") {

                            try {

                                for(int index = 0; index < placeArrayList.size(); index++) {
                                    weatherService.getWeatherDataOWM(placeArrayList.get(index), refreshPlaceListCallback);
                                }

                                swipeRefreshLayout.setRefreshing(false);
                            } catch (Exception e) {
                                Snackbar.make(container, mContext.getString(R.string.error_cannot_refresh_place_list), Snackbar.LENGTH_LONG)
                                        .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                        .setMaxInlineActionWidth(3)
                                        .show();
                                e.printStackTrace();
                            }
                        }
                        else {
                            swipeRefreshLayout.setRefreshing(false);
                            Snackbar.make(container, mContext.getString(R.string.error_no_api_key_registered), Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                                    .setMaxInlineActionWidth(3)
                                    .show();
                        }
                    }
                }
        );


        //  No API key registered
        if (API_KEY != null) {

            //  Show the Floating Action Button to add Places
            addPlacesFab.setVisibility(View.VISIBLE);
        } else {
            //  Hide the Floating Action Button to add Places
            addPlacesFab.setVisibility(View.GONE);
        }


        //  Display data about places
        try {

            //  Show current saved data
            placeArrayList = dataPlaces.getPlaces();

            if (!placeArrayList.isEmpty()) {

                noPlacesRegisteredTextView.setVisibility(View.GONE);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                placeItemAdapter = new PlaceItemAdapter(mContext, placeArrayList, root);
                for (int i = 0; i < placeArrayList.size(); i++) {
                    placeList.addView(placeItemAdapter.getView(i, null, null), i);
                }

                //  Refresh data
                for (int index = 0; index < placeArrayList.size(); index++) {
                    weatherService.getWeatherDataOWM(placeArrayList.get(index), refreshPlaceListCallback);
                    Log.d("WeatherUpdate", placeArrayList.get(index).getCity());
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