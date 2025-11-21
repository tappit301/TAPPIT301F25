package com.example.eventapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrganizerLandingFragment extends Fragment {

    private static final String TAG = "OrganizerLanding";

    // RecyclerView + empty state
    private RecyclerView rvEvents;
    private LinearLayout emptyState;

    // List the adapter shows (filtered)
    private final List<Event> eventList = new ArrayList<>();

    // Master list from Firestore (all events)
    private final List<Event> allEvents = new ArrayList<>();

    private EventAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    // Upcoming / Past buttons
    private MaterialButton btnUpcoming;
    private MaterialButton btnPast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        // ---------- NAV TO CREATE EVENT (unchanged) ----------
        NavController navController = Navigation.findNavController(view);
        View.OnClickListener createClick =
                v -> navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment);

        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        MaterialButton btnCreateEventTop = view.findViewById(R.id.btnCreateEventTop);

        if (btnAddEventEmpty != null) btnAddEventEmpty.setOnClickListener(createClick);
        if (btnAddEvent != null) btnAddEvent.setOnClickListener(createClick);
        if (btnCreateEventTop != null) btnCreateEventTop.setOnClickListener(createClick);

        // ---------- RecyclerView setup (unchanged) ----------
        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(eventList);
        rvEvents.setAdapter(adapter);

        // ---------- Toggle buttons ----------
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        if (btnUpcoming != null && btnPast != null) {
            btnUpcoming.setOnClickListener(v -> applyFilter(true));   // show upcoming
            btnPast.setOnClickListener(v -> applyFilter(false));      // show past
        }

        loadOrganizerEvents();
    }

    // ----------------- FIRESTORE LOAD (almost same as before) -----------------
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

                    allEvents.clear();
                    eventList.clear();

                    if (snapshots == null || snapshots.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        showEmptyState(true);
                        return;
                    }

                    snapshots.getDocuments().forEach(doc -> {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setId(doc.getId());
                            allEvents.add(event);    // store full list
                        }
                    });

                    // Default view: upcoming events
                    applyFilter(true);
                });
    }

    // ----------------- FILTER LOGIC -----------------
    /** showUpcoming = true -> Upcoming, false -> Past */
    private void applyFilter(boolean showUpcoming) {
        eventList.clear();

        for (Event e : allEvents) {
            if (isUpcoming(e) == showUpcoming) {
                eventList.add(e);
            }
        }

        showEmptyState(eventList.isEmpty());
        adapter.notifyDataSetChanged();
    }

    // Compare event date+time to "now"
    private boolean isUpcoming(Event event) {
        try {
            String dateStr = event.getDate();   // e.g. "12/11/2025"
            String timeStr = event.getTime();   // e.g. "14:30"

            if (dateStr == null || dateStr.isEmpty() ||
                    timeStr == null || timeStr.isEmpty()) {
                // If date/time missing, treat as upcoming so it at least shows somewhere
                return true;
            }

            String full = dateStr + " " + timeStr;

            // Format must match CreateEventFragment: day/month/year + 24h time
            SimpleDateFormat sdf =
                    new SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault());
            sdf.setLenient(false);

            Date eventDate = sdf.parse(full);
            Date now = new Date();

            return eventDate != null && eventDate.after(now);
        } catch (Exception e) {
            Log.e(TAG, "Date parse error for event '" +
                    event.getTitle() + "': " + e.getMessage());
            // If parsing fails, don't crash – consider it upcoming
            return true;
        }
    }

    private void showEmptyState(boolean showEmpty) {
        if (showEmpty) {
            rvEvents.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvEvents.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}
