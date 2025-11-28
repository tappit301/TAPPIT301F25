package com.example.eventapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

public class AdminManageUsersFragment extends Fragment {

    private AdminManager adminManager;

    public AdminManageUsersFragment() {
        super(R.layout.admin_manage_users);
    }

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
