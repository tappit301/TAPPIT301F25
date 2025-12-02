package com.example.eventapp.admin;

/**
 * Data model representing an organizer account.
 * Stores the organizer's user ID, email, and the number of events they created.
 */
public class OrganizerUser {

    public String uid;
    public String email;
    public int eventCount;

    /**
     * Creates a new organizer model with the given data.
     *
     * @param uid the organizer's user ID
     * @param email the organizer's email address
     * @param eventCount how many events the organizer has created
     */
    public OrganizerUser(String uid, String email, int eventCount) {
        this.uid = uid;
        this.email = email;
        this.eventCount = eventCount;
    }
}
