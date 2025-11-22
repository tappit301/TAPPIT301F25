package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * This is the first activity that launches when the app starts.
 * Checks if a user is already signed in and redirects them
 * to the correct screen.
 *
 * If the user is logged in, they are sent to LandingHostActivity.
 * If not, they are sent to LoginActivity.
 *
 * Author: tappit
 */
public class LauncherActivity extends AppCompatActivity {

    /**
     * Called when the launcher activity is created.
     * Decides which screen to open based on the user's login state,
     * then closes itself so it doesn't remain in the back stack.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(this, LandingHostActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
