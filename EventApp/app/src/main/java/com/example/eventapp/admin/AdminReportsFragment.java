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
 * Fragment that provides access to admin reports and statistics.
 * Currently demonstrates loading and logging report data.
 */
public class AdminReportsFragment extends Fragment {

    /**
     * Creates the fragment and assigns its layout resource.
     */
    public AdminReportsFragment() {
        super(R.layout.admin_reports);
    }

    /**
     * Checks admin login status and sets up actions for viewing reports.
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

        view.findViewById(R.id.btnViewReports).setOnClickListener(v ->
                AdminManager.getInstance().getNotificationLogs()
                        .addOnSuccessListener(snap ->
                                Log.d("ADMIN", "Reports count = " + snap.size()))
                        .addOnFailureListener(e ->
                                Log.e("ADMIN", "Error loading reports", e)));
    }
}
