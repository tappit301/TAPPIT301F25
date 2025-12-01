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

public class AdminSystemConfigFragment extends Fragment {

    public AdminSystemConfigFragment() {
        super(R.layout.admin_system_config);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        if (!AdminSession.isLoggedIn(requireContext())) {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminLoginFragment);
            return;
        }

        EditText key = view.findViewById(R.id.etConfigKey);
        EditText value = view.findViewById(R.id.etConfigValue);

        view.findViewById(R.id.btnSaveConfig).setOnClickListener(v -> {

            String configKey = key.getText().toString().trim();
            String configValue = value.getText().toString().trim();

            if (configKey.isEmpty()) {
                Toast.makeText(getContext(), "Key cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            AdminManager.getInstance()
                    .setConfigValue(configKey, configValue)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(getContext(), "Config saved successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
        });
    }
}
