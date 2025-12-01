package com.example.eventapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrganizerLandingFragment extends Fragment {

    private static final String TAG = "OrganizerLanding";

    private RecyclerView rvEvents;
    private LinearLayout emptyState;

    private final List<Event> allEvents = new ArrayList<>();
    private final Set<String> eventIdSet = new HashSet<>();
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

        // ------------------------------------------------------------
        // EXPLORE BUTTON
        // ------------------------------------------------------------
        ImageButton btnExplore = view.findViewById(R.id.btnExplore);
        if (btnExplore != null) {
            btnExplore.setOnClickListener(v ->
                    navController.navigate(R.id.action_organizerLandingFragment_to_exploreEventsFragment)
            );
        }

        // ------------------------------------------------------------
        // CREATE EVENT BUTTONS
        // ------------------------------------------------------------
        View.OnClickListener createEventClick = v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment);

        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        MaterialButton btnCreateEventTop = view.findViewById(R.id.btnCreateEventTop);

        if (btnAddEventEmpty != null) btnAddEventEmpty.setOnClickListener(createEventClick);
        if (btnAddEvent != null) btnAddEvent.setOnClickListener(createEventClick);
        if (btnCreateEventTop != null) btnCreateEventTop.setOnClickListener(createEventClick);

        // ------------------------------------------------------------
        // PROFILE BUTTON & LOAD AVATAR
        // ------------------------------------------------------------
        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                    navController.navigate(R.id.action_organizerLandingFragment_to_profileFragment)
            );
            loadProfileAvatar(btnProfile);
        }

        // ------------------------------------------------------------
        // NOTIFICATION BUTTON (TOP RIGHT)
        // ------------------------------------------------------------
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationsDialog());
        }

        // ------------------------------------------------------------
        // RECYCLER + TABS
        // ------------------------------------------------------------
        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        setupFilterButtons();

        adapter = new EventAdapter(upcomingEvents,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment);
        rvEvents.setAdapter(adapter);

        loadAllEventsForUser();
    }

    // ------------------------------------------------------------
    // LOAD PROFILE IMAGE INTO TOOLBAR ICON
    // ------------------------------------------------------------
    private void loadProfileAvatar(ImageView imageView) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String url = doc.getString("profileImageUrl");
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this).load(url).circleCrop().into(imageView);
                    }
                });
    }

    // ------------------------------------------------------------
    // FILTER BUTTON LOGIC
    // ------------------------------------------------------------
    private void setupFilterButtons() {

        btnUpcoming.setOnClickListener(v -> {
            btnUpcoming.setChecked(true);
            btnPast.setChecked(false);

            adapter = new EventAdapter(upcomingEvents,
                    R.id.action_organizerLandingFragment_to_eventDetailsFragment);
            rvEvents.setAdapter(adapter);
        });

        btnPast.setOnClickListener(v -> {
            btnPast.setChecked(true);
            btnUpcoming.setChecked(false);

            adapter = new EventAdapter(pastEvents,
                    R.id.action_organizerLandingFragment_to_eventDetailsFragment);
            rvEvents.setAdapter(adapter);
        });

        btnUpcoming.setChecked(true);
    }

    // ------------------------------------------------------------
    // LOAD ORGANIZED + JOINED EVENTS
    // ------------------------------------------------------------
    private void loadAllEventsForUser() {

        String userId;

        FirebaseUser fb = auth.getCurrentUser();
        if (fb != null) {
            userId = fb.getUid();
        } else {
            SharedPreferences prefs =
                    requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            userId = prefs.getString("GUEST_USER_ID", null);
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in or join an event first.", Toast.LENGTH_SHORT).show();
            return;
        }

        allEvents.clear();
        eventIdSet.clear();
        upcomingEvents.clear();
        pastEvents.clear();

        // LOAD EVENTS CREATED BY USER
        firestore.collection("events")
                .whereEqualTo("organizerId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap != null) {
                        for (QueryDocumentSnapshot doc : snap) {
                            Event e = doc.toObject(Event.class);
                            e.setId(doc.getId());
                            if (eventIdSet.add(e.getId()))
                                allEvents.add(e);
                        }
                    }
                    loadJoinedEvents(userId);
                })
                .addOnFailureListener(e -> loadJoinedEvents(userId));
    }

    private void loadJoinedEvents(String userId) {
        firestore.collectionGroup("attendees")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(attendeeSnap -> {

                    if (attendeeSnap == null || attendeeSnap.isEmpty()) {
                        refreshLists();
                        return;
                    }

                    for (DocumentSnapshot attendeeDoc : attendeeSnap) {

                        String eventId = attendeeDoc.getReference()
                                .getParent()
                                .getParent()
                                .getId();

                        if (eventIdSet.contains(eventId)) continue;

                        firestore.collection("events")
                                .document(eventId)
                                .get()
                                .addOnSuccessListener(eventDoc -> {
                                    if (eventDoc.exists()) {
                                        Event e = eventDoc.toObject(Event.class);
                                        e.setId(eventDoc.getId());
                                        if (eventIdSet.add(e.getId()))
                                            allEvents.add(e);
                                    }
                                    refreshLists();
                                });
                    }
                })
                .addOnFailureListener(e -> refreshLists());
    }

    // ------------------------------------------------------------
    // REFRESH LISTS + UPDATE ADAPTER
    // ------------------------------------------------------------
    private void refreshLists() {

        splitEvents();

        boolean empty = allEvents.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);

        List<Event> source = btnUpcoming.isChecked() ? upcomingEvents : pastEvents;

        adapter = new EventAdapter(source,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment);
        rvEvents.setAdapter(adapter);
    }

    // ------------------------------------------------------------
    // SPLIT UPCOMING VS PAST
    // ------------------------------------------------------------
    private void splitEvents() {
        upcomingEvents.clear();
        pastEvents.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date now = new Date();

        for (Event e : allEvents) {
            try {
                Date d = sdf.parse(e.getDate() + " " + e.getTime());
                if (d != null && !d.before(now)) {
                    upcomingEvents.add(e);
                } else {
                    pastEvents.add(e);
                }
            } catch (Exception ex) {
                pastEvents.add(e);
            }
        }
    }

    // ------------------------------------------------------------
    // NOTIFICATION DIALOG
    // ------------------------------------------------------------
    private void showNotificationsDialog() {

        String userId;
        FirebaseUser fb = auth.getCurrentUser();

        if (fb != null) {
            userId = fb.getUid();
        } else {
            SharedPreferences prefs =
                    requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            userId = prefs.getString("GUEST_USER_ID", null);
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in to view notifications.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot == null || snapshot.isEmpty()) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Notifications")
                                .setMessage("No notifications yet.")
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }

                    List<String> messages = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        messages.add(doc.getString("message"));
                    }

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Notifications")
                            .setItems(messages.toArray(new String[0]), null)
                            .setPositiveButton("Close", null)
                            .show();
                });
    }
}

