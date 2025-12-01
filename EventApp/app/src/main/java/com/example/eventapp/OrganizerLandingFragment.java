package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrganizerLandingFragment extends Fragment {

    private FirebaseFirestore firestore;

    // UI
    private RecyclerView rvEvents;
    private LinearLayout emptyStateLayout;
    private MaterialButton btnUpcoming;
    private MaterialButton btnPast;
    private MaterialButtonToggleGroup toggleGroup;

    // Data
    private final List<Event> fullList = new ArrayList<>();
    private final List<Event> filteredList = new ArrayList<>();
    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.landing_page, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore(); // null in tests

        // Top Toolbar Buttons
        ImageButton btnExplore = view.findViewById(R.id.btnExplore);
        MaterialButton btnCreateTop = view.findViewById(R.id.btnCreateEventTop);
        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        ImageButton btnNotifications = view.findViewById(R.id.btnNotifications);

        // Recycler + Empty Layout
        rvEvents = view.findViewById(R.id.rvEvents);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);

        // FAB
        View fabAddEvent = view.findViewById(R.id.btnAddEvent);

        // Toggle Group
        toggleGroup = view.findViewById(R.id.toggleEventType);
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(filteredList,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment);
        rvEvents.setAdapter(adapter);

        // --- Navigation buttons ---
        btnExplore.setOnClickListener(v ->
                safeNavigate(view, R.id.action_organizerLandingFragment_to_exploreEventsFragment));

        btnCreateTop.setOnClickListener(v ->
                safeNavigate(view, R.id.action_organizerLandingFragment_to_createEventFragment));

        fabAddEvent.setOnClickListener(v ->
                safeNavigate(view, R.id.action_organizerLandingFragment_to_createEventFragment));

        btnAddEventEmpty.setOnClickListener(v ->
                safeNavigate(view, R.id.action_organizerLandingFragment_to_createEventFragment));

        // Toggle: Upcoming / Past
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                applyFilter(checkedId == R.id.btnUpcoming);
            }
        });

        loadEventsFromFirestore();
    }

    private void loadEventsFromFirestore() {

        if (firestore == null) {
            // Robolectric test mode
            updateEmptyState();
            return;
        }

        firestore.collection("events")
                .get()
                .addOnSuccessListener(query -> {
                    fullList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Event e = doc.toObject(Event.class);
                        fullList.add(e);
                    }

                    // Default view is "Upcoming"
                    applyFilter(true);
                })
                .addOnFailureListener(e -> updateEmptyState());
    }

    /** Filter list by upcoming/past */
    private void applyFilter(boolean showUpcoming) {
        filteredList.clear();

        for (Event e : fullList) {
            if (showUpcoming && !e.isPastEvent()) {
                filteredList.add(e);
            } else if (!showUpcoming && e.isPastEvent()) {
                filteredList.add(e);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            rvEvents.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            rvEvents.setVisibility(View.VISIBLE);
        }
    }

    /** Safe nav controller for tests */
    private void safeNavigate(View root, int actionId) {
        NavController nav = null;
        try {
            nav = Navigation.findNavController(root);
        } catch (Exception ignored) { }

        if (nav != null) {
            nav.navigate(actionId);
        }
    }
}
