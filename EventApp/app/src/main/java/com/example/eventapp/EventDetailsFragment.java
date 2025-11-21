package com.example.eventapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn;
    private MaterialButton btnViewQr;
    private MaterialButton btnDeleteEvent;

    private String eventId;
    private String organizerId;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        MaterialButton btnEditEvent = view.findViewById(R.id.btnEditEvent);


        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
            organizerId = args.getString("organizerId", "");

            ((TextView) view.findViewById(R.id.tvEventTitle))
                    .setText(args.getString("title", ""));
            ((TextView) view.findViewById(R.id.tvEventDate))
                    .setText(args.getString("date", ""));
            ((TextView) view.findViewById(R.id.tvEventTime))
                    .setText(args.getString("time", ""));
            ((TextView) view.findViewById(R.id.tvEventLocation))
                    .setText(args.getString("location", ""));
            ((TextView) view.findViewById(R.id.tvEventDescription))
                    .setText(args.getString("desc", ""));
        }

        // Back button
        ImageView btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        if (currentUser != null && organizerId != null && organizerId.equals(currentUser.getUid())) {
            btnDeleteEvent.setVisibility(View.VISIBLE);
            btnEditEvent.setVisibility(View.VISIBLE);
            joinBtn.setVisibility(View.VISIBLE);  // Organizer can join too

        } else {
            btnDeleteEvent.setVisibility(View.GONE);
            btnEditEvent.setVisibility(View.GONE);
            joinBtn.setVisibility(View.VISIBLE);
        }


        // Check if already joined
        checkIfAlreadyJoined();

        joinBtn.setOnClickListener(this::addUserToWaitingList);

        btnViewQr.setOnClickListener(v -> {
            String qrPayload = "Event: " +
                    ((TextView) view.findViewById(R.id.tvEventTitle)).getText() +
                    "\nDate: " +
                    ((TextView) view.findViewById(R.id.tvEventDate)).getText() +
                    "\nTime: " +
                    ((TextView) view.findViewById(R.id.tvEventTime)).getText() +
                    "\nLocation: " +
                    ((TextView) view.findViewById(R.id.tvEventLocation)).getText();

            Bundle bundle = new Bundle();
            bundle.putString("qrData", qrPayload);
            bundle.putBoolean("cameFromDetails", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, bundle);
        });

        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void addUserToWaitingList(View v) {
        if (currentUser == null) {
            Snackbar.make(v, "Please log in first.", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Missing eventId — cannot join waiting list");
            Snackbar.make(v, "Error: Event not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        DocumentReference attendeeRef = firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid());

        attendeeRef.set(new AttendeeData(currentUser))
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added \u2714");
                    joinBtn.setAlpha(0.6f); // faded look
                    Snackbar.make(v, "Joined waiting list.", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error joining waiting list", e));
    }

    private void checkIfAlreadyJoined() {
        if (currentUser == null || eventId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        joinBtn.setEnabled(false);
                        joinBtn.setText("Added \u2714");
                        joinBtn.setAlpha(0.6f);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking existing waiting list entry", e));
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEvent() {
        firestore.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteAttendeesForEvent();

                    Snackbar.make(requireView(), "Event deleted.", Snackbar.LENGTH_SHORT).show();

                    NavHostFragment.findNavController(this)
                            .popBackStack(R.id.organizerLandingFragment, false);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting event", e));
    }

    private void deleteAttendeesForEvent() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().delete();
                    }
                    firestore.collection("eventAttendees").document(eventId).delete();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error deleting attendees", e));
    }

    private static class AttendeeData {
        private final String userId;
        private final String email;
        private final Timestamp joinedAt;

        public AttendeeData(FirebaseUser user) {
            this.userId = user.getUid();
            this.email = user.getEmail();
            this.joinedAt = Timestamp.now();
        }
    }
}
