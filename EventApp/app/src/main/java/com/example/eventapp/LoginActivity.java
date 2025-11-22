package com.example.eventapp;

import android.content.Intent;
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
 * This is the Activity that lets users log in with their email and password.
 * Verifies credential using Firebase Authentication and redirects
 * to the landing screen if login is successful.
 *
 * Author: tappit
 */
public class LoginActivity extends AppCompatActivity {

    /** Tag used for logging. */
    private static final String TAG = "LoginActivity";

    /** Email input field. */
    private EditText emailInput;

    /** Password input field. */
    private EditText passwordInput;

    /** Button to trigger the login process. */
    private Button loginButton;

    /** Button to switch to the sign-up screen. */
    private Button signUpToggleButton;

    /** Firebase authentication instance. */
    private FirebaseAuth auth;

    /**
     * Called when the activity is created.
     * Sets up the login form and button listeners.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseHelper.getAuth();

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.btnSignIn);
        signUpToggleButton = findViewById(R.id.btnSignUpToggle);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            signInUser(email, password);
        });

        signUpToggleButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });
    }

    /**
     * Signs in a user with the provided email and password.
     * If successful, redirects to {@link LandingHostActivity}.
     *
     * @param email the user's email address
     * @param password the user's password
     */
    private void signInUser(String email, String password) {
        Log.d(TAG, "Attempting login for " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "Login successful: " + user.getUid());
                        Toast.makeText(this, "Welcome back, " + user.getEmail(), Toast.LENGTH_SHORT).show();

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
