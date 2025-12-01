package com.example.eventapp;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class LauncherActivityTest {

    private MockedStatic<FirebaseAuth> firebaseAuthMockStatic;
    private FirebaseAuth mockAuth;

    @Before
    public void setup() {
        firebaseAuthMockStatic = Mockito.mockStatic(FirebaseAuth.class);
        mockAuth = Mockito.mock(FirebaseAuth.class);
        firebaseAuthMockStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
    }

    @After
    public void teardown() {
        firebaseAuthMockStatic.close();
    }

    @Test
    public void whenUserLoggedIn_opensLandingHostActivity() {
        FirebaseUser mockUser = Mockito.mock(FirebaseUser.class);
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        LauncherActivity activity = Robolectric.buildActivity(LauncherActivity.class)
                .setup()
                .get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent next = shadow.getNextStartedActivity();

        assertNotNull(next);
        assertEquals(LandingHostActivity.class.getName(), next.getComponent().getClassName());
    }

    @Test
    public void whenUserNotLoggedIn_opensLoginActivity() {
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(null);

        LauncherActivity activity = Robolectric.buildActivity(LauncherActivity.class)
                .setup()
                .get();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent next = shadow.getNextStartedActivity();

        assertNotNull(next);
        assertEquals(LoginActivity.class.getName(), next.getComponent().getClassName());
    }
}
