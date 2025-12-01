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
    private RecyclerView rvEvents;
    private LinearLayout emptyStateLayout;

    private final List<Event> fullList = new ArrayList<>();
    private final List<Event> filteredList = new ArrayList<>();
    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.landing_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle sis) {
        firestore = FirebaseHelper.getFirestore();

        rvEvents = view.findViewById(R.id.rvEvents);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        MaterialButtonToggleGroup toggle = view.findViewById(R.id.toggleEventType);

        rvEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(filteredList,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment);
        rvEvents.setAdapter(adapter);

        view.findViewById(R.id.btnCreateEventTop)
                .setOnClickListener(v -> safeNav(view,
                        R.id.action_organizerLandingFragment_to_createEventFragment));

        view.findViewById(R.id.btnAddEvent)
                .setOnClickListener(v -> safeNav(view,
                        R.id.action_organizerLandingFragment_to_createEventFragment));

        view.findViewById(R.id.btnAddEventEmpty)
                .setOnClickListener(v -> safeNav(view,
                        R.id.action_organizerLandingFragment_to_createEventFragment));

        view.findViewById(R.id.btnExplore)
                .setOnClickListener(v -> safeNav(view,
                        R.id.action_organizerLandingFragment_to_exploreEventsFragment));

        toggle.addOnButtonCheckedListener((g, id, checked) -> {
            if (checked) applyFilter(id == R.id.btnUpcoming);
        });

        loadEvents();
    }

    private void loadEvents() {
        if (firestore == null) {
            updateEmptyState();
            return;
        }

        firestore.collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    fullList.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());
                        fullList.add(e);
                    }
                    applyFilter(true);
                })
                .addOnFailureListener(e -> updateEmptyState());
    }

    private void applyFilter(boolean showUpcoming) {
        filteredList.clear();
        for (Event e : fullList) {
            if (showUpcoming && !e.isPastEvent()) filteredList.add(e);
            else if (!showUpcoming && e.isPastEvent()) filteredList.add(e);
        }
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = filteredList.isEmpty();
        emptyStateLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void safeNav(View root, int actionId) {
        try {
            NavController nav = Navigation.findNavController(root);
            nav.navigate(actionId);
        } catch (Exception ignored) {}
    }
}
