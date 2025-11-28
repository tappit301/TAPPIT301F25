package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventVH> {

    private final List<Event> eventList;
    private final OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public AdminEventAdapter(List<Event> list, OnEventClickListener listener) {
        this.eventList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EventVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_event, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH h, int pos) {
        Event e = eventList.get(pos);
        h.title.setText(e.getTitle());
        h.date.setText(e.getDate());
        h.location.setText(e.getLocation());

        h.itemView.setOnClickListener(v -> listener.onEventClick(e));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventVH extends RecyclerView.ViewHolder {
        TextView title, date, location;

        public EventVH(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.txtEventTitle);
            date = v.findViewById(R.id.txtEventDate);
            location = v.findViewById(R.id.txtEventLocation);
        }
    }
}
