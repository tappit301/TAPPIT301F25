package com.example.eventapp.admin;

/**
 * Data model representing a user in the admin panel.
 * Stores the user's ID, email, and display name.
 */
public class AdminUserModel {

    private String id;
    private String email;
    private String name;

    /**
     * Empty constructor required for Firestore deserialization.
     */
    public AdminUserModel() {}

    /**
     * Returns the user ID.
     *
     * @return the user's ID
     */
    public String getId() { return id; }

    /**
     * Sets the user ID.
     *
     * @param id the user's ID
     */
    public void setId(String id) { this.id = id; }

    /**
     * Returns the user's email.
     *
     * @return the email address
     */
    public String getEmail() { return email; }

    /**
     * Sets the user's email.
     *
     * @param email the email address
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the user's name.
     *
     * @return the display name
     */
    public String getName() { return name; }

    /**
     * Sets the user's name.
     *
     * @param name the display name
     */
    public void setName(String name) { this.name = name; }
}
