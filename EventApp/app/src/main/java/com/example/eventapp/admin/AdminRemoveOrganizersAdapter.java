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

/**
 * Adapter that displays a list of organizer accounts along with how many events each created.
 * Allows the admin to remove an organizer through a callback method.
 */
public class AdminRemoveOrganizersAdapter
        extends RecyclerView.Adapter<AdminRemoveOrganizersAdapter.Holder> {

    /**
     * Listener interface for handling organizer removal.
     */
    public interface OnRemoveClickListener {
        /**
         * Called when the admin confirms an organizer should be removed.
         *
         * @param uid the organizer's user ID
         */
        void onRemove(String uid);
    }

    private final List<OrganizerUser> organizers;
    private final OnRemoveClickListener listener;

    /**
     * Creates the adapter with a list of organizer users and a remove callback.
     *
     * @param organizers list of organizer data models
     * @param listener callback for removing an organizer
     */
    public AdminRemoveOrganizersAdapter(List<OrganizerUser> organizers,
                                        OnRemoveClickListener listener) {
        this.organizers = organizers;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single organizer row.
     */
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new Holder(v);
    }

    /**
     * Populates the row with organizer information and attaches the remove button action.
     */
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

    /**
     * Returns the total number of organizers displayed.
     */
    @Override
    public int getItemCount() {
        return organizers.size();
    }

    /**
     * ViewHolder storing UI components for a single organizer row.
     */
    static class Holder extends RecyclerView.ViewHolder {
        TextView email, eventCount;
        Button removeBtn;

        /**
         * Initializes views for displaying organizer info.
         *
         * @param itemView the row view
         */
        public Holder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.tvOrganizerEmail);
            eventCount = itemView.findViewById(R.id.tvOrganizerEventCount);
            removeBtn = itemView.findViewById(R.id.btnRemoveOrganizer);
        }
    }
}
