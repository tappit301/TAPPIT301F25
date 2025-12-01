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
import com.google.firebase.firestore.FirebaseFirestore;

public class EventMapFragment extends Fragment implements OnMapReadyCallback {

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

        Bundle mapBundle = null;
        if (savedInstanceState != null) {
            mapBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapBundle);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap gMap) {
        googleMap = gMap;
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        if (eventId == null || eventId.isEmpty()) {
            Log.e("MAP", "Event ID is missing — cannot load map");
            return;
        }
        loadEventLocation();
    }

    private void loadEventLocation() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Log.e("MAP", "Event doc does NOT exist. Probably deleted.");
                        return;   // prevents crash
                    }

                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");

                    if (lat == null || lng == null) {
                        Log.e("MAP", "Event missing lat/lng");
                        return;   // prevents crash
                    }

                    LatLng pos = new LatLng(lat, lng);

                    googleMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(doc.getString("title"))
                    );

                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
                })
                .addOnFailureListener(e ->
                        Log.e("MAP", "Failed to load event", e)
                );
    }

    // --- Map lifecycle ---
    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onStart() { super.onStart(); mapView.onStart(); }
    @Override public void onPause() { mapView.onPause(); super.onPause(); }
    @Override public void onStop() { super.onStop(); mapView.onStop(); }
    @Override public void onDestroyView() { mapView.onDestroy(); super.onDestroyView(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapBundle == null) {
            mapBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapBundle);
        }

        mapView.onSaveInstanceState(mapBundle);
    }
}
