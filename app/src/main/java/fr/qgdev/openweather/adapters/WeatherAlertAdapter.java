package fr.qgdev.openweather.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.WeatherAlert;

public class WeatherAlertAdapter extends BaseAdapter {
    private final Context context;
    private final Place place;
    private final List<WeatherAlert> weatherAlertsList;
    private final LayoutInflater inflater;

    public WeatherAlertAdapter(Context context, Place place) {
        this.context = context;
        this.place = place;
        this.inflater = LayoutInflater.from(context);
        this.weatherAlertsList = place.getWeatherAlertsArrayList();
    }


    @Override
    public int getCount() {
        return weatherAlertsList.size();
    }

    @Override
    public WeatherAlert getItem(int position) {
        return weatherAlertsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflater.inflate(R.layout.adapter_weather_alert, null);
        WeatherAlert currentWeatherAlert = getItem(position);

        TextView eventTextView = view.findViewById(R.id.event);
        TextView senderTextView = view.findViewById(R.id.sender);
        TextView startDateTextView = view.findViewById(R.id.start_date);
        TextView endDateTextView = view.findViewById(R.id.end_date);
        TextView descriptionTextView = view.findViewById(R.id.description);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String timeOffset = pref.getString("time_offset", null);
        String timeFormat = pref.getString("time_format", null);

        //  start and end date
        SimpleDateFormat hourFormat;

        if (timeFormat.contains("24")) {
            hourFormat = new SimpleDateFormat("dd/MM - HH:mm");
        } else {
            hourFormat = new SimpleDateFormat("dd/MM - KK:mm a");
        }

        if (timeOffset.contains("place"))
            hourFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of(place.getTimeZone())));

        eventTextView.setText(currentWeatherAlert.getEvent());
        senderTextView.setText(currentWeatherAlert.getSender());
        startDateTextView.setText(hourFormat.format(currentWeatherAlert.getStart_dtDate()));
        endDateTextView.setText(hourFormat.format(currentWeatherAlert.getEnd_dtDate()));
        descriptionTextView.setText(currentWeatherAlert.getDescription());

        return view;
    }
}
