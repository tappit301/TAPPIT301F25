package com.example.eventapp;

import static org.junit.Assert.*;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;

public class ExploreEventsFragmentTest {

    private FragmentScenario<ExploreEventsFragment> launch() {
        return FragmentScenario.launchInContainer(
                ExploreEventsFragment.class,
                null,
                R.style.Theme_EventApp,
                new androidx.fragment.app.FragmentFactory()   // ← FIX
        );
    }

    @Test
    public void fragmentLaunches() {
        launch().onFragment(f -> assertNotNull(f.getView()));
    }

    @Test
    public void backButtonExists() {
        launch().onFragment(f ->
                assertNotNull(f.requireView().findViewById(R.id.btnBackExplore))
        );
    }

    @Test
    public void categoriesExist() {
        launch().onFragment(f -> {
            assertNotNull(f.requireView().findViewById(R.id.catEntertainment));
            assertNotNull(f.requireView().findViewById(R.id.catSports));
            assertNotNull(f.requireView().findViewById(R.id.catTechnology));
            assertNotNull(f.requireView().findViewById(R.id.catHealth));
            assertNotNull(f.requireView().findViewById(R.id.catOthers));
        });
    }
}
