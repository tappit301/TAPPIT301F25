package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminRemoveOrganizersFragment extends Fragment {

    private RecyclerView recycler;
    private final List<AdminUserModel> organizers = new ArrayList<>();

    public AdminRemoveOrganizersFragment() {
        super(R.layout.admin_browse_organizers);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerOrganizers);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadOrganizers();
    }

    private void loadOrganizers() {
        AdminManager.getInstance().getAllOrganizers()
                .addOnSuccessListener(snapshot -> {
                    organizers.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        AdminUserModel u = new AdminUserModel();
                        u.setId(doc.getId());
                        u.setEmail(doc.getString("email"));
                        u.setName(doc.getString("name"));
                        organizers.add(u);
                    }

                    recycler.setAdapter(new AdminUserAdapter(
                            organizers,
                            organizer -> showDeleteDialog(organizer)
                    ));
                });
    }

    private void showDeleteDialog(AdminUserModel organizer) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Organizer?")
                .setMessage("Are you sure you want to remove " + organizer.getName() + "?")
                .setPositiveButton("Remove", (dialog, which) -> deleteOrganizer(organizer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteOrganizer(AdminUserModel organizer) {
        AdminManager.getInstance()
                .deleteOrganizer(organizer.getId())
                .addOnSuccessListener(unused -> loadOrganizers());
    }
}
