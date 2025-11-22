package com.example.eventapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-memory event repository.
 */
public class EventRepository {

    private static EventRepository instance;
    private final List<Event> events = new ArrayList<>();

    private EventRepository() {}

    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    public void addEvent(Event event) {
        events.add(event);
    }

    public List<Event> getAllEvents() {
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
