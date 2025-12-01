package com.example.eventapp;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SignInActivityTest {

    @Test
    public void emptyEmail_showsToast() {
        SignInActivity activity = Robolectric.buildActivity(SignInActivity.class)
                .setup().get();

        EditText email = activity.findViewById(R.id.editTextEmail);
        EditText pass = activity.findViewById(R.id.editTextPassword);

        pass.setText("123456");

        Button btn = activity.findViewById(R.id.btnSignIn);
        btn.performClick();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void emptyPassword_showsToast() {
        SignInActivity activity = Robolectric.buildActivity(SignInActivity.class)
                .setup().get();

        EditText email = activity.findViewById(R.id.editTextEmail);
        email.setText("test@example.com");

        Button btn = activity.findViewById(R.id.btnSignIn);
        btn.performClick();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void successfulLogin_opensLanding() {
        SignInActivity activity = Robolectric.buildActivity(SignInActivity.class)
                .setup().get();

        EditText email = activity.findViewById(R.id.editTextEmail);
        EditText pass = activity.findViewById(R.id.editTextPassword);

        email.setText("test@example.com");
        pass.setText("123456");

        Button btn = activity.findViewById(R.id.btnSignIn);
        btn.performClick();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(LandingHostActivity.class.getName(),
                next.getComponent().getClassName());
    }

    @Test
    public void toggleMovesToSignUp() {
        SignInActivity activity = Robolectric.buildActivity(SignInActivity.class)
                .setup().get();

        Button btn = activity.findViewById(R.id.btnSignUpToggle);
        btn.performClick();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(SignUpActivity.class.getName(),
                next.getComponent().getClassName());
    }
}
