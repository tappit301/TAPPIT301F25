package com.example.eventapp;

import com.google.firebase.Timestamp;

public class Attendee {

    private String userId;
    private String name;
    private String email;
    private String status;      // e.g. "waiting", "selected"
    private Timestamp joinedAt; // when they joined the event

    // Needed for Firestore deserialization
    public Attendee() {
    }

    // Constructor you are using in loadWaitingList()
    public Attendee(String userId, String email, Timestamp joinedAt) {
        this.userId = userId;
        this.email = email;
        this.joinedAt = joinedAt;

        // If you don’t have an actual name, just show the email
        this.name = email;

        // Default status for this screen
        this.status = "waiting";
    }

    // NEW constructor for ManageEvents
    public Attendee(String userId, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
    }

    // --- Getters used by the adapter ---

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    // Optional setters if you ever need them
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }
}
