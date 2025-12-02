package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseEventsFragment extends Fragment {

    private RecyclerView recycler;
    private final List<Event> events = new ArrayList<>();

    public AdminBrowseEventsFragment() {
        super(R.layout.admin_browse_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerEvents);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEvents();
    }

    private void loadEvents() {
        AdminManager.getInstance().getAllEvents()
                .addOnSuccessListener(snap -> {
                    events.clear();
                    for (var doc : snap) {
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());
                        events.add(e);
                    }

                    recycler.setAdapter(new AdminEventAdapter(events, event -> {
                        showDeleteDialog(event);
                    }));
                });
    }

    private void showDeleteDialog(Event event) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event?")
                .setMessage("Are you sure you want to delete \"" + event.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent(Event event) {
        AdminManager.getInstance()
                .deleteEvent(event.getId())
                .addOnSuccessListener(unused -> loadEvents());
    }
}
