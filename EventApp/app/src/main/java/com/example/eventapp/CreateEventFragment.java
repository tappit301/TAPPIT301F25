package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private EditText etEventTitle, etEventDescription, etEventDate, etEventTime,
            etEventLocation, etEventCapacity, etEventCategory;

    private ImageView ivEventImage;
    private MaterialButton btnPublishEvent, btnCancel;

    FirebaseAuth auth;
    FirebaseFirestore db;

    public CreateEventFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        db   = FirebaseHelper.getFirestore();

        ivEventImage     = view.findViewById(R.id.ivEventImage);
        etEventTitle     = view.findViewById(R.id.etEventTitle);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        etEventDate      = view.findViewById(R.id.etEventDate);
        etEventTime      = view.findViewById(R.id.etEventTime);
        etEventLocation  = view.findViewById(R.id.etEventLocation);
        etEventCapacity  = view.findViewById(R.id.etEventCapacity);
        etEventCategory  = view.findViewById(R.id.etEventCategory);
        btnPublishEvent  = view.findViewById(R.id.btnPublishEvent);
        btnCancel        = view.findViewById(R.id.btnCancel);

        etEventDate.setOnClickListener(v -> showDatePicker());
        etEventTime.setOnClickListener(v -> showTimePicker());

        btnPublishEvent.setOnClickListener(v -> saveEvent(v));
        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(getContext(),
                (view, year, month, day) ->
                        etEventDate.setText(day + "/" + (month + 1) + "/" + year),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();

        new TimePickerDialog(getContext(),
                (view, hour, minute) ->
                        etEventTime.setText(String.format("%02d:%02d", hour, minute)),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveEvent(View view) {

        String title = etEventTitle.getText().toString().trim();
        String desc  = etEventDescription.getText().toString().trim();
        String date  = etEventDate.getText().toString().trim();
        String time  = etEventTime.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        String capacity = etEventCapacity.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date)
                || TextUtils.isEmpty(time) || TextUtils.isEmpty(location)) {
            Toast.makeText(getContext(),
                    "Please fill all required fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("time", time);
        event.put("location", location);
        event.put("category", category);
        event.put("capacity", capacity);
        event.put("organizerId", uid);
        event.put("createdAt", com.google.firebase.Timestamp.now());

        db.collection("events")
                .add(event)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(getContext(), "Event created", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).navigateUp();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
}
