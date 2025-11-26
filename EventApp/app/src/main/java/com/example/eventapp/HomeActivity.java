package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * The first screen of the app that lets users choose between
 * signing in, creating an account, or viewing events as a guest.
 *
 * If a user is already signed in, they are automatically redirected
 * to the landing page without seeing this screen.
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // If a user is already signed in → skip home screen
        if (currentUser != null) {
            Log.d("AuthCheck", "Signed in. Going to LandingHostActivity...");
            goToLandingAsUser();
            return;
        }

        setupHomeUI();
    }

    /**
     * Sets up toolbar and main buttons.
     */
    private void setupHomeUI() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tappit");
        }

        Button btnGettingStarted = findViewById(R.id.btnGettingStarted);
        btnGettingStarted.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class))
        );

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SignUpActivity.class))
        );

        // ⭐ GUEST MODE BUTTON ⭐
        Button btnViewEvents = findViewById(R.id.button_view_events);
        btnViewEvents.setOnClickListener(v -> continueAsGuest());
    }

    /**
     * Guest mode: no login required.
     * Simply store a preference flag & open LandingHostActivity.
     */
    private void continueAsGuest() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", true).apply();

        Toast.makeText(this, "Continuing as guest...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        startActivity(intent);
        finish();
    }

    /** Normal logged-in navigation */
    private void goToLandingAsUser() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", false).apply();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Inflate toolbar menu (Profile / Sign In).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles top-right toolbar actions.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Toast.makeText(this, "Sign in to view profile", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_sign_in) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
