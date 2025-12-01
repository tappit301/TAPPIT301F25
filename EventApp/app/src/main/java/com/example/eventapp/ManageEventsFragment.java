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

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ManageEventsFragment extends Fragment {

    private static final String TAG = "ManageEventsFragment";

    private RecyclerView recyclerView;
    private AttendeeAdapter adapter;

    private final List<Attendee> attendees = new ArrayList<>();
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
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        firestore = FirebaseHelper.getFirestore();

        recyclerView = view.findViewById(R.id.recyclerViewList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AttendeeAdapter(attendees);
        recyclerView.setAdapter(adapter);

        Bundle args = getArguments();
        if (args != null) eventId = args.getString("eventId");

        if (eventId == null) {
            Log.e(TAG, "Missing eventId");
            return;
        }

        view.findViewById(R.id.btnEditEvent).setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("eventId", eventId);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_manageEventsFragment_to_createEventFragment, b);
        });

        loadAttendees();
    }

    private void loadAttendees() {
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
                        Log.e(TAG, "Failed loading attendees", e));
    }
}
