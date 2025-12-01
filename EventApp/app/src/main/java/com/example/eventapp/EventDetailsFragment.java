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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private ImageView ivEventCover;

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private String eventId;
    private String organizerId;
    private String organizerEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        firestore = FirebaseHelper.getFirestore();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnManageEvent = view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        ivEventCover = view.findViewById(R.id.ivEventCover);

        loadArgumentsIntoUI(view);
        configureOrganizerUI();
        configureJoinWaitlist();
        configureQrButton(view);
        configureManageEventButton(view);
        configureDeleteButton();
    }

    private void loadArgumentsIntoUI(View view) {
        Bundle args = getArguments();
        if (args == null) return;

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
        tvOrg.setText(organizerEmail);

        String imageUrl = args.getString("imageUrl", "");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_img)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(ivEventCover);
        } else {
            ivEventCover.setImageResource(R.drawable.placeholder_img);
        }
    }

    private void configureOrganizerUI() {
        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
    }

    private void configureJoinWaitlist() {
        joinBtn.setVisibility(View.VISIBLE);

        checkIfAlreadyJoined();
        joinBtn.setOnClickListener(this::addUserToWaitingList);
    }

    private void configureQrButton(View view) {
        btnViewQr.setOnClickListener(v -> {

            String payload = "Event: " +
                    ((TextView) view.findViewById(R.id.tvEventTitle)).getText() +
                    "\nDate: " +
                    ((TextView) view.findViewById(R.id.tvEventDate)).getText() +
                    "\nTime: " +
                    ((TextView) view.findViewById(R.id.tvEventTime)).getText() +
                    "\nLocation: " +
                    ((TextView) view.findViewById(R.id.tvEventLocation)).getText();

            Bundle b = new Bundle();
            b.putString("qrData", payload);
            b.putBoolean("cameFromDetails", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, b);
        });
    }

    private void configureManageEventButton(View view) {
        btnManageEvent.setOnClickListener(v -> {

            Bundle args = new Bundle();
            args.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_manageEventsFragment, args);
        });
    }

    private void configureDeleteButton() {
        btnDeleteEvent.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Delete Event?")
                        .setMessage("This cannot be undone.")
                        .setPositiveButton("Delete", (d, w) -> deleteEvent())
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    private void deleteEvent() {
        firestore.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(requireView(), "Event deleted", Snackbar.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this)
                            .popBackStack(R.id.organizerLandingFragment, false);
                });
    }

    private void addUserToWaitingList(View v) {
        if (currentUser == null) {
            Snackbar.make(v, "Please log in first.", Snackbar.LENGTH_LONG).show();
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
                    joinBtn.setText("Added ✓");
                    joinBtn.setAlpha(0.6f);
                });
    }

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
                        joinBtn.setText("Added ✓");
                        joinBtn.setAlpha(0.6f);
                    }
                });
    }
}
