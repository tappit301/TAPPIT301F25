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

        // Ensure Admin is logged in
        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        // ----------------------------------------------------------
        // BUTTON: Manage Events (Browse Events)
        // ----------------------------------------------------------
        view.findViewById(R.id.btnAdminManageEvents).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_browseEvents)
        );

        // ----------------------------------------------------------
        // BUTTON: Manage Users (Browse Profiles)
        // ----------------------------------------------------------
        view.findViewById(R.id.btnAdminManageUsers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_browseUsers)
        );

        // ----------------------------------------------------------
        // BUTTON: Manage Images
        // ----------------------------------------------------------
        view.findViewById(R.id.btnAdminManageImages).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_browseImages)
        );

        // ----------------------------------------------------------
        // BUTTON: Remove Organizers
        // ----------------------------------------------------------
        view.findViewById(R.id.btnAdminRemoveOrganizers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_removeOrganizers)
        );

        // ----------------------------------------------------------
        // BUTTON: Notification Logs
        // ----------------------------------------------------------
        view.findViewById(R.id.btnAdminNotificationLogs).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_dashboard_to_notificationLogs)
        );

        // ----------------------------------------------------------
        // BUTTON: System Config (Optional)
        // ----------------------------------------------------------
        View systemConfigButton = view.findViewById(R.id.btnAdminSystemConfig);
        if (systemConfigButton != null) {
            systemConfigButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_dashboard_to_systemConfig)
            );
        }
    }
}
