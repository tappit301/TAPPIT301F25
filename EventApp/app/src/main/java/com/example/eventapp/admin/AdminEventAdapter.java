package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter used by the admin to display a list of events.
 * Each row shows event details and provides a delete action.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final OnEventClickListener listener;

    /**
     * Listener interface for handling delete actions on an event.
     */
    public interface OnEventClickListener {
        /**
         * Called when the admin chooses to delete an event.
         *
         * @param event the selected event
         */
        void onDeleteClicked(Event event);
    }

    /**
     * Creates an adapter with a list of events and a click listener.
     *
     * @param events the list of events to display
     * @param listener callback for delete actions
     */
    public AdminEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single event row.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to the row at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    /**
     * Returns the total number of events being shown.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder that represents a single event row for the admin.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView title, organizer, date, deleteBtn;

        /**
         * Finds and stores references to the UI elements for a row.
         *
         * @param itemView the row view
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvEventTitleAdmin);
            organizer = itemView.findViewById(R.id.tvEventOrganizerAdmin);
            date = itemView.findViewById(R.id.tvEventDateAdmin);
            deleteBtn = itemView.findViewById(R.id.tvDeleteEventAdmin);
        }

        /**
         * Binds the event information to the UI elements for this row.
         * Formats the event date and sets up the delete button action.
         *
         * @param event the event being displayed
         * @param listener listener for delete actions
         */
        void bind(Event event, OnEventClickListener listener) {

            title.setText(event.getTitle());
            organizer.setText("By: " + event.getOrganizerEmail());

            String d = event.getDate();
            String t = event.getTime();

            if (d != null && t != null && !d.isEmpty() && !t.isEmpty()) {
                try {
                    String input = d + " " + t;
                    SimpleDateFormat inputFormat =
                            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.US);

                    String formatted = outputFormat.format(inputFormat.parse(input));
                    date.setText(formatted);

                } catch (Exception e) {
                    date.setText("Invalid date");
                }
            } else {
                date.setText("No date set");
            }

            deleteBtn.setOnClickListener(v -> listener.onDeleteClicked(event));
        }
    }
}
