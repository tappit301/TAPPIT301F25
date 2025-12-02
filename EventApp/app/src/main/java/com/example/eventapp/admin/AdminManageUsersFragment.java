package com.example.eventapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

/**
 * Fragment that provides admin controls for managing user accounts.
 * Demonstrates deleting a user through the AdminManager.
 */
public class AdminManageUsersFragment extends Fragment {

    private AdminManager adminManager;

    /**
     * Creates the fragment and assigns its layout resource.
     */
    public AdminManageUsersFragment() {
        super(R.layout.admin_manage_users);
    }

    /**
     * Checks admin login, initializes the admin manager, and sets up user actions.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        adminManager = AdminManager.getInstance();

        view.findViewById(R.id.btnDeleteUser).setOnClickListener(v -> {

            String userId = "DEMO_USER_ID";

            adminManager.deleteUser(userId)
                    .addOnSuccessListener(unused ->
                            Log.d("ADMIN", "User deleted"))
                    .addOnFailureListener(e ->
                            Log.e("ADMIN", "Error deleting user", e));
        });
    }
}
