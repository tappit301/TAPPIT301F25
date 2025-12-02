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
 * Adapter that displays a list of users for the admin.
 * Each row shows the user's name and email and supports click actions.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserVH> {

    private final List<AdminUserModel> list;
    private final OnUserClickListener listener;

    /**
     * Listener for responding to user row clicks.
     */
    public interface OnUserClickListener {
        /**
         * Called when a user row is clicked.
         *
         * @param user the selected user
         */
        void onUserClick(AdminUserModel user);
    }

    /**
     * Creates the adapter with a list of users and a click callback.
     *
     * @param list the list of users to display
     * @param listener callback for user selection
     */
    public AdminUserAdapter(List<AdminUserModel> list, OnUserClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    /**
     * Inflates the layout for a single user row.
     */
    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false));
    }

    /**
     * Binds a user's data to the row and sets up the click listener.
     */
    @Override
    public void onBindViewHolder(@NonNull UserVH h, int pos) {
        AdminUserModel u = list.get(pos);
        h.txtEmail.setText(u.getEmail());
        h.txtName.setText(u.getName());

        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    /**
     * Returns the number of users shown.
     */
    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * ViewHolder containing UI components for displaying one user's information.
     */
    public static class UserVH extends RecyclerView.ViewHolder {
        TextView txtEmail, txtName;

        /**
         * Initializes the text views for email and name.
         *
         * @param v the row view
         */
        public UserVH(@NonNull View v) {
            super(v);
            txtEmail = v.findViewById(R.id.txtUserEmail);
            txtName = v.findViewById(R.id.txtUserName);
        }
    }
}
