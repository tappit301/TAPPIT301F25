package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class EventDataTest {

    private List<Event> events;

    @Before
    public void init() {
        events = new ArrayList<>();
        events.add(new Event("Dog Walk", "Fun pet event", "2025-11-22", "09:00", "Rutherford Park"));
    }

    @Test
    public void testAddEvent() {
        int start = events.size();
        events.add(new Event("Yoga", "Morning session", "2025-11-23", "07:00", "SUB Hall"));
        assertEquals(start + 1, events.size());
    }

    @Test
    public void testEventTitlesNotEmpty() {
        for (Event e : events) {
            assertNotNull(e.getTitle());
            assertFalse(e.getTitle().isEmpty());
        }
    }

    @Test
    public void testMultipleEventsPersist() {
        events.add(new Event("Music Fest", "Outdoor concert", "2025-11-24", "18:00", "Quad"));
        events.add(new Event("Hackathon", "Coding competition", "2025-11-25", "08:00", "ECERF"));
        assertTrue(events.size() >= 3);
    }
}
