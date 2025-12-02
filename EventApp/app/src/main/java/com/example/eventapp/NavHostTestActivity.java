package com.example.eventapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

/**
 * A minimal activity used only for testing navigation flows.
 * It dynamically loads the app's navigation graph into a NavHostFragment
 * without requiring the full application UI.
 */
public class NavHostTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavHostFragment host = NavHostFragment.create(R.navigation.nav_graph);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, host, "TEST_NAV_HOST")
                .commitNow();
    }
}
