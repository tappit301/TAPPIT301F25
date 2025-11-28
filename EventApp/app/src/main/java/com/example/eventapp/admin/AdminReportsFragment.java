package com.example.eventapp.admin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

public class AdminReportsFragment extends Fragment {

    public AdminReportsFragment() {
        super(R.layout.admin_reports);
    }

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
