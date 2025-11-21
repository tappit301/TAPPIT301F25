package com.example.eventapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment allowing organizers to create new events and upload an event poster.
 * After publishing, the event is stored in Firestore and the image is uploaded
 * to Firebase Storage.
 */
public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    /** Date input */
    private EditText etDate;

    /** Time input */
    private EditText etTime;

    /** Firebase */
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    /** Image UI components */
    private MaterialCardView cardEventImage;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;

    /** Selected cover image URI */
    private Uri selectedImageUri = null;

    /** ActivityResult launcher for gallery picking */
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_event, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase
        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();
        storage = FirebaseStorage.getInstance();

        // Image Picker Launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            placeholderLayout.setVisibility(View.GONE);
                            ivEventImage.setVisibility(View.VISIBLE);
                            ivEventImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        MaterialButton btnPublish = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Poster views
        cardEventImage = view.findViewById(R.id.cardEventImage);
        ivEventImage = view.findViewById(R.id.ivEventImage);
        placeholderLayout = view.findViewById(R.id.layoutAddCoverPlaceholder);

        // Date & Time pickers
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // Image picker
        cardEventImage.setOnClickListener(v -> openImagePicker());

        // Buttons
        btnPublish.setOnClickListener(v -> publishEvent(view));
        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    /** Opens the gallery to pick an image */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /** Publish button handler */
    private void publishEvent(View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String title = ((EditText) view.findViewById(R.id.etEventTitle)).getText().toString().trim();
        String desc = ((EditText) view.findViewById(R.id.etEventDescription)).getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = ((EditText) view.findViewById(R.id.etEventLocation)).getText().toString().trim();
        String category = ((EditText) view.findViewById(R.id.etEventCategory)).getText().toString().trim();
        String maxCapStr = ((EditText) view.findViewById(R.id.etEventCapacity)).getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxCapacity = -1;
        if (!maxCapStr.isEmpty()) {
            try {
                maxCapacity = Integer.parseInt(maxCapStr);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid capacity number.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create doc reference so we know ID BEFORE upload
        DocumentReference eventRef = firestore.collection("events").document();
        String eventId = eventRef.getId();

        if (selectedImageUri != null) {
            uploadImageAndSave(eventRef, eventId, selectedImageUri, title, desc, date, time, location, category, maxCapacity, user, view);
        } else {
            saveEvent(eventRef, null, title, desc, date, time, location, category, maxCapacity, user, view);
        }
    }

    /** Uploads image then saves event */
    private void uploadImageAndSave(
            DocumentReference eventRef,
            String eventId,
            Uri imageUri,
            String title,
            String desc,
            String date,
            String time,
            String location,
            String category,
            int maxCapacity,
            FirebaseUser user,
            View view
    ) {
        StorageReference imageRef = storage.getReference()
                .child("event_covers/" + eventId + ".jpg");

        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUrl -> {
                    saveEvent(eventRef, downloadUrl.toString(), title, desc, date, time, location, category, maxCapacity, user, view);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** Saves event to Firestore with or without image */
    private void saveEvent(
            DocumentReference eventRef,
            @Nullable String imageUrl,
            String title,
            String desc,
            String date,
            String time,
            String location,
            String category,
            int maxCapacity,
            FirebaseUser user,
            View view
    ) {
        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("time", time);
        event.put("location", location);
        event.put("category", category);
        event.put("maxCapacity", maxCapacity);
        event.put("createdAt", Timestamp.now());
        event.put("organizerId", user.getUid());
        event.put("organizerEmail", user.getEmail());

        if (imageUrl != null) {
            event.put("imageUrl", imageUrl);
        }

        eventRef.set(event)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Event published!", Toast.LENGTH_SHORT).show();

                    // Generate QR payload
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
                    Toast.makeText(getContext(), "Failed to publish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Shows date picker */
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) ->
                        etDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Shows time picker */
    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hour, min) ->
                        etTime.setText(String.format("%02d:%02d", hour, min)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true).show();
    }
}
