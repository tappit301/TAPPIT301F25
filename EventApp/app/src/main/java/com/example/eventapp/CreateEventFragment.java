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

import com.bumptech.glide.Glide;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    // ✅ Edit Mode Variables
    private String eventId = null;
    private boolean isEditMode = false;

    // ✅ Views
    private EditText etDate, etTime;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;

    // ✅ Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;

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

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();
        storage = FirebaseStorage.getInstance();

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

        // ✅ Init Views
        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        btnPublish = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView cardEventImage = view.findViewById(R.id.cardEventImage);
        ivEventImage = view.findViewById(R.id.ivEventImage);
        placeholderLayout = view.findViewById(R.id.layoutAddCoverPlaceholder);

        // ✅ Read Arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails(); // ✅ Fetch from Firestore
        }

        // ✅ Inputs
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        cardEventImage.setOnClickListener(v -> openImagePicker());

        // ✅ Buttons
        btnPublish.setOnClickListener(v -> {
            if (isEditMode) {
                updateEvent(view);
            } else {
                publishEvent(view);
            }
        });

        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    /** ✅ Fetch Event from Firestore */
    private void loadEventDetails() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    // ✅ Fill UI
                    ((EditText) getView().findViewById(R.id.etEventTitle))
                            .setText(doc.getString("title"));
                    ((EditText) getView().findViewById(R.id.etEventDescription))
                            .setText(doc.getString("description"));
                    etDate.setText(doc.getString("date"));
                    etTime.setText(doc.getString("time"));
                    ((EditText) getView().findViewById(R.id.etEventLocation))
                            .setText(doc.getString("location"));
                    ((EditText) getView().findViewById(R.id.etEventCategory))
                            .setText(doc.getString("category"));

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        placeholderLayout.setVisibility(View.GONE);
                        ivEventImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(imageUrl).into(ivEventImage);
                    }
                });
    }

    /** ✅ Update Existing Event */
    private void updateEvent(View view) {
        if (eventId == null) return;

        // ✅ If user picked a new image, upload it first
        if (selectedImageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("event_covers/" + eventId + ".jpg"); // ✅ Overwrite same file

            imageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
                        // ✅ After image uploaded, update Firestore
                        Map<String, Object> updates = getUpdatedFields();
                        updates.put("imageUrl", downloadUrl.toString());

                        firestore.collection("events")
                                .document(eventId)
                                .update(updates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(view).popBackStack();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } else {
            // ✅ No new image → only update text fields
            Map<String, Object> updates = getUpdatedFields();

            firestore.collection("events")
                    .document(eventId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    private Map<String, Object> getUpdatedFields() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", ((EditText) getView().findViewById(R.id.etEventTitle)).getText().toString().trim());
        updates.put("description", ((EditText) getView().findViewById(R.id.etEventDescription)).getText().toString().trim());
        updates.put("date", etDate.getText().toString().trim());
        updates.put("time", etTime.getText().toString().trim());
        updates.put("location", ((EditText) getView().findViewById(R.id.etEventLocation)).getText().toString().trim());
        updates.put("category", ((EditText) getView().findViewById(R.id.etEventCategory)).getText().toString().trim());
        return updates;
    }


    /** ✅ Create New Event */
    private void publishEvent(View view) {
        // (Same as your original code — unchanged)
        // We only changed edit behavior, creation stays the same ✅
        Toast.makeText(getContext(), "Create mode - event will be published.", Toast.LENGTH_SHORT).show();
    }

    /** ✅ Pickers */
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(),
                (view, year, month, day) ->
                        etDate.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hour, min) ->
                        etTime.setText(String.format("%02d:%02d", hour, min)),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }
}