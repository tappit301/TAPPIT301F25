package com.example.eventapp;

import android.os.Bundle;
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

/**
 * Fragment that displays all events belonging to a selected category.
 * Loads events from Firestore and updates the list in real time.
 */
public class CategoryEventsFragment extends Fragment {

    private RecyclerView rvCategoryEvents;
    private EventAdapter adapter;
    private FirebaseFirestore firestore;

    private final List<Event> eventList = new ArrayList<>();
    private String categoryName;

    /**
     * Inflates the layout for this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_events, container, false);
    }

    /**
     * Initializes the RecyclerView, retrieves the selected category name,
     * sets up the adapter, and loads events from Firestore.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategoryEvents = view.findViewById(R.id.rvCategoryEvents);
        rvCategoryEvents.setLayoutManager(new LinearLayoutManager(requireContext()));

        firestore = FirebaseHelper.getFirestore();

        if (getArguments() != null) {
            categoryName = getArguments().getString("categoryName");
        }

        adapter = new EventAdapter(eventList,
                R.id.action_categoryEventsFragment_to_eventDetailsFragment);
        rvCategoryEvents.setAdapter(adapter);

        loadCategoryEvents();
    }

    /**
     * Loads events that match the selected category from Firestore.
     * Uses a snapshot listener so updates appear in real time.
     */
    private void loadCategoryEvents() {
        firestore.collection("events")
                .whereEqualTo("category", categoryName)
                .addSnapshotListener((snapshot, e) -> {
                    eventList.clear();

                    if (snapshot != null) {
                        snapshot.forEach(doc -> {
                            Event ev = doc.toObject(Event.class);
                            ev.setId(doc.getId());
                            eventList.add(ev);
                        });
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
