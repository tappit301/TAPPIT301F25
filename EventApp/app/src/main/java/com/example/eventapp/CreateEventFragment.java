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

/**
 * Fragment used for creating and editing events.
 * Supports fields for date, time, category, location, cover image, pricing,
 * capacity, organizer details, and geolocation requirements.
 */
public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";

    private String eventId = null;
    private boolean isEditMode = false;

    private EditText etDate, etTime, etTitle, etDescription, etLocation, etCategory, etPrice, etCapacity;
    private ImageView ivEventImage;
    private LinearLayout placeholderLayout;
    private MaterialButton btnPublish;
    private SwitchMaterial switchRequireGeo;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    /**
     * Inflates the layout containing event creation fields.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_event, container, false);
    }

    /**
     * Initializes Firebase references and the image picker callback.
     *
     * @param savedInstanceState previous state, if any
     */
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

    /**
     * Connects views, handles edit mode, sets listeners for date/time pickers,
     * and initializes event create/update actions.
     *
     * @param view root layout
     * @param savedInstanceState previously saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        etCategory.setFocusable(false);
        etCategory.setOnClickListener(v -> showCategoryPicker());

        Bundle args = getArguments();
        if (args != null && args.containsKey("eventId")) {
            isEditMode = true;
            eventId = args.getString("eventId");
            btnPublish.setText("Save Changes");
            loadEventDetails();
        }

        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
        cardEventImage.setOnClickListener(v -> openImagePicker());

        btnPublish.setOnClickListener(v -> {
            if (isEditMode) updateEvent(view);
            else publishEvent(view);
        });

        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    /**
     * Shows the category picker dialog with predefined choices.
     */
    private void showCategoryPicker() {
        String[] categories = new String[] {
                "Technology", "Sports", "Entertainment", "Health"
        };

        int checkedItem = -1;
        String current = etCategory.getText().toString().trim();

        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equalsIgnoreCase(current)) checkedItem = i;
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

    /**
     * Loads existing event details from Firestore when editing an event.
     * Populates all fields including geolocation and cover image.
     */
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

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        placeholderLayout.setVisibility(View.GONE);
                        ivEventImage.setVisibility(View.VISIBLE);
                        Glide.with(requireContext()).load(imageUrl).into(ivEventImage);
                    }
                });
    }

    /**
     * Validates input fields and decides whether to publish as a signed-in user
     * or prompt a guest organizer for details.
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

        if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
            doPublishEvent(view, title, desc, date, time, location, category,
                    user.getUid(), user.getEmail());
            return;
        }

        showGuestOrganizerDialog(view, title, desc, date, time, location, category);
    }

    /**
     * Shows a dialog for guest organizers to enter name and email,
     * and generates a stable guest organizer ID using device information.
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
                    String guestEmail = etEmail.getText() != null
                            ? etEmail.getText().toString().trim() : "";

                    if (guestEmail.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Email is required to create an event.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Context ctx = requireContext();
                    SharedPreferences prefs =
                            ctx.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

                    String guestId = prefs.getString("GUEST_ORGANIZER_ID", null);
                    if (guestId == null) {
                        String deviceId = Settings.Secure.getString(
                                ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
                        guestId = "guest_" + deviceId;
                        prefs.edit().putString("GUEST_ORGANIZER_ID", guestId).apply();
                    }

                    doPublishEvent(anchorView, title, desc, date, time, location, category,
                            guestId, guestEmail);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Writes a new event document to Firestore.
     * Includes metadata such as geolocation, price, capacity, organizer info,
     * and automatically generates a QR payload.
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

        String priceStr = etPrice.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

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
            try { if (!priceStr.isEmpty()) priceVal = Double.parseDouble(priceStr); }
            catch (Exception ignored) {}
            event.put("price", priceVal);

            try { if (!capacityStr.isEmpty()) event.put("capacity",
                    Integer.parseInt(capacityStr)); }
            catch (Exception ignored) {}

            boolean requireGeo = switchRequireGeo.isChecked();
            event.put("requireGeolocation", requireGeo);

            event.put("lat", latLng.latitude);
            event.put("lng", latLng.longitude);

            event.put("createdAt", Timestamp.now());
            event.put("organizerId", organizerId);
            event.put("organizerEmail", organizerEmail);

            firestore.collection("events")
                    .add(event)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(getContext(), "Event published successfully!",
                                Toast.LENGTH_SHORT).show();

                        String qrPayload = "Event: " + title +
                                "\nDate: " + date +
                                "\nTime: " + time +
                                "\nLocation: " + location +
                                "\nOrganizer: " + organizerEmail;

                        Bundle bundle = new Bundle();
                        bundle.putString("qrData", qrPayload);
                        Navigation.findNavController(view)
                                .navigate(R.id.qrCodeFragment, bundle);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Updates an existing event with edited fields, including optional image upload.
     */
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

            double priceVal = 0.0;
            try { priceVal = Double.parseDouble(etPrice.getText().toString().trim()); }
            catch (Exception ignored) {}
            updates.put("price", priceVal);

            try {
                updates.put("capacity", Integer.parseInt(etCapacity.getText().toString().trim()));
            } catch (Exception ignored) {}

            updates.put("requireGeolocation", switchRequireGeo.isChecked());
            updates.put("lat", latLng.latitude);
            updates.put("lng", latLng.longitude);

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
                                        Toast.makeText(getContext(), "Event updated!",
                                                Toast.LENGTH_SHORT).show();
                                        Navigation.findNavController(view).popBackStack();
                                    });
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(),
                                        "Image upload failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            } else {
                firestore.collection("events")
                        .document(eventId)
                        .update(updates)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Event updated!",
                                    Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(),
                                        "Update failed: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * Opens a date picker dialog for the event date field.
     */
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

    /**
     * Opens a time picker dialog for the event time field.
     */
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

    /**
     * Opens the device gallery for selecting an event cover image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Converts a written address into a LatLng using the system geocoder.
     * Returns null through the callback if geocoding fails.
     *
     * @param address the text address to convert
     * @param callback callback receiving the resulting LatLng
     */
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
