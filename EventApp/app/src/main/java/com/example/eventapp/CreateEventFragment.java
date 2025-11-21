package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    private EditText etDate, etTime;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String existingEventId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);

        EditText etTitle = view.findViewById(R.id.etEventTitle);
        EditText etDesc = view.findViewById(R.id.etEventDescription);
        EditText etLocation = view.findViewById(R.id.etEventLocation);

        MaterialButton btnPublish = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {

            existingEventId = args.getString("eventId");

            etTitle.setText(args.getString("title", ""));
            etDesc.setText(args.getString("desc", ""));
            etDate.setText(args.getString("date", ""));
            etTime.setText(args.getString("time", ""));
            etLocation.setText(args.getString("location", ""));

            btnPublish.setText("Save Changes");
        }
        // =====================================================================

        btnPublish.setOnClickListener(v -> {
            if (existingEventId == null) {
                publishEvent(view);
            } else {
                updateEvent(existingEventId, view);
            }
        });

        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    // --------------------- CREATE NEW EVENT --------------------------------
    private void publishEvent(View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = ((EditText) view.findViewById(R.id.etEventTitle)).getText().toString().trim();
        String desc = ((EditText) view.findViewById(R.id.etEventDescription)).getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = ((EditText) view.findViewById(R.id.etEventLocation)).getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("time", time);
        event.put("location", location);
        event.put("createdAt", Timestamp.now());
        event.put("organizerId", user.getUid());
        event.put("organizerEmail", user.getEmail());

        firestore.collection("events")
                .add(event)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Event published successfully!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Event saved by " + user.getEmail());

                    String qrPayload = "Event: " + title +
                            "\nDate: " + date +
                            "\nTime: " + time +
                            "\nLocation: " + location +
                            "\nOrganizer: " + user.getEmail();

                    Bundle bundle = new Bundle();
                    bundle.putString("qrData", qrPayload);
                    Navigation.findNavController(view).navigate(R.id.qrCodeFragment, bundle);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding event", e);
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ----------------------- UPDATE EXISTING EVENT --------------------------
    private void updateEvent(String eventId, View view) {

        String title = ((EditText) view.findViewById(R.id.etEventTitle)).getText().toString().trim();
        String desc = ((EditText) view.findViewById(R.id.etEventDescription)).getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = ((EditText) view.findViewById(R.id.etEventLocation)).getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", desc);
        updates.put("date", date);
        updates.put("time", time);
        updates.put("location", location);

        firestore.collection("events")
                .document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------- DATE & TIME PICKERS ---------------------
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) ->
                        etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) ->
                        etTime.setText(String.format("%02d:%02d", hourOfDay, minute)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true).show();
    }
}
