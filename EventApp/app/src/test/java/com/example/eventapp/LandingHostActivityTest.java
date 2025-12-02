package com.example.eventapp;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class LandingHostActivityTest {

    @Before
    public void setup() {
        // Required: prevents FirebaseAuth redirect
        TestMode.IS_TEST = true;
    }

    @Test
    public void activityLaunchesSuccessfully() {
        LandingHostActivity activity =
                Robolectric.buildActivity(LandingHostActivity.class)
                        .setup()
                        .get();

        assertNotNull(activity);
    }

    @Test
    public void toolbarExists() {
        LandingHostActivity activity =
                Robolectric.buildActivity(LandingHostActivity.class)
                        .setup()
                        .get();

        Toolbar toolbar = activity.findViewById(R.id.topAppBar);
        assertNotNull("Toolbar should not be null", toolbar);
    }

    @Test
    public void navHostFragmentLoads() {
        LandingHostActivity activity =
                Robolectric.buildActivity(LandingHostActivity.class)
                        .setup()
                        .get();

        FragmentManager fm = activity.getSupportFragmentManager();
        NavHostFragment host =
                (NavHostFragment) fm.findFragmentById(R.id.nav_host_fragment);

        assertNotNull("NavHostFragment should be present", host);
    }

    @Test
    public void navControllerAvailable() {
        LandingHostActivity activity =
                Robolectric.buildActivity(LandingHostActivity.class)
                        .setup()
                        .get();

        NavHostFragment host =
                (NavHostFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        assertNotNull("NavHostFragment should exist", host);
        assertNotNull("NavController should not be null", host.getNavController());
    }
}
