package com.example.eventapp;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.example.eventapp.Attendee;
import com.example.eventapp.AttendeeAdapter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AttendeeAdapterTest {

    @Test
    public void testItemCountMatchesListSize() {
        List<Attendee> attendees = Arrays.asList(
                new Attendee("John", "john@mail.com", "Checked-in"),
                new Attendee("Jane", "jane@mail.com", "Pending")
        );

        AttendeeAdapter adapter = new AttendeeAdapter(attendees);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testOnBindViewHolderPopulatesViews() {
        List<Attendee> attendees = Arrays.asList(
                new Attendee("John", "john@mail.com", "Checked-in")
        );

        AttendeeAdapter adapter = new AttendeeAdapter(attendees);

        View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
                .inflate(R.layout.item_attendee, null, false);

        AttendeeAdapter.AttendeeViewHolder holder =
                new AttendeeAdapter.AttendeeViewHolder(view);

        adapter.onBindViewHolder(holder, 0);

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        TextView tvStatus = view.findViewById(R.id.tvStatus);

        assertEquals("John", tvName.getText().toString());
        assertEquals("john@mail.com", tvEmail.getText().toString());
        assertEquals("Status: Checked-in", tvStatus.getText().toString());
    }
}
