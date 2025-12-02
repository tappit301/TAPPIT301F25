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
 * Activity responsible for user authentication.
 * Supports:
 * - Login with email & password
 * - Navigation to the sign-up screen
 * - Guest access (bypassing authentication)
 *
 * After successful login, the user is redirected to {@link LandingHostActivity}.
 */
public class LoginActivity extends AppCompatActivity {

    /** Log tag used for debugging login actions. */
    private static final String TAG = "LoginActivity";

    /** Input field for user email. */
    private EditText emailInput;

    /** Input field for user password. */
    private EditText passwordInput;

    /** Button to initiate login request. */
    private Button loginButton;

    /** Button to switch to the sign-up activity. */
    private Button signUpToggleButton;

    /** Firebase Authentication instance for user login. */
    private FirebaseAuth auth;

    /**
     * Initializes the login screen UI, Firebase references,
     * and sets up listeners for login, guest mode, and navigation.
     *
     * @param savedInstanceState previously saved UI state, if any
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

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            signInUser(email, password);
        });

        // Navigate to the sign-up screen
        signUpToggleButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        });

        // Guest access (skips authentication)
        Button btnContinueAsGuest = findViewById(R.id.btnContinueAsGuest);
        btnContinueAsGuest.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, LandingHostActivity.class);
            intent.putExtra("isGuest", true);  // This flag prevents auth screens
            startActivity(intent);
            finish();
        });
    }

    /**
     * Attempts to sign in a user with the given email and password.
     * If successful, the user is redirected to {@link LandingHostActivity}.
     *
     * @param email    the email entered by the user
     * @param password the password entered by the user
     */
    private void signInUser(String email, String password) {
        Log.d(TAG, "Attempting login for " + email);

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Log.d(TAG, "Login successful: " + user.getUid());
                        Toast.makeText(this,
                                "Welcome back, " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

                        // Clear back stack and navigate to dashboard
                        Intent intent = new Intent(LoginActivity.this, LandingHostActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Login failed: " + e.getMessage());
                    Toast.makeText(this,
                            "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
