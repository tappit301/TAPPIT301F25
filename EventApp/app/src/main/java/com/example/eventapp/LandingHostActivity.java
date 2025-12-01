package com.example.eventapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LandingHostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ============ AUTH CHECK (safely handles guest mode) ============
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        boolean guestMode = prefs.getBoolean("GUEST_MODE", false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!guestMode && user == null) {
            // No user logged in → redirect to Login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_landing_host);

        // ============ Notification permission (Android 13+) ============
        requestNotificationPermissionIfNeeded();

        // ============ Toolbar ============
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // ============ Navigation Setup ============
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Auto-open Explore if coming as Guest
            if (getIntent().getBooleanExtra("openExplore", false) || guestMode) {
                navController.navigate(R.id.exploreEventsFragment);
            }

            // Set up toolbar with navigation
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
    }

    // Inflate toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.landing_menu, menu);
        return true;
    }

    // Toolbar icon handling
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            // TODO: Navigate to profile
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Navigation up (back button in toolbar)
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        return (navHostFragment != null &&
                navHostFragment.getNavController().navigateUp())
                || super.onSupportNavigateUp();
    }

    // Request notification permission (Android 13+)
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001
                );
            }
        }
    }
}

