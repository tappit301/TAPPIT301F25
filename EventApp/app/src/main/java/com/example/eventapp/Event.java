package com.example.eventapp;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {

    private String id;
    private String title;
    private String description;
    private String location;
    private String category;

    private String date;     // "30/11/2025"
    private String time;     // "06:30"

    private String organizerId;
    private String organizerEmail;

    private String imageUrl;

    private int capacity;
    private int attendeeCount;

    // Used by admin screens
    private Timestamp dateTime;

    public Event() {} // Required for Firestore

    // ----------------- GETTERS -----------------
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getOrganizerId() { return organizerId; }
    public String getOrganizerEmail() { return organizerEmail; }
    public String getImageUrl() { return imageUrl; }
    public int getCapacity() { return capacity; }
    public int getAttendeeCount() { return attendeeCount; }
    public Timestamp getDateTime() { return dateTime; }

    // ----------------- SETTERS -----------------
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setAttendeeCount(int attendeeCount) { this.attendeeCount = attendeeCount; }
    public void setDateTime(Timestamp dateTime) { this.dateTime = dateTime; }

    // ----------------- HELPERS -----------------

    /** Used by organizer/explorer filtering */
    public boolean isPastEvent() {

        // Case 1: Admin uses Timestamp dateTime
        if (dateTime != null) {
            return dateTime.toDate().before(new Date());
        }

        // Case 2: Organizer UI has date + time strings
        if (date != null && time != null) {
            try {
                String raw = date + " " + time;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                Date eventDate = sdf.parse(raw);

                return eventDate != null && eventDate.before(new Date());
            } catch (ParseException ignored) {}
        }

        return false; // No valid date → treat as upcoming
    }
}
