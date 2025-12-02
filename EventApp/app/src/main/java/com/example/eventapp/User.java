package com.example.eventapp;

/**
 * Represents a user in the system.
 * Supports attendee, organizer, and admin roles.
 * Used for Firestore serialization/deserialization.
 */
public class User {

    private String id;
    private String name;
    private String email;
    private String role; // attendee, organizer, admin

    /** Empty constructor required for Firestore. */
    public User() {}

    /** @return user ID (Firebase UID or guest ID) */
    public String getId() {
        return id;
    }

    /** @param id sets the user ID */
    public void setId(String id) {
        this.id = id;
    }

    /** @return user's display name */
    public String getName() {
        return name;
    }

    /** @param name sets the user's display name */
    public void setName(String name) {
        this.name = name;
    }

    /** @return user's email address */
    public String getEmail() {
        return email;
    }

    /** @param email sets the user's email address */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return role of user (attendee, organizer, admin) */
    public String getRole() {
        return role;
    }

    /** @param role sets the user's role */
    public void setRole(String role) {
        this.role = role;
    }
}
