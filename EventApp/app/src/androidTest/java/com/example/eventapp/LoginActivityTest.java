package com.example.eventapp;

import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Very simple sanity test:
 * - Launches LoginActivity
 * - Verifies it starts without crashing.
 */
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginActivity_launchesSuccessfully() {
        assertNotNull(activityRule);
    }
}