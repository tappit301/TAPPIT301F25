package com.example.eventapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    // Edit Mode Variables
    private String eventId = null;
    private boolean isEditMode = false;

    // Views
    private EditText etDate, etTime;
    private EditText etTitle, etDescription, etLocation;
    private EditText etCategory, etPrice, etCapacity;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;
    private SwitchMaterial switchRequireGeo;

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
                            if (placeholderLayout != null) {
                                placeholderLayout.setVisibility(View.GONE);
                            }
                            if (ivEventImage != null) {
                                ivEventImage.setVisibility(View.VISIBLE);
                                ivEventImage.setImageURI(selectedImageUri);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Views
        etDate        = view.findViewById(R.id.etEventDate);
        etTime        = view.findViewById(R.id.etEventTime);
        etTitle       = view.findViewById(R.id.etEventTitle);
        etDescription = view.findViewById(R.id.etEventDescription);
        etLocation    = view.findViewById(R.id.etEventLocation);
        etCategory    = view.findViewById(R.id.etEventCategory);
        etPrice       = view.findViewById(R.id.etEventPrice);
        etCapacity    = view.findViewById(R.id.etEventCapacity);

        btnPublish        = view.findViewById(R.id.btnPublishEvent);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialCardView cardEventImage = view.findViewById(R.id.cardEventImage);
        ivEventImage      = view.findViewById(R.id.ivEventImage);
        placeholderLayout = view.findViewById(R.id.layoutAddCoverPlaceholder);
        switchRequireGeo  = view.findViewById(R.id.switchRequireGeo);

        // Category dropdown behaviour
        etCategory.setFocusable(false);
        etCategory.setClickable(true);
        etCategory.setOnClickListener(v -> showCategoryPicker());

        // Read Arguments (edit mode)
        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails(); // Fetch from Firestore (includes price / capacity / requireGeo)
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
                publishEvent(view);   // One method for normal + guest
            }
        });

        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    /** Shows the category picker dialog */
    private void showCategoryPicker() {
        String[] categories = new String[] {
                "Technology",
                "Sports",
                "Entertainment",
                "Health"
        };

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

    /** Fetch Event from Firestore (edit mode) */
    private void loadEventDetails() {
        firestore.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    if (etTitle != null)       etTitle.setText(doc.getString("title"));
                    if (etDescription != null) etDescription.setText(doc.getString("description"));
                    if (etDate != null)        etDate.setText(doc.getString("date"));
                    if (etTime != null)        etTime.setText(doc.getString("time"));
                    if (etLocation != null)    etLocation.setText(doc.getString("location"));
                    if (etCategory != null)    etCategory.setText(doc.getString("category"));

                    // Price
                    Double price = doc.getDouble("price");
                    if (price != null && etPrice != null) {
                        etPrice.setText(String.valueOf(price));
                    }

                    // Capacity
                    Long cap = doc.getLong("capacity");
                    if (cap != null && etCapacity != null) {
                        etCapacity.setText(String.valueOf(cap));
                    }

                    // Require geolocation
                    Boolean requireGeo = doc.getBoolean("requireGeolocation");
                    if (switchRequireGeo != null) {
                        switchRequireGeo.setChecked(requireGeo != null && requireGeo);
                    }

                    // Image
                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        placeholderLayout.setVisibility(View.GONE);
                        ivEventImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(imageUrl).into(ivEventImage);
                    }
                });
    }

    /**
     * Publish event:
     * - If Firebase user with email → use that
     * - Else → show popup asking for name + email, then publish as "guest organizer"
     *   (still storing organizerId + organizerEmail, plus geo and price).
     */
    private void publishEvent(View view) {
        if (getView() == null) return;

        String title    = etTitle.getText().toString().trim();
        String desc     = etDescription.getText().toString().trim();
        String date     = etDate.getText().toString().trim();
        String time     = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(getContext(), "Please choose a category.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        // Signed-in user with email → publish directly
        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            String organizerId = user.getUid();
            String organizerEmail = user.getEmail();

            doPublishEvent(view, title, desc, date, time, location, category,
                    organizerId, organizerEmail);
            return;
        }

        // Guest / missing email → show dialog to collect name + email, then publish
        showGuestOrganizerDialog(view, title, desc, date, time, location, category);
    }

    /**
     * Show popup asking guest for name + email, then publish with a stable guest ID
     * remembered via SharedPreferences.
     */
    private void showGuestOrganizerDialog(View anchorView,
                                          String title,
                                          String desc,
                                          String date,
                                          String time,
                                          String location,
                                          String category) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_guest_organizer, null, false);

        TextInputEditText etName = dialogView.findViewById(R.id.etGuestName);
        TextInputEditText etEmail = dialogView.findViewById(R.id.etGuestEmail);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Enter your details")
                .setView(dialogView)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String guestName = etName.getText() != null
                            ? etName.getText().toString().trim()
                            : "";
                    String guestEmail = etEmail.getText() != null
                            ? etEmail.getText().toString().trim()
                            : "";

                    if (guestEmail.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Email is required to create an event.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Build a stable guest organizer ID using device id
                    Context ctx = requireContext();
                    SharedPreferences prefs =
                            ctx.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
                    String guestId = prefs.getString("GUEST_ORGANIZER_ID", null);

                    if (guestId == null) {
                        String deviceId = Settings.Secure.getString(
                                ctx.getContentResolver(),
                                Settings.Secure.ANDROID_ID
                        );
                        guestId = "guest_" + deviceId;
                        prefs.edit().putString("GUEST_ORGANIZER_ID", guestId).apply();
                    }

                    // Optionally use guestName if you want to store it somewhere

                    doPublishEvent(anchorView, title, desc, date, time, location, category,
                            guestId, guestEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Actually writes the event document to Firestore using the given organizerId/email,
     * including:
     *  - price
     *  - capacity
     *  - requireGeolocation
     *  - lat/lng (from geocoded address)
     */
    private void doPublishEvent(View view,
                                String title,
                                String desc,
                                String date,
                                String time,
                                String location,
                                String category,
                                String organizerId,
                                String organizerEmail) {

        // Read price / capacity from fields
        String priceStr    = etPrice != null ? etPrice.getText().toString().trim() : "";
        String capacityStr = etCapacity != null ? etCapacity.getText().toString().trim() : "";

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

            double priceVal = 0.0;
            if (!priceStr.isEmpty()) {
                try {
                    priceVal = Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}
            }
            event.put("price", priceVal);

            if (!capacityStr.isEmpty()) {
                try {
                    event.put("capacity", Integer.parseInt(capacityStr));
                } catch (NumberFormatException ignored) {}
            }

            boolean requireGeo = switchRequireGeo != null && switchRequireGeo.isChecked();
            event.put("requireGeolocation", requireGeo);
            event.put("lat", latLng.latitude);
            event.put("lng", latLng.longitude);

            event.put("createdAt", Timestamp.now());
            event.put("organizerId", organizerId);
            event.put("organizerEmail", organizerEmail);

            firestore.collection("events")
                    .add(event)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(), "Event published successfully!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Event saved by " + organizerEmail);

                        String qrPayload = "Event: " + title +
                                "\nDate: " + date +
                                "\nTime: " + time +
                                "\nLocation: " + location +
                                "\nOrganizer: " + organizerEmail;

                        Bundle bundle = new Bundle();
                        bundle.putString("qrData", qrPayload);
                        Navigation.findNavController(view).navigate(R.id.qrCodeFragment, bundle);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding event", e);
                        Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    /** Update Existing Event (with geo, price, capacity, requireGeolocation, and optional new image) */
    private void updateEvent(View view) {
        if (eventId == null) return;

        String location = etLocation.getText().toString().trim();
        if (location.isEmpty()) {
            Toast.makeText(getContext(), "Location is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        geocodeAddress(location, latLng -> {
            if (latLng == null) {
                Toast.makeText(getContext(), "Invalid location.", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("title",       etTitle.getText().toString().trim());
            updates.put("description", etDescription.getText().toString().trim());
            updates.put("date",        etDate.getText().toString().trim());
            updates.put("time",        etTime.getText().toString().trim());
            updates.put("location",    location);
            updates.put("category",    etCategory.getText().toString().trim());

            String priceStr = etPrice.getText().toString().trim();
            double priceVal = 0.0;
            if (!priceStr.isEmpty()) {
                try {
                    priceVal = Double.parseDouble(priceStr);
                } catch (NumberFormatException ignored) {}
            }
            updates.put("price", priceVal);

            String capStr = etCapacity.getText().toString().trim();
            if (!capStr.isEmpty()) {
                try {
                    updates.put("capacity", Integer.parseInt(capStr));
                } catch (NumberFormatException ignored) {}
            }

            boolean requireGeo = switchRequireGeo != null && switchRequireGeo.isChecked();
            updates.put("requireGeolocation", requireGeo);
            updates.put("lat", latLng.latitude);
            updates.put("lng", latLng.longitude);

            // If a new image was selected, upload and include imageUrl
            if (selectedImageUri != null) {
                StorageReference imageRef = storage.getReference()
                        .child("event_covers/" + eventId + ".jpg");

                imageRef.putFile(selectedImageUri)
                        .continueWithTask(task -> {
                            if (!task.isSuccessful()) throw task.getException();
                            return imageRef.getDownloadUrl();
                        })
                        .addOnSuccessListener(downloadUrl -> {
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
                // No new image; just update fields
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
        });
    }

    /** Date picker */
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

    /** Time picker */
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

    /** Image picker */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /** Geocoding helper */
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
