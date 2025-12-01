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

        requestNotificationPermissionIfNeeded();

        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            if (getIntent().getBooleanExtra("openExplore", false)) {
                navController.navigate(R.id.exploreEventsFragment);
            }

            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController);
            }
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            boolean guestMode = prefs.getBoolean("GUEST_MODE", false);
            boolean fromGuest = getIntent().getBooleanExtra("FROM_GUEST", false);

            if (guestMode || fromGuest) {
                // send guest straight to ExploreEvents
                navController.navigate(R.id.exploreEventsFragment);
            }
        }
    }

<<<<<<< HEAD
    /**
     * Inflate the toolbar menu for the landing page.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.landing_menu, menu);
        return true;
    }

    /**
     * Handle clicks on toolbar menu items.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            // Open profile screen
            // startActivity(new Intent(LandingHostActivity.this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Handles the back navigation when the user presses
     * the ActionBar’s back button.
     *
     * @return true if navigation is handled by the NavController
     */
=======
>>>>>>> origin/yashit_new
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
