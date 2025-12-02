package com.example.eventapp;

import com.google.firebase.Timestamp;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttendeeTest {

    @Test
    public void testWaitingListConstructor() {
        Timestamp now = Timestamp.now();
        Attendee a = new Attendee("u1", "john@example.com", now);

        assertEquals("u1", a.getUserId());
        assertEquals("john@example.com", a.getEmail());

        // IMPORTANT: name defaults to email
        assertEquals("john@example.com", a.getName());

        assertEquals("waiting", a.getStatus());
        assertEquals(now, a.getJoinedAt());
    }

    @Test
    public void testManageEventsConstructor() {
        Attendee a = new Attendee("u2", "anna@example.com", "selected");

        assertEquals("u2", a.getUserId());
        assertEquals("anna@example.com", a.getEmail());
        assertEquals("selected", a.getStatus());

        // This constructor does NOT set name → name stays null unless email fallback is added
        // If you want name = email, add: this.name = email; in that constructor
        assertNull(a.getName());
    }

    @Test
    public void testSetters() {
        Attendee a = new Attendee();
        Timestamp now = Timestamp.now();

        a.setUserId("u3");
        a.setEmail("mark@example.com");
        a.setName("Mark");
        a.setStatus("waiting");
        a.setJoinedAt(now);

        assertEquals("u3", a.getUserId());
        assertEquals("mark@example.com", a.getEmail());
        assertEquals("Mark", a.getName());
        assertEquals("waiting", a.getStatus());
        assertEquals(now, a.getJoinedAt());
    }
}
