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
import androidx.navigation.fragment.NavHostFragment;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private EditText etTitle, etDesc, etDate, etTime, etLocation, etCategory;
    private MaterialButton btnPublish;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle sis) {
        return inflater.inflate(R.layout.create_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle sis) {

        firestore = FirebaseHelper.getFirestore();
        auth = FirebaseHelper.getAuth();

        etTitle = view.findViewById(R.id.etEventTitle);
        etDesc = view.findViewById(R.id.etEventDescription);
        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        etLocation = view.findViewById(R.id.etEventLocation);
        etCategory = view.findViewById(R.id.etEventCategory);

        btnPublish = view.findViewById(R.id.btnPublishEvent);

        etDate.setOnClickListener(v -> pickDate());
        etTime.setOnClickListener(v -> pickTime());

        btnPublish.setOnClickListener(v -> publishEvent());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (dp, y, m, d) ->
                etDate.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void pickTime() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (tp, h, m) ->
                etTime.setText(String.format("%02d:%02d", h, m)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE), true).show();
    }

    private void publishEvent() {
        if (firestore == null || auth.getCurrentUser() == null) return;

        long timestamp = convertToTimestamp(
                etDate.getText().toString(),
                etTime.getText().toString()
        );

        Map<String, Object> data = new HashMap<>();
        data.put("title", etTitle.getText().toString());
        data.put("description", etDesc.getText().toString());
        data.put("date", etDate.getText().toString());
        data.put("time", etTime.getText().toString());
        data.put("location", etLocation.getText().toString());
        data.put("category", etCategory.getText().toString());
        data.put("organizerId", auth.getCurrentUser().getUid());
        data.put("organizerEmail", auth.getCurrentUser().getEmail());
        data.put("timestamp", timestamp);

        firestore.collection("events")
                .add(data)
                .addOnSuccessListener(doc ->
                        NavHostFragment.findNavController(this).popBackStack())
                .addOnFailureListener(e -> {});
    }

    private long convertToTimestamp(String date, String time) {
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);
            return sdf.parse(date + " " + time).getTime();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}
