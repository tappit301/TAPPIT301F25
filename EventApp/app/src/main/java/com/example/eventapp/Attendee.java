package com.example.eventapp;

import com.google.firebase.Timestamp;

/**
 * Model representing a user who has joined an event.
 * Stores basic attendee information such as ID, name, email, status, and join time.
 */
public class Attendee {

    private String userId;
    private String name;
    private String email;
    private String status;
    private Timestamp joinedAt;

    /**
     * Empty constructor required for Firestore deserialization.
     */
    public Attendee() {
    }

    /**
     * Creates an attendee with a user ID, email, and join timestamp.
     * Sets the display name to the email and uses a default status of waiting.
     *
     * @param userId the attendee's user ID
     * @param email the attendee's email
     * @param joinedAt the time the attendee joined the event
     */
    public Attendee(String userId, String email, Timestamp joinedAt) {
        this.userId = userId;
        this.email = email;
        this.joinedAt = joinedAt;
        this.name = email;
        this.status = "waiting";
    }

    /**
     * Creates an attendee with a user ID, email, and explicit status.
     *
     * @param userId the attendee's user ID
     * @param email the attendee's email
     * @param status the attendee's status in the event
     */
    public Attendee(String userId, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
    }

    /**
     * Returns the attendee's user ID.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the attendee's display name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the attendee's email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the attendee's current status.
     *
     * @return the status value
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns when the attendee joined the event.
     *
     * @return the join timestamp
     */
    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    /**
     * Sets the attendee's user ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Sets the attendee's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the attendee's email.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the attendee's status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the time the attendee joined the event.
     */
    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
