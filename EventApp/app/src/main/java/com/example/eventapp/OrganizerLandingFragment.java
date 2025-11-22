package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

public class OrganizerLandingFragment extends Fragment {

    private RecyclerView rvEvents;
    private LinearLayout emptyState;

    private final List<Event> eventList = new ArrayList<>();
    private EventAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

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

        // ============================
        //       TOP TOOLBAR
        // ============================

        ImageButton btnExplore = view.findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_exploreEventsFragment)
        );

        MaterialButton btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        btnCreateEvent.setOnClickListener(v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment)
        );

        ImageButton btnNotifications = view.findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Notifications coming soon!", Toast.LENGTH_SHORT).show()
        );

        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null)
                startActivity(new Intent(requireContext(), SignInActivity.class));
            else
                Toast.makeText(requireContext(), "Signed in as " + user.getEmail(), Toast.LENGTH_SHORT).show();
        });

        // ============================
        //      LIST + EMPTY STATE
        // ============================

        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(eventList,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment);
        rvEvents.setAdapter(adapter);

        // ============================
        //     FLOATING ADD BUTTON
        // ============================

        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        btnAddEvent.setOnClickListener(v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment)
        );

        loadEvents();
    }

    private void loadEvents() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("events")
                .whereEqualTo("organizerId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {

                    eventList.clear();

                    if (snapshots != null) {
                        snapshots.forEach(doc -> {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                eventList.add(event);
                            }
                        });
                    }

                    boolean hasEvents = !eventList.isEmpty();
                    emptyState.setVisibility(hasEvents ? View.GONE : View.VISIBLE);
                    rvEvents.setVisibility(hasEvents ? View.VISIBLE : View.GONE);

                    adapter.notifyDataSetChanged();
                });
    }
}
