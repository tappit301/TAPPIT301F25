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

public class AdminBrowseUsersFragment extends Fragment {

    private RecyclerView recycler;
    private final List<AdminUserModel> users = new ArrayList<>();

    public AdminBrowseUsersFragment() {
        super(R.layout.admin_browse_users);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerUsers);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadUsers();
    }

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

    private void showDeleteDialog(AdminUserModel user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Profile?")
                .setMessage("Are you sure you want to delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(AdminUserModel user) {
        AdminManager.getInstance().deleteUser(user.getId())
                .addOnSuccessListener(unused -> loadUsers());
    }
}
