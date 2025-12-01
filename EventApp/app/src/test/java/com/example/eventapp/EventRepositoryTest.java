package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;
<<<<<<< HEAD
import static org.junit.Assert.*;

import java.util.List;

=======
import java.util.List;

import static org.junit.Assert.*;

>>>>>>> origin/Pavan
public class EventRepositoryTest {

    private EventRepository repository;

    @Before
    public void setup() {
<<<<<<< HEAD
        // Reset singleton before each test to ensure isolation
=======
>>>>>>> origin/Pavan
        repository = EventRepository.getInstance();
        repository.getEvents().clear();
    }

    @Test
    public void testSingletonReturnsSameInstance() {
        EventRepository repo2 = EventRepository.getInstance();
        assertSame(repository, repo2);
    }

    @Test
    public void testAddEventStoresEvent() {
<<<<<<< HEAD
        Event e = new Event("Hackathon", "Coding event", "2025-12-01", "09:00", "UofA");
        repository.addEvent(e);

        List<Event> events = repository.getEvents();
=======
        Event e = new Event(
                "Hackathon", "Coding event",
                "2025-12-01", "09:00",
                "UofA", "Tech"
        );

        repository.addEvent(e);

        List<Event> events = repository.getEvents();

>>>>>>> origin/Pavan
        assertEquals(1, events.size());
        assertEquals("Hackathon", events.get(0).getTitle());
    }

    @Test
    public void testMultipleEventsStored() {
<<<<<<< HEAD
        repository.addEvent(new Event("Music Fest", "Live DJ", "2025-12-05", "20:00", "Campus Hall"));
        repository.addEvent(new Event("Workshop", "AI hands-on", "2025-12-10", "14:00", "ECERF"));
=======
        repository.addEvent(new Event(
                "Music Fest", "Live DJ",
                "2025-12-05", "20:00",
                "Campus Hall", "Entertainment"
        ));

        repository.addEvent(new Event(
                "Workshop", "AI hands-on",
                "2025-12-10", "14:00",
                "ECERF", "Tech"
        ));
>>>>>>> origin/Pavan

        assertEquals(2, repository.getEvents().size());
    }

    @Test
    public void testEventsPersistInMemory() {
<<<<<<< HEAD
        repository.addEvent(new Event("Test Event", "Description", "2025-12-20", "10:00", "SUB"));
        EventRepository repoAgain = EventRepository.getInstance(); // same singleton
=======
        repository.addEvent(new Event(
                "Test Event", "Description",
                "2025-12-20", "10:00",
                "SUB", "General"
        ));

        EventRepository repoAgain = EventRepository.getInstance();

>>>>>>> origin/Pavan
        assertFalse(repoAgain.getEvents().isEmpty());
    }
}
