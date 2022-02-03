package fr.qgdev.openweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TimeZone;

import fr.qgdev.openweather.FormattingService;
import fr.qgdev.openweather.Place;
import fr.qgdev.openweather.R;
import fr.qgdev.openweather.weather.WeatherAlert;


/**
 * WeatherAlertAdapter
 * <p>
 * Contains weather alerts
 * </p>
 *
 * @author Quentin GOMES DOS REIS
 * @version 1
 * @see RecyclerView.ViewHolder
 */
public class WeatherAlertAdapter extends BaseAdapter {
    private final Context context;
    private final Place place;
    private final List<WeatherAlert> weatherAlertsList;
    private final LayoutInflater inflater;
    private final FormattingService formattingService;


    /**
     * WeatherAlertAdapter Constructor
     * <p>
     * Just the constructor of WeatherAlertAdapter class
     * </p>
     *
     * @param context           Context of the application to inflate layout
     * @param place             Place which contains Weather Alerts
     * @param formattingService The formatting service to format dates
     * @apiNote None of the parameters can be null
     */
    public WeatherAlertAdapter(@NotNull Context context, @NotNull Place place, @NotNull FormattingService formattingService) {
        this.context = context;
        this.place = place;
        this.inflater = LayoutInflater.from(context);
        this.weatherAlertsList = place.getWeatherAlertsArrayList();
        this.formattingService = formattingService;
    }


    /**
     * getCount()
     * <p>
     * Just return the number of Weather Alerts
     * </p>
     *
     * @return The number of weather alerts
     */
    @Override
    public int getCount() {
        return weatherAlertsList.size();
    }


    /**
     * getItem(int position)
     * <p>
     * Just return the number of Weather Alerts
     * </p>
     *
     * @param position The index of the wanted item
     * @return The weather alert item at the given position
     */
    @Override
    public WeatherAlert getItem(int position) {
        return weatherAlertsList.get(position);
    }


    /**
     * getItemId(int position)
     * <p>
     * Just to get Item Id
     * UNIMPLEMENTED
     * </p>
     *
     * @param position The index of the wanted item id
     * @return Will return 0 because method not implemented
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }


    /**
     * getView(int position, View view, ViewGroup parent)
     * <p>
     * Generate a view for an item at a wanted position
     * </p>
     *
     * @param position The index of the wanted item
     * @param view     The view that will be inflated and completed with data
     * @param parent   The parent view
     * @return Built and completed view
     */
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

        eventTextView.setText(currentWeatherAlert.getEvent());
        senderTextView.setText(currentWeatherAlert.getSender());
        startDateTextView.setText(formattingService.getFormattedFullTimeHour(currentWeatherAlert.getStart_dtDate(), timeZone));
        endDateTextView.setText(formattingService.getFormattedFullTimeHour(currentWeatherAlert.getEnd_dtDate(), timeZone));
        descriptionTextView.setText(currentWeatherAlert.getDescription());

        return view;
    }
}
