package com.example.eventapp;

import com.google.firebase.Timestamp;
import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Unified Event model used across organizer/explorer/admin.
 * Supports:
 *  - date/time strings (your UI)
 *  - Timestamp dateTime (Pavan)
 *  - geolocation
 *  - price, capacity, attendeeCount
 *  - imageUrl
 *  - admin/organizer filters
 */
public class Event {

    private String id;
    private String title;
    private String description;

    private String date;     // "30/11/2025"
    private String time;     // "06:30"
    private String location;
    private String category;

    private String organizerId;
    private String organizerEmail;

    private String imageUrl;
    private long timestamp;      // Used by your screens

    private int capacity;
    private int attendeeCount;

    private boolean requireGeolocation;
    private double price;

    // Optional — used by admin side
    private Timestamp dateTime;

    // Firebase requires empty constructor
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

    // ----------------------------
    // GETTERS
    // ----------------------------
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }

    public String getOrganizerId() { return organizerId; }
    public String getOrganizerEmail() { return organizerEmail; }

    public String getImageUrl() { return imageUrl; }
    public long getTimestamp() { return timestamp; }

    public int getCapacity() { return capacity; }
    public int getAttendeeCount() { return attendeeCount; }

    public boolean isRequireGeolocation() { return requireGeolocation; }
    public double getPrice() { return price; }

    public Timestamp getDateTime() { return dateTime; }

    // ----------------------------
    // SETTERS
    // ----------------------------
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
    public void setDateTime(Timestamp dateTime) { this.dateTime = dateTime; }

    // ----------------------------
    // LOGIC — Check Past Event
    // ----------------------------
    public boolean isPastEvent() {

        // Case 1: Admin timestamp
        if (dateTime != null) {
            return dateTime.toDate().before(new Date());
        }

        // Case 2: Organizer UI date + time
        if (date != null && time != null) {
            try {
                String raw = date + " " + time;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
                Date eventDate = sdf.parse(raw);

                return eventDate != null && eventDate.before(new Date());
            } catch (ParseException ignored) {}
        }

        return false;
    }
}
