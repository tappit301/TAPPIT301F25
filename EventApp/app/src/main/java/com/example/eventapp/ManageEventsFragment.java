package com.example.eventapp;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Allows organizers to manage a specific event.
 * They can view attendees by status, edit the event,
 * run the lottery, open the map view, and export CSV.
 */
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

    /**
     * Initializes UI, loads event ID, sets up list switching and controls.
     */
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
            Log.e(TAG, "No eventId received");
            return;
        }

        // Edit Event
        btnEditEvent.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, b);
        });

        // Load lists
        btnWaiting.setOnClickListener(v -> loadListByStatus("waiting"));
        btnSelected.setOnClickListener(v -> loadListByStatus("selected"));
        btnEnrolled.setOnClickListener(v -> loadListByStatus("enrolled"));
        btnCancelled.setOnClickListener(v -> loadListByStatus("cancelled"));

        // Lottery
        btnRunLottery.setOnClickListener(v -> showLotteryDialog());

        // Map button
        View btnViewMap = view.findViewById(R.id.btnViewMap);
        btnViewMap.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_eventMapFragment, b);
        });

        // CSV EXPORT button
        Button btnExportCSV = view.findViewById(R.id.btnExportCSV);
        btnExportCSV.setOnClickListener(v -> {
            // Runtime permission (Android 6–10)
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);

            exportAllListsCSV(eventId);
        });

        loadListByStatus("waiting");
    }

    /**
     * Loads attendees filtered by the chosen status.
     */
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
                        Log.e(TAG, "Error loading list", e));
    }

    /**
     * Confirmation dialog before lottery.
     */
    private void showLotteryDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Run Lottery")
                .setMessage("This will randomly pick attendees from the waiting list.")
                .setPositiveButton("Run", (d, w) -> runLottery())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Randomly selects users from waiting list up to maxAttendees.
     */
    private void runLottery() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<String> waitingList = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> waitingList.add(doc.getId()));

                    if (waitingList.isEmpty()) {
                        Snackbar.make(requireView(),
                                "No users on the waiting list.",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    firestore.collection("events")
                            .document(eventId)
                            .get()
                            .addOnSuccessListener(eventDoc -> {
                                long max = eventDoc.getLong("maxAttendees") != null
                                        ? eventDoc.getLong("maxAttendees")
                                        : 1;

                                Collections.shuffle(waitingList, new Random());
                                List<String> selected = waitingList.subList(
                                        0,
                                        (int) Math.min(max, waitingList.size())
                                );

                                for (String uid : selected) {
                                    firestore.collection("eventAttendees")
                                            .document(eventId)
                                            .collection("attendees")
                                            .document(uid)
                                            .update("status", "selected");
                                }

                                Snackbar.make(requireView(),
                                        "Lottery completed. " + selected.size() + " users selected.",
                                        Snackbar.LENGTH_LONG).show();

                                loadListByStatus("selected");
                            });
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Lottery failed", e));
    }

    /**
     * EXPORT ALL LISTS — waiting, selected, enrolled, cancelled — into one CSV file.
     */
    private void exportAllListsCSV(String eventId) {

        String[] statuses = new String[]{"waiting", "selected", "enrolled", "cancelled"};

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("List,Email,UID\n");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final int[] remaining = {statuses.length};

        for (String status : statuses) {

            db.collection("eventAttendees")
                    .document(eventId)
                    .collection("attendees")
                    .whereEqualTo("status", status)
                    .get()
                    .addOnSuccessListener(snap -> {

                        snap.getDocuments().forEach(doc -> {
                            String email = doc.getString("email");
                            String uid = doc.getString("userId");

                            csvBuilder.append(status).append(",")
                                    .append(email == null ? "" : email).append(",")
                                    .append(uid).append("\n");
                        });

                        remaining[0]--;
                        if (remaining[0] == 0) {
                            saveCSVToFile(csvBuilder.toString(),
                                    "event_" + eventId + "_all_lists.csv");
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "CSV export failed for: " + status, e);
                        remaining[0]--;
                        if (remaining[0] == 0) {
                            saveCSVToFile(csvBuilder.toString(),
                                    "event_" + eventId + "_all_lists.csv");
                        }
                    });
        }
    }

    /**
     * Saves CSV into Downloads folder.
     */
    private void saveCSVToFile(String csv, String fileName) {
        try {
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloads, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csv.getBytes());
            fos.close();

            Snackbar.make(requireView(),
                    "CSV saved to Downloads: " + fileName,
                    Snackbar.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Snackbar.make(requireView(),
                    "Failed to save CSV: " + e.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        }
    }
}
