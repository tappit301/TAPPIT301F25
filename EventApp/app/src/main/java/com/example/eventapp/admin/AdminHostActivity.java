package com.example.eventapp.admin;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapp.R;

/**
 * Activity that hosts all admin-related fragments.
 * Serves as the main container for the admin dashboard and its navigation flow.
 */
public class AdminHostActivity extends AppCompatActivity {

    /**
     * Sets up the activity layout that will display admin fragments.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_host);
    }
}
