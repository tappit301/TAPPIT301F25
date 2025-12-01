package com.example.eventapp;

import android.text.InputType;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.example.eventapp.utils.NotificationHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ManageEventsFragment extends Fragment {

    private static final String TAG = "ManageEventsFragment";

    private RecyclerView recyclerView;
    private AttendeeAdapter adapter;
    private final List<Attendee> attendees = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String eventId;

    // UI elements
    private View btnWaiting, btnSelected, btnEnrolled, btnCancelled;
    private View btnEditEvent, btnRunLottery;
    private View btnNotifyWaiting, btnNotifySelected, btnNotifyCancelled, btnExportCsv;
    private View btnViewMap;

    public ManageEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();

        recyclerView = view.findViewById(R.id.recyclerViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AttendeeAdapter(attendees);
        recyclerView.setAdapter(adapter);

        // UI BUTTONS
        btnEditEvent = view.findViewById(R.id.btnEditEvent);
        btnRunLottery = view.findViewById(R.id.btnRunLottery);

        btnWaiting = view.findViewById(R.id.btnWaiting);
        btnSelected = view.findViewById(R.id.btnSelected);
        btnEnrolled = view.findViewById(R.id.btnEnrolled);
        btnCancelled = view.findViewById(R.id.btnCancelled);

        btnNotifyWaiting = view.findViewById(R.id.btnNotifyWaiting);
        btnNotifySelected = view.findViewById(R.id.btnNotifySelected);
        btnNotifyCancelled = view.findViewById(R.id.btnNotifyCancelled);
        btnExportCsv = view.findViewById(R.id.btnExportCsv);

        btnViewMap = view.findViewById(R.id.btnViewMap);

        // Read eventId
        Bundle args = getArguments();
        if (args != null)
            eventId = args.getString("eventId", "");

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "⚠ No eventId received!");
            return;
        }

        // ----------- EDIT EVENT -----------
        btnEditEvent.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, b);
        });

        // ----------- VIEW MAP -----------
        btnViewMap.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_eventMapFragment, b);
        });

        // ----------- FILTER BUTTONS -----------
        btnWaiting.setOnClickListener(v -> loadList("waiting"));
        btnSelected.setOnClickListener(v -> loadList("selected"));
        btnEnrolled.setOnClickListener(v -> loadList("enrolled"));
        btnCancelled.setOnClickListener(v -> loadList("cancelled"));

        // ----------- LOTTERY -----------
        btnRunLottery.setOnClickListener(v -> showSampleSizeDialog());

        // ----------- NOTIFICATIONS -----------
        btnNotifyWaiting.setOnClickListener(v -> notifyGroup("waiting"));
        btnNotifySelected.setOnClickListener(v -> notifyGroup("selected"));
        btnNotifyCancelled.setOnClickListener(v -> notifyGroup("cancelled"));

        // ----------- CSV EXPORT -----------
        btnExportCsv.setOnClickListener(v -> exportCsv());

        // DEFAULT = waiting list
        loadList("waiting");
    }

    // =====================================================================
    // LOAD LIST BASED ON STATUS
    // =====================================================================
    private void loadList(String status) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    attendees.clear();
                    snapshot.forEach(doc -> attendees.add(
                            new Attendee(
                                    doc.getString("userId"),
                                    doc.getString("email"),
                                    status
                            )
                    ));
                    adapter.notifyDataSetChanged();
                });
    }

    // =====================================================================
    // LOTTERY
    // =====================================================================
    private void showSampleSizeDialog() {
        final EditText input = new EditText(requireContext());
        input.setHint("How many people?");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Run Lottery")
                .setView(input)
                .setPositiveButton("Run", (dialog, which) -> {
                    String n = input.getText().toString().trim();
                    if (!n.isEmpty()) runLottery(Integer.parseInt(n));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void runLottery(int sampleSize) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "waiting")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> list = new ArrayList<>();
                    snapshot.forEach(doc -> list.add(doc.getId()));

                    if (list.isEmpty()) {
                        Snackbar.make(requireView(), "No users waiting.", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    Collections.shuffle(list, new Random());
                    List<String> selected = list.subList(0, Math.min(sampleSize, list.size()));

                    selected.forEach(uid ->
                            firestore.collection("eventAttendees")
                                    .document(eventId)
                                    .collection("attendees")
                                    .document(uid)
                                    .update("status", "selected")
                    );

                    Snackbar.make(requireView(),
                            "Selected " + selected.size() + " users", Snackbar.LENGTH_LONG).show();

                    loadList("selected");
                });
    }

    // =====================================================================
    // NOTIFICATIONS
    // =====================================================================
    private void notifyGroup(String status) {

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Snackbar.make(requireView(), "No users found.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    String message = switch (status) {
                        case "waiting" -> "You are still on the waiting list.";
                        case "selected" -> "You were selected! Please accept or decline.";
                        case "cancelled" -> "Your spot has been cancelled.";
                        default -> "Event update.";
                    };

                    snapshot.forEach(doc ->
                            NotificationHelper.notifyUser(
                                    getContext(),
                                    doc.getId(),
                                    "event_update",
                                    "Event Update",
                                    message
                            ));

                    Snackbar.make(requireView(),
                            "Notifications sent.", Snackbar.LENGTH_LONG).show();
                });
    }

    // =====================================================================
    // CSV EXPORT
    // =====================================================================
    private void exportCsv() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "enrolled")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Snackbar.make(requireView(), "No enrolled users.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    StringBuilder csv = new StringBuilder("UserId,Email,Status\n");

                    snapshot.forEach(doc -> {
                        csv.append(doc.getString("userId")).append(",");
                        csv.append(doc.getString("email")).append(",");
                        csv.append("enrolled").append("\n");
                    });

                    try {
                        File file = new File(requireContext().getExternalFilesDir(null),
                                "enrolled_list.csv");

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(csv.toString().getBytes());
                        fos.close();

                        Snackbar.make(requireView(),
                                "CSV saved:\n" + file.getAbsolutePath(),
                                Snackbar.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.e(TAG, "CSV export failed", e);
                    }
                });
    }

}
