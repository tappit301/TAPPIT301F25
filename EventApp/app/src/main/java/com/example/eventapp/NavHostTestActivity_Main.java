package com.example.eventapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Test activity for loading the main navigation graph in isolation.
 * Used by UI tests to render fragments without launching the full app.
 */
public class NavHostTestActivity_Main extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NavHostFragment host =
                NavHostFragment.create(R.navigation.nav_graph);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, host, "TEST_NAV_HOST")
                .commitNow();
    }
}
