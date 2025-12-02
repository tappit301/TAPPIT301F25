package com.example.eventapp;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.eventapp.utils.FirebaseHelper;
import com.example.eventapp.utils.NotificationHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows event details and lets users join waiting lists, view QR codes,
 * respond to selections, and allows organizers to manage or delete events.
 */
public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";
    private static final int LOCATION_PERMISSION_CODE = 101;

    private boolean requireGeolocation = true;
    private FusedLocationProviderClient fusedLocationClient;

    private String pendingUserIdForJoin;
    private String pendingEmailForJoin;

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private MaterialButton btnAccept, btnDecline;
    private ImageView ivEventCover;

    private String eventId;
    private String organizerId;
    private String organizerEmail;
    private String eventTitle;

    private TextView tvEventPrice;

    private FirebaseFirestore firestore;
    private FirebaseUser firebaseUser;

    private String localUserId;
    private String localUserEmail;
    private String localUserName;

    /**
     * Inflates UI for event details.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    /**
     * Loads event data, initializes controls, and configures actions.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        resolveLocalIdentity();

        joinBtn = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr = view.findViewById(R.id.btnViewQr);
        btnManageEvent = view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        ivEventCover = view.findViewById(R.id.ivEventCover);

        btnAccept = view.findViewById(R.id.btnAccept);
        btnDecline = view.findViewById(R.id.btnDecline);

        if (btnAccept != null) btnAccept.setVisibility(View.GONE);
        if (btnDecline != null) btnDecline.setVisibility(View.GONE);

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId", "");
            organizerId = args.getString("organizerId", "");
            organizerEmail = args.getString("organizerEmail", "");
            eventTitle = args.getString("title", "Event");

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

            tvEventPrice = view.findViewById(R.id.tvEventPrice);

            TextView tvOrg = view.findViewById(R.id.tvOrganizerName);
            tvOrg.setText(organizerEmail == null ? "" : organizerEmail);

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

        fetchGeolocationRequirement();

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        boolean isOrganizer =
                firebaseUser != null &&
                        organizerId != null &&
                        organizerId.equals(firebaseUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        joinBtn.setVisibility(View.VISIBLE);

        checkIfAlreadyJoined();
        joinBtn.setOnClickListener(this::handleJoinClick);

        if (btnAccept != null) btnAccept.setOnClickListener(v -> handleAccept());
        if (btnDecline != null) btnDecline.setOnClickListener(v -> handleDecline());

        refreshEventPrice();

        btnViewQr.setOnClickListener(v -> {
            if (eventId == null || eventId.isEmpty()) {
                Snackbar.make(v, "Missing event ID.", Snackbar.LENGTH_LONG).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("qrData", eventId);
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

    /**
     * Loads Firebase or guest identity for this device.
     */
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

    /**
     * Loads whether this event requires geolocation for joining.
     */
    private void fetchGeolocationRequirement() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean value = doc.getBoolean("requireGeolocation");
                        requireGeolocation = value != null && value;
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading geolocation requirement", e));
    }

    /**
     * Handles join button click for both Firebase and guest users.
     */
    private void handleJoinClick(View v) {
        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Event not found.", Snackbar.LENGTH_LONG).show();
            return;
        }

        if (localUserId != null && localUserEmail != null) {
            if (requireGeolocation) {
                requestUserLocationAndJoin(v, localUserId, localUserEmail);
            } else {
                addUserToWaitingListWithIdentity(v, localUserId, localUserEmail, null);
            }
            return;
        }

        showGuestInfoDialog(v);
    }

    /**
     * Shows dialog for guest users to enter name and email.
     */
    private void showGuestInfoDialog(View anchorView) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_guest_info, null);

        TextInputEditText etName = dialogView.findViewById(R.id.etGuestName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etGuestEmail);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Your details")
                .setMessage("Enter your name and email to join the waiting list.")
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

                    String androidId = Settings.Secure.getString(
                            requireContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID
                    );

                    localUserId = "guest_" + androidId;
                    localUserEmail = email;
                    localUserName = name;

                    SharedPreferences prefs =
                            requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

                    prefs.edit()
                            .putString("GUEST_USER_ID", localUserId)
                            .putString("GUEST_USER_EMAIL", localUserEmail)
                            .putString("GUEST_USER_NAME", localUserName)
                            .apply();

                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("userId", localUserId);
                    userDoc.put("name", localUserName);
                    userDoc.put("email", localUserEmail);
                    userDoc.put("type", "guest");
                    userDoc.put("createdAt", Timestamp.now());

                    firestore.collection("users")
                            .document(localUserId)
                            .set(userDoc);

                    if (requireGeolocation) {
                        requestUserLocationAndJoin(anchorView, localUserId, localUserEmail);
                    } else {
                        addUserToWaitingListWithIdentity(anchorView, localUserId, localUserEmail, null);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Requests user location if required before joining.
     */
    private void requestUserLocationAndJoin(View v, String userId, String email) {
        pendingUserIdForJoin = userId;
        pendingEmailForJoin = email;

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Snackbar.make(v, "Unable to fetch location.", Snackbar.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> geoData = new HashMap<>();
            geoData.put("lat", location.getLatitude());
            geoData.put("lng", location.getLongitude());

            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1
                );
                if (addresses != null && !addresses.isEmpty()) {
                    geoData.put("city", addresses.get(0).getLocality());
                    geoData.put("country", addresses.get(0).getCountryName());
                }
            } catch (IOException ignored) {}

            addUserToWaitingListWithIdentity(v, userId, email, geoData);
        });
    }

    /**
     * Handles permission result for location access.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                View root = getView();
                if (root != null && pendingUserIdForJoin != null && pendingEmailForJoin != null) {
                    requestUserLocationAndJoin(root, pendingUserIdForJoin, pendingEmailForJoin);
                }

            } else {
                Snackbar.make(requireView(),
                        "Location permission is required.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Adds a user (Firebase or guest) to the waiting list.
     */
    private void addUserToWaitingListWithIdentity(View v,
                                                  String userId,
                                                  String email,
                                                  @Nullable Map<String, Object> geoData) {

        if (eventId == null || eventId.isEmpty()) {
            Snackbar.make(v, "Event not found.", Snackbar.LENGTH_LONG).show();
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

        if (geoData != null) data.putAll(geoData);

        attendeeRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added");
                    joinBtn.setAlpha(0.6f);
                    Snackbar.make(v, "Joined waiting list.", Snackbar.LENGTH_SHORT).show();

                    NotificationHelper.notifyUser(
                            requireContext(),
                            userId,
                            "WAITLIST_ADDED",
                            "Waiting list joined",
                            "You were added to the waiting list for " + eventTitle
                    );

                    if (organizerId != null && !organizerId.isEmpty()) {
                        NotificationHelper.notifyUser(
                                requireContext(),
                                organizerId,
                                "ORGANIZER_NEW_WAITLIST",
                                "New waiting list participant",
                                email + " joined the waiting list."
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(v, "Failed: " + e.getMessage(),
                                Snackbar.LENGTH_LONG).show()
                );
    }

    /**
     * Checks if current user already joined the waiting list.
     */
    private void checkIfAlreadyJoined() {
        if (eventId == null || eventId.isEmpty()) return;

        String idToCheck;
        if (firebaseUser != null) {
            idToCheck = firebaseUser.getUid();
        } else {
            SharedPreferences prefs =
                    requireContext().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

            idToCheck = prefs.getString("GUEST_USER_ID", null);
        }

        if (idToCheck == null) return;

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
                });
    }

    /**
     * Checks and shows accept/decline options if user was selected.
     */
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

                        NotificationHelper.notifyUser(
                                requireContext(),
                                localUserId,
                                "USER_SELECTED",
                                "You were selected!",
                                "You have been selected for " + eventTitle
                        );

                    } else if ("not_selected".equals(status)) {

                        NotificationHelper.notifyUser(
                                requireContext(),
                                localUserId,
                                "USER_NOT_SELECTED",
                                "Not selected",
                                "You were not selected for " + eventTitle
                        );
                    }
                });
    }

    /**
     * Confirms and processes a user's acceptance of their spot.
     */
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

                    String who = (localUserEmail != null && !localUserEmail.isEmpty())
                            ? localUserEmail
                            : "A participant";

                    if (organizerId != null) {
                        NotificationHelper.notifyUser(
                                requireContext(),
                                organizerId,
                                "ORGANIZER_USER_ACCEPTED",
                                "Invitation accepted",
                                who + " accepted their spot."
                        );
                    }
                });
    }

    /**
     * Handles declining a selection and triggers replacement pick.
     */
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

                    String who = (localUserEmail != null && !localUserEmail.isEmpty())
                            ? localUserEmail
                            : "A participant";

                    if (organizerId != null) {
                        NotificationHelper.notifyUser(
                                requireContext(),
                                organizerId,
                                "ORGANIZER_USER_DECLINED",
                                "Invitation declined",
                                who + " declined their spot."
                        );
                    }

                    triggerReplacementDraw();
                });
    }

    /**
     * Randomly selects a new user when someone declines.
     */
    private void triggerReplacementDraw() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "not_selected")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) return;

                    DocumentSnapshot next = snapshot.getDocuments()
                            .get((int) (Math.random() * snapshot.size()));

                    firestore.collection("eventAttendees")
                            .document(eventId)
                            .collection("attendees")
                            .document(next.getId())
                            .update("status", "selected");
                });
    }

    /**
     * Shows a confirmation dialog before deleting the event.
     */
    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Event?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the event and notifies all attendees.
     */
    private void deleteEvent() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String safeTitle = (eventTitle == null || eventTitle.isEmpty())
                            ? "this event"
                            : "\"" + eventTitle + "\"";

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String attendeeUserId = doc.getString("userId");

                        if (attendeeUserId != null) {
                            NotificationHelper.notifyUser(
                                    requireContext(),
                                    attendeeUserId,
                                    "EVENT_CANCELLED",
                                    "Event cancelled",
                                    "The event " + safeTitle + " was cancelled."
                            );
                        }
                    }

                    firestore.collection("events")
                            .document(eventId)
                            .delete()
                            .addOnSuccessListener(aVoid -> deleteAttendeesForEvent());
                });
    }

    /**
     * Deletes all attendee records for the event.
     */
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
                            .document(eventId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Snackbar.make(requireView(), "Event deleted.",
                                        Snackbar.LENGTH_SHORT).show();

                                NavHostFragment.findNavController(this)
                                        .popBackStack(R.id.organizerLandingFragment, false);
                            });
                });
    }

    /**
     * Simple Firestore attendee model.
     */
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

    /**
     * Loads dynamic event price from Firestore.
     */
    private void refreshEventPrice() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Double price = doc.getDouble("price");
                    if (price != null && tvEventPrice != null) {
                        tvEventPrice.setText("Price: $" + price);
                    }
                });
    }
}
