package com.example.eventapp;

import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestApp.class)
public class CreateEventFragmentTest {

    private CreateEventFragment launchFragment() {
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                .setup()
                .get();

        CreateEventFragment fragment = new CreateEventFragment();

        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(fragment, null);
        ft.commitNow();

        return fragment;
    }

    @Test
    public void fragmentLaunchesSuccessfully() {
        CreateEventFragment fragment = launchFragment();
        assertNotNull(fragment.getView());
    }

    @Test
    public void categoryField_isClickable() {
        CreateEventFragment fragment = launchFragment();
        EditText category = fragment.requireView().findViewById(R.id.etEventCategory);
        assertNotNull(category);
        category.performClick();
    }

    @Test
    public void dateField_isClickable() {
        CreateEventFragment fragment = launchFragment();
        EditText date = fragment.requireView().findViewById(R.id.etEventDate);
        assertNotNull(date);
        date.performClick();
    }

    @Test
    public void publishButton_doesNotCrashWithEmptyFields() {
        CreateEventFragment fragment = launchFragment();

        EditText title = fragment.requireView().findViewById(R.id.etEventTitle);
        EditText date = fragment.requireView().findViewById(R.id.etEventDate);
        EditText time = fragment.requireView().findViewById(R.id.etEventTime);
        EditText category = fragment.requireView().findViewById(R.id.etEventCategory);
        MaterialButton publish = fragment.requireView().findViewById(R.id.btnPublishEvent);

        title.setText("");
        date.setText("");
        time.setText("");
        category.setText("");

        publish.performClick();
    }
}
