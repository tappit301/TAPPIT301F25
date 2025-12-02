package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter that displays a list of events in a RecyclerView.
 * Each event card shows the title, date, and cover image,
 * and clicking a card navigates to its detail screen.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;
    private final int navActionId;

    /**
     * Creates an adapter for displaying events and navigating to a details fragment.
     *
     * @param eventList    list of events to display
     * @param navActionId  navigation action used when an item is clicked
     */
    public EventAdapter(List<Event> eventList, int navActionId) {
        this.eventList = eventList;
        this.navActionId = navActionId;
    }

    /**
     * Inflates the layout for a single event card.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds event data to a card and prepares navigation to the detail screen.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate() + " • " + event.getTime());

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_img)
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.placeholder_img);
        }

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
            bundle.putString("imageUrl", event.getImageUrl());

            Navigation.findNavController(v).navigate(navActionId, bundle);
        });
    }

    /**
     * Returns the number of events displayed.
     */
    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    /**
     * Holds references to UI elements for a single event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView ivPoster;
        TextView tvTitle, tvDate;

        /**
         * Initializes views for displaying event information.
         *
         * @param itemView the event card layout
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivEventImage);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
        }
    }
}
