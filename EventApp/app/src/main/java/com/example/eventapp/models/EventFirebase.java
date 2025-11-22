package com.example.eventapp.models;

import com.google.firebase.Timestamp;

public class EventFirebase {

    private String title;
    private String description;
    private String location;
    private String category;
    private String organizerId;
    private String organizerName;
    private int capacity;
    private int attendeeCount;
    private Timestamp dateTime;

    // Empty constructor required by Firestore
    public EventFirebase() {}

    // Full constructor
    public EventFirebase(String title, String description, String location, String category,
                         String organizerId, String organizerName,
                         int capacity, int attendeeCount, Timestamp dateTime) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.category = category;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.capacity = capacity;
        this.attendeeCount = attendeeCount;
        this.dateTime = dateTime;
    }

    // --- Getters and Setters ---
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getAttendeeCount() {
        return attendeeCount;
    }

    public void setAttendeeCount(int attendeeCount) {
        this.attendeeCount = attendeeCount;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }
}
