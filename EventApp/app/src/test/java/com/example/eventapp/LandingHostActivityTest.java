package com.example.eventapp;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class LandingHostActivityTest {

    @Test
    public void activityLaunchesSuccessfully() {
        assertNotNull(Robolectric.buildActivity(LandingHostActivity.class)
                .setup().get());
    }

    @Test
    public void toolbarExists() {
        LandingHostActivity activity = Robolectric.buildActivity(LandingHostActivity.class)
                .setup().get();
        assertNotNull(activity.findViewById(R.id.topAppBar));
    }

    @Test
    public void navHostFragmentLoads() {
        LandingHostActivity activity = Robolectric.buildActivity(LandingHostActivity.class)
                .setup().get();

        FragmentManager fm = activity.getSupportFragmentManager();
        assertNotNull(fm.findFragmentById(R.id.nav_host_fragment));
    }

    @Test
    public void navControllerAvailable() {
        LandingHostActivity activity = Robolectric.buildActivity(LandingHostActivity.class)
                .setup().get();

        NavHostFragment host = (NavHostFragment)
                activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        assertNotNull(host);
        assertNotNull(host.getNavController());
    }
}
