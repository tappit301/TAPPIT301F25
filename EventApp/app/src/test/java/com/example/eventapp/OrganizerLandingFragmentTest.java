package com.example.eventapp;

import static org.junit.Assert.*;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;

public class OrganizerLandingFragmentTest {

    private FragmentScenario<OrganizerLandingFragment> launch() {
        return FragmentScenario.launchInContainer(
                OrganizerLandingFragment.class,
                null,
                R.style.Theme_EventApp,
                new androidx.fragment.app.FragmentFactory()   // ← FIX
        );
    }

    @Test
    public void fragmentLaunchesSuccessfully() {
        launch().onFragment(f -> assertNotNull(f.getView()));
    }

    @Test
    public void exploreButtonExists() {
        launch().onFragment(f ->
                assertNotNull(f.requireView().findViewById(R.id.btnExplore))
        );
    }

    @Test
    public void createTopButtonExists() {
        launch().onFragment(f ->
                assertNotNull(f.requireView().findViewById(R.id.btnCreateEventTop))
        );
    }

    @Test
    public void profileButtonExists() {
        launch().onFragment(f ->
                assertNotNull(f.requireView().findViewById(R.id.btnProfile))
        );
    }

    @Test
    public void floatingAddButtonExists() {
        launch().onFragment(f ->
                assertNotNull(f.requireView().findViewById(R.id.btnAddEvent))
        );
    }
}
