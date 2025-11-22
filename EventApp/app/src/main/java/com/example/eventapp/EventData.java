package com.example.eventapp;

import java.util.ArrayList;
import java.util.List;

/**
 * This is aasimple data holder class that stores all {@link Event} objects
 * in a shared static list while the app is running.
 *
 * This class makes it easy to temporarily keep events in memory
 * without reloading from Firestore.
 *
 * @author tappit
 */
public class EventData {

    /** A static list containing all events created or fetched. */
    private static final List<Event> eventList = new ArrayList<>();

    /**
     * Returns the list of all stored events.
     *
     * @return list of {@link Event} objects
     */
    public static List<Event> getEventList() {
        return eventList;
    }

    /**
     * Adds a new event to the shared list.
     *
     * @param event the {@link Event} to add
     */
    public static void addEvent(Event event) {
        eventList.add(event);
    }

    /**
     * Clears all events from the list.
     * It is Useful when refreshing or logging out.
     */
    public static void clearEvents() {
        eventList.clear();
    }
}
