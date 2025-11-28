package com.example.eventapp.admin;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;

    private AdminManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized AdminManager getInstance() {
        if (instance == null) {
            instance = new AdminManager();
        }
        return instance;
    }

    // ------------------------------------------------------------
    // Remove Events
    // ------------------------------------------------------------
    public Task<Void> deleteEvent(@NonNull String eventId) {
        return db.collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("ADMIN", "Event deleted: " + eventId))
                .addOnFailureListener(e ->
                        Log.e("ADMIN", "Error deleting event", e));
    }

    // ------------------------------------------------------------
    // Remove Users (Profiles)
    // ------------------------------------------------------------
    public Task<Void> deleteUser(@NonNull String userId) {
        return db.collection("users")
                .document(userId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("ADMIN", "User deleted: " + userId))
                .addOnFailureListener(e ->
                        Log.e("ADMIN", "Error deleting user", e));
    }

    // ------------------------------------------------------------
    // Remove Images
    // ------------------------------------------------------------
    public Task<Void> deleteImage(@NonNull String imageId) {
        return db.collection("images")
                .document(imageId)
                .delete();
    }

    // ------------------------------------------------------------
    // Browse Events
    // ------------------------------------------------------------
    public Task<QuerySnapshot> getAllEvents() {
        return db.collection("events").get();
    }

    // ------------------------------------------------------------
    // Browse Users
    // ------------------------------------------------------------
    public Task<QuerySnapshot> getAllUsers() {
        return db.collection("users").get();
    }

    // ------------------------------------------------------------
    // Browse Images
    // ------------------------------------------------------------
    public Task<QuerySnapshot> getAllImages() {
        return db.collection("images").get();
    }

    // ------------------------------------------------------------
    // NEW: Browse Organizers Only
    // ------------------------------------------------------------
    public Task<QuerySnapshot> getAllOrganizers() {
        return db.collection("users")
                .whereEqualTo("role", "organizer")
                .get()
                .addOnSuccessListener(snap ->
                        Log.d("ADMIN", "Loaded organizers: " + snap.size()))
                .addOnFailureListener(e ->
                        Log.e("ADMIN", "Error loading organizers", e));
    }

    // ------------------------------------------------------------
    // Remove Organizer
    // ------------------------------------------------------------
    public Task<Void> deleteOrganizer(@NonNull String organizerId) {
        return db.collection("users")
                .document(organizerId)
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("ADMIN", "Organizer deleted: " + organizerId))
                .addOnFailureListener(e ->
                        Log.e("ADMIN", "Error deleting organizer", e));
    }

    // ------------------------------------------------------------
    // Notification Logs
    // ------------------------------------------------------------
    public Task<QuerySnapshot> getNotificationLogs() {
        return db.collection("notification_logs")
                .orderBy("timestamp")
                .get();
    }

    // ------------------------------------------------------------
    // System Config
    // ------------------------------------------------------------
    public Task<Void> setConfigValue(@NonNull String key, @NonNull String value) {
        return db.collection("system_config")
                .document("app_config")
                .update(key, value);
    }
}
