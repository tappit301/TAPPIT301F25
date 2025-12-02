package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

import java.util.List;

/**
 * Adapter used to display a list of notification log messages for the admin.
 */
public class AdminNotificationLogAdapter extends RecyclerView.Adapter<AdminNotificationLogAdapter.LogVH> {

    private final List<String> logs;

    /**
     * Creates the adapter with the log messages to display.
     *
     * @param logs the list of notification log entries
     */
    public AdminNotificationLogAdapter(List<String> logs) {
        this.logs = logs;
    }

    /**
     * Inflates the layout for a single log row.
     */
    @NonNull
    @Override
    public LogVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification_log, parent, false);
        return new LogVH(v);
    }

    /**
     * Binds a log message to the row's text view.
     */
    @Override
    public void onBindViewHolder(@NonNull LogVH holder, int position) {
        holder.message.setText(logs.get(position));
    }

    /**
     * Returns how many log entries are shown.
     */
    @Override
    public int getItemCount() {
        return logs.size();
    }

    /**
     * ViewHolder that stores UI components for a single log entry.
     */
    public static class LogVH extends RecyclerView.ViewHolder {
        TextView message;

        /**
         * Initializes the text view for a log entry.
         *
         * @param itemView the row view
         */
        public LogVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.txtNotificationLog);
        }
    }
}
