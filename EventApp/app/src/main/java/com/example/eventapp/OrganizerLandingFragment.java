package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class OrganizerLandingFragment extends Fragment {

    private static final String TAG = "OrganizerLanding";

    private RecyclerView rvEvents;
    private LinearLayout emptyState;

    private final List<Event> allEvents = new ArrayList<>();
    private final List<Event> upcomingEvents = new ArrayList<>();
    private final List<Event> pastEvents = new ArrayList<>();

    private EventAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private MaterialButton btnUpcoming, btnPast;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        NavController navController = Navigation.findNavController(view);

        ImageButton btnExplore = view.findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), HomeActivity.class);
            intent.putExtra("fromExplore", true);
            startActivity(intent);
        });


        View.OnClickListener createClick = v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment);

        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        MaterialButton btnCreateEventTop = view.findViewById(R.id.btnCreateEventTop);

        if (btnAddEventEmpty != null) btnAddEventEmpty.setOnClickListener(createClick);
        if (btnAddEvent != null) btnAddEvent.setOnClickListener(createClick);
        if (btnCreateEventTop != null) btnCreateEventTop.setOnClickListener(createClick);

        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // 🔹 Initial adapter: UPCOMING events → details from organizerLanding
        adapter = new EventAdapter(
                upcomingEvents,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment
        );
        rvEvents.setAdapter(adapter);

        setupFilters();
        loadOrganizerEvents();
    }

    // ----------------------------------------------------------
    // FILTER BUTTON LOGIC
    // ----------------------------------------------------------
    private void setupFilters() {

        btnUpcoming.setOnClickListener(v -> {
            btnUpcoming.setChecked(true);
            btnPast.setChecked(false);

            adapter = new EventAdapter(
                    upcomingEvents,
                    R.id.action_organizerLandingFragment_to_eventDetailsFragment
            );
            rvEvents.setAdapter(adapter);
        });

        btnPast.setOnClickListener(v -> {
            btnPast.setChecked(true);
            btnUpcoming.setChecked(false);

            adapter = new EventAdapter(
                    pastEvents,
                    R.id.action_organizerLandingFragment_to_eventDetailsFragment
            );
            rvEvents.setAdapter(adapter);
        });

        // Default selection = UPCOMING
        btnUpcoming.setChecked(true);
    }

    // ----------------------------------------------------------
    // FIRESTORE LOAD
    // ----------------------------------------------------------
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
                        Log.e(TAG, "Firestore load failed", error);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        allEvents.clear();
                        upcomingEvents.clear();
                        pastEvents.clear();

                        adapter.notifyDataSetChanged();
                        emptyState.setVisibility(View.VISIBLE);
                        rvEvents.setVisibility(View.GONE);
                        return;
                    }

                    allEvents.clear();
                    for (var doc : snapshots) {
                        Event e = doc.toObject(Event.class);
                        if (e != null) {
                            e.setId(doc.getId());
                            allEvents.add(e);
                        }
                    }

                    splitEventsByTime();

                    emptyState.setVisibility(View.GONE);
                    rvEvents.setVisibility(View.VISIBLE);

                    // Recreate adapter based on which tab is active
                    if (btnUpcoming.isChecked()) {
                        adapter = new EventAdapter(
                                upcomingEvents,
                                R.id.action_organizerLandingFragment_to_eventDetailsFragment
                        );
                    } else {
                        adapter = new EventAdapter(
                                pastEvents,
                                R.id.action_organizerLandingFragment_to_eventDetailsFragment
                        );
                    }

                    rvEvents.setAdapter(adapter);
                });
    }

    // ----------------------------------------------------------
    // UPCOMING vs PAST SEPARATION
    // ----------------------------------------------------------
    private void splitEventsByTime() {
        upcomingEvents.clear();
        pastEvents.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date now = new Date();

        for (Event e : allEvents) {
            try {
                Date eventDate = sdf.parse(e.getDate() + " " + e.getTime());
                if (eventDate != null && eventDate.after(now)) {
                    upcomingEvents.add(e);
                } else {
                    pastEvents.add(e);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Date parse failed", ex);
            }
        }
    }
}
