package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.WeatherAlert;

public class WeatherAlertAdapter extends BaseAdapter {
    private final Context context;
    private final Place place;
    private final List<WeatherAlert> weatherAlertsList;
    private final LayoutInflater inflater;
    private static FormattingService formattingService;

    public WeatherAlertAdapter(Context context, Place place, FormattingService formattingService) {
        this.context = context;
        this.place = place;
        this.inflater = LayoutInflater.from(context);
        this.weatherAlertsList = place.getWeatherAlertsArrayList();
        WeatherAlertAdapter.formattingService = formattingService;
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
        TimeZone timeZone = place.getTimeZone();

        TextView eventTextView = view.findViewById(R.id.event);
        TextView senderTextView = view.findViewById(R.id.sender);
        TextView startDateTextView = view.findViewById(R.id.start_date);
        TextView endDateTextView = view.findViewById(R.id.end_date);
        TextView descriptionTextView = view.findViewById(R.id.description);

        descriptionTextView.setLinksClickable(true);
        descriptionTextView.setLinkTextColor(this.context.getColor(R.color.colorAccent));

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String timeOffset = pref.getString("time_offset", null);
        String timeFormat = pref.getString("time_format", null);

        eventTextView.setText(currentWeatherAlert.getEvent());
        senderTextView.setText(currentWeatherAlert.getSender());
        startDateTextView.setText(formattingService.getFormattedFullTimeHour(currentWeatherAlert.getStart_dtDate(), timeZone));
        endDateTextView.setText(formattingService.getFormattedFullTimeHour(currentWeatherAlert.getEnd_dtDate(), timeZone));
        descriptionTextView.setText(currentWeatherAlert.getDescription());

        return view;
    }
}
