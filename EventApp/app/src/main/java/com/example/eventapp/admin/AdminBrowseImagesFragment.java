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

public class AdminBrowseImagesFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> imageIds = new ArrayList<>();

    public AdminBrowseImagesFragment() {
        super(R.layout.admin_browse_images);  // you already created this layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadImageList();
    }

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

    private void deleteImage(String imageId) {

        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("event_covers/" + imageId);

        ref.delete()
                .addOnSuccessListener(a -> {
                    Toast.makeText(requireContext(),
                            "Image deleted successfully",
                            Toast.LENGTH_SHORT).show();

                    loadImageList(); // refresh UI
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to delete: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
