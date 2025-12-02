package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment that allows the admin to view and remove organizers.
 * Displays organizers along with the number of events they created.
 */
public class AdminRemoveOrganizersFragment extends Fragment {

    private RecyclerView recycler;
    private final List<OrganizerUser> organizers = new ArrayList<>();

    /**
     * Creates the fragment and sets the layout resource.
     */
    public AdminRemoveOrganizersFragment() {
        super(R.layout.admin_remove_organizers);
    }

    /**
     * Sets up the RecyclerView and loads organizer data when the view is created.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerOrganizers);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadOrganizers();
    }

    /**
     * Loads organizers by scanning event documents and counting how many events each organizer created.
     * Filters out the admin account and sorts organizers alphabetically.
     */
    private void loadOrganizers() {

        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(snap -> {

                    Map<String, OrganizerUser> map = new HashMap<>();

                    snap.getDocuments().forEach(doc -> {
                        String email = doc.getString("organizerEmail");
                        String uid = doc.getString("organizerId");

                        if (email == null || uid == null) return;
                        if (email.equalsIgnoreCase("admin@tappit.ca")) return;

                        if (!map.containsKey(uid)) {
                            map.put(uid, new OrganizerUser(uid, email, 1));
                        } else {
                            map.get(uid).eventCount++;
                        }
                    });

                    organizers.clear();
                    organizers.addAll(map.values());

                    organizers.sort(Comparator.comparing(u -> u.email.toLowerCase()));

                    recycler.setAdapter(new AdminRemoveOrganizersAdapter(
                            organizers,
                            this::removeOrganizer
                    ));

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Removes an organizer by updating the user's role back to attendee.
     * Reloads the organizer list after the update succeeds.
     *
     * @param uid the organizer's user ID
     */
    private void removeOrganizer(String uid) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("role", "attendee")
                .addOnSuccessListener(a -> {
                    Toast.makeText(requireContext(),
                            "Organizer removed", Toast.LENGTH_SHORT).show();
                    loadOrganizers();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
