package com.example.eventapp;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Button;

import androidx.appcompat.widget.Toolbar;

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
@Config(sdk = 34)
public class HomeActivityTest {

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

        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class)
                .setup()
                .get();

        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent nextIntent = shadowActivity.getNextStartedActivity();

        assertNotNull(nextIntent);
        assertEquals(LandingHostActivity.class.getName(),
                nextIntent.getComponent().getClassName());
    }

    @Test
    public void whenUserNotLoggedIn_toolbarIsPresent() {
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(null);

        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class)
                .setup()
                .get();

        Toolbar toolbar = activity.findViewById(R.id.topAppBar);
        assertNotNull(toolbar);
    }

    @Test
    public void clickingGettingStarted_opensLoginActivity() {
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(null);

        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class)
                .setup()
                .get();

        Button btn = activity.findViewById(R.id.btnGettingStarted);
        btn.performClick();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent intent = shadow.getNextStartedActivity();

        assertEquals(LoginActivity.class.getName(),
                intent.getComponent().getClassName());
    }

    @Test
    public void clickingCreateAccount_opensSignupActivity() {
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(null);

        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class)
                .setup()
                .get();

        Button btn = activity.findViewById(R.id.btnCreateAccount);
        btn.performClick();

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent intent = shadow.getNextStartedActivity();

        assertEquals(SignUpActivity.class.getName(),
                intent.getComponent().getClassName());
    }

    @Test
    public void clickingAdminLoginMenu_opensAdminHostActivity() {
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(null);

        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class)
                .setup()
                .get();

        Menu menu = activity.getToolbar().getMenu();
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        activity.onOptionsItemSelected(menu.findItem(R.id.action_admin_login));

        ShadowActivity shadow = Shadows.shadowOf(activity);
        Intent intent = shadow.getNextStartedActivity();

        assertNotNull(intent);
        assertEquals("com.example.eventapp.admin.AdminHostActivity",
                intent.getComponent().getClassName());
    }
}
