package com.example.eventapp;

import static org.junit.Assert.assertNotNull;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ManageEventsFragmentTest {

    @Test
    public void manageEventsFragment_launchesWithArgs() {
        // Arrange
        Bundle args = new Bundle();
        args.putString("eventId", "sampleEventId");

        FragmentScenario<ManageEventsFragment> scenario =
                FragmentScenario.launchInContainer(
                        ManageEventsFragment.class,
                        args,
                        R.style.Theme_EventApp
                );

        scenario.onFragment(fragment -> {
            assertNotNull(fragment);
            assertNotNull(fragment.getView());
        });
    }
}