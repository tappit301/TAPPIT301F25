package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExploreEventsFragment extends Fragment {

    private RecyclerView rvExplore;
    private LinearLayout emptyLayout;
    private EventAdapter adapter;

    private final List<Event> events = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle sis) {
        firestore = FirebaseHelper.getFirestore();

        rvExplore = view.findViewById(R.id.rvExploreEvents);
        emptyLayout = view.findViewById(R.id.exploreEmptyLayout);

        rvExplore.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventAdapter(events,
                R.id.action_exploreEventsFragment_to_eventDetailsFragment);
        rvExplore.setAdapter(adapter);

        loadEvents();
    }

    private void loadEvents() {
        if (firestore == null) {
            updateState();
            return;
        }

        firestore.collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    events.clear();
                    for (QueryDocumentSnapshot doc : snap) {
                        Event e = doc.toObject(Event.class);
                        e.setId(doc.getId());
                        events.add(e);
                    }
                    adapter.notifyDataSetChanged();
                    updateState();
                })
                .addOnFailureListener(e -> updateState());
    }

    private void updateState() {
        boolean empty = events.isEmpty();
        emptyLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvExplore.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
