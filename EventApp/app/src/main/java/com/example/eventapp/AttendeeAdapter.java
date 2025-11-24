package com.example.eventapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.AttendeeViewHolder> {

    private List<Attendee> attendeeList;

    public AttendeeAdapter(List<Attendee> attendeeList) {
        this.attendeeList = attendeeList;
    }

    @NonNull
    @Override
    public AttendeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendee, parent, false);
        return new AttendeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeViewHolder holder, int position) {
        Attendee attendee = attendeeList.get(position);

        holder.tvName.setText(attendee.getName());
        holder.tvEmail.setText(attendee.getEmail());
        holder.tvStatus.setText("Status: " + attendee.getStatus());
    }

    @Override
    public int getItemCount() {
        return attendeeList.size();
    }

    public static class AttendeeViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvStatus;

        public AttendeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}