package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EventDetailsFragment extends Fragment {

    private TextView detailTitle, detailDate, detailTime, detailLocation, detailDescription;
    private Button btnViewQr;

    public EventDetailsFragment() {}

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

        detailTitle = view.findViewById(R.id.detailTitle);
        detailDate = view.findViewById(R.id.detailDate);
        detailTime = view.findViewById(R.id.detailTime);
        detailLocation = view.findViewById(R.id.detailLocation);
        detailDescription = view.findViewById(R.id.detailDescription);
        btnViewQr = view.findViewById(R.id.btnViewQr);

        // --- Get data from navigation bundle ---
        Bundle args = getArguments();
        if (args == null) return;

        String title = args.getString("title", "No Title");
        String date = args.getString("date", "");
        String time = args.getString("time", "");
        String location = args.getString("location", "No location provided");
        String desc = args.getString("desc", "");
        String eventId = args.getString("eventId", "");

        // --- Bind to UI ---
        detailTitle.setText(title);
        detailDate.setText(date);
        detailTime.setText(time);
        detailLocation.setText(location);
        detailDescription.setText(desc);

        // --- QR Button ---
        btnViewQr.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            // Navigate to your QR fragment
            // Example: Navigation.findNavController(v).navigate(R.id.action_details_to_qr, bundle);
        });
    }
}
