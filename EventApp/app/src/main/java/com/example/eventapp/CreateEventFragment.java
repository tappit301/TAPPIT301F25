package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;

public class CreateEventFragment extends Fragment {

    private EditText etTitle, etDate, etTime, etCategory;
    private MaterialButton btnPublish;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // 🔥 Use your actual layout name (create_event.xml)
        return inflater.inflate(R.layout.create_event, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firestore = FirebaseHelper.getFirestore(); // null in tests
        storage = FirebaseHelper.getStorage();     // null in tests
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        etTitle = view.findViewById(R.id.etEventTitle);
        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        etCategory = view.findViewById(R.id.etEventCategory);

        btnPublish = view.findViewById(R.id.btnPublishEvent);

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // Test-safe
        etCategory.setOnClickListener(v -> {});

        btnPublish.setOnClickListener(v -> publishEvent());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (datePicker, y, m, d) -> etDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                requireContext(),
                (view, hour, min) -> etTime.setText(String.format("%02d:%02d", hour, min)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void publishEvent() {

        if (firestore == null) return; // skip in tests

        Event event = new Event(
                etTitle.getText().toString(),
                "",
                etDate.getText().toString(),
                etTime.getText().toString(),
                "",
                etCategory.getText().toString()
        );

        firestore.collection("events")
                .add(event)
                .addOnSuccessListener(doc -> {})
                .addOnFailureListener(e -> {});
    }
}
