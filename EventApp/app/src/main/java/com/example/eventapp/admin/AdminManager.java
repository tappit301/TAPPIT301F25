package com.example.eventapp.admin;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager class that provides admin-level access to Firestore data.
 * Handles operations for users, organizers, events, images, configuration values,
 * notification logs, reports, and event attendees.
 */
public class AdminManager {

    private static AdminManager instance;
    private final FirebaseFirestore db;

    /**
     * Creates a new AdminManager and initializes the Firestore reference.
     */
    private AdminManager() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns the singleton instance of the AdminManager.
     *
     * @return the shared instance
     */
    public static AdminManager getInstance() {
        if (instance == null) instance = new AdminManager();
        return instance;
    }

    /**
     * Returns all users stored in the users collection.
     *
     * @return a task containing the user documents
     */
    public Task<QuerySnapshot> getAllUsers() {
        return db.collection("users").get();
    }

    /**
     * Deletes a user from the users collection.
     *
     * @param userId the ID of the user to delete
     * @return a task tracking the delete operation
     */
    public Task<Void> deleteUser(String userId) {
        return db.collection("users").document(userId).delete();
    }

    /**
     * Returns all organizers, filtered by type = "organizer".
     *
     * @return a task containing organizer documents
     */
    public Task<QuerySnapshot> getAllOrganizers() {
        return db.collection("users")
                .whereEqualTo("type", "organizer")
                .get();
    }

    /**
     * Deletes an organizer account using its document ID.
     *
     * @param organizerId the organizer's ID
     * @return a task tracking the delete operation
     */
    public Task<Void> deleteOrganizer(String organizerId) {
        return db.collection("users").document(organizerId).delete();
    }

    /**
     * Retrieves all events from the events collection.
     *
     * @return a task with event documents
     */
    public Task<QuerySnapshot> getAllEvents() {
        return db.collection("events").get();
    }

    /**
     * Deletes an event using its document ID.
     *
     * @param eventId the event ID
     * @return a task tracking the delete operation
     */
    public Task<Void> deleteEvent(String eventId) {
        return db.collection("events").document(eventId).delete();
    }

    /**
     * Retrieves all events that contain a non-empty image URL.
     *
     * @return a task with event image data
     */
    public Task<QuerySnapshot> getAllImages() {
        return db.collection("events")
                .whereNotEqualTo("imageUrl", "")
                .get();
    }

    /**
     * Fetches all notification logs ordered by creation time.
     *
     * @return a task containing log documents
     */
    public Task<QuerySnapshot> getNotificationLogs() {
        return db.collection("notifications")
                .orderBy("createdAt")
                .get();
    }

    /**
     * Updates a configuration value in the config collection.
     *
     * @param key the config key
     * @param value the new config value
     * @return a task tracking the update
     */
    public Task<Void> setConfigValue(String key, String value) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", value);

        return db.collection("config").document(key).set(map);
    }

    /**
     * Fetches a configuration value for the given key.
     *
     * @param key the config key
     * @return a task containing the config document
     */
    public Task<DocumentSnapshot> getConfigValue(String key) {
        return db.collection("config").document(key).get();
    }

    /**
     * Retrieves all user documents for reporting.
     *
     * @return a task with user report data
     */
    public Task<QuerySnapshot> getUserReport() {
        return db.collection("users").get();
    }

    /**
     * Retrieves all event documents for reporting.
     *
     * @return a task with event report data
     */
    public Task<QuerySnapshot> getEventReport() {
        return db.collection("events").get();
    }

    /**
     * Retrieves all lottery log documents.
     *
     * @return a task with lottery log data
     */
    public Task<QuerySnapshot> getLotteryLogs() {
        return db.collection("eventLotteryLogs").get();
    }

    /**
     * Retrieves all attendees for a specific event.
     *
     * @param eventId the event ID
     * @return a task with attendee documents
     */
    public Task<QuerySnapshot> getEventAttendees(String eventId) {
        return db.collection("eventAttendees")
                .document(eventId)
                .collection("attendees")
                .get();
    }
}
