package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Handles user login using Firebase Authentication.
 * Supports: Email login + Guest Mode redirection.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button signUpToggleButton;
    private Button btnContinueAsGuest;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseHelper.getAuth();

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.btnSignIn);
        signUpToggleButton = findViewById(R.id.btnSignUpToggle);
        btnContinueAsGuest = findViewById(R.id.btnContinueAsGuest);

        // --- SIGN IN BUTTON ---
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            signInUser(email, password);
        });

        // --- GO TO SIGNUP ---
        signUpToggleButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        // --- GUEST MODE ---
        btnContinueAsGuest.setOnClickListener(v -> continueAsGuest());
    }

    private void continueAsGuest() {
        // Persist guest mode
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", true).apply();

        Intent intent = new Intent(LoginActivity.this, LandingHostActivity.class);
        intent.putExtra("openExplore", true);
        startActivity(intent);

        finish();
    }

    /**
     * Signs in the user with Firebase Authentication.
     */
    private void signInUser(String email, String password) {
        Log.d(TAG, "Attempting login for " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "Login successful: " + user.getUid());
                        Toast.makeText(this, "Welcome back, " + user.getEmail(), Toast.LENGTH_SHORT).show();

                        // Disable guest mode
                        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
                        prefs.edit().putBoolean("GUEST_MODE", false).apply();

                        Intent intent = new Intent(LoginActivity.this, LandingHostActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed: " + e.getMessage());
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

