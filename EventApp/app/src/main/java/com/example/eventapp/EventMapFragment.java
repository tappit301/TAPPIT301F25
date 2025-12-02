package com.example.eventapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays an event map with:
 * - A marker for the event location
 * - Markers for attendees who shared their location
 */
public class EventMapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "EventMapFragment";
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    private String eventId;
    private MapView mapView;
    private GoogleMap googleMap;

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_event_map, container, false);
    }

    /**
     * Sets up the map, reads eventId, and begins loading map data.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId", "");
        }

        view.findViewById(R.id.btnBackMap).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        mapView = view.findViewById(R.id.eventMapView);

        Bundle mapBundle = savedInstanceState != null
                ? savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
                : null;

        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    /**
     * Called when the Google Map is ready to be interacted with.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "Event ID is missing — cannot load map");
            return;
        }

        loadEventLocation();
        loadAttendeeLocations();
    }

    /**
     * Loads and displays the event's main location marker.
     */
    private void loadEventLocation() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat == null || lng == null) {
                        Log.e(TAG, "Event missing lat/lng");
                        return;
                    }

                    LatLng pos = new LatLng(lat, lng);

                    googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(doc.getString("title"))
                    );

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load event", e)
                );
    }

    /**
     * Loads attendee markers if they have shared coordinates.
     */
    private void loadAttendeeLocations() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) return;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Double lat = doc.getDouble("lat");
                        Double lng = doc.getDouble("lng");

                        if (lat == null || lng == null) continue;

                        String email = doc.getString("email");
                        String status = doc.getString("status");

                        LatLng pos = new LatLng(lat, lng);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(pos)
                                .title(email != null ? email : "Attendee");

                        if (status != null) {
                            markerOptions.snippet("Status: " + status);
                        }

                        googleMap.addMarker(markerOptions);
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to load attendee locations", e)
                );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapView != null) mapView.onStart();
    }

    @Override
    public void onPause() {
        if (mapView != null) mapView.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mapView != null) mapView.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) mapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) mapView.onLowMemory();
    }

    /**
     * Saves the map state on rotation or backgrounding.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle);
        }

        if (mapView != null) {
            mapView.onSaveInstanceState(mapBundle);
        }
    }
}
