package com.example.eventapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
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

    // Channel ID used by NotificationHelper
    public static final String CHANNEL_ID = "eventapp_main_notifications";

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

        // Create notifications channel (Siya's addition)
        createNotificationChannel();

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

    @Override
    public void onTerminate() {
        super.onTerminate();

        SharedPreferences prefs = getSharedPreferences("app_state", MODE_PRIVATE);
        prefs.edit().putBoolean("was_running", false).apply();
        Log.d("FirebaseInit", "App terminated — will require login next launch");
    }

    /**
     * Registers the notification channel used for all app notifications.
     * Required on Android O and above.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Event notifications";
            String description = "Notifications about waitlists, selections, and event updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
