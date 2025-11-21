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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Shows full event details including cover image, description,
 * date/time/location, join waiting list, and QR code.
 */
public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn, btnViewQr;
    private ImageView ivEventCover;

    private String eventId;
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
        ivEventCover = view.findViewById(R.id.ivEventCover);

        Bundle args = getArguments();
        if (args != null) {

            eventId = args.getString("eventId");

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

            // ⭐ Load event poster image
            String imageUrl = args.getString("imageUrl", null);
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
                NavHostFragment.findNavController(this).popBackStack());

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

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, bundle);
        });
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

        attendeeRef.set(new AttendeeData(currentUser))
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added ✅");
                    Snackbar.make(v, "Joined waiting list.", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error joining waiting list", e));
    }

    /** Check if already joined */
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
                        joinBtn.setText("Added ✅");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error checking existing waiting list entry", e));
    }

    /** Firestore attendee model */
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
