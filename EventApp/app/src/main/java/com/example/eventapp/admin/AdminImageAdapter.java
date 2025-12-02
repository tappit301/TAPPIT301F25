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

/**
 * Adapter that displays a list of image IDs stored in Firebase.
 * Shows the thumbnail preview and allows the admin to delete an image.
 */
public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.Holder> {

    /**
     * Listener interface for handling delete actions on an image.
     */
    public interface OnDeleteClick {
        /**
         * Called when the admin chooses to delete an image.
         *
         * @param imageId the ID of the image to delete
         */
        void deleteImage(String imageId);
    }

    private final List<String> imageIds;
    private final OnDeleteClick deleteListener;

    /**
     * Creates the adapter with a list of image IDs and a delete callback.
     *
     * @param imageIds the images to display
     * @param deleteListener listener that handles deletion
     */
    public AdminImageAdapter(List<String> imageIds, OnDeleteClick deleteListener) {
        this.imageIds = imageIds;
        this.deleteListener = deleteListener;
    }

    /**
     * Inflates the layout for a single image row.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false);
        return new Holder(v);
    }

    /**
     * Binds the image ID and loads its thumbnail from Firebase Storage.
     * Also sets up the delete confirmation dialog.
     */
    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String id = imageIds.get(position);

        holder.imageId.setText(id);

        FirebaseStorage.getInstance()
                .getReference("event_covers/" + id)
                .getDownloadUrl()
                .addOnSuccessListener(uri ->
                        Glide.with(holder.thumbnail.getContext())
                                .load(uri)
                                .placeholder(R.drawable.placeholder_img)
                                .into(holder.thumbnail)
                );

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

    /**
     * Returns how many images are displayed.
     */
    @Override
    public int getItemCount() {
        return imageIds.size();
    }

    /**
     * ViewHolder that stores references to the UI elements for a single image item.
     */
    static class Holder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView imageId;
        Button deleteBtn;

        /**
         * Initializes UI components for an image row.
         *
         * @param itemView the row view
         */
        public Holder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.ivThumbnail);
            imageId = itemView.findViewById(R.id.tvImageIdAdmin);
            deleteBtn = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
