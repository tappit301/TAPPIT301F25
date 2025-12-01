package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

public class AdminDashboardFragment extends Fragment {

    public AdminDashboardFragment() {
        super(R.layout.admin_dashboard);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        // 1) Check admin login
        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        // 2) Manage Events
        view.findViewById(R.id.btnAdminManageEvents).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseEventsFragment)
        );

        // 3) Manage Users
        view.findViewById(R.id.btnAdminManageUsers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseUsersFragment)
        );

        // 4) Manage Images
        view.findViewById(R.id.btnAdminManageImages).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseImagesFragment)
        );

        // 5) Remove Organizers
        view.findViewById(R.id.btnAdminRemoveOrganizers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminRemoveOrganizersFragment)
        );

        // 6) Notification Logs
        view.findViewById(R.id.btnAdminNotificationLogs).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminNotificationLogsFragment)
        );

        // 7) System Config
        View systemConfigButton = view.findViewById(R.id.btnAdminSystemConfig);
        if (systemConfigButton != null) {
            systemConfigButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_adminDashboardFragment_to_adminSystemConfigFragment)
            );
        }
    }
}
