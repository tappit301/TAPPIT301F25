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
 * Adapter class that binds a list of {@link Event} objects to a RecyclerView.
 * Each item displays the event image, title, and date/time.
 * Clicking an event navigates to the EventDetailsFragment with event data.
 *
 * @author tappit
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;

    public EventAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDate.setText(event.getDate() + " • " + event.getTime());

        // ⭐ Load event poster image
        Glide.with(holder.itemView.getContext())
                .load(event.getImageUrl())
                .placeholder(R.drawable.placeholder_img)
                .centerCrop()
                .into(holder.ivEventImage);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getId());
            bundle.putString("title", event.getTitle());
            bundle.putString("desc", event.getDescription());
            bundle.putString("date", event.getDate());
            bundle.putString("time", event.getTime());
            bundle.putString("location", event.getLocation());
            bundle.putString("imageUrl", event.getImageUrl()); // ⭐ pass image

            Navigation.findNavController(v).navigate(
                    R.id.action_organizerLandingFragment_to_eventDetailsFragment,
                    bundle
            );
        });
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvTitle;
        TextView tvDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
        }
    }
}
