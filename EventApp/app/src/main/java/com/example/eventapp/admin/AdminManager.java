package com.example.eventapp.admin;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;

    private AdminManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static AdminManager getInstance() {
        if (instance == null) instance = new AdminManager();
        return instance;
    }

    // ---------------------------
    // USERS
    // ---------------------------

    /** All users (from users collection) */
    public Task<QuerySnapshot> getAllUsers() {
        return db.collection("users").get();
    }

    /** Delete a user */
    public Task<Void> deleteUser(String userId) {
        return db.collection("users").document(userId).delete();
    }

    // ---------------------------
    // ORGANIZERS
    // ---------------------------

    /** All organizers: users where type = "organizer" */
    public Task<QuerySnapshot> getAllOrganizers() {
        return db.collection("users")
                .whereEqualTo("type", "organizer")
                .get();
    }

    /** Delete organizer account */
    public Task<Void> deleteOrganizer(String organizerId) {
        return db.collection("users").document(organizerId).delete();
    }

    // ---------------------------
    // EVENTS
    // ---------------------------

    /** Pull ALL events from events collection */
    public Task<QuerySnapshot> getAllEvents() {
        return db.collection("events").get();
    }

    /** Delete event */
    public Task<Void> deleteEvent(String eventId) {
        return db.collection("events").document(eventId).delete();
    }

    // ---------------------------
    // EVENT IMAGES
    // ---------------------------
    // You do NOT have "eventImages" in Firestore, so we point this to events.

    /** Get all event images → resolved by looking at events that contain imageUrl */
    public Task<QuerySnapshot> getAllImages() {
        return db.collection("events")
                .whereNotEqualTo("imageUrl", "")
                .get();
    }

    // ---------------------------
    // NOTIFICATION LOGS
    // ---------------------------

    /** Fetch all notification logs */
    public Task<QuerySnapshot> getNotificationLogs() {
        return db.collection("notifications")
                .orderBy("createdAt")
                .get();
    }

    // ---------------------------
    // CONFIG VALUES
    // ---------------------------

    /** Change config value */
    public Task<Void> setConfigValue(String key, String value) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", value);

        return db.collection("config").document(key).set(map);
    }

    /** Retrieve single config value */
    public Task<DocumentSnapshot> getConfigValue(String key) {
        return db.collection("config").document(key).get();
    }

    // ---------------------------
    // REPORTS / STATISTICS
    // ---------------------------

    public Task<QuerySnapshot> getUserReport() {
        return db.collection("users").get();
    }

    public Task<QuerySnapshot> getEventReport() {
        return db.collection("events").get();
    }

    public Task<QuerySnapshot> getLotteryLogs() {
        return db.collection("eventLotteryLogs").get();
    }

    // ---------------------------
    // EVENT ATTENDEES
    // ---------------------------

    public Task<QuerySnapshot> getEventAttendees(String eventId) {
        return db.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get();
    }
}
