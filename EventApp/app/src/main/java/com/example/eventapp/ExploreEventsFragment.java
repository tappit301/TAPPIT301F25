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
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Final merged ExploreEventsFragment.
 * Includes filtering, back button, category popup, and Firestore loading.
 */
public class ExploreEventsFragment extends Fragment {

    private RecyclerView rvExploreEvents;
    private EventAdapter adapter;
    private LinearLayout emptyLayout;
    private ImageButton btnFilter;

    private FirebaseFirestore firestore;

    // Full list from Firestore
    private final List<Event> fullList = new ArrayList<>();
    // List shown after filtering
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

        // Back button
        ImageButton btnBack = view.findViewById(R.id.btnBackExplore);
        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
    }

    // ---------------- FIRESTORE LOAD ----------------
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

                                // Only load today and future events
                                if (isTodayOrFuture(ev)) {
                                    fullList.add(ev);
                                }
                            }
                        });
                    }

                    filteredList.addAll(fullList);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    private boolean isTodayOrFuture(Event e) {
        if (e.getDate() == null || e.getTime() == null) return false;

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            Date eventDateTime = sdf.parse(e.getDate() + " " + e.getTime());
            if (eventDateTime == null) return false;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date todayStart = cal.getTime();

            return !eventDateTime.before(todayStart);

        } catch (ParseException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ---------------- FILTER MENU ----------------
    private void setupFilterMenu() {
        btnFilter.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnFilter);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.popup_menu_filter, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.filter_all:
                        filterEvents("ALL");
                        break;
                    case R.id.filter_entertainment:
                        filterEvents("Entertainment");
                        break;
                    case R.id.filter_sports:
                        filterEvents("Sports");
                        break;
                    case R.id.filter_tech:
                        filterEvents("Technology");
                        break;
                    case R.id.filter_health:
                        filterEvents("Health");
                        break;
                }
                return true;
            });

            popup.show();
        });
    }

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
