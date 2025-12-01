package com.example.eventapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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

public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";
    private static final int LOCATION_PERMISSION_CODE = 101;

    // UI
    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private MaterialButton btnAccept, btnDecline;
    private ImageView ivEventCover;

    // Event data
    private String eventId;
    private String organizerId;
    private String organizerEmail;
    private String eventName = "";
    private boolean requireGeolocation = false;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    // Location
    private FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // UI references
        joinBtn       = view.findViewById(R.id.btnJoinWaitingList);
        btnViewQr     = view.findViewById(R.id.btnViewQr);
        btnManageEvent= view.findViewById(R.id.btnManageEvent);
        btnDeleteEvent= view.findViewById(R.id.btnDeleteEvent);
        ivEventCover  = view.findViewById(R.id.ivEventCover);
        btnAccept     = view.findViewById(R.id.btnAccept);
        btnDecline    = view.findViewById(R.id.btnDecline);

        // Accept / Decline buttons
        btnAccept.setOnClickListener(v -> handleAccept());
        btnDecline.setOnClickListener(v -> handleDecline());

        // ---------- READ ARGUMENTS ----------
        Bundle args = getArguments();
        if (args != null) {
            eventId       = args.getString("eventId", "");
            organizerId   = args.getString("organizerId", "");
            organizerEmail= args.getString("organizerEmail", "");
            eventName     = args.getString("title", "This Event");

            ((TextView) view.findViewById(R.id.tvEventTitle))
                    .setText(eventName);
            ((TextView) view.findViewById(R.id.tvEventDate))
                    .setText(args.getString("date", ""));
            ((TextView) view.findViewById(R.id.tvEventTime))
                    .setText(args.getString("time", ""));
            ((TextView) view.findViewById(R.id.tvEventLocation))
                    .setText(args.getString("location", ""));
            ((TextView) view.findViewById(R.id.tvEventDescription))
                    .setText(args.getString("desc", ""));

            // Price from args (if passed)
            String priceArg = args.getString("price", null);
            TextView tvPrice = view.findViewById(R.id.tvEventPrice);
            if (tvPrice != null) {
                if (priceArg != null) {
                    tvPrice.setText("Price: $" + priceArg);
                } else {
                    tvPrice.setText("");
                }
            }

            TextView tvOrg = view.findViewById(R.id.tvOrganizerName);
            tvOrg.setText(organizerEmail != null ? organizerEmail : "");

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

        // Optional: refresh from Firestore (e.g., price might change)
        refreshEventDetails();

        // Back button
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        // Organizer-only controls
        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        // Load whether geolocation is required
        fetchGeolocationRequirement();

        // Check if user already joined
        checkIfAlreadyJoined();

        // Join waiting list
        joinBtn.setOnClickListener(v -> {
            if (requireGeolocation) {
                requestUserLocationAndJoin(v);
            } else {
                addUserToWaitingList(v, null);
            }
        });

        // View QR
        btnViewQr.setOnClickListener(v -> {
            TextView tvTitle    = view.findViewById(R.id.tvEventTitle);
            TextView tvDate     = view.findViewById(R.id.tvEventDate);
            TextView tvTime     = view.findViewById(R.id.tvEventTime);
            TextView tvLocation = view.findViewById(R.id.tvEventLocation);
            TextView tvPrice    = view.findViewById(R.id.tvEventPrice);

            String qrData =
                    "Event: " + (tvTitle != null ? tvTitle.getText() : "") +
                            "\nDate: " + (tvDate != null ? tvDate.getText() : "") +
                            "\nTime: " + (tvTime != null ? tvTime.getText() : "") +
                            "\nLocation: " + (tvLocation != null ? tvLocation.getText() : "") +
                            (tvPrice != null && tvPrice.getText().length() > 0
                                    ? ("\n" + tvPrice.getText()) : "");

            Bundle bundle = new Bundle();
            bundle.putString("qrData", qrData);
            bundle.putBoolean("cameFromDetails", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, bundle);
        });

        // Manage event
        btnManageEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_manageEventsFragment, bundle);
        });

        // Delete event
        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmation());

        // Show accept/decline if selected
        checkSelectionStatus();
    }

    // ===============================
    // Fetch geolocation requirement
    // ===============================
    private void fetchGeolocationRequirement() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean value = doc.getBoolean("requireGeolocation");
                        requireGeolocation = value != null && value;
                        Log.d(TAG, "Geolocation required: " + requireGeolocation);
                    }
                });
    }

    // ===============================
    // Refresh some event details (e.g., price)
    // ===============================
    private void refreshEventDetails() {
        if (eventId == null || eventId.isEmpty()) return;

        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || getView() == null) return;

                    TextView tvPrice = getView().findViewById(R.id.tvEventPrice);
                    if (tvPrice != null) {
                        Double price = doc.getDouble("price");
                        if (price != null) {
                            tvPrice.setText("Price: $" + price);
                        }
                    }

                    // Optionally re-sync title/location if they changed
                    TextView tvTitle    = getView().findViewById(R.id.tvEventTitle);
                    TextView tvLocation = getView().findViewById(R.id.tvEventLocation);

                    if (tvTitle != null) {
                        String t = doc.getString("title");
                        if (t != null) {
                            tvTitle.setText(t);
                            eventName = t;
                        }
                    }

                    if (tvLocation != null) {
                        String loc = doc.getString("location");
                        if (loc != null) {
                            tvLocation.setText(loc);
                        }
                    }
                });
    }

    // ===============================
    // Request location + join waiting list
    // ===============================
    private void requestUserLocationAndJoin(View v) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
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
                            Address addr = addresses.get(0);
                            geoData.put("city", addr.getLocality());
                            geoData.put("country", addr.getCountryName());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    addUserToWaitingList(v, geoData);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                View root = getView();
                if (root != null) {
                    requestUserLocationAndJoin(root);  // retry join after permission
                }

            } else {
                Snackbar.make(requireView(),
                        "Location permission is required for this event.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // ===============================
    // Add user to waiting list (with optional geo)
    // ===============================
    private void addUserToWaitingList(View v, @Nullable Map<String, Object> geoData) {
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

        if (geoData != null) {
            data.putAll(geoData);
        }

        attendeeRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added ✅");
                    joinBtn.setAlpha(0.6f);

                    Snackbar.make(v,
                            "Joined waiting list for " + eventName,
                            Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(v,
                            "Failed: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    // ===============================
    // Check if already joined
    // ===============================
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
                });
    }

    // ===============================
    // Show accept / decline if selected
    // ===============================
    private void checkSelectionStatus() {
        if (currentUser == null || eventId == null || eventId.isEmpty()) return;

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

    // ===============================
    // Accept invitation
    // ===============================
    private void handleAccept() {
        if (currentUser == null || eventId == null || eventId.isEmpty()) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .update("status", "enrolled")
                .addOnSuccessListener(unused -> {

                    // Local + Firestore notification
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

    // ===============================
    // Decline invitation
    // ===============================
    private void handleDecline() {
        if (currentUser == null || eventId == null || eventId.isEmpty()) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .update("status", "cancelled")
                .addOnSuccessListener(unused -> {

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
                .addOnFailureListener(e ->
                        Log.e(TAG, "Decline failed", e));
    }

    // ===============================
    // Select next user (replacement draw)
    // ===============================
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
                            );
                });
    }

    // ===============================
    // Delete event
    // ===============================
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

    // Optional: data holder (currently unused but harmless)
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
