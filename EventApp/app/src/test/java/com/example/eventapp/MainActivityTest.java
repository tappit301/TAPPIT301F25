package com.example.eventapp;

import android.view.View;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class MainActivityTest {

    @Test
    public void activityLaunchesSuccessfully() {
        assertNotNull(Robolectric.buildActivity(MainActivity.class)
                .setup().get());
    }

    @Test
    public void mainViewExists() {
        MainActivity activity = Robolectric.buildActivity(MainActivity.class)
                .setup().get();
        assertNotNull(activity.findViewById(R.id.main));
    }
}
