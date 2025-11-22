package com.example.eventapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

/**
 * The main activity that hosts all fragments after a user logs in.
 * It sets up the navigation graph and connects the ActionBar
 * to the navigation controller to handle back navigation properly.
 *
 * Author: tappit
 */
public class LandingHostActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets the layout, initializes the toolbar, and configures navigation
     * so fragments like OrganizerLandingFragment and EventDetailsFragment
     * can be displayed smoothly.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_host);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            if (getSupportActionBar() != null) {
                NavigationUI.setupActionBarWithNavController(this, navController);
            }
        }
    }

    /**
     * Handles the back navigation when the user presses
     * the ActionBar’s back button.
     *
     * @return true if navigation is handled by the NavController
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            return navHostFragment.getNavController().navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
