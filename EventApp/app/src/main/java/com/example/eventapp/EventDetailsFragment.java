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
import com.example.eventapp.utils.NotificationHelper;
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

public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private MaterialButton btnAccept, btnDecline;
    private ImageView ivEventCover;

    private String eventId;
    private String organizerId;
    private String organizerEmail;
    private String eventName = "";   // ⭐ NEW: event name stored here

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

        // UI references
        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnManageEvent = view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        ivEventCover = view.findViewById(R.id.ivEventCover);

        btnAccept = view.findViewById(R.id.btnAccept);
        btnDecline = view.findViewById(R.id.btnDecline);

        btnAccept.setOnClickListener(v -> handleAccept());
        btnDecline.setOnClickListener(v -> handleDecline());

        // ----------- READ ARGUMENTS -----------

        Bundle args = getArguments();
        if (args != null) {

            eventId = args.getString("eventId", "");
            organizerId = args.getString("organizerId", "");
            organizerEmail = args.getString("organizerEmail", "");

            eventName = args.getString("title", "This Event");   // ⭐ STORE EVENT NAME

            ((TextView) view.findViewById(R.id.tvEventTitle)).setText(eventName);
            ((TextView) view.findViewById(R.id.tvEventDate)).setText(args.getString("date", ""));
            ((TextView) view.findViewById(R.id.tvEventTime)).setText(args.getString("time", ""));
            ((TextView) view.findViewById(R.id.tvEventLocation)).setText(args.getString("location", ""));
            ((TextView) view.findViewById(R.id.tvEventDescription)).setText(args.getString("desc", ""));

            TextView tvOrg = view.findViewById(R.id.tvOrganizerName);
            tvOrg.setText(organizerEmail != null ? organizerEmail : "");

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

        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // If current user is the organizer
        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        checkIfAlreadyJoined();

        joinBtn.setOnClickListener(this::addUserToWaitingList);

        btnViewQr.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("qrData",
                    "Event: " + eventName +
                            "\nDate: " + ((TextView) view.findViewById(R.id.tvEventDate)).getText() +
                            "\nTime: " + ((TextView) view.findViewById(R.id.tvEventTime)).getText() +
                            "\nLocation: " + ((TextView) view.findViewById(R.id.tvEventLocation)).getText()
            );
            bundle.putBoolean("cameFromDetails", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, bundle);
        });

        btnManageEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_manageEventsFragment, bundle);
        });

        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmation());

        checkSelectionStatus();
    }

    // ------------------ ADD USER TO WAITING LIST ------------------

    private void addUserToWaitingList(View v) {
        if (currentUser == null) {
            Snackbar.make(v, "Please log in first.", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Event not found.", Snackbar.LENGTH_LONG).show();
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
        data.put("status", "waiting");

        attendeeRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added to waiting list");
                    joinBtn.setAlpha(0.6f);

                    Snackbar.make(v, "Joined waiting list for " + eventName, Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(v, "Failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }

    // ------------------ CHECK IF ALREADY JOINED ------------------

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
                        joinBtn.setText("Added to waiting list");
                        joinBtn.setAlpha(0.6f);
                    }
                });
    }

    // ------------------ SHOW SELECT STATUS ------------------

    private void checkSelectionStatus() {
        if (currentUser == null || eventId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String status = doc.getString("status");

                    if ("selected".equals(status)) {
                        btnAccept.setVisibility(View.VISIBLE);
                        btnDecline.setVisibility(View.VISIBLE);
                        joinBtn.setVisibility(View.GONE);
                    }
                });
    }

    // ------------------ ACCEPT INVITATION ------------------

    private void handleAccept() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .update("status", "enrolled")
                .addOnSuccessListener(unused -> {

                    // ⭐ Notify user with event name
                    NotificationHelper.notifyUser(
                            getContext(),
                            currentUser.getUid(),
                            "USER_ENROLLED",
                            "Enrollment Confirmed",
                            eventName + ": You are now enrolled!"
                    );

                    Snackbar.make(requireView(),
                            "You are enrolled!", Snackbar.LENGTH_LONG).show();

                    btnAccept.setVisibility(View.GONE);
                    btnDecline.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Accept failed", e));
    }

    // ------------------ DECLINE INVITATION ------------------

    private void handleDecline() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {

                    // ⭐ Notify user with event name
                    NotificationHelper.notifyUser(
                            getContext(),
                            currentUser.getUid(),
                            "USER_DECLINED",
                            "Spot Declined",
                            eventName + ": You have declined your invitation."
                    );

                    Snackbar.make(requireView(),
                            "Invitation declined.", Snackbar.LENGTH_LONG).show();

                    btnAccept.setVisibility(View.GONE);
                    btnDecline.setVisibility(View.GONE);

                    triggerReplacementDraw();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Decline failed", e));
    }

    // ------------------ SELECT NEXT USER ------------------

    private void triggerReplacementDraw() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "not_selected")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        Log.d(TAG, "No replacement available.");
                        return;
                    }

                    DocumentSnapshot next = snapshot.getDocuments()
                            .get((int) (Math.random() * snapshot.size()));

                    firestore.collection("eventAttendees")
                            .document(eventId)
                            .collection("attendees")
                            .document(next.getId())
                            .update("status", "selected")
                            .addOnSuccessListener(unused ->
                                    Log.d(TAG, "Replacement selected: " + next.getId())
                            );
                });
    }

    // ------------------ DELETE EVENT ------------------

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

                    Snackbar.make(requireView(),
                            "Event deleted.", Snackbar.LENGTH_SHORT).show();

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
                    firestore.collection("eventAttendees")
                            .document(eventId).delete();
                });
    }

    // ------------------ DATA CLASS ------------------

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
            this.status = "waiting";
        }
    }
}
