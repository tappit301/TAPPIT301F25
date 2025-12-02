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

/**
 * Fragment that handles the admin login screen.
 * Validates the admin's email and password locally and begins a session on success.
 */
public class AdminLoginFragment extends Fragment {

    private static final String ADMIN_EMAIL = "admin@tappit.ca";
    private static final String ADMIN_PASSWORD = "admin123";

    /**
     * Creates the login fragment and sets the layout resource.
     */
    public AdminLoginFragment() {
        super(R.layout.fragment_admin_login);
    }

    /**
     * Sets up the login form and verifies credentials when the admin submits the form.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
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

            if (!e.equals(ADMIN_EMAIL) || !p.equals(ADMIN_PASSWORD)) {
                Toast.makeText(getContext(), "Invalid admin credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminSession.login(requireContext());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_adminLoginFragment_to_adminDashboardFragment);
        });
    }
}
