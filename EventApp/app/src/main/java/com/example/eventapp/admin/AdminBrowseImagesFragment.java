package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows an admin to browse all uploaded event cover images.
 * Displays image IDs in a list and lets the admin delete any image.
 */
public class AdminBrowseImagesFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> imageIds = new ArrayList<>();

    /**
     * Creates the fragment and sets its layout resource.
     */
    public AdminBrowseImagesFragment() {
        super(R.layout.admin_browse_images);
    }

    /**
     * Sets up the RecyclerView and loads the list of images when the view is ready.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadImageList();
    }

    /**
     * Loads all image IDs stored under the event cover directory in Firebase Storage.
     * Updates the RecyclerView once the list is retrieved.
     */
    private void loadImageList() {
        FirebaseStorage.getInstance()
                .getReference("event_covers")
                .listAll()
                .addOnSuccessListener(list -> {

                    imageIds.clear();

                    list.getItems().forEach(item ->
                            imageIds.add(item.getName())
                    );

                    recycler.setAdapter(new AdminImageAdapter(
                            imageIds,
                            this::deleteImage
                    ));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load images: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Deletes the selected image from Firebase Storage and refreshes the list after deletion.
     *
     * @param imageId the ID of the image to delete
     */
    private void deleteImage(String imageId) {

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("event_covers/" + imageId);

        ref.delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(requireContext(),
                            "Image deleted successfully",
                            Toast.LENGTH_SHORT).show();

                    loadImageList();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to delete: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
