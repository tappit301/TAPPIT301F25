package com.example.eventapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;   // ✅ FIXED IMPORT
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class ExploreEventsFragment extends Fragment {

    private RecyclerView rvExploreEvents;
    private EventAdapter adapter;
    private LinearLayout emptyLayout;
    private ImageButton btnFilter;

    private FirebaseFirestore firestore;

    private final List<Event> fullList = new ArrayList<>();
    private final List<Event> filteredList = new ArrayList<>();

    public ExploreEventsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseHelper.getFirestore();

        rvExploreEvents = view.findViewById(R.id.rvExploreEvents);
        emptyLayout = view.findViewById(R.id.exploreEmptyLayout);
        btnFilter = view.findViewById(R.id.btnFilter);

        rvExploreEvents.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(
                filteredList,
                R.id.action_exploreEventsFragment_to_eventDetailsFragment
        );
        rvExploreEvents.setAdapter(adapter);

        setupFilterMenu();
        loadEventsFromFirestore();
    }

    // -----------------------------------------
    // LOAD EVENTS FROM FIRESTORE
    // -----------------------------------------
    private void loadEventsFromFirestore() {
        firestore.collection("events")
                .addSnapshotListener((snap, e) -> {

                    fullList.clear();
                    filteredList.clear();

                    if (snap != null) {
                        snap.forEach(doc -> {
                            Event ev = doc.toObject(Event.class);
                            if (ev != null) {
                                ev.setId(doc.getId());
                                fullList.add(ev);
                            }
                        });
                    }

                    filteredList.addAll(fullList);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    // -----------------------------------------
    // FILTER MENU (POPUP)
    // -----------------------------------------
    private void setupFilterMenu() {
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnFilter);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.popup_menu_filter, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {

                int id = item.getItemId();

                // ---- OPTION 1: IF–ELSE ----
                if (id == R.id.filter_all) {
                    filterEvents("ALL");

                } else if (id == R.id.filter_music) {
                    filterEvents("Music");

                } else if (id == R.id.filter_sports) {
                    filterEvents("Sports");

                } else if (id == R.id.filter_tech) {
                    filterEvents("Tech");

                } else if (id == R.id.filter_social) {
                    filterEvents("Social");
                }

                return true;
            });

            popup.show();
        });
    }

    // -----------------------------------------
    // FILTERING
    // -----------------------------------------
    private void filterEvents(String category) {
        filteredList.clear();

        if (category.equals("ALL")) {
            filteredList.addAll(fullList);
        } else {
            for (Event e : fullList) {
                if (e.getCategory() != null &&
                        e.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(e);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean empty = filteredList.isEmpty();
        emptyLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvExploreEvents.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
