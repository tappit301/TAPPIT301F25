package com.example.eventapp;

import org.junit.Test;
import static org.junit.Assert.*;

// IMPORTANT: Import YOUR app's User class
import com.example.eventapp.User;

public class UserTest {

    @Test
    public void gettersAndSettersWork() {
        User u = new User(); // now works (Firestore requires empty constructor)

        u.setId("ID1");
        u.setName("John Doe");
        u.setEmail("john@example.com");
        u.setRole("organizer");

        assertEquals("ID1", u.getId());
        assertEquals("John Doe", u.getName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("organizer", u.getRole());
    }
}
