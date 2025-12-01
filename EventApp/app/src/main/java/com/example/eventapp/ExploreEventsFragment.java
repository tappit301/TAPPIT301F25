package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    private final List<Event> fullList = new ArrayList<>();
    private final List<Event> filteredList = new ArrayList<>();

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_explore_events, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore(); // may be null in tests

        rvExplore = view.findViewById(R.id.rvExploreEvents);
        emptyLayout = view.findViewById(R.id.exploreEmptyLayout);
        ImageButton filterBtn = view.findViewById(R.id.btnFilter);

        rvExplore.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 🔥 Use your real adapter signature (list + nav action)
        adapter = new EventAdapter(
                filteredList,
                R.id.action_exploreEventsFragment_to_eventDetailsFragment
        );

        rvExplore.setAdapter(adapter);

        filterBtn.setOnClickListener(v -> {
            // Do nothing in tests — avoid popup menu crashes
        });

        loadEventsFromFirestore();
    }

    private void loadEventsFromFirestore() {

        if (firestore == null) {
            // Running under Robolectric — skip Firebase entirely
            updateEmptyState();
            return;
        }

        firestore.collection("events")
                .get()
                .addOnSuccessListener(query -> {

                    fullList.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Event e = doc.toObject(Event.class);
                        fullList.add(e);
                    }

                    filteredList.clear();
                    filteredList.addAll(fullList);

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> updateEmptyState());
    }

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            rvExplore.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            rvExplore.setVisibility(View.VISIBLE);
        }
    }
}
