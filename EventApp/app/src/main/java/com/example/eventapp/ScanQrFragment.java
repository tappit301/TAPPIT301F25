package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class ScanQrFragment extends Fragment {

    private DecoratedBarcodeView barcodeScannerView;
    private FirebaseFirestore firestore;
    private boolean handlingResult = false;

    public ScanQrFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();
        NavController navController = Navigation.findNavController(view);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBarScanQr);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        }

        barcodeScannerView = view.findViewById(R.id.barcodeScannerView);

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (handlingResult) {
                    return;
                }
                if (result.getText() == null || result.getText().isEmpty()) {
                    return;
                }

                handlingResult = true;
                String scannedData = result.getText().trim();

                // The QR code encodes the eventId
                fetchEventAndGoToDetails(scannedData, navController);
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                // Not used
            }
        });
    }

    /**
     * Looks up the event in Firestore using the scanned eventId
     * and navigates to EventDetailsFragment with all required arguments.
     */
    private void fetchEventAndGoToDetails(String eventId, NavController navController) {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        Toast.makeText(requireContext(),
                                "No event found for this QR.",
                                Toast.LENGTH_SHORT).show();
                        handlingResult = false;
                        barcodeScannerView.resume();
                        return;
                    }

                    Bundle args = new Bundle();
                    // Required by EventDetailsFragment
                    args.putString("eventId", eventId);
                    args.putString("organizerId", doc.getString("organizerId"));

                    args.putString("title", doc.getString("title"));

                    String desc = doc.getString("desc");
                    if (desc == null) {
                        desc = doc.getString("description");
                    }
                    args.putString("desc", desc != null ? desc : "");

                    args.putString("date", doc.getString("date"));
                    args.putString("time", doc.getString("time"));
                    args.putString("location", doc.getString("location"));

                    navController.navigate(R.id.action_scanQrFragment_to_eventDetailsFragment, args);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to load event: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    handlingResult = false;
                    barcodeScannerView.resume();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
            handlingResult = false;
        }
    }

    @Override
    public void onPause() {
        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }
        super.onPause();
    }
}