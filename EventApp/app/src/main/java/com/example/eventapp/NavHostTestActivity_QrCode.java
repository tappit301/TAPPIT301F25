package com.example.eventapp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class NavHostTestActivity_QrCode extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = new Bundle();
        args.putString("qrData", "");
        args.putBoolean("cameFromDetails", false);

        NavHostFragment host =
                NavHostFragment.create(R.navigation.nav_graph, args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, host, "TEST_NAV_HOST")
                .commitNow();

        NavController nav = host.getNavController();

        // Make QR the visible destination
        nav.navigate(R.id.qrCodeFragment, args);
    }
}
