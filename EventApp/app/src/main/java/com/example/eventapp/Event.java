package com.example.eventapp;

/**
 * Event model mapped from Firestore.
 * Includes title, description, date, time, location, organizer info,
 * capacity, imageUrl, timestamps, geolocation requirement, and price.
 */
public class Event {

    private String id;
    private String title;
    private String description;
    private String date;
    private String time;
    private String location;
    private String category;

    private String organizerId;
    private String organizerEmail;

    private String imageUrl;
    private long timestamp;

    private int capacity;
    private int attendeeCount;

    private boolean requireGeolocation;

    private double price;   // ⭐ ADDED because your layout uses price

    // REQUIRED empty constructor for Firestore
    public Event() {}

    public Event(String title, String description, String date, String time,
                 String location, String category) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.category = category;
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

    public boolean isRequireGeolocation() { return requireGeolocation; }

    public double getPrice() { return price; }

    // --------------------
    // SETTERS
    // --------------------
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }

    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    public void setPrice(double price) { this.price = price; }
}
