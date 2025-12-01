package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

import java.util.List;

public class AdminRemoveOrganizersAdapter
        extends RecyclerView.Adapter<AdminRemoveOrganizersAdapter.Holder> {

    public interface OnRemoveClickListener { void onRemove(String uid); }

    private final List<OrganizerUser> organizers;
    private final OnRemoveClickListener listener;

    public AdminRemoveOrganizersAdapter(List<OrganizerUser> organizers,
                                        OnRemoveClickListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        OrganizerUser u = organizers.get(position);

        holder.email.setText(u.email);
        holder.eventCount.setText("Events created: " + u.eventCount);

        holder.removeBtn.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Remove Organizer?")
                    .setMessage("Are you sure you want to remove " + u.email + "?")
                    .setPositiveButton("Yes", (dialog, which) -> listener.onRemove(u.uid))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView email, eventCount;
        Button removeBtn;

        public Holder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.tvOrganizerEmail);
            eventCount = itemView.findViewById(R.id.tvOrganizerEventCount);
            removeBtn = itemView.findViewById(R.id.btnRemoveOrganizer);
        }
    }
}
