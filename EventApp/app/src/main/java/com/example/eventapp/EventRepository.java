package com.example.eventapp;

import java.util.ArrayList;
import java.util.List;

/**
 * this is a simple singleton class that stores and manages all {@link Event} objects.
 *
 * This repository keeps event data in memory during the app's runtime,
 * so different fragments can easily access the same list of events.</p>
 *
 *>Note: This is not connected to Firestore . it only stores data locally
 * while the app is open.
 *
 * @author tappit
 */
public class EventRepository {

    /** The single shared instance of this repository. */
    private static EventRepository instance;

    /** List that holds all events currently in memory. */
    private final List<Event> events = new ArrayList<>();

    /**
     * Private constructor to \prevent creating multiple instances.
     */
    private EventRepository() {}

    /**
     * Returns the one shared instance of this repository.
     * Creates it if it doesn't already exist.
     *
     * @return the {@link EventRepository} instance
     */
    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    /**
     * Adds an event to the inmemory list.
     *
     * @param event the {@link Event} to add
     */
    public void addEvent(Event event) {
        events.add(event);
    }

    /**
     * Returns all events currently stored in the repository.
     *
     * @return list of {@link Event} objects
     */
    public List<Event> getEvents() {
        return events;
    }

    public List<Event> getEventsByCategory(String category) {
        if (category.equals("All")) return events;

        List<Event> filtered = new ArrayList<>();
        for (Event e : events) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                filtered.add(e);
            }
        }
        return filtered;
    }
}
