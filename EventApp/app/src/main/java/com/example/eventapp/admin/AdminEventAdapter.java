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

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private final List<Event> events;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onDeleteClicked(Event event);
    }

    public AdminEventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // --------------------------------------------------

    static class EventViewHolder extends RecyclerView.ViewHolder {

        TextView title, organizer, date, deleteBtn;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.tvEventTitleAdmin);
            organizer = itemView.findViewById(R.id.tvEventOrganizerAdmin);
            date = itemView.findViewById(R.id.tvEventDateAdmin);
            deleteBtn = itemView.findViewById(R.id.tvDeleteEventAdmin);
        }

        void bind(Event event, OnEventClickListener listener) {

            title.setText(event.getTitle());
            organizer.setText("By: " + event.getOrganizerEmail());

            // ----------------------------
            // FORMAT DATE + TIME PROPERLY
            // ----------------------------
            String d = event.getDate();   // example: "2025-01-10"
            String t = event.getTime();   // example: "14:30"

            if (d != null && t != null && !d.isEmpty() && !t.isEmpty()) {
                try {
                    String input = d + " " + t; // "2025-01-10 14:30"

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

            // DELETE BUTTON
            deleteBtn.setOnClickListener(v -> listener.onDeleteClicked(event));
        }
    }
}
