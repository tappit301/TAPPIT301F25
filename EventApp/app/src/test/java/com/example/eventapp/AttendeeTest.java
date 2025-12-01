package com.example.eventapp;

import org.junit.Test;
import static org.junit.Assert.*;

public class AttendeeTest {

    @Test
    public void testConstructorAndGetters() {
        Attendee attendee = new Attendee("John Doe", "john@example.com", "Checked-in");

        assertEquals("John Doe", attendee.getName());
        assertEquals("john@example.com", attendee.getEmail());
        assertEquals("Checked-in", attendee.getStatus());
    }
}
