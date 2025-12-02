package com.example.eventapp;

/**
 * Model representing an event stored in Firestore.
 * Contains descriptive information such as title, date, time, location,
 * organizer details, category, image URL, and optional metadata such as
 * capacity, attendee count, timestamps, and geolocation requirements.
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

    /**
     * Required empty constructor for Firestore deserialization.
     */
    public Event() {}

    /**
     * Creates a basic event model with the main event details.
     *
     * @param title event title
     * @param description event description
     * @param date event date as a string
     * @param time event time as a string
     * @param location event location text
     * @param category category that this event belongs to
     */
    public Event(String title,
                 String description,
                 String date,
                 String time,
                 String location,
                 String category) {

        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.location = location;
        this.category = category;
    }

    /**
     * Returns whether geolocation validation is required for this event.
     *
     * @return true if geolocation is required
     */
    public boolean isRequireGeolocation() { return requireGeolocation; }

    /**
     * Sets whether geolocation validation is required.
     *
     * @param requireGeolocation true to enable geolocation requirement
     */
    public void setRequireGeolocation(boolean requireGeolocation) {
        this.requireGeolocation = requireGeolocation;
    }

    /**
     * Returns the Firestore document ID for this event.
     */
    public String getId() { return id; }

    /**
     * Returns the event title.
     */
    public String getTitle() { return title; }

    /**
     * Returns the event description.
     */
    public String getDescription() { return description; }

    /**
     * Returns the event date string.
     */
    public String getDate() { return date; }

    /**
     * Returns the event time string.
     */
    public String getTime() { return time; }

    /**
     * Returns the event location text.
     */
    public String getLocation() { return location; }

    /**
     * Returns the organizer's user ID.
     */
    public String getOrganizerId() { return organizerId; }

    /**
     * Returns the organizer's email address.
     */
    public String getOrganizerEmail() { return organizerEmail; }

    /**
     * Returns the event category.
     */
    public String getCategory() { return category; }

    /**
     * Returns the URL of the event's image.
     */
    public String getImageUrl() { return imageUrl; }

    /**
     * Returns the event timestamp used for sorting or filtering.
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Returns the maximum attendance capacity.
     */
    public int getCapacity() { return capacity; }

    /**
     * Returns the number of attendees currently joined or waitlisted.
     */
    public int getAttendeeCount() { return attendeeCount; }

    /**
     * Sets the event ID.
     *
     * @param id Firestore document ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Sets the event title.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Sets the event category.
     */
    public void setCategory(String category) { this.category = category; }

    /**
     * Sets the event description.
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Sets the event date.
     */
    public void setDate(String date) { this.date = date; }

    /**
     * Sets the event time.
     */
    public void setTime(String time) { this.time = time; }

    /**
     * Sets the event location text.
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Sets the organizer's user ID.
     */
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    /**
     * Sets the organizer's email address.
     */
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }

    /**
     * Sets the event's image URL.
     */
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    /**
     * Sets the timestamp for sorting and filtering.
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Sets the maximum attendance capacity.
     */
    public void setCapacity(int capacity) { this.capacity = capacity; }

    /**
     * Sets the attendee count.
     */
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }
}
