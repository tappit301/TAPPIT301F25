package com.example.eventapp;

/**
 * Event model mapped from Firestore.
 * Includes title, description, date, time, location, organizer info,
 * and optional imageUrl for event poster.
 */
public class Event {

    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;
    private String organizerId;
    private String organizerEmail;

    /** ⭐ Poster image URL */
    private String imageUrl;

    public Event() {}

    public Event(String title, String description, String date,
                 String time, String location) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerEmail() { return organizerEmail; }
    public String getImageUrl() { return imageUrl; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setLocation(String location) { this.location = location; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
