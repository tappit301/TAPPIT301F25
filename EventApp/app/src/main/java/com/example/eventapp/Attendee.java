package com.example.eventapp;

public class Attendee {

    private String userId;
    private String email;
    private String name;
    private String status;

    // Required empty constructor for Firestore
    public Attendee() {}

    // Main constructor used throughout the project
    public Attendee(String userId, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.name = "";
    }

    // Full constructor (optional extended)
    public Attendee(String userId, String name, String email, String status) {
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.name = name;
    }

    // Wrapper constructor (for compatibility with Yashit's code)
    // If Yashit created an Attendee using (name, email, status),
    // we assign userId = "", and fill in the fields.
    public Attendee(String name, String email, String status, boolean fromYashit) {
        this.name = name;
        this.email = email;
        this.status = status;
        this.userId = "";
    }

    public String getUserId() {
        return userId != null ? userId : "";
    }

    public String getEmail() {
        return email != null ? email : "No email";
    }

    public String getStatus() {
        return status != null ? status : "unknown";
    }

    public String getName() {
        return name != null && !name.isEmpty() ? name : "Unknown";
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
