package com.example.eventapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying a list of attendees in a RecyclerView.
 * Shows each attendee's name, email, and status.
 */
public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {

    private final List<Attendee> attendeeList;

    /**
     * Creates the adapter with a list of attendees.
     *
     * @param attendeeList the attendees to display
     */
    public AttendeeAdapter(List<Attendee> attendeeList) {
        this.attendeeList = attendeeList;
    }

    /**
     * Inflates the layout for a single attendee row.
     */
    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendee, parent, false);
        return new AttendeeViewHolder(view);
    }

    /**
     * Binds an attendee's information to the row views.
     */
    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        Attendee attendee = attendeeList.get(position);

        holder.tvName.setText(attendee.getName());
        holder.tvEmail.setText(attendee.getEmail());
        holder.tvStatus.setText("Status: " + attendee.getStatus());
    }

    /**
     * Returns the number of attendees displayed.
     */
    @Override
    public int getItemCount() {
        return attendeeList != null ? attendeeList.size() : 0;
    }

    /**
     * ViewHolder that stores UI elements for an attendee item.
     */
    public static class AttendeeViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvStatus;

        /**
         * Initializes the text views for attendee information.
         *
         * @param itemView the row view
         */
        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
