package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class EventRepositoryTest {

    private EventRepository repository;

    @Before
    public void setup() {
        // Reset singleton before each test to ensure isolation
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
        Event e = new Event("Hackathon", "Coding event", "2025-12-01", "09:00", "UofA");
        repository.addEvent(e);

        List<Event> events = repository.getEvents();
        assertEquals(1, events.size());
        assertEquals("Hackathon", events.get(0).getTitle());
    }

    @Test
    public void testMultipleEventsStored() {
        repository.addEvent(new Event("Music Fest", "Live DJ", "2025-12-05", "20:00", "Campus Hall"));
        repository.addEvent(new Event("Workshop", "AI hands-on", "2025-12-10", "14:00", "ECERF"));

        assertEquals(2, repository.getEvents().size());
    }

    @Test
    public void testEventsPersistInMemory() {
        repository.addEvent(new Event("Test Event", "Description", "2025-12-20", "10:00", "SUB"));
        EventRepository repoAgain = EventRepository.getInstance(); // same singleton
        assertFalse(repoAgain.getEvents().isEmpty());
    }
}
