package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
<<<<<<< HEAD
=======

>>>>>>> origin/Pavan
import static org.junit.Assert.*;

public class EventDataTest {

    private List<Event> events;

    @Before
    public void init() {
        events = new ArrayList<>();
<<<<<<< HEAD
        events.add(new Event("Dog Walk", "Fun pet event", "2025-11-22", "09:00", "Rutherford Park"));
=======
        events.add(new Event(
                "Dog Walk",
                "Fun pet event",
                "2025-11-22",
                "09:00",
                "Rutherford Park",
                "Animals"
        ));
>>>>>>> origin/Pavan
    }

    @Test
    public void testAddEvent() {
        int start = events.size();
<<<<<<< HEAD
        events.add(new Event("Yoga", "Morning session", "2025-11-23", "07:00", "SUB Hall"));
=======

        events.add(new Event(
                "Yoga",
                "Morning session",
                "2025-11-23",
                "07:00",
                "SUB Hall",
                "Wellness"
        ));

>>>>>>> origin/Pavan
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
<<<<<<< HEAD
        events.add(new Event("Music Fest", "Outdoor concert", "2025-11-24", "18:00", "Quad"));
        events.add(new Event("Hackathon", "Coding competition", "2025-11-25", "08:00", "ECERF"));
=======
        events.add(new Event(
                "Music Fest", "Outdoor concert", "2025-11-24",
                "18:00", "Quad", "Entertainment"
        ));
        events.add(new Event(
                "Hackathon", "Coding competition", "2025-11-25",
                "08:00", "ECERF", "Tech"
        ));

>>>>>>> origin/Pavan
        assertTrue(events.size() >= 3);
    }
}
