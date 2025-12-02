package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Hosts the main navigation graph of the app once a user or guest enters the system.
 * This activity:
 *  - Validates authentication unless the user enters as a guest.
 *  - Sets up the main toolbar.
 *  - Loads the NavHostFragment responsible for fragment navigation.
 *
 * The activity acts as a container for all post-login (or guest mode) UI.
 */
public class LandingHostActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Handles authentication validation, initializes the toolbar,
     * and wires the NavController to the navigation graph.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine if user is entering without authentication
        boolean isGuest = getIntent().getBooleanExtra("isGuest", false);

        /**
         * If not in guest mode and not in automated test mode,
         * ensure the user is authenticated. If not authenticated,
         * redirect to LoginActivity.
         */
        if (!isGuest && !TestMode.IS_TEST) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }

        setContentView(R.layout.activity_landing_host);

        // Set up the top app bar
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Locate NavHostFragment defined in the layout
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        /**
         * If the NavHostFragment is available, extract its NavController
         * and bind it to the ActionBar for automatic back button handling.
         */
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController);
        }
    }

    /**
     * Ensures the system back button or toolbar back arrow
     * correctly navigates up the fragment stack.
     *
     * @return true if navigation was handled by the NavController;
     *         false if default system behavior should run instead
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}