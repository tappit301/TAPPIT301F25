package com.example.eventapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.eventapp.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows users to edit their profile details such as name,
 * phone number, email, and profile picture.
 */
public class EditProfileFragment extends Fragment {

    private ImageView imgEditProfileAvatar;
    private TextInputEditText inputFullName;
    private TextInputEditText inputPhoneNumber;
    private TextInputEditText inputEmail;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    /**
     * Default constructor required for fragment instantiation.
     */
    public EditProfileFragment() { }

    /**
     * Initializes the image picker launcher used for selecting a new profile picture.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadProfileImage(imageUri);
                        }
                    }
                }
        );
    }

    /**
     * Inflates the profile editing screen.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    /**
     * Sets up UI elements, loads existing profile data,
     * and attaches button actions for saving and updating profile information.
     *
     * @param view root view of the fragment
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        NavController navController = Navigation.findNavController(view);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBarEditProfile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        }

        imgEditProfileAvatar = view.findViewById(R.id.imgEditProfileAvatar);
        inputFullName = view.findViewById(R.id.inputFullName);
        inputPhoneNumber = view.findViewById(R.id.inputPhoneNumber);
        inputEmail = view.findViewById(R.id.inputEmail);

        MaterialButton btnChangeProfilePicture = view.findViewById(R.id.btnChangeProfilePicture);
        MaterialButton btnSaveChanges = view.findViewById(R.id.btnSaveChanges);

        loadExistingProfile();

        btnChangeProfilePicture.setOnClickListener(v -> openImagePicker());
        btnSaveChanges.setOnClickListener(v -> saveProfile(navController));
    }

    /**
     * Loads the user's current profile information from FirebaseAuth and Firestore.
     */
    private void loadExistingProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        if (user.getDisplayName() != null) inputFullName.setText(user.getDisplayName());
        if (user.getEmail() != null) inputEmail.setText(user.getEmail());
        if (user.getPhoneNumber() != null) inputPhoneNumber.setText(user.getPhoneNumber());

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String profileImageUrl = doc.getString("profileImageUrl");

                        if (name != null) inputFullName.setText(name);
                        if (email != null) inputEmail.setText(email);
                        if (phone != null) inputPhoneNumber.setText(phone);
                        if (profileImageUrl != null) showProfileImage(profileImageUrl);
                    }
                });
    }

    /**
     * Opens the gallery for selecting a new profile image.
     */
    private void openImagePicker() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to change your profile picture.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Uploads a new profile image to Firebase Storage and saves its URL in Firestore.
     *
     * @param imageUri the URI of the selected image
     */
    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("profilePictures")
                .child(uid + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    String url = downloadUri.toString();

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("profileImageUrl", url);

                                    firestore.collection("users")
                                            .document(uid)
                                            .set(data, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(), "Profile picture updated.", Toast.LENGTH_SHORT).show();
                                                showProfileImage(url);
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(requireContext(), "Failed to save picture: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays the given image URL inside the avatar view.
     *
     * @param url the profile image URL
     */
    private void showProfileImage(String url) {
        if (imgEditProfileAvatar == null) return;

        Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgEditProfileAvatar);
    }

    /**
     * Saves the updated profile data (name, email, phone) to Firestore.
     *
     * @param navController used to return to the previous screen on success
     */
    private void saveProfile(NavController navController) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        String name = textOrEmpty(inputFullName);
        String phone = textOrEmpty(inputPhoneNumber);
        String email = textOrEmpty(inputEmail);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Name and email are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phone);
        data.put("email", email);

        firestore.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "Profile updated.\nNote: you still log in with your original email.",
                            Toast.LENGTH_LONG).show();
                    navController.navigateUp();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Safely extracts trimmed text from a TextInputEditText, returning an empty string if null.
     *
     * @param editText the input field
     * @return the trimmed text value or empty string
     */
    private String textOrEmpty(TextInputEditText editText) {
        return editText.getText() != null
                ? editText.getText().toString().trim()
                : "";
    }
}
