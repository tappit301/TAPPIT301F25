package com.example.eventapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * This is tThe default launcher activity created by Android Studio.
 * Sets up an edge-to-edge layout and adjusts view padding
 * to fit system bars like the status bar and navigation bar.
 *
 * This activity can be used as a testing or placeholder screen.
 *
 * Author: tappit
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Called when the activity is created.
     * Initializes the layout and handles padding for system insets.
     *
     * @param savedInstanceState saved state of the activity, if any
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
