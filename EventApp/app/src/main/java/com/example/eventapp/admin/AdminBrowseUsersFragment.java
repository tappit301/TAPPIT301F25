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

/**
 * Fragment that lets the admin browse all registered users.
 * Shows each user's name and email and allows deleting any user profile.
 */
public class AdminBrowseUsersFragment extends Fragment {

    private RecyclerView recycler;
    private final List<AdminUserModel> users = new ArrayList<>();

    /**
     * Creates the fragment and sets its layout resource.
     */
    public AdminBrowseUsersFragment() {
        super(R.layout.admin_browse_users);
    }

    /**
     * Initializes the RecyclerView and loads the list of users when the view is created.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerUsers);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadUsers();
    }

    /**
     * Loads all users from the database and updates the RecyclerView.
     * Converts each document into a user model with ID, name, and email.
     */
    private void loadUsers() {
        AdminManager.getInstance().getAllUsers()
                .addOnSuccessListener(snapshot -> {
                    users.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        AdminUserModel u = new AdminUserModel();
                        u.setId(doc.getId());
                        u.setEmail(doc.getString("email"));
                        u.setName(doc.getString("name"));
                        users.add(u);
                    }

                    recycler.setAdapter(new AdminUserAdapter(users, user -> {
                        showDeleteDialog(user);
                    }));
                });
    }

    /**
     * Shows a confirmation dialog before deleting a user's profile.
     *
     * @param user the user to delete
     */
    private void showDeleteDialog(AdminUserModel user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile?")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deletes the selected user from the database and refreshes the list afterward.
     *
     * @param user the user to delete
     */
    private void deleteUser(AdminUserModel user) {
        AdminManager.getInstance().deleteUser(user.getId())
                .addOnSuccessListener(unused -> loadUsers());
    }
}
