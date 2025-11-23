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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    // Edit Mode Variables
    private String eventId = null;
    private boolean isEditMode = false;

    // Views
    private EditText etDate, etTime;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;

    // Category input (existing view, just wired up now)
    private EditText etCategory; // uses R.id.etEventCategory

    // Firebase
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

        // Init Views
        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        btnPublish = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView cardEventImage = view.findViewById(R.id.cardEventImage);
        ivEventImage = view.findViewById(R.id.ivEventImage);
        placeholderLayout = view.findViewById(R.id.layoutAddCoverPlaceholder);

        // ✅ Category field (existing EditText in XML)
        etCategory = view.findViewById(R.id.etEventCategory);

        // Make category not freely typed: user picks from list
        etCategory.setFocusable(false);
        etCategory.setClickable(true);
        etCategory.setOnClickListener(v -> showCategoryPicker());

        // Read Arguments (edit mode)
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails(); // Fetch from Firestore
        }

        // Inputs
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        cardEventImage.setOnClickListener(v -> openImagePicker());

        // Buttons
        btnPublish.setOnClickListener(v -> {
            if (isEditMode) {
                updateEvent(view);
            } else {
                publishEvent(view);
            }
        });

        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    /** Shows the category picker dialog */
    private void showCategoryPicker() {
        // Fixed options
        String[] categories = new String[] {
                "Technology",
                "Sports",
                "Entertainment",
                "Health"
        };

        // Pre-select current value if it matches one of them
        int checkedItem = -1;
        String current = etCategory.getText().toString().trim();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(current)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select category")
                .setSingleChoiceItems(categories, checkedItem, (dialog, which) -> {
                    etCategory.setText(categories[which]);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Fetch Event from Firestore */
    private void loadEventDetails() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

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

    /** Update Existing Event */
    private void updateEvent(View view) {
        if (eventId == null) return;

        if (selectedImageUri != null) {
            StorageReference imageRef = storage.getReference()
                    .child("event_covers/" + eventId + ".jpg");

            imageRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) throw task.getException();
                        return imageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
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

    /** Create New Event (your original logic goes here) */
    private void publishEvent(View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getView() == null) return;

        String title = ((EditText) getView().findViewById(R.id.etEventTitle))
                .getText().toString().trim();
        String desc = ((EditText) getView().findViewById(R.id.etEventDescription))
                .getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = ((EditText) getView().findViewById(R.id.etEventLocation))
                .getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: require category
        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Please choose a category.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("time", time);
        event.put("location", location);
        event.put("category", category);
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

    /** Pickers */
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