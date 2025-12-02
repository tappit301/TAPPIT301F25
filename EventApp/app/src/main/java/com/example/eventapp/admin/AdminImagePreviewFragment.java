package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Fragment that displays a full preview of a selected event image.
 * Loads the image from Firebase Storage and shows a progress indicator while loading.
 */
public class AdminImagePreviewFragment extends Fragment {

    /**
     * Creates the fragment and sets its layout.
     */
    public AdminImagePreviewFragment() {
        super(R.layout.fragment_admin_image_preview);
    }

    /**
     * Retrieves the image ID from arguments, loads the image from Firebase Storage,
     * and displays it with a progress bar while loading.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        ImageView imageView = view.findViewById(R.id.ivPreviewImage);
        ProgressBar progressBar = view.findViewById(R.id.progressBarImage);

        String imageId = requireArguments().getString("imageId");

        if (imageId == null || imageId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing image ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String path = "event_covers/" + imageId + ".jpg";

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference()
                .child(path);

        progressBar.setVisibility(View.VISIBLE);

        ref.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    progressBar.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.placeholder_img)
                            .error(R.drawable.placeholder_img)
                            .into(imageView);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Failed to load image: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
