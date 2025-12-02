package com.example.eventapp.admin;

import android.os.Bundle;
import android.util.Log;
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
 * Fragment used by administrators to view and manage all events in the system.
 * Displays every event in Firestore regardless of category or status.
 * Admins can later extend this screen to edit or delete events.
 */
public class AdminManageEventsFragment extends Fragment {

    private RecyclerView recyclerView;

    /** List containing all events retrieved from Firestore. */
    private final List<Event> eventList = new ArrayList<>();

    /**
     * Default constructor which sets the layout resource for this fragment.
     * The layout contains the RecyclerView for displaying events.
     */
    public AdminManageEventsFragment() {
        super(R.layout.admin_manage_events);
    }

    /**
     * Called when the fragment's view is fully created.
     * This checks if the admin is logged in, initializes the RecyclerView,
     * and begins loading events from Firestore.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Prevent access if admin is not authenticated
        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        // Set up RecyclerView for displaying event list
        recyclerView = view.findViewById(R.id.adminEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEvents();
    }

    /**
     * Loads all events from Firestore, converts documents into Event objects,
     * sets their unique Firestore IDs, and displays them using the admin adapter.
     * Logs each event's raw date field to assist with debugging.
     */
    private void loadEvents() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    eventList.clear();

                    for (var doc : snap) {
                        Log.d("ADMIN_EVENT", "Raw date in Firestore = " + doc.get("date"));

                        // Convert Firestore document into Event model
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());

                        eventList.add(e);
                    }

                    // Create and attach adapter after loading all events
                    AdminEventAdapter adapter = new AdminEventAdapter(eventList, event -> {
                        // Placeholder for delete or edit logic
                    });

                    recyclerView.setAdapter(adapter);
                });
    }
}
