package com.example.eventapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class OrganizerLandingFragmentTest {

    private FragmentScenario<OrganizerLandingFragment> launchFragment() {

        FragmentScenario<OrganizerLandingFragment> scenario =
                FragmentScenario.launchInContainer(
                        OrganizerLandingFragment.class,
                        null,
                        R.style.Theme_EventApp,
                        (FragmentFactory) null
                );

        scenario.onFragment(fragment -> {
            TestNavHostController nav = new TestNavHostController(fragment.requireContext());
            nav.setGraph(R.navigation.organizer_nav_graph);
            nav.setCurrentDestination(R.id.organizerLandingFragment);
            Navigation.setViewNavController(fragment.requireView(), nav);
        });

        return scenario;
    }

    @Test
    public void fragmentLaunchesSuccessfully() {
        assertNotNull(launchFragment());
        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test
    public void exploreButton_navigatesToExplore() {
        FragmentScenario<OrganizerLandingFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            TestNavHostController nav =
                    (TestNavHostController) Navigation.findNavController(fragment.requireView());

            fragment.requireView().findViewById(R.id.btnExplore).performClick();

            assertEquals(R.id.exploreEventsFragment, nav.getCurrentDestination().getId());
        });

        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test
    public void topCreateButton_navigatesToCreateEvent() {
        FragmentScenario<OrganizerLandingFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            TestNavHostController nav =
                    (TestNavHostController) Navigation.findNavController(fragment.requireView());

            fragment.requireView().findViewById(R.id.btnCreateEventTop).performClick();

            assertEquals(R.id.createEventFragment, nav.getCurrentDestination().getId());
        });

        shadowOf(Looper.getMainLooper()).idle();
    }

    @Test
    public void fabCreateButton_navigatesToCreateEvent() {
        FragmentScenario<OrganizerLandingFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            TestNavHostController nav =
                    (TestNavHostController) Navigation.findNavController(fragment.requireView());

            fragment.requireView().findViewById(R.id.btnAddEvent).performClick();

            assertEquals(R.id.createEventFragment, nav.getCurrentDestination().getId());
        });

        shadowOf(Looper.getMainLooper()).idle();
    }
}
