package com.example.eventapp;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.view.ViewGroup;

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
    private List<Attendee> attendees = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String eventId;

    // UI
    private View btnWaiting, btnSelected, btnEnrolled, btnCancelled;
    private View btnEditEvent, btnRunLottery;
    private View btnNotifyWaiting, btnNotifySelected, btnNotifyCancelled, btnExportCsv;

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

        // buttons
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

        // Get event ID
        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventId", "");

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "No eventId received");
            return;
        }

        // ------------------ BUTTON LISTENERS ------------------

        btnEditEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, bundle);
        });

        btnRunLottery.setOnClickListener(v -> showSampleSizeDialog());

        btnWaiting.setOnClickListener(v -> loadList("waiting"));
        btnSelected.setOnClickListener(v -> loadList("selected"));
        btnEnrolled.setOnClickListener(v -> loadList("enrolled"));
        btnCancelled.setOnClickListener(v -> loadList("cancelled"));

        btnNotifyWaiting.setOnClickListener(v -> sendNotificationToGroup("waiting"));
        btnNotifySelected.setOnClickListener(v -> sendNotificationToGroup("selected"));
        btnNotifyCancelled.setOnClickListener(v -> sendNotificationToGroup("cancelled"));

        btnExportCsv.setOnClickListener(v -> exportEnrolledCsv());

        loadList("waiting");
    }

    // ------------------------- LOAD LIST ----------------------------

    private void loadList(String status) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    attendees.clear();
                    snapshot.getDocuments().forEach(doc -> {
                        String uid = doc.getString("userId");
                        String email = doc.getString("email");
                        attendees.add(new Attendee(uid, email, status));
                    });
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading list", e));
    }

    // ------------------------- LOTTERY ----------------------------

    private void showSampleSizeDialog() {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("How many people to sample?");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Lottery")
                .setMessage("Enter the number of people to select:")
                .setView(input)
                .setPositiveButton("Run", (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) {
                        Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    runLottery(Integer.parseInt(text));
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
                    List<String> waitingList = new ArrayList<>();
                    snapshot.getDocuments().forEach(doc -> waitingList.add(doc.getId()));

                    if (waitingList.isEmpty()) {
                        Snackbar.make(requireView(), "No users in waiting list.", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    Collections.shuffle(waitingList, new Random());
                    int pick = Math.min(sampleSize, waitingList.size());

                    List<String> selected = waitingList.subList(0, pick);

                    for (String uid : selected) {
                        firestore.collection("eventAttendees")
                                .document(eventId)
                                .collection("attendees")
                                .document(uid)
                                .update("status", "selected");
                    }

                    Snackbar.make(requireView(), pick + " users selected!", Snackbar.LENGTH_LONG).show();
                    loadList("selected");
                });
    }

    // ------------------------- NOTIFICATION ----------------------------

    private void sendNotificationToGroup(String status) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Snackbar.make(requireView(), "No users in this list.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // Corrected messages
                    String message;
                    switch (status) {
                        case "waiting":
                            message = "You are still on the waiting list for this event.";
                            break;
                        case "selected":
                            message = "You have been selected! Please accept or decline your spot.";
                            break;
                        case "cancelled":
                            message = "Your spot has been cancelled. Contact the organizer for help.";
                            break;
                        default:
                            message = "You have an update regarding the event.";
                            break;
                    }

                    String title = "Event Update";

                    // Send notification to each user
                    snapshot.getDocuments().forEach(doc -> {
                        String uid = doc.getString("userId");

                        NotificationHelper.notifyUser(
                                getContext(),
                                uid,
                                "group_notify",
                                title,
                                message
                        );
                    });

                    Snackbar.make(requireView(), "Notifications sent!", Snackbar.LENGTH_LONG).show();
                });
    }

    // ------------------------- CSV EXPORT ----------------------------

    private void exportEnrolledCsv() {
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

                    snapshot.getDocuments().forEach(doc -> {
                        csv.append(doc.getString("userId")).append(",");
                        csv.append(doc.getString("email")).append(",");
                        csv.append("enrolled").append("\n");
                    });

                    try {
                        File file = new File(
                                requireContext().getExternalFilesDir(null),
                                "enrolled_list.csv"
                        );

                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(csv.toString().getBytes());
                        fos.close();

                        Snackbar.make(requireView(),
                                "CSV saved: " + file.getAbsolutePath(),
                                Snackbar.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.e(TAG, "CSV export failed", e);
                    }
                });
    }
}

