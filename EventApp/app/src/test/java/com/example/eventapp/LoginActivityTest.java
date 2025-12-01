package com.example.eventapp;

import android.content.Intent;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.*;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class LoginActivityTest {

    private MockedStatic<FirebaseHelper> firebaseHelperMock;
    private FirebaseAuth mockAuth;
    private LoginActivity activity;

    private EditText email;
    private EditText pass;
    private Button loginButton;
    private Button signUpButton;

    @Before
    public void setup() {
        firebaseHelperMock = Mockito.mockStatic(FirebaseHelper.class);

        mockAuth = Mockito.mock(FirebaseAuth.class);
        firebaseHelperMock.when(FirebaseHelper::getAuth).thenReturn(mockAuth);

        activity = Robolectric.buildActivity(LoginActivity.class)
                .setup()
                .get();
        activity.setTheme(R.style.Theme_EventApp);

        email = activity.findViewById(R.id.editTextEmail);
        pass = activity.findViewById(R.id.editTextPassword);
        loginButton = activity.findViewById(R.id.btnSignIn);
        signUpButton = activity.findViewById(R.id.btnSignUpToggle);
    }

    @After
    public void teardown() {
        firebaseHelperMock.close();
    }

    @Test
    public void loginFailure_showsToast() {
        email.setText("a@b.com");
        pass.setText("1234");

        Task<AuthResult> failureTask = Tasks.forException(new Exception("Invalid"));
        Mockito.when(mockAuth.signInWithEmailAndPassword("a@b.com", "1234"))
                .thenReturn(failureTask);

        loginButton.performClick();

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        String toast = ShadowToast.getTextOfLatestToast();
        assertNotNull(toast);
        assertTrue(toast.contains("Invalid"));
    }

    @Test
    public void loginSuccess_opensLandingHostActivity() {
        FirebaseUser mockUser = Mockito.mock(FirebaseUser.class);
        Mockito.when(mockUser.getEmail()).thenReturn("a@b.com");
        Mockito.when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        email.setText("a@b.com");
        pass.setText("1234");

        Task<AuthResult> successTask = Tasks.forResult(Mockito.mock(AuthResult.class));
        Mockito.when(mockAuth.signInWithEmailAndPassword("a@b.com", "1234"))
                .thenReturn(successTask);

        loginButton.performClick();

        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(LandingHostActivity.class.getName(), next.getComponent().getClassName());
    }

    @Test
    public void clickingSignUp_opensSignUpActivity() {
        signUpButton.performClick();

        Intent next = Shadows.shadowOf(activity).getNextStartedActivity();
        assertNotNull(next);
        assertEquals(SignUpActivity.class.getName(), next.getComponent().getClassName());
    }
}
