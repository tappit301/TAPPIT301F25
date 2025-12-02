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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment that allows the admin to browse all organizer emails.
 * Reads all events, extracts organizer emails, and shows unique values in a list.
 */
public class AdminBrowseOrganizersFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> organizerEmails = new ArrayList<>();

    /**
     * Creates the fragment and sets the associated layout resource.
     */
    public AdminBrowseOrganizersFragment() {
        super(R.layout.admin_browse_organizers);
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
     * Loads all organizer emails by scanning event documents.
     * Filters out the admin account and keeps only unique emails.
     * Updates the RecyclerView after loading.
     */
    private void loadOrganizers() {
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(snap -> {

                    Set<String> uniqueEmails = new HashSet<>();

                    snap.getDocuments().forEach(doc -> {
                        String email = doc.getString("organizerEmail");
                        if (email != null && !email.equalsIgnoreCase("admin@tappit.ca")) {
                            uniqueEmails.add(email);
                        }
                    });

                    organizerEmails.clear();
                    organizerEmails.addAll(uniqueEmails);

                    recycler.setAdapter(new AdminOrganizerAdapter(organizerEmails, requireContext()));

                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
