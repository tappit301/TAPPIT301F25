package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows the admin to view and manage all events in the system.
 * Loads events from Firestore and displays them in a list.
 */
public class AdminManageEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private final List<Event> eventList = new ArrayList<>();

    /**
     * Creates the fragment and sets its layout resource.
     */
    public AdminManageEventsFragment() {
        super(R.layout.admin_manage_events);
    }

    /**
     * Checks if the admin is logged in, sets up the event list,
     * and loads all events from the database.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        recyclerView = view.findViewById(R.id.adminEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEvents();
    }

    /**
     * Loads all events from Firestore, converts documents into event objects,
     * and displays them using an adapter.
     */
    private void loadEvents() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    eventList.clear();
                    for (var doc : snap) {
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());
                        eventList.add(e);
                    }

                    AdminEventAdapter adapter = new AdminEventAdapter(eventList, event -> {
                        // Reserved for future navigation or delete actions
                    });

                    recyclerView.setAdapter(adapter);
                });
    }
}
