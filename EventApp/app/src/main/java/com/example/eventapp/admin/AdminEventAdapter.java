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
 * Adapter responsible for displaying all events in the admin's event management screen.
 * Shows the event title, organizer email, formatted date, and a delete button.
 * Used inside {@link AdminManageEventsFragment}.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    /** List of all events retrieved from Firestore. */
    private final List<Event> events;

    /** Listener for handling delete button actions. */
    private final OnEventClickListener listener;

    /**
     * Listener interface for event-level actions such as deletion.
     */
    public interface OnEventClickListener {
        /**
         * Called when the admin taps the "delete" action for an event.
         *
         * @param event the event the admin chose to delete
         */
        void onDeleteClicked(Event event);
    }

    /**
     * Creates an adapter for showing events to the administrator.
     *
     * @param events   list of events retrieved from Firestore
     * @param listener callback triggered when an admin clicks delete
     */
    public AdminEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single event row in the admin view.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds an event to its corresponding ViewHolder for display.
     *
     * @param holder   the ViewHolder into which the event should be rendered
     * @param position index of the event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position), listener);
    }

    /**
     * Returns the total number of events being displayed.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * ViewHolder representing a single event row in the admin event list.
     * Displays title, organizer email, formatted date-time, and a delete button.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView organizer;
        TextView date;
        TextView deleteBtn;

        /**
         * Initializes view references for each UI element inside the event row layout.
         *
         * @param itemView the root layout for a single admin event row
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvEventTitleAdmin);
            organizer = itemView.findViewById(R.id.tvEventOrganizerAdmin);
            date = itemView.findViewById(R.id.tvEventDateAdmin);
            deleteBtn = itemView.findViewById(R.id.tvDeleteEventAdmin);
        }

        /**
         * Binds event details to the UI elements, including formatting the date/time.
         * Also attaches a click listener to the delete button.
         *
         * @param event    the event being rendered
         * @param listener callback to trigger when delete is pressed
         */
        void bind(Event event, OnEventClickListener listener) {

            // Set event title and organizer email
            title.setText(event.getTitle());
            organizer.setText("By: " + event.getOrganizerEmail());

            // Format date and time if they exist
            String d = event.getDate(); // Example: "30/11/2025"
            String t = event.getTime(); // Example: "06:30"

            if (d != null && t != null && !d.isEmpty() && !t.isEmpty()) {
                try {
                    // Combine date & time into one string
                    String input = d + " " + t;

                    // Input format MUST match Firestore's stored format
                    SimpleDateFormat inputFormat =
                            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

                    // Output user-friendly format
                    SimpleDateFormat outputFormat =
                            new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.US);

                    date.setText(outputFormat.format(inputFormat.parse(input)));

                } catch (Exception e) {
                    date.setText("Invalid date");
                }
            } else {
                date.setText("No date set");
            }

            // Delete button triggers the callback
            deleteBtn.setOnClickListener(v -> listener.onDeleteClicked(event));
        }
    }
}
