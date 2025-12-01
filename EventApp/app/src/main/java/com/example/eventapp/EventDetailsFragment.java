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

        loadArgumentsIntoUI(view);
        refreshEventDetails();
        configureOrganizerUI();
        configureJoinButton(view);
        configureQrButton(view);
        configureManageButton();
        configureDeleteButton();

        btnAccept.setOnClickListener(v -> handleAccept());
        btnDecline.setOnClickListener(v -> handleDecline());

        checkSelectionStatus();
    }

    // -------------------------
    // READ NAVIGATION ARGUMENTS
    // -------------------------
    private void loadArgumentsIntoUI(View view) {
        Bundle args = getArguments();
        if (args == null) return;

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

        fetchGeolocationRequirement();
        checkIfAlreadyJoined();
    }

    // -------------------------
    // ORGANIZER UI CONFIG
    // -------------------------
    private void configureOrganizerUI() {
        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
    }

    // -------------------------
    // REFRESH EVENT DETAILS (PRICE, TITLE…)
    // -------------------------
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
                });
    }

    // -------------------------
    // GEOLOCATION REQUIREMENT
    // -------------------------
    private void fetchGeolocationRequirement() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean req = doc.getBoolean("requireGeolocation");
                    requireGeolocation = req != null && req;
                });
    }

    // -------------------------
    // JOIN BUTTON
    // -------------------------
    private void configureJoinButton(View view) {
        joinBtn.setVisibility(View.VISIBLE);
        joinBtn.setOnClickListener(v -> {
            if (requireGeolocation)
                requestUserLocationAndJoin(v);
            else
                addUserToWaitingList(v, null);
        });
    }

    // -------------------------
    // QR BUTTON
    // -------------------------
    private void configureQrButton(View view) {
        btnViewQr.setOnClickListener(v -> {
            String qrData =
                    "Event: " + ((TextView) view.findViewById(R.id.tvEventTitle)).getText() +
                            "\nDate: " + ((TextView) view.findViewById(R.id.tvEventDate)).getText() +
                            "\nTime: " + ((TextView) view.findViewById(R.id.tvEventTime)).getText() +
                            "\nLocation: " + ((TextView) view.findViewById(R.id.tvEventLocation)).getText();

            Bundle b = new Bundle();
            b.putString("qrData", qrData);
            b.putBoolean("cameFromDetails", true);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_qrCodeFragment, b);
        });
    }

    // -------------------------
    // MANAGE EVENT
    // -------------------------
    private void configureManageButton() {
        btnManageEvent.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_eventDetailsFragment_to_manageEventsFragment, b);
        });
    }

    // -------------------------
    // DELETE EVENT
    // -------------------------
    private void configureDeleteButton() {
        btnDeleteEvent.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Event?")
                    .setMessage("This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteEvent() {
        firestore.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(requireView(),
                            "Event deleted.", Snackbar.LENGTH_SHORT).show();

                    NavHostFragment.findNavController(this)
                            .popBackStack(R.id.organizerLandingFragment, false);
                });
    }

    // -------------------------
    // JOIN WITH GEOLOCATION
    // -------------------------
    private void requestUserLocationAndJoin(View v) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
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
                                location.getLongitude(), 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            Address addr = addresses.get(0);
                            geoData.put("city", addr.getLocality());
                            geoData.put("country", addr.getCountryName());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Geocoder error", e);
                    }

                    addUserToWaitingList(v, geoData);
                });
    }

    // -------------------------
    // JOIN WAITLIST
    // -------------------------
    private void addUserToWaitingList(View v, @Nullable Map<String, Object> geoData) {
        if (currentUser == null) {
            Snackbar.make(v, "Please log in first.", Snackbar.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("userId", currentUser.getUid());
        data.put("email", currentUser.getEmail());
        data.put("joinedAt", Timestamp.now());
        data.put("status", "waiting");

        if (geoData != null) data.putAll(geoData);

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added ✓");
                    joinBtn.setAlpha(0.6f);

                    Snackbar.make(v, "Joined waiting list for " + eventName,
                            Snackbar.LENGTH_SHORT).show();
                });
    }

    // -------------------------
    // CHECK IF ALREADY JOINED
    // -------------------------
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

    // -------------------------
    // ACCEPT / DECLINE
    // -------------------------
    private void checkSelectionStatus() {
        if (currentUser == null || eventId == null) return;

        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    if ("selected".equals(doc.getString("status"))) {
                        btnAccept.setVisibility(View.VISIBLE);
                        btnDecline.setVisibility(View.VISIBLE);
                        joinBtn.setVisibility(View.GONE);
                    }
                });
    }

    private void handleAccept() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .document(currentUser.getUid())
                .update("status", "enrolled")
                .addOnSuccessListener(unused -> {
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
                });
    }

    private void handleDecline() {
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
                });
    }

    private void triggerReplacementDraw() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .whereEqualTo("status", "not_selected")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) return;

                    DocumentSnapshot next = snapshot.getDocuments()
                            .get((int) (Math.random() * snapshot.size()));

                    next.getReference().update("status", "selected");
                });
    }
}
