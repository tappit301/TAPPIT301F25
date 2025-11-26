package com.example.eventapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ManageEventsFragment extends Fragment {

    private static final String TAG = "ManageEventsFragment";

    private RecyclerView recyclerView;
    private AttendeeAdapter adapter;
    private List<Attendee> attendees = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String eventId;

    private View btnWaiting, btnSelected, btnEnrolled, btnCancelled;
    private View btnEditEvent, btnRunLottery;

    public ManageEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();

        recyclerView = view.findViewById(R.id.recyclerViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AttendeeAdapter(attendees);
        recyclerView.setAdapter(adapter);

        btnEditEvent = view.findViewById(R.id.btnEditEvent);
        btnRunLottery = view.findViewById(R.id.btnRunLottery);

        btnWaiting = view.findViewById(R.id.btnWaiting);
        btnSelected = view.findViewById(R.id.btnSelected);
        btnEnrolled = view.findViewById(R.id.btnEnrolled);
        btnCancelled = view.findViewById(R.id.btnCancelled);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId", "");
        }

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Error: missing eventId");
            return;
        }

        btnEditEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, bundle);
        });

        btnWaiting.setOnClickListener(v -> loadListByStatus("waiting"));
        btnSelected.setOnClickListener(v -> loadListByStatus("selected"));
        btnEnrolled.setOnClickListener(v -> loadListByStatus("enrolled"));
        btnCancelled.setOnClickListener(v -> loadListByStatus("cancelled"));

        btnRunLottery.setOnClickListener(v -> showLotteryDialog());

        // Load waiting list by default
        loadListByStatus("waiting");
    }

    private void loadListByStatus(String status) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    attendees.clear();
                    snapshot.getDocuments().forEach(doc -> {
                        String email = doc.getString("email");
                        String uid = doc.getString("userId");
                        attendees.add(new Attendee(uid, email, status));
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading attendees", e));
    }

    private void showLotteryDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Run Lottery")
                .setMessage("This will randomly pick attendees from the waiting list up to the event capacity.")
                .setPositiveButton("Run", (dialog, which) -> runLottery())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void runLottery() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(waitingSnapshot -> {

                    List<String> waitingList = new ArrayList<>();
                    for (var doc : waitingSnapshot.getDocuments()) {
                        waitingList.add(doc.getId());
                    }

                    if (waitingList.isEmpty()) {
                        Snackbar.make(requireView(), "No users are currently waiting.", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    // Fetch capacity from event
                    firestore.collection("events")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(eventDoc -> {

                                Long cap = eventDoc.getLong("capacity");
                                if (cap == null) cap = 1L;

                                long capacity = cap;

                                // Shuffle list to randomize
                                Collections.shuffle(waitingList, new Random());

                                // Pick min(capacity, waitingListSize)
                                List<String> selected = waitingList.subList(0,
                                        (int) Math.min(capacity, waitingList.size())
                                );

                                for (String uid : selected) {
                                    firestore.collection("eventAttendees")
                                            .document(eventId)
                                            .collection("attendees")
                                            .document(uid)
                                            .update("status", "selected")
                                            .addOnFailureListener(e ->
                                                    Log.e(TAG, "Error selecting attendee", e));
                                }

                                Snackbar.make(requireView(),
                                        selected.size() + " attendee(s) selected.",
                                        Snackbar.LENGTH_LONG).show();

                                loadListByStatus("selected");
                            })
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Error fetching event capacity", e));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error fetching waiting list", e));
    }
}
