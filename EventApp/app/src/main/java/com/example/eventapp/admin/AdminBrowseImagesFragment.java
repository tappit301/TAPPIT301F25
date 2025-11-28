package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminBrowseImagesFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> imageIds = new ArrayList<>();

    public AdminBrowseImagesFragment() {
        super(R.layout.admin_browse_images);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadImages();
    }

    private void loadImages() {
        AdminManager.getInstance().getAllImages()
                .addOnSuccessListener(snap -> {
                    imageIds.clear();
                    for (var doc : snap) {
                        imageIds.add(doc.getId());
                    }
                    recycler.setAdapter(new AdminImageAdapter(imageIds));
                });
    }
}
