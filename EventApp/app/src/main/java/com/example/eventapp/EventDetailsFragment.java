package com.example.eventapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.material.textfield.TextInputEditText;
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
 * - Join waiting list (Firebase user or guest)
 * - QR view
 * - Organizer-only: Manage(Edit) + Delete
 * - NEW: Accept / Decline invitation if user is selected
 */
public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private MaterialButton btnAccept, btnDecline; // ✅ NEW
    private ImageView ivEventCover;

    private String eventId;
    private String organizerId;
    private String organizerEmail;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    // Local identity (can be Firebase user or guest user)
    private String localUserId;
    private String localUserEmail;
    private String localUserName;

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
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Resolve identity (Firebase user or previously-saved guest)
        resolveLocalIdentity();

        // ----- UI references -----
        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnManageEvent = view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        ivEventCover = view.findViewById(R.id.ivEventCover);

        // ✅ NEW: Accept / Decline buttons
        btnAccept = view.findViewById(R.id.btnAccept);
        btnDecline = view.findViewById(R.id.btnDecline);

        if (btnAccept != null) btnAccept.setVisibility(View.GONE);
        if (btnDecline != null) btnDecline.setVisibility(View.GONE);

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
                firebaseUser != null &&
                        organizerId != null &&
                        organizerId.equals(firebaseUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        // Join waiting list visible for everyone (organizer can join too)
        joinBtn.setVisibility(View.VISIBLE);

        // Disable join if already joined (for Firebase user or guest)
        checkIfAlreadyJoined();

        // Join click (handles Firebase user or guest)
        joinBtn.setOnClickListener(this::handleJoinClick);

        // ✅ NEW: Accept / Decline handlers
        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> handleAccept());
        }
        if (btnDecline != null) {
            btnDecline.setOnClickListener(v -> handleDecline());
        }

        // ------------------------------------------------------------------
        // QR click: encodes ONLY eventId so ScanQrFragment can look it up
        // ------------------------------------------------------------------
        btnViewQr.setOnClickListener(v -> {
            if (eventId == null || eventId.isEmpty()) {
                Snackbar.make(v, "Error: Missing event ID for QR.", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, "btnViewQr clicked but eventId is null/empty");
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("qrData", eventId);        // QR contains eventId
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

        // ✅ NEW: check if user is selected and show Accept/Decline
        checkSelectionStatus();
    }

    // -----------------------------------------------------------------------
    // IDENTITY RESOLUTION (Firebase user OR guest user stored in preferences)
    // -----------------------------------------------------------------------
    private void resolveLocalIdentity() {
        if (firebaseUser != null) {
            localUserId = firebaseUser.getUid();
            localUserEmail = firebaseUser.getEmail();
            localUserName = firebaseUser.getDisplayName();
            return;
        }

        SharedPreferences prefs =
                requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        localUserId = prefs.getString("GUEST_USER_ID", null);
        localUserEmail = prefs.getString("GUEST_USER_EMAIL", null);
        localUserName = prefs.getString("GUEST_USER_NAME", null);
    }

    // -----------------------------------------------------------------------
    // JOIN BUTTON CLICK
    // -----------------------------------------------------------------------
    private void handleJoinClick(View v) {
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Error: Event not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // If we already have an identity (Firebase user or saved guest), just join
        if (localUserId != null && localUserEmail != null) {
            addUserToWaitingListWithIdentity(v, localUserId, localUserEmail);
            return;
        }

        // No Firebase user and no saved guest identity -> ask for details
        showGuestInfoDialog(v);
    }

    // -----------------------------------------------------------------------
    // GUEST DIALOG: ASK NAME + EMAIL, CREATE GUEST USER, THEN JOIN
    // -----------------------------------------------------------------------
    private void showGuestInfoDialog(View anchorView) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_guest_info, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etGuestName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etGuestEmail);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Your details")
                .setMessage("Enter your name and email so we can add you to the waiting list.")
                .setView(dialogView)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String name = etName.getText() != null
                            ? etName.getText().toString().trim()
                            : "";
                    String email = etEmail.getText() != null
                            ? etEmail.getText().toString().trim()
                            : "";

                    if (name.isEmpty() || email.isEmpty()) {
                        Snackbar.make(anchorView,
                                "Name and email are required.",
                                Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    // Build a stable guest ID based on device id
                    String androidId = Settings.Secure.getString(
                            requireContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID
                    );
                    localUserId = "guest_" + androidId;
                    localUserEmail = email;
                    localUserName = name;

                    // Save locally
                    SharedPreferences prefs =
                            requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("GUEST_USER_ID", localUserId)
                            .putString("GUEST_USER_EMAIL", localUserEmail)
                            .putString("GUEST_USER_NAME", localUserName)
                            .apply();

                    // Save guest user to Firestore like a normal user document
                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("userId", localUserId);
                    userDoc.put("name", localUserName);
                    userDoc.put("email", localUserEmail);
                    userDoc.put("type", "guest");
                    userDoc.put("createdAt", Timestamp.now());

                    firestore.collection("users")
                            .document(localUserId)
                            .set(userDoc)
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to save guest user document", e));

                    // Now actually join the waiting list using this guest identity
                    addUserToWaitingListWithIdentity(anchorView, localUserId, localUserEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // -----------------------------------------------------------------------
    // ACTUAL JOIN LOGIC (REUSED FOR FIREBASE USER OR GUEST USER)
    // -----------------------------------------------------------------------
    private void addUserToWaitingListWithIdentity(View v, String userId, String email) {
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Error: Event not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        DocumentReference attendeeRef = firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("email", email);
        data.put("joinedAt", Timestamp.now());
        data.put("status", "waiting");

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

    /** Old signature kept but now routes through unified logic. */
    private void addUserToWaitingList(View v) {
        handleJoinClick(v);
    }

    /** Check if already joined (works for Firebase user or guest user). */
    private void checkIfAlreadyJoined() {
        if (eventId == null || eventId.isEmpty()) return;

        // Use Firebase uid if present; otherwise, check guest id from prefs
        String idToCheck;
        if (firebaseUser != null) {
            idToCheck = firebaseUser.getUid();
        } else {
            SharedPreferences prefs =
                    requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
            idToCheck = prefs.getString("GUEST_USER_ID", null);
        }

        if (idToCheck == null) {
            return;
        }

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(idToCheck)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        joinBtn.setEnabled(false);
                        joinBtn.setText("Added");
                        joinBtn.setAlpha(0.6f);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking waiting list entry", e));
    }

    // ✅ NEW — Check selection status after lottery and show Accept/Decline
    private void checkSelectionStatus() {
        if (eventId == null || eventId.isEmpty()) return;
        if (localUserId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(localUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String status = doc.getString("status");
                    if ("selected".equals(status)) {
                        if (btnAccept != null) btnAccept.setVisibility(View.VISIBLE);
                        if (btnDecline != null) btnDecline.setVisibility(View.VISIBLE);
                        if (joinBtn != null) joinBtn.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking selection status", e));
    }

    // ✅ NEW — Accept Invitation
    private void handleAccept() {
        if (eventId == null || eventId.isEmpty() || localUserId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(localUserId)
                .update("status", "enrolled")
                .addOnSuccessListener(unused -> {
                    Snackbar.make(requireView(), "You are enrolled!", Snackbar.LENGTH_LONG).show();
                    if (btnAccept != null) btnAccept.setVisibility(View.GONE);
                    if (btnDecline != null) btnDecline.setVisibility(View.GONE);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error accepting invitation", e));
    }

    // ✅ NEW — Decline Invitation
    private void handleDecline() {
        if (eventId == null || eventId.isEmpty() || localUserId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(localUserId)
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {
                    Snackbar.make(requireView(), "Invitation declined.", Snackbar.LENGTH_LONG).show();
                    if (btnAccept != null) btnAccept.setVisibility(View.GONE);
                    if (btnDecline != null) btnDecline.setVisibility(View.GONE);
                    triggerReplacementDraw();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error declining invitation", e));
    }

    // ✅ NEW — Automatically select next user after someone declines
    private void triggerReplacementDraw() {
        if (eventId == null || eventId.isEmpty()) return;

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
                            )
                            .addOnFailureListener(e ->
                                    Log.e(TAG, "Failed to update replacement attendee", e));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error running replacement draw", e));
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

    /** Firestore attendee model (you can still use this elsewhere if needed). */
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
