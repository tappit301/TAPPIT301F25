package com.example.eventapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.eventapp.admin.AdminHostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(HomeActivity.this, LandingHostActivity.class));
            finish();
            return;
        }

        setupHomeUI();
    }

    private void setupHomeUI() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // === USER BUTTONS ===
        Button btnGettingStarted = findViewById(R.id.btnGettingStarted);
        btnGettingStarted.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SignUpActivity.class)));

        Button btnViewEvents = findViewById(R.id.button_view_events);
        btnViewEvents.setOnClickListener(v ->
                Toast.makeText(this, "Guest view coming soon!", Toast.LENGTH_SHORT).show());

        // === ADMIN BUTTON ===
        findViewById(R.id.btnAdminLogin).setOnClickListener(v -> {
            Intent adminIntent = new Intent(HomeActivity.this, AdminHostActivity.class);
            startActivity(adminIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.action_sign_in) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
