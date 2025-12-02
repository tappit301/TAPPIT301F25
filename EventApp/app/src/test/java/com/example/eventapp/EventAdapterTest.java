package com.example.eventapp;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EventAdapterTest {

    private EventAdapter adapter;
    private List<Event> events;

    @Before
    public void setUp() {
        events = new ArrayList<>();
        events.add(new Event(
                "Hackathon", "24h code event", "2025-12-02",
                "09:00", "UofA Lab", "Tech"
        ));
        events.add(new Event(
                "Music Night", "Live DJ", "2025-12-05",
                "20:00", "Campus Hall", "Entertainment"
        ));

        // ✅ FIX: Pass required second argument
        adapter = new EventAdapter(events, 0);
    }

    @Test
    public void itemCount_matchesListSize() {
        assertEquals(events.size(), adapter.getItemCount());
    }

    @Test
    public void itemCount_updatesWhenListChanges() {
        int start = adapter.getItemCount();

        events.add(new Event(
                "Workshop", "AI 101", "2025-12-10",
                "14:00", "ECERF", "Tech"
        ));

        assertEquals(start + 1, adapter.getItemCount());
    }
}
