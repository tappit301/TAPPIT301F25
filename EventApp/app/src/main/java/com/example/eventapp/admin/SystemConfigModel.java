package com.example.eventapp.admin;

/**
 * Data model representing a system configuration value.
 * Stores a single string value used for app-wide settings.
 */
public class SystemConfigModel {

    public String value;

    /**
     * Empty constructor required for Firestore deserialization.
     */
    public SystemConfigModel() { }

    /**
     * Creates a config model with the given value.
     *
     * @param value the configuration value
     */
    public SystemConfigModel(String value) {
        this.value = value;
    }
}
