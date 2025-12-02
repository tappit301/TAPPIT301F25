package com.example.eventapp;

import static org.junit.Assert.assertEquals;

import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.google.firebase.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AttendeeAdapterTest {

    @Test
    public void testOnBindViewHolderPopulatesViews() {
        // Test data
        Attendee attendee = new Attendee("123", "email@test.com", Timestamp.now());
        List<Attendee> list = new ArrayList<>();
        list.add(attendee);

        AttendeeAdapter adapter = new AttendeeAdapter(list);

        // Real Activity + parent ViewGroup
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                .setup()
                .get();

        FrameLayout parent = new FrameLayout(activity);

        // Create ViewHolder
        AttendeeAdapter.AttendeeViewHolder vh =
                adapter.onCreateViewHolder(parent, 0);

        // Bind
        adapter.onBindViewHolder(vh, 0);

        // Assertions
        assertEquals("email@test.com", vh.tvEmail.getText().toString());
        assertEquals("email@test.com", vh.tvName.getText().toString());
        assertEquals("Status: waiting", vh.tvStatus.getText().toString());
    }
}
