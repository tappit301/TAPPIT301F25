package com.example.eventapp;

public class Event {

    private String id;
    private String title;
    private String date;
    private String time;
    private String location;
    private String description;
    private String organizerId;
    private String category;

    // REQUIRED empty constructor for Firestore
    public Event() {}

    public Event(String title, String date, String time, String location,
                 String description, String category, String organizerId) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
        this.category = category;
        this.organizerId = organizerId;
    }

    // ---------- ID ----------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // ---------- Getters & setters ----------
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
