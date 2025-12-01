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
 * Home screen where user chooses:
 * - Login
 * - Create Account
 * - View Events as Guest
 *
 * If already logged in → directly open LandingHostActivity.
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        mAuth = FirebaseAuth.getInstance();

        boolean fromExplore = getIntent().getBooleanExtra("fromExplore", false);

        // AUTO LOGIN unless coming back manually
        if (!fromExplore) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                goToLandingAsUser();
                return;
            }
        }

        setupHomeUI();
    }

    /** Sets up toolbar + buttons */
    private void setupHomeUI() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tappit");
        }

        Button btnGettingStarted = findViewById(R.id.btnGettingStarted);
        btnGettingStarted.setOnClickListener(
                v -> startActivity(new Intent(HomeActivity.this, LoginActivity.class))
        );

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(
                v -> startActivity(new Intent(HomeActivity.this, SignUpActivity.class))
        );

        // Guest Mode
        Button btnViewEvents = findViewById(R.id.button_view_events);
        btnViewEvents.setOnClickListener(v -> continueAsGuest());
    }

    /** Guest mode logic */
    private void continueAsGuest() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", true).apply();

        Toast.makeText(this, "Continuing as guest...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        intent.putExtra("openExplore", true);
        startActivity(intent);
        finish();
    }

    /** Logged-in navigation */
    private void goToLandingAsUser() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", false).apply();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        startActivity(intent);
        finish();
    }

    // Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    // Toolbar item click handling
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Toast.makeText(this, "Sign in to view profile", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_sign_in) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
