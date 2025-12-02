package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

/**
 * Fragment that displays the main admin dashboard.
 * Provides navigation to all admin management features such as events, users, images, organizers, and system settings.
 */
public class AdminDashboardFragment extends Fragment {

    /**
     * Creates the dashboard fragment and sets its layout.
     */
    public AdminDashboardFragment() {
        super(R.layout.admin_dashboard);
    }

    /**
     * Sets up button navigation and checks if the admin is logged in.
     * Redirects to the login screen if no active session is found.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        view.findViewById(R.id.btnAdminManageEvents).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseEventsFragment)
        );

        view.findViewById(R.id.btnAdminManageUsers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseUsersFragment)
        );

        view.findViewById(R.id.btnAdminManageImages).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminBrowseImagesFragment)
        );

        view.findViewById(R.id.btnAdminRemoveOrganizers).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminRemoveOrganizersFragment)
        );

        view.findViewById(R.id.btnAdminNotificationLogs).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_adminDashboardFragment_to_adminNotificationLogsFragment)
        );

        View systemConfigButton = view.findViewById(R.id.btnAdminSystemConfig);
        if (systemConfigButton != null) {
            systemConfigButton.setOnClickListener(v ->
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_adminDashboardFragment_to_adminSystemConfigFragment)
            );
        }
    }
}
