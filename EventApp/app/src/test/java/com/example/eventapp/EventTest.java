package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event("Music Fest", "Live concert", "2025-12-10", "19:30", "UofA Hall");
    }

    @Test
    public void getters_returnConstructorValues() {
        assertEquals("Music Fest", event.getTitle());
        assertEquals("Live concert", event.getDescription());
        assertEquals("2025-12-10", event.getDate());
        assertEquals("19:30", event.getTime());
        assertEquals("UofA Hall", event.getLocation());
    }

    @Test
    public void title_isNotEmpty() {
        assertNotNull(event.getTitle());
        assertFalse(event.getTitle().isEmpty());
    }
}
