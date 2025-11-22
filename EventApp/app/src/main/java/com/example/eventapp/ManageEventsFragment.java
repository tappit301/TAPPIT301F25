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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Attendee;
import com.example.eventapp.AttendeeAdapter;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsFragment extends Fragment {

    private static final String TAG = "ManageEventsFragment";
    private RecyclerView recyclerView;
    private AttendeeAdapter adapter;
    private List<Attendee> attendees = new ArrayList<>();

    private FirebaseFirestore firestore;
    private String eventId;

    public ManageEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();

        recyclerView = view.findViewById(R.id.recyclerViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AttendeeAdapter(attendees);
        recyclerView.setAdapter(adapter);

        // ✅ Read args FIRST
        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId", "");
        }

        if (eventId == null || eventId.isEmpty()) {
            Log.e(TAG, "No eventId received");
            return;
        }

        // ✅ EDIT EVENT BUTTON CLICK (args now AVAILABLE)
        View btnEditEvent = view.findViewById(R.id.btnEditEvent);

        btnEditEvent.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            bundle.putString("title", args.getString("title", ""));
            bundle.putString("desc", args.getString("desc", ""));
            bundle.putString("date", args.getString("date", ""));
            bundle.putString("time", args.getString("time", ""));
            bundle.putString("location", args.getString("location", ""));
            bundle.putString("imageUrl", args.getString("imageUrl", ""));

            NavHostFragment.findNavController(ManageEventsFragment.this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, bundle);
        });

        loadWaitingList();
    }

    private void loadWaitingList() {
        firestore.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get()
                .addOnSuccessListener(snapshot -> {
                    attendees.clear();

                    snapshot.getDocuments().forEach(doc -> {
                        String email = doc.getString("email");
                        attendees.add(new Attendee("", email, "waiting"));
                    });

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading attendees", e));
    }
}
