package com.example.eventapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    // Edit mode
    private String eventId = null;
    private boolean isEditMode = false;

    // Views
    private EditText etDate, etTime, etCategory, etPrice, etCapacity;
    private EditText etTitle, etDescription, etLocation;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;
    private SwitchMaterial switchRequireGeo;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;
    private String existingImageUrl = null;

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
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init views
        etDate       = view.findViewById(R.id.etEventDate);
        etTime       = view.findViewById(R.id.etEventTime);
        etTitle      = view.findViewById(R.id.etEventTitle);
        etDescription= view.findViewById(R.id.etEventDescription);
        etLocation   = view.findViewById(R.id.etEventLocation);
        etCategory   = view.findViewById(R.id.etEventCategory);
        etPrice      = view.findViewById(R.id.etEventPrice);
        etCapacity   = view.findViewById(R.id.etEventCapacity);

        switchRequireGeo   = view.findViewById(R.id.switchRequireGeo);
        ivEventImage       = view.findViewById(R.id.ivEventImage);
        placeholderLayout  = view.findViewById(R.id.layoutAddCoverPlaceholder);
        MaterialCardView cardImage = view.findViewById(R.id.cardEventImage);
        btnPublish         = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);

        // Category picker
        etCategory.setFocusable(false);
        etCategory.setClickable(true);
        etCategory.setOnClickListener(v -> showCategoryPicker());

        // Click listeners
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        cardImage.setOnClickListener(v -> openImagePicker());

        btnPublish.setOnClickListener(v -> {
            if (isEditMode) updateEvent(view);
            else publishNewEvent(view);
        });

        btnCancel.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        // Edit mode data
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails();
        }
    }

    // ----------------------- Load existing event -----------------------
    private void loadEventDetails() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etTitle.setText(doc.getString("title"));
                    etDescription.setText(doc.getString("description"));
                    etDate.setText(doc.getString("date"));
                    etTime.setText(doc.getString("time"));
                    etLocation.setText(doc.getString("location"));
                    etCategory.setText(doc.getString("category"));

                    Double price = doc.getDouble("price");
                    if (price != null) etPrice.setText(String.valueOf(price));

                    Long cap = doc.getLong("capacity");
                    if (cap != null) etCapacity.setText(String.valueOf(cap));

                    Boolean requireGeo = doc.getBoolean("requireGeolocation");
                    switchRequireGeo.setChecked(requireGeo != null && requireGeo);

                    existingImageUrl = doc.getString("imageUrl");
                    if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                        placeholderLayout.setVisibility(View.GONE);
                        ivEventImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(existingImageUrl).into(ivEventImage);
                    }
                });
    }

    // ----------------------- Publish NEW event -----------------------
    private void publishNewEvent(View view) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String title       = etTitle.getText().toString().trim();
        String desc        = etDescription.getText().toString().trim();
        String date        = etDate.getText().toString().trim();
        String time        = etTime.getText().toString().trim();
        String location    = etLocation.getText().toString().trim();
        String category    = etCategory.getText().toString().trim();
        String priceStr    = etPrice.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        geocodeAddress(location, latLng -> {
            if (latLng == null) {
                Toast.makeText(getContext(), "Invalid location.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> event = new HashMap<>();
            event.put("title", title);
            event.put("description", desc);
            event.put("date", date);
            event.put("time", time);
            event.put("location", location);
            event.put("category", category);

            event.put("price", priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr));

            if (!capacityStr.isEmpty()) {
                event.put("capacity", Integer.parseInt(capacityStr));
            }

            event.put("requireGeolocation", switchRequireGeo.isChecked());
            event.put("lat", latLng.latitude);
            event.put("lng", latLng.longitude);

            event.put("organizerId", user.getUid());
            event.put("organizerEmail", user.getEmail());
            event.put("createdAt", Timestamp.now());

            firestore.collection("events")
                    .add(event)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(getContext(), "Event published!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // ----------------------- Update event -----------------------
    private void updateEvent(View view) {
        if (eventId == null) return;

        String location = etLocation.getText().toString().trim();

        geocodeAddress(location, latLng -> {
            if (latLng == null) {
                Toast.makeText(getContext(), "Invalid location.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("title", etTitle.getText().toString().trim());
            updates.put("description", etDescription.getText().toString().trim());
            updates.put("date", etDate.getText().toString().trim());
            updates.put("time", etTime.getText().toString().trim());
            updates.put("location", location);
            updates.put("category", etCategory.getText().toString().trim());

            String priceStr = etPrice.getText().toString().trim();
            updates.put("price", priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr));

            String capStr = etCapacity.getText().toString().trim();
            if (!capStr.isEmpty()) {
                updates.put("capacity", Integer.parseInt(capStr));
            }

            updates.put("requireGeolocation", switchRequireGeo.isChecked());
            updates.put("lat", latLng.latitude);
            updates.put("lng", latLng.longitude);

            firestore.collection("events")
                    .document(eventId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Event updated!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // ----------------------- Category picker -----------------------
    private void showCategoryPicker() {
        String[] categories = {"Technology", "Sports", "Entertainment", "Health", "Others"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Category")
                .setSingleChoiceItems(categories, -1, (dialog, which) -> {
                    etCategory.setText(categories[which]);
                    dialog.dismiss();
                })
                .show();
    }

    // ----------------------- Image Picker -----------------------
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    // ----------------------- Date Picker -----------------------
    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, year, month, day) ->
                        etDate.setText(day + "/" + (month + 1) + "/" + year),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    // ----------------------- Time Picker -----------------------
    private void showTimePicker() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(
                requireContext(),
                (view, hour, min) ->
                        etTime.setText(String.format("%02d:%02d", hour, min)),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                true
        ).show();
    }

    // ----------------------- Geocoding helper -----------------------
    private void geocodeAddress(String address, OnSuccessListener<LatLng> callback) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> results = geocoder.getFromLocationName(address, 1);
            if (results != null && !results.isEmpty()) {
                Address loc = results.get(0);
                callback.onSuccess(new LatLng(loc.getLatitude(), loc.getLongitude()));
            } else {
                callback.onSuccess(null);
            }
        } catch (Exception e) {
            callback.onSuccess(null);
        }
    }
}
