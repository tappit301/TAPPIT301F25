package com.example.eventapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Custom Application class that initializes Firebase services
 * and manages app-level setup such as offline persistence and
 * session handling during cold starts.
 *
 * Author: tappit
 */
public class App extends Application {

    /**
     * Called when the application is first created.
     * Initializes Firebase, enables Firestore offline persistence,
     * and checks if the app was launched after a full termination.
     * If it is a cold start, the FirebaseAuth session is cleared.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        Log.d("FirebaseInit", "Firebase initialized");

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
        );

        SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);
        boolean wasRunning = prefs.getBoolean("was_running", false);

        if (!wasRunning) {
            Log.d("FirebaseInit", "Cold start detected — clearing FirebaseAuth session");
            try {
                FirebaseAuth.getInstance().signOut();
            } catch (Exception e) {
                Log.e("FirebaseInit", "Error clearing session: " + e.getMessage());
            }
        }

        prefs.edit().putBoolean("was_running", true).apply();
    }

    /**
     * Called when the app process is terminated (mainly on emulators or debug mode).
     * Marks the app as not running so that a new Firebase session is required
     * on the next launch.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);
        prefs.edit().putBoolean("was_running", false).apply();
        Log.d("FirebaseInit", "App terminated — will require login next launch");
    }
}
