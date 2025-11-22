package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the Landing screen for organizers that shows all events they created.
 * This fragment listens to Firestore for the current user's events
 * and displays them in a RecyclerView. Users can also navigate to
 * the Create Event screen from here.
 *
 * Data is ordered by creation time, with the newest events first.
 *
 * Author: tappit
 */
public class OrganizerLandingFragment extends Fragment {

    /** Tag used for logging messages. */
    private static final String TAG = "OrganizerLanding";

    /** RecyclerView that lists the user's events. */
    private RecyclerView rvEvents;

    /** Layout displayed when no events are found. */
    private LinearLayout emptyState;

    /** Adapter that binds events to the RecyclerView. */
    private EventAdapter adapter;

    /** List that stores all fetched events. */
    private final List<Event> eventList = new ArrayList<>();

    /** Firebase authentication instance. */
    private FirebaseAuth auth;

    /** Firestore database instance. */
    private FirebaseFirestore firestore;

    /**
     * Inflates the layout for the organizer landing page.
     *
     * @param inflater LayoutInflater used to inflate the view
     * @param container Parent container for the fragment
     * @param savedInstanceState Saved state, if any
     * @return The inflated landing page view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_page, container, false);
    }

    /**
     * Sets up the RecyclerView, initializes navigation buttons,
     * and starts listening for the organizer's events in Firestore.
     *
     * @param view The fragment's root view
     * @param savedInstanceState Previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        NavController navController = Navigation.findNavController(view);
        View.OnClickListener createClick =
                v -> navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment);

        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        MaterialButton btnCreateEventTop = view.findViewById(R.id.btnCreateEventTop);

        if (btnAddEventEmpty != null) btnAddEventEmpty.setOnClickListener(createClick);
        if (btnAddEvent != null) btnAddEvent.setOnClickListener(createClick);
        if (btnCreateEventTop != null) btnCreateEventTop.setOnClickListener(createClick);

        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(eventList);
        rvEvents.setAdapter(adapter);

        loadOrganizerEvents();

        TextView GoHome = view.findViewById(R.id.AppName);
        GoHome.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), HomeActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Loads the current organizer's events from Firestore and updates
     * the list whenever data changes. Shows an empty state if there are no events.
     */
    private void loadOrganizerEvents() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String organizerId = user.getUid();

        firestore.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore listen failed", error);
                        Toast.makeText(getContext(),
                                "Failed to load events: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        eventList.clear();
                        adapter.notifyDataSetChanged();
                        emptyState.setVisibility(View.VISIBLE);
                        rvEvents.setVisibility(View.GONE);
                        return;
                    }

                    eventList.clear();
                    snapshots.getDocuments().forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            eventList.add(event);
                        }
                    });

                    emptyState.setVisibility(View.GONE);
                    rvEvents.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                });
    }
}
