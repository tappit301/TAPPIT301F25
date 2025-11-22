package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This is the Activity that allows users to sign in with their email and password.
 * After signing in successfully, users are redirected to the landing page.
 * The sign-up button takes users to the registration screen.
 *
 * Author: tappit
 */
public class SignInActivity extends AppCompatActivity {

    /** Input field for the user's email address. */
    private EditText emailInput;

    /** Input field for the user's password. */
    private EditText passwordInput;

    /** Button that triggers the sign-in process. */
    private Button signInButton;

    /** Button that switches to the sign-up screen. */
    private Button signUpToggleButton;

    /**
     * Called when the activitiis created.
     * Connects UI elements and sets up button click listeners
     * for signing in and switching to the sign up page.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        signInButton = findViewById(R.id.btnSignIn);
        signUpToggleButton = findViewById(R.id.btnSignUpToggle);

        signInButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Temporary success message — replace with real Firebase login later
            Toast.makeText(this, "Signed in successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignInActivity.this, LandingHostActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        signUpToggleButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
