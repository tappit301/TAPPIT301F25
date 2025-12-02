package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AttendeeAdapter adapter;
    private List<Attendee> attendees = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String eventId;

    private View btnWaiting, btnSelected, btnEnrolled, btnCancelled;
    private Button btnEditEvent, btnRunLottery, btnDeleteEvent, btnExportCSV, btnViewQr;

    public ManageEventsFragment() {
        super(R.layout.fragment_manage_events);  // ⭐ This fixes the "cannot resolve layout" error
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        // ⭐ Retrieve event ID from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        recyclerView = view.findViewById(R.id.recyclerViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Buttons
        btnEditEvent = view.findViewById(R.id.btnEditEvent);
        btnRunLottery = view.findViewById(R.id.btnRunLottery);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
        btnExportCSV = view.findViewById(R.id.btnExportCSV);
        btnViewQr = view.findViewById(R.id.btnViewQr);

        btnWaiting = view.findViewById(R.id.btnWaiting);
        btnSelected = view.findViewById(R.id.btnSelected);
        btnEnrolled = view.findViewById(R.id.btnEnrolled);
        btnCancelled = view.findViewById(R.id.btnCancelled);

        // ⭐ QR Button (working + navigating)
        btnViewQr.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("qrData", eventId);     // QR contains eventId
            bundle.putBoolean("cameFromDetails", false);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_qrCodeFragment, bundle);
        });

        // TODO: Add your attendee loading, filtering, lottery, CSV export etc.
    }
}
