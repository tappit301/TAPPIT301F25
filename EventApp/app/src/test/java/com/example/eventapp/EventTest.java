package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;
<<<<<<< HEAD

=======
>>>>>>> origin/Pavan
import static org.junit.Assert.*;

public class EventTest {

    private Event event;

    @Before
    public void setUp() {
<<<<<<< HEAD
        event = new Event("Music Fest", "Live concert", "2025-12-10", "19:30", "UofA Hall");
=======
        event = new Event(
                "Music Fest",
                "Live concert",
                "2025-12-10",
                "19:30",
                "UofA Hall",
                "Entertainment"
        );
>>>>>>> origin/Pavan
    }

    @Test
    public void getters_returnConstructorValues() {
        assertEquals("Music Fest", event.getTitle());
        assertEquals("Live concert", event.getDescription());
        assertEquals("2025-12-10", event.getDate());
        assertEquals("19:30", event.getTime());
        assertEquals("UofA Hall", event.getLocation());
<<<<<<< HEAD
=======
        assertEquals("Entertainment", event.getCategory());
>>>>>>> origin/Pavan
    }

    @Test
    public void title_isNotEmpty() {
        assertNotNull(event.getTitle());
        assertFalse(event.getTitle().isEmpty());
    }
}
