package com.example.eventapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.Event;
import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminManageEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private final List<Event> eventList = new ArrayList<>();

    public AdminManageEventsFragment() {
        super(R.layout.admin_manage_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        recyclerView = view.findViewById(R.id.adminEventsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadEvents();
    }

    private void loadEvents() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    eventList.clear();
                    for (var doc : snap) {
                        Log.d("ADMIN_EVENT", "Raw date in Firestore = " + doc.get("date"));

                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());

                        eventList.add(e);
                    }

                    AdminEventAdapter adapter = new AdminEventAdapter(eventList, event -> {
                        // delete logic later
                    });

                    recyclerView.setAdapter(adapter);
                });
    }
}
