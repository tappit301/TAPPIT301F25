package com.example.eventapp;

/**
 * Event model mapped from Firestore.
 * Includes title, description, date, time, location, organizer info,
 * capacity, imageUrl for event poster, timestamps, and other metadata.
 */
public class Event {

    private String id;
    private String title;
    private String description;
    private String date;          // YYYY-MM-DD or user format
    private String time;          // HH:mm
    private String location;
    private String category;

    private String organizerId;
    private String organizerEmail;

    /** Poster image URL (Firebase Storage) */
    private String imageUrl;

    /** For upcoming / past filtering */
    private long timestamp;

    /** Optional: capacity if you use lotteries */
    private int capacity;

    /** Optional: attendees count (for waitlist UI) */
    private int attendeeCount;

    // REQUIRED empty constructor for Firestore
    public Event() {}

    public Event(String title, String description, String date,
                 String time, String location, String category) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.category = category;
    }

    private boolean requireGeolocation;

    public boolean isRequireGeolocation() { return requireGeolocation; }
    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    // --------------------
    // GETTERS
    // --------------------

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }

    public String getOrganizerId() { return organizerId; }
    public String getOrganizerEmail() { return organizerEmail; }

    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }

    public int getCapacity() { return capacity; }
    public int getAttendeeCount() { return attendeeCount; }

    // --------------------
    // SETTERS
    // --------------------

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setLocation(String location) { this.location = location; }

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }
}
