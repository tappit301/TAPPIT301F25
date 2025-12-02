package com.example.eventapp;

import static org.junit.Assert.assertNotNull;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsFragmentTest {

    @Test
    public void fragmentLaunchesSuccessfully() {
        Bundle args = new Bundle();
        args.putString("eventId", "sampleEventId");
        args.putString("organizerId", "sampleOrganizerId");
        args.putString("organizerEmail", "organizer@example.com");
        args.putString("title", "Sample Event");
        args.putString("date", "01/12/2025");
        args.putString("time", "18:00");
        args.putString("location", "Edmonton");
        args.putString("desc", "Sample description");
        args.putString("imageUrl", "");

        FragmentScenario<EventDetailsFragment> scenario =
                FragmentScenario.launchInContainer(
                        EventDetailsFragment.class,
                        args,
                        R.style.Theme_EventApp
                );

        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getView());
        });
    }
}