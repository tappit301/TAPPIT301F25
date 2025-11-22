package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapp.models.UserFirebase;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * It is the Activity that handles user registration using Firebase Authentication.
 * Creates a new Firebase account, updates the user profile,
 * and saves the user's information in Firestore.
 *
 * Author: tappit
 */
public class SignUpActivity extends AppCompatActivity {

    /** Tag used for logging messages. */
    private static final String TAG = "SignUpActivity";

    /** Input field for the user's full name. */
    private EditText fullNameInput;

    /** Input field for the user's email address. */
    private EditText emailInput;

    /** Input field for the user's password. */
    private EditText passwordInput;

    /** Input field for confirming the password. */
    private EditText confirmPasswordInput;

    /** Button that triggers the sign-up process. */
    private Button signUpButton;

    /** Button that switches back to the sign-in screen. */
    private Button signInToggleButton;

    /**
     * Called when the activity is created.
     * Connects UI elements and handles button actions for user registration.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullNameInput = findViewById(R.id.editTextFullName);
        emailInput = findViewById(R.id.editTextSignUpEmail);
        passwordInput = findViewById(R.id.editTextSignUpPassword);
        confirmPasswordInput = findViewById(R.id.editTextConfirmPassword);
        signUpButton = findViewById(R.id.btnSignUp);
        signInToggleButton = findViewById(R.id.btnSignInToggle);

        signUpButton.setOnClickListener(v -> {
            String fullName = fullNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email)
                    || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d(TAG, "Attempting signup for " + email);
            signUpUser(fullName, email, password);
        });

        signInToggleButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    /**
     * This helps in creatingg a new user account using Firebase Authentication.
     * Once successful, updates the user profile with their name
     * and calls Firestore to store user details.
     *
     * @param fullName the user's full name
     * @param email the user's email address
     * @param password the user's chosen password
     */
    private void signUpUser(String fullName, String email, String password) {
        FirebaseHelper.getAuth()
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        Log.e(TAG, "FirebaseUser is null after signup");
                        Toast.makeText(this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    firebaseUser.reload().addOnCompleteListener(task -> {
                        FirebaseUser refreshedUser = FirebaseHelper.getAuth().getCurrentUser();
                        if (refreshedUser == null) {
                            Log.e(TAG, "User not found after reload");
                            Toast.makeText(this, "Could not confirm user login. Try again.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String userId = refreshedUser.getUid();
                        Log.d(TAG, "Firebase signup success, UID: " + userId);

                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(fullName)
                                .build();

                        refreshedUser.updateProfile(profile)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Profile updated. Creating Firestore record...");
                                    createUserInFirestore(userId, fullName, email);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Profile update failed", e);
                                    Toast.makeText(this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firebase signup failed", e);
                    Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Help in Creating a new user document in Firestore with the user's information.
     * After saving successfully, redirects the user to the landing page.
     *
     * @param userId the Firebase user ID
     * @param fullName the user's full name
     * @param email the user's email address
     */
    private void createUserInFirestore(String userId, String fullName, String email) {
        UserFirebase user = new UserFirebase(userId, email, fullName, Timestamp.now());

        FirebaseHelper.getFirestore()
                .collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User document created successfully in Firestore for: " + email);
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(SignUpActivity.this, LandingHostActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user in Firestore", e);
                    Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
