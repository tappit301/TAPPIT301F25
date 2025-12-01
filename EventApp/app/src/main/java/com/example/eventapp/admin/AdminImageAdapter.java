package com.example.eventapp.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventapp.R;
import com.google.firebase.storage.FirebaseStorage;

import java.util.List;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.Holder> {

    public interface OnDeleteClick {
        void deleteImage(String imageId);
    }

    private final List<String> imageIds;
    private final OnDeleteClick deleteListener;

    public AdminImageAdapter(List<String> imageIds, OnDeleteClick deleteListener) {
        this.imageIds = imageIds;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String id = imageIds.get(position);

        holder.imageId.setText(id);

        // Load thumbnail from Firebase Storage
        FirebaseStorage.getInstance()
                .getReference("event_covers/" + id)
                .getDownloadUrl()
                .addOnSuccessListener(uri ->
                        Glide.with(holder.thumbnail.getContext())
                                .load(uri)
                                .placeholder(R.drawable.placeholder_img)
                                .into(holder.thumbnail)
                );

        // Delete popup
        holder.deleteBtn.setOnClickListener(v -> {
            Context ctx = v.getContext();
            new AlertDialog.Builder(ctx)
                    .setTitle("Delete Image?")
                    .setMessage("Are you sure you want to delete this image?\n\n" + id)
                    .setPositiveButton("Delete", (d, w) -> deleteListener.deleteImage(id))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return imageIds.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView imageId;
        Button deleteBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.ivThumbnail);
            imageId = itemView.findViewById(R.id.tvImageIdAdmin);
            deleteBtn = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
