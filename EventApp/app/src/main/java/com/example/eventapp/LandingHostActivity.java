package com.example.eventapp;

import android.Manifest;
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

public class LandingHostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_host);

        // Ask for notification permission on Android 13+
        requestNotificationPermissionIfNeeded();

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // Navigation setup
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // If redirected to explore
            if (getIntent().getBooleanExtra("openExplore", false)) {
                navController.navigate(R.id.exploreEventsFragment);
            }

            // Configure ActionBar for navigation
            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController);
            }

            // Guest mode handling
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            boolean guestMode = prefs.getBoolean("GUEST_MODE", false);
            boolean fromGuest = getIntent().getBooleanExtra("FROM_GUEST", false);

            if (guestMode || fromGuest) {
                navController.navigate(R.id.exploreEventsFragment);
            }
        }
    }

    /** Inflate toolbar menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.landing_menu, menu);
        return true;
    }

    /** Handle toolbar menu icons */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // TODO: Navigate to profile fragment
            // NavHostFragment.findNavController(...)
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** Handle navigation up */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp()
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    /** Request notification permission if needed (Android 13+) */
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
