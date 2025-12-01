package com.example.eventapp;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.*;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.stubbing.Answer;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class SignUpActivityTest {

    private MockedStatic<FirebaseHelper> firebaseMock;
    private FirebaseAuth mockAuth;
    private FirebaseFirestore mockFs;

    @Before
    public void setup() {
        firebaseMock = Mockito.mockStatic(FirebaseHelper.class);

        mockAuth = Mockito.mock(FirebaseAuth.class);
        mockFs = Mockito.mock(FirebaseFirestore.class);

        firebaseMock.when(FirebaseHelper::getAuth).thenReturn(mockAuth);
        firebaseMock.when(FirebaseHelper::getFirestore).thenReturn(mockFs);
    }

    @After
    public void teardown() {
        firebaseMock.close();
    }

    @Test
    public void emptyFields_showsToast() {
        SignUpActivity activity =
                Robolectric.buildActivity(SignUpActivity.class).setup().get();

        Button btn = activity.findViewById(R.id.btnSignUp);
        btn.performClick();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void passwordMismatch_showsToast() {
        SignUpActivity activity =
                Robolectric.buildActivity(SignUpActivity.class).setup().get();

        activity.<EditText>findViewById(R.id.editTextFullName).setText("A");
        activity.<EditText>findViewById(R.id.editTextSignUpEmail).setText("a@b.com");
        activity.<EditText>findViewById(R.id.editTextSignUpPassword).setText("123456");
        activity.<EditText>findViewById(R.id.editTextConfirmPassword).setText("654321");

        activity.findViewById(R.id.btnSignUp).performClick();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void shortPassword_showsToast() {
        SignUpActivity activity =
                Robolectric.buildActivity(SignUpActivity.class).setup().get();

        activity.<EditText>findViewById(R.id.editTextFullName).setText("A");
        activity.<EditText>findViewById(R.id.editTextSignUpEmail).setText("a@b.com");
        activity.<EditText>findViewById(R.id.editTextSignUpPassword).setText("123");
        activity.<EditText>findViewById(R.id.editTextConfirmPassword).setText("123");

        activity.findViewById(R.id.btnSignUp).performClick();

        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void toggleToLogin() {
        SignUpActivity activity =
                Robolectric.buildActivity(SignUpActivity.class).setup().get();

        activity.findViewById(R.id.btnSignInToggle).performClick();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertEquals(LoginActivity.class.getName(),
                next.getComponent().getClassName());
    }
}
