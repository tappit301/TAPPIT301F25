package com.example.eventapp;

import android.content.Context;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventId", "");

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Error: Missing eventId");
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

        btnRunLottery.setOnClickListener(v -> showSampleSizeDialog());

        btnNotifyWaiting.setOnClickListener(v -> notifyByStatus("waiting"));
        btnNotifySelected.setOnClickListener(v -> notifyByStatus("selected"));
        btnNotifyCancelled.setOnClickListener(v -> notifyByStatus("cancelled"));

        btnExportCsv.setOnClickListener(v -> exportCsv());

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
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getString("userId");
                        String email = doc.getString("email");
                        attendees.add(new Attendee(uid, email, status));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading attendees", e));
    }

    private void showSampleSizeDialog() {
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter number to sample");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Run Lottery")
                .setMessage("How many entrants should be selected?")
                .setView(input)
                .setPositiveButton("Run", (dialog, which) -> {
                    String txt = input.getText().toString().trim();
                    if (txt.isEmpty()) {
                        Toast.makeText(requireContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    runLottery(Integer.parseInt(txt));
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
                .addOnSuccessListener(waiting -> {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot d : waiting) ids.add(d.getId());
                    if (ids.isEmpty()) {
                        Snackbar.make(requireView(), "Nobody is in the waiting list.", Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    Collections.shuffle(ids, new Random());
                    int count = Math.min(sampleSize, ids.size());
                    List<String> chosen = ids.subList(0, count);

                    for (String uid : chosen) {
                        firestore.collection("eventAttendees")
                                .document(eventId)
                                .collection("attendees")
                                .document(uid)
                                .update("status", "selected")
                                .addOnSuccessListener(a ->
                                        NotificationHelper.notifyUser(
                                                getContext(),
                                                uid,
                                                "lottery_selected",
                                                "You were selected!",
                                                "Please accept or decline your invitation."
                                        )
                                );
                    }

                    loadListByStatus("selected");
                    Snackbar.make(requireView(), count + " selected.", Snackbar.LENGTH_LONG).show();
                });
    }

    private void notifyByStatus(String status) {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", status)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getString("userId");
                        NotificationHelper.notifyUser(
                                getContext(),
                                uid,
                                "custom_broadcast",
                                "Event Update",
                                "Organizer sent you a message regarding your event status (" + status + ")."
                        );
                    }
                    Snackbar.make(requireView(),
                            "Notified all " + status + " users.",
                            Snackbar.LENGTH_LONG).show();
                });
    }

    private void exportCsv() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "enrolled")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        Snackbar.make(requireView(),
                                "No enrolled users to export.",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    StringBuilder csv = new StringBuilder();
                    csv.append("userId,email,status\n");

                    for (QueryDocumentSnapshot doc : snapshot) {
                        csv.append(doc.getString("userId")).append(",");
                        csv.append(doc.getString("email")).append(",");
                        csv.append("enrolled\n");
                    }

                    try {
                        File f = new File(requireContext().getExternalFilesDir(null),
                                "Event_" + eventId + "_enrolled.csv");
                        FileOutputStream out = new FileOutputStream(f);
                        out.write(csv.toString().getBytes());
                        out.close();

                        Snackbar.make(requireView(),
                                "CSV saved: " + f.getAbsolutePath(),
                                Snackbar.LENGTH_LONG).show();

                    } catch (IOException e) {
                        Log.e(TAG, "CSV EXPORT FAILED", e);
                    }
                });
    }
}

