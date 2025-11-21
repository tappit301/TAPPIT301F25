package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 *  THis is the adapter class that connects a list of {@link Event} objects
 * to a RecyclerView so each event can be displayed in the UI.
 *
 * Each list item shows the event title and date/time,
 * and lets the user tap to open full event details.</p>
 *
 * @author tappit
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** List of events to display. */
    private final List<Event> eventList;

    /**
     * Constructs the adapter with a given list of events.
     *
     * @param eventList list of {@link Event} objects to show
     */
    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    /**
     * Inflates the layout for each event item when a new ViewHolder is created.
     *
     * @param parent   parent ViewGroup where the new view will be added
     * @param viewType type of view (unused here)
     * @return a new {@link EventViewHolder} for the list item
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data (title and date/time) to the list item and
     * handles the click action to navigate to the Event Details screen.
     *
     * @param holder   the {@link EventViewHolder} that holds the views
     * @param position the index of the current event in the list
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate() + " • " + event.getTime());

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getId());
            bundle.putString("title", event.getTitle());
            bundle.putString("desc", event.getDescription());
            bundle.putString("date", event.getDate());
            bundle.putString("time", event.getTime());
            bundle.putString("location", event.getLocation());
            bundle.putString("organizerId", event.getOrganizerId());
            bundle.putString("organizerEmail", event.getOrganizerEmail());

            try {
                Navigation.findNavController(v).navigate(
                        R.id.action_organizerLandingFragment_to_eventDetailsFragment,
                        bundle
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * @return the number of events in the list
     */
    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    /**
     * ViewHolder that stores references to the views inside each
     * event list item for quick access during scrolling.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        /** TextView that shows the event title. */
        TextView tvTitle;

        /** TextView that shows the event date and time. */
        TextView tvDate;

        /**
         * Creates a new ViewHolder for one event list item.
         *
         * @param itemView the inflated layout for the item
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
        }
    }
}
