package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
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

/**
 * Home screen shown to users before signing in.
 * Handles navigation to login, sign-up, guest mode, or admin login.
 */
public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    /**
     * Initializes the home screen and redirects signed-in users immediately.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Log.d("AuthCheck", "Signed in. Going to LandingHostActivity...");
            goToLandingAsUser();
            return;
        }

        setupHomeUI();
    }

    /**
     * Sets up buttons, toolbar, and guest access for the home page.
     */
    private void setupHomeUI() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tappit");
        }

        Button btnGettingStarted = findViewById(R.id.btnGettingStarted);
        btnGettingStarted.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LoginActivity.class)));

        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCreateAccount.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, SignUpActivity.class)));

        Button btnViewEvents = findViewById(R.id.button_view_events);
        btnViewEvents.setOnClickListener(v -> continueAsGuest());
    }

    /**
     * Enables guest mode and navigates to the main event screen.
     */
    private void continueAsGuest() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", true).apply();

        Toast.makeText(this, "Continuing as guest...", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navigates authenticated users to the landing screen.
     */
    private void goToLandingAsUser() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit().putBoolean("GUEST_MODE", false).apply();

        Intent intent = new Intent(HomeActivity.this, LandingHostActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Inflates the action bar menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * Handles menu selections such as sign-in, profile, or admin login.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Toast.makeText(this, "Sign in to view profile", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (id == R.id.action_sign_in) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }

        if (id == R.id.action_admin_login) {
            startActivity(new Intent(HomeActivity.this, AdminHostActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Exposed for testing: returns the home toolbar.
     */
    public Toolbar getToolbar() {
        return findViewById(R.id.topAppBar);
    }
}
