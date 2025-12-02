package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.R;

public class AdminLoginFragment extends Fragment {

    private static final String ADMIN_EMAIL = "admin@tappit.ca";
    private static final String ADMIN_PASSWORD = "admin123";

    public AdminLoginFragment() {
        super(R.layout.fragment_admin_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        EditText email = view.findViewById(R.id.adminEmail);
        EditText password = view.findViewById(R.id.adminPassword);

        view.findViewById(R.id.btnAdminLoginSubmit).setOnClickListener(v -> {

            String e = email.getText().toString().trim();
            String p = password.getText().toString().trim();

            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(getContext(), "Please enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Local-only admin login
            if (!e.equals(ADMIN_EMAIL) || !p.equals(ADMIN_PASSWORD)) {
                Toast.makeText(getContext(), "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save admin session
            AdminSession.login(requireContext());

            // Navigate to dashboard
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminLoginFragment_to_adminDashboardFragment);
        });
    }
}
