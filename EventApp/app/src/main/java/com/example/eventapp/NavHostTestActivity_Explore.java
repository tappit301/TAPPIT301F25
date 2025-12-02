package com.example.eventapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Test activity that loads the organizer navigation graph
 * and immediately navigates to the ExploreEventsFragment.
 * Used for isolated UI testing without launching the full app.
 */
public class NavHostTestActivity_Explore extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavHostFragment host =
                NavHostFragment.create(R.navigation.organizer_nav_graph);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, host, "TEST_NAV_HOST")
                .commitNow();

        NavController nav = host.getNavController();
        nav.navigate(R.id.exploreEventsFragment);
    }
}
