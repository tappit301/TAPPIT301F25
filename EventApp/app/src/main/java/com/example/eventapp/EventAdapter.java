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

        // ⭐ Load event image if exists
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(event.getImageUrl())
                    .placeholder(R.drawable.placeholder_img)   // your grey box
                    .into(holder.ivPoster);
        } else {
            holder.ivPoster.setImageResource(R.drawable.placeholder_img);
        }

        // ⭐ Navigate to details
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

            // ⭐ IMPORTANT: poster image
            bundle.putString("imageUrl", event.getImageUrl());

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

        ImageView ivPoster;
        TextView tvTitle, tvDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPoster = itemView.findViewById(R.id.ivEventImage);  // make sure this ID exists
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDate = itemView.findViewById(R.id.tvEventDate);
        }
    }
}
