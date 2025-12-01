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
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailsFragment extends Fragment {

    private static final String TAG = "EventDetailsFragment";
    private static final int LOCATION_PERMISSION_CODE = 101;

    private MaterialButton joinBtn, btnViewQr, btnManageEvent, btnDeleteEvent;
    private ImageView ivEventCover;

    private String eventId;
    private String organizerId;
    private String organizerEmail;

    private boolean requireGeolocation = false;   // ⭐ NEW

    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    private FusedLocationProviderClient fusedLocationClient; // ⭐ NEW

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

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

            ((TextView) view.findViewById(R.id.tvEventPrice))
                    .setText("Price: $" + args.getString("price", "0"));

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

        refreshEventDetails();

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        boolean isOrganizer =
                currentUser != null &&
                        organizerId != null &&
                        organizerId.equals(currentUser.getUid());

        btnManageEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        btnDeleteEvent.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

        joinBtn.setVisibility(View.VISIBLE);

        fetchGeolocationRequirement(); // ⭐ IMPORTANT

        checkIfAlreadyJoined();

        joinBtn.setOnClickListener(v -> {
            if (requireGeolocation) {
                requestUserLocationAndJoin(v);
            } else {
                addUserToWaitingList(v, null);
            }
        });

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
            bundle.putString("price", args.getString("price", "0"));


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

    }
    // ===============================
    // ⭐ Fetch Event requireGeolocation
    // ===============================
    private void fetchGeolocationRequirement() {
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

    private void refreshEventDetails() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Double price = doc.getDouble("price");
                        TextView tvPrice = getView().findViewById(R.id.tvEventPrice);
                        if (price != null) {
                            tvPrice.setText("Price: $" + price);
                        }

                        // Update everything else too if you want
                        ((TextView) getView().findViewById(R.id.tvEventTitle))
                                .setText(doc.getString("title"));
                        ((TextView) getView().findViewById(R.id.tvEventLocation))
                                .setText(doc.getString("location"));
                    }
                });
    }


    // ===============================
    // ⭐ Step 1: Request location
    // ===============================
    private void requestUserLocationAndJoin(View v) {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

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
                    requestUserLocationAndJoin(root);   // ⭐ retry join after permission
                }

            } else {
                Snackbar.make(requireView(),
                        "Location permission is required for this event.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }


    // ===============================
    // ⭐ Step 2: Join waiting list with location
    // ===============================
    private void addUserToWaitingList(View v, @Nullable Map<String, Object> geoData) {
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

        if (geoData != null) data.putAll(geoData); // ⭐ Add geolocation

        attendeeRef.set(data)
                .addOnSuccessListener(aVoid -> {
                    joinBtn.setEnabled(false);
                    joinBtn.setText("Added ✅");
                    joinBtn.setAlpha(0.6f);
                    Snackbar.make(v, "Joined waiting list.", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(v, "Failed to join: " + e.getMessage(),
                            Snackbar.LENGTH_LONG).show();
                });
    }

    // ===============================
    // Check if already joined
    // ===============================
    private void checkIfAlreadyJoined() {
        if (currentUser == null) return;

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
        firestore.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    deleteAttendeesForEvent();
                    Snackbar.make(requireView(), "Event deleted.",
                            Snackbar.LENGTH_SHORT).show();

                    NavHostFragment.findNavController(this)
                            .popBackStack(R.id.organizerLandingFragment, false);
                });
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
}
