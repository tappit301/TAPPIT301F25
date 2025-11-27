package com.example.eventapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.eventapp.utils.FirebaseHelper;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private ImageView imgProfileAvatar;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

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

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.activity_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseHelper.getAuth();
        firestore = FirebaseHelper.getFirestore();

        NavController navController = Navigation.findNavController(view);

        MaterialToolbar toolbar = view.findViewById(R.id.topAppBar);
        if (toolbar != null) {
            // If you want back nav from profile, you could enable this:
            // toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
        }

        imgProfileAvatar = view.findViewById(R.id.imgProfileAvatar);

        LinearLayout rowEditProfile = view.findViewById(R.id.rowEditProfile);
        LinearLayout rowScanQr = view.findViewById(R.id.rowScanQr);
        LinearLayout rowNotifications = view.findViewById(R.id.rowNotifications);
        LinearLayout rowLogout = view.findViewById(R.id.rowLogout);
        LinearLayout rowDeleteAccount = view.findViewById(R.id.rowDeleteAccount);
        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);

        if (rowEditProfile != null) {
            rowEditProfile.setOnClickListener(v ->
                    navController.navigate(R.id.action_profileFragment_to_editProfileFragment));
        }

        if (rowScanQr != null) {
            rowScanQr.setOnClickListener(v ->
                    navController.navigate(R.id.action_profileFragment_to_scanQrFragment));
        }

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Toast.makeText(requireContext(),
                        isChecked ? "Notifications enabled" : "Notifications disabled",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (rowLogout != null) {
            rowLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Toast.makeText(requireContext(),
                        "Logged out",
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireContext(), HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });
        }

        if (rowDeleteAccount != null) {
            rowDeleteAccount.setOnClickListener(v ->
                    showDeleteAccountConfirmation(navController));
        }

        if (imgProfileAvatar != null) {
            imgProfileAvatar.setOnClickListener(v -> openImagePicker());
        }

        loadProfileImage();
    }

    private void openImagePicker() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "Please sign in to change your profile picture.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "Not signed in.",
                    Toast.LENGTH_SHORT).show();
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
                                            .set(data)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(),
                                                        "Profile picture updated.",
                                                        Toast.LENGTH_SHORT).show();
                                                showProfileImage(url);
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(requireContext(),
                                                            "Failed to save picture: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show());
                                })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void loadProfileImage() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || imgProfileAvatar == null) {
            return;
        }

        String uid = user.getUid();

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc != null && doc.exists()) {
                        String url = doc.getString("profileImageUrl");
                        if (url != null && !url.isEmpty()) {
                            showProfileImage(url);
                        }
                    }
                });
    }

    private void showProfileImage(String url) {
        if (imgProfileAvatar == null) {
            return;
        }

        Glide.with(this)
                .load(url)
                .circleCrop()
                .into(imgProfileAvatar);

    }

    private void showDeleteAccountConfirmation(NavController navController) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete account?")
                .setMessage("This will permanently delete your account and profile data. This action cannot be undone.")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    showReauthDialog(navController);
                })
                .show();
    }

    private void showReauthDialog(NavController navController) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "No signed-in user.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputLayout inputLayout = new TextInputLayout(requireContext());
        inputLayout.setHint("Password");

        TextInputEditText inputPassword = new TextInputEditText(requireContext());
        inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputLayout.addView(inputPassword);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        inputLayout.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Password")
                .setMessage("Please enter your password to confirm account deletion.")
                .setView(inputLayout)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Continue", (dialog, which) -> {
                    String password = inputPassword.getText() != null
                            ? inputPassword.getText().toString().trim()
                            : "";

                    if (password.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Password cannot be empty.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    reauthenticateAndDelete(password, navController);
                })
                .show();
    }

    private void reauthenticateAndDelete(String password, NavController navController) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(),
                    "No signed-in user.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Cannot delete account: missing email.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    performAccountDeletion(navController, user);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Re-authentication failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void performAccountDeletion(NavController navController, FirebaseUser user) {
        String uid = user.getUid();

        firestore.collection("users")
                .document(uid)
                .delete()
                .addOnCompleteListener(task -> {
                    deleteAuthUser(user);
                });
    }

    private void deleteAuthUser(FirebaseUser user) {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(),
                            "Account deleted.",
                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Failed to delete account: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}