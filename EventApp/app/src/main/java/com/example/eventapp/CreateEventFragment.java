package com.example.eventapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

    private String eventId = null;
    private boolean isEditMode = false;

    private EditText etDate, etTime, etCategory, etMaxAttendees;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;

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

        etDate = view.findViewById(R.id.etEventDate);
        etTime = view.findViewById(R.id.etEventTime);
        etCategory = view.findViewById(R.id.etEventCategory);
        etMaxAttendees = view.findViewById(R.id.etEventCapacity);

        btnPublish = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView cardEventImage = view.findViewById(R.id.cardEventImage);
        ivEventImage = view.findViewById(R.id.ivEventImage);
        placeholderLayout = view.findViewById(R.id.layoutAddCoverPlaceholder);

        etCategory.setFocusable(false);
        etCategory.setClickable(true);
        etCategory.setOnClickListener(v -> showCategoryPicker());

        cardEventImage.setOnClickListener(v -> openImagePicker());
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        btnPublish.setOnClickListener(v -> publishEvent(v));
        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails();
        }
    }

    private void showCategoryPicker() {
        String[] categories = new String[]{
                "Technology",
                "Sports",
                "Entertainment",
                "Health"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select category")
                .setItems(categories, (dialog, which) -> etCategory.setText(categories[which]))
                .show();
    }

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
                    ((EditText) getView().findViewById(R.id.etEventLocation))
                            .setText(doc.getString("location"));

                    etDate.setText(doc.getString("date"));
                    etTime.setText(doc.getString("time"));
                    etCategory.setText(doc.getString("category"));

                    Long capacity = doc.getLong("capacity");
                    if (capacity != null) {
                        etMaxAttendees.setText(String.valueOf(capacity));
                    }

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        placeholderLayout.setVisibility(View.GONE);
                        ivEventImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(imageUrl).into(ivEventImage);
                    }
                });
    }

    /* --------------------- VASU’S UPDATE EVENT FEATURE --------------------- */

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

    /* ---------------------------------------------------------------------- */

    private void publishEvent(View view) {
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
        String maxStr = etMaxAttendees.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Please choose a category.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maxStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter maximum attendees.", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxAttendees = Integer.parseInt(maxStr);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            doPublishEvent(view, title, desc, date, time, location,
                    category, user.getUid(), user.getEmail(), maxAttendees);
        }
    }

    private void doPublishEvent(View view,
                                String title,
                                String desc,
                                String date,
                                String time,
                                String location,
                                String category,
                                String organizerId,
                                String organizerEmail,
                                int maxAttendees) {

        Map<String, Object> event = new HashMap<>();
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("time", time);
        event.put("location", location);
        event.put("category", category);
        event.put("createdAt", Timestamp.now());
        event.put("organizerId", organizerId);
        event.put("organizerEmail", organizerEmail);
        event.put("capacity", maxAttendees);

        firestore.collection("events")
                .add(event)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Event Published!", Toast.LENGTH_SHORT).show();

                    Bundle bundle = new Bundle();
                    bundle.putString("qrData", doc.getId());
                    Navigation.findNavController(view).navigate(R.id.qrCodeFragment, bundle);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

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
