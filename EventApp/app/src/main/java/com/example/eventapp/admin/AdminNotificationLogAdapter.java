package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

import java.util.List;

public class AdminNotificationLogAdapter extends RecyclerView.Adapter<AdminNotificationLogAdapter.LogVH> {

    private final List<String> logs;

    public AdminNotificationLogAdapter(List<String> logs) {
        this.logs = logs;
    }

    @NonNull
    @Override
    public LogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification_log, parent, false);
        return new LogVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogVH holder, int position) {
        holder.message.setText(logs.get(position));
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    public static class LogVH extends RecyclerView.ViewHolder {
        TextView message;

        public LogVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.txtNotificationLog);
        }
    }
}
