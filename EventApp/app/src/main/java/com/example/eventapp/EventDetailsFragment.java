package com.example.eventapp;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

import java.util.HashMap;
import java.util.Map;


/**
 * Combined Event Details:
 * - Poster image
 * - Join waiting list
 * - QR view
 * - Organizer-only: Manage(Edit) + Delete
 */
public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private ImageView ivEventCover;

    private String eventId;
    private String organizerId;
    private String organizerEmail;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnManageEvent = view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        ivEventCover = view.findViewById(R.id.ivEventCover);

        // ----- Read args -----
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId", "");
            organizerId = args.getString("organizerId", "");
            organizerEmail = args.getString("organizerEmail", "");

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

            TextView tvOrg = view.findViewById(R.id.tvOrganizerName);
            tvOrg.setText(organizerEmail == null ? "" : organizerEmail);

            // Poster
            String imageUrl = args.getString("imageUrl", "");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_img)
                        .error(R.drawable.placeholder_img)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(ivEventCover);
            } else {
                ivEventCover.setImageResource(R.drawable.placeholder_img);
            }
        }

        // Back
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Organizer-only button visibility
        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        // Join waiting list visible for everyone (organizer can join too)
        joinBtn.setVisibility(View.VISIBLE);

        // Disable join if already joined
        checkIfAlreadyJoined();

        // Join click
        joinBtn.setOnClickListener(this::addUserToWaitingList);

        // QR click
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

        // Manage Event click -> Go to ManageEventsFragment with eventId
        btnManageEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_manageEventsFragment, bundle);
        });


        // Delete click
        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmation());
    }

    /** Add user to waiting list */
    private void addUserToWaitingList(View v) {
        if (currentUser == null) {
            Snackbar.make(v, "Please log in first.", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Error: Event not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        DocumentReference attendeeRef = firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid());
        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUid());
        data.put("email", currentUser.getEmail());
        data.put("joinedAt", Timestamp.now());
        data.put("status", "waiting"); // ✅ NEW FIELD

        attendeeRef.set(data)


                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added ✅");
                    joinBtn.setAlpha(0.6f);
                    Snackbar.make(v, "Joined waiting list.", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error joining waiting list", e);
                    Snackbar.make(v, "Failed to join: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    /** Check if already joined */
    private void checkIfAlreadyJoined() {
        if (currentUser == null || eventId == null || eventId.isEmpty()) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        joinBtn.setEnabled(false);
                        joinBtn.setText("Added ✅");
                        joinBtn.setAlpha(0.6f);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking waiting list entry", e));
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
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteAttendeesForEvent();
                    Snackbar.make(requireView(), "Event deleted.",
                            Snackbar.LENGTH_SHORT).show();

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

    /** Firestore attendee model */
    public static class AttendeeData {
        public String userId;
        public String email;
        public Timestamp joinedAt;
        public String status;

        public AttendeeData() {}

        public AttendeeData(FirebaseUser user) {
            this.userId = user.getUid();
            this.email = user.getEmail();
            this.joinedAt = Timestamp.now();
            this.status = "waiting"; // ✅ Default status
        }
    }

}
