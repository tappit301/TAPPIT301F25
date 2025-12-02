package com.example.eventapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
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

        // ---------------- EXPLORE BUTTON ----------------
        ImageButton btnExplore = view.findViewById(R.id.btnExplore);
        btnExplore.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_organizerLandingFragment_to_exploreEventsFragment);});

        // ---------------- CREATE EVENT BUTTONS ----------------
        View.OnClickListener createClick = v ->
                navController.navigate(R.id.action_organizerLandingFragment_to_createEventFragment);

        MaterialButton btnAddEventEmpty = view.findViewById(R.id.btnAddEventEmpty);
        FloatingActionButton btnAddEvent = view.findViewById(R.id.btnAddEvent);
        MaterialButton btnCreateEventTop = view.findViewById(R.id.btnCreateEventTop);

        if (btnAddEventEmpty != null) btnAddEventEmpty.setOnClickListener(createClick);
        if (btnAddEvent != null) btnAddEvent.setOnClickListener(createClick);
        if (btnCreateEventTop != null) btnCreateEventTop.setOnClickListener(createClick);

        // ---------------- PROFILE ICON (if present) ----------------
        ImageView btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v ->
                    navController.navigate(R.id.action_organizerLandingFragment_to_profileFragment)
            );
            // If you had a method to load avatar into toolbar:
            // loadProfileAvatarIntoToolbar(btnProfile);
        }

        // ---------------- NOTIFICATIONS BUTTON (IN-APP DROPDOWN) ----------------
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationsDialog());
        }

        // ---------------- RV + FILTERS (your existing logic) ----------------
        btnUpcoming = view.findViewById(R.id.btnUpcoming);
        btnPast = view.findViewById(R.id.btnPast);

        rvEvents = view.findViewById(R.id.rvEvents);
        emptyState = view.findViewById(R.id.emptyStateLayout);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        // Default adapter = upcoming events
        adapter = new EventAdapter(
                upcomingEvents,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment
        );
        rvEvents.setAdapter(adapter);

        setupFilters();
        loadAllEventsForUser();
    }

    // Load user's profile picture URL and show it in toolbar icon
    private void loadProfileAvatarIntoToolbar(ImageView imageView) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return;
        }

        String uid = user.getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String url = doc.getString("profileImageUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this)
                                    .load(url)
                                    .circleCrop()   // small circular icon
                                    .into(imageView);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load profile avatar", e));
    }

    // ----------------------
    // FILTER BUTTONS
    // ----------------------
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

        btnUpcoming.setChecked(true);
    }

    // ----------------------
    // LOAD CREATED + JOINED
    // ----------------------
    private void loadAllEventsForUser() {
        String userId = null;
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            userId = user.getUid();
        } else {
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            userId = prefs.getString("GUEST_USER_ID", null);
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Please sign in or join an event first.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String effectiveUserId = userId;

        allEvents.clear();
        eventIdSet.clear();
        upcomingEvents.clear();
        pastEvents.clear();

        // 1 — Load ORGANIZED events
        firestore.collection("events")
                .whereEqualTo("organizerId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                if (eventIdSet.add(event.getId())) {
                                    allEvents.add(event);
                                }
                            }
                        }
                    }

                    loadJoinedEvents(effectiveUserId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load organized", e);
                    loadJoinedEvents(effectiveUserId);
                });
    }

    private void loadJoinedEvents(String userId) {

        firestore.collectionGroup("attendees")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(attendeeSnap -> {

                    if (attendeeSnap == null || attendeeSnap.isEmpty()) {
                        refreshListsAndAdapter();
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
                                        if (e != null) {
                                            e.setId(eventDoc.getId());
                                            if (eventIdSet.add(e.getId()))
                                                allEvents.add(e);
                                        }
                                    }
                                    refreshListsAndAdapter();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load joined", e);
                    refreshListsAndAdapter();
                });
    }

    // ----------------------
    // SPLIT + ADAPTER REFRESH
    // ----------------------
    private void refreshListsAndAdapter() {

        splitEventsByTime();

        boolean empty = allEvents.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvEvents.setVisibility(empty ? View.GONE : View.VISIBLE);

        List<Event> source = btnUpcoming.isChecked() ? upcomingEvents : pastEvents;

        adapter = new EventAdapter(
                source,
                R.id.action_organizerLandingFragment_to_eventDetailsFragment
        );
        rvEvents.setAdapter(adapter);
    }

    private void splitEventsByTime() {
        upcomingEvents.clear();
        pastEvents.clear();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        Date now = new Date();

        for (Event e : allEvents) {
            try {
                Date d = sdf.parse(e.getDate() + " " + e.getTime());
                if (d != null && !d.before(now))
                    upcomingEvents.add(e);
                else
                    pastEvents.add(e);

            } catch (Exception ex) {
                Log.e(TAG, "Parse failed for " + e.getId());
                pastEvents.add(e);
            }
        }
    }

    // ----------------------------------------------------------
// NOTIFICATIONS: FETCH ALL FOR CURRENT USER & SHOW DIALOG
// ----------------------------------------------------------
    /**
     * Fetch all notifications for this user from Firestore and show them in a dialog.
     * No filtering, just a simple list. If none, show “No notifications yet”.
     */
    private void showNotificationsDialog() {
        // 1) Determine which userId to use: Firebase user or guest
        String userId = null;

        FirebaseUser firebaseUser = auth != null ? auth.getCurrentUser()
                : FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseUser.getUid();
        } else {
            // Fallback to guest ID (same prefs you used in EventDetailsFragment)
            SharedPreferences prefs =
                    requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            userId = prefs.getString("GUEST_USER_ID", null);
        }

        if (userId == null) {
            Toast.makeText(getContext(),
                    "No user found. Please sign in or join an event first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 2) Query Firestore for this user's notifications
        firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)  // just to keep it small
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
                        String msg = doc.getString("message");
                        if (msg == null || msg.trim().isEmpty()) {
                            msg = "(no message)";
                        }
                        messages.add(msg);
                    }

                    CharSequence[] items = messages.toArray(new CharSequence[0]);

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Notifications")
                            .setItems(items, null)    // just display, no click actions
                            .setPositiveButton("Close", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load notifications", e);
                    Toast.makeText(getContext(),
                            "Failed to load notifications: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }



}
