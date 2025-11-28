package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

import java.util.List;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ImageVH> {

    private final List<String> images;

    public AdminImageAdapter(List<String> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageVH holder, int position) {
        holder.id.setText(images.get(position));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageVH extends RecyclerView.ViewHolder {
        TextView id;

        public ImageVH(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.txtImageId);
        }
    }
}
