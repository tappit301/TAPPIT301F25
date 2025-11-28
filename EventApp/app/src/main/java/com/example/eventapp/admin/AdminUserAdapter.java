package com.example.eventapp.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;

import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserVH> {

    private final List<AdminUserModel> list;
    private final OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(AdminUserModel user);
    }

    public AdminUserAdapter(List<AdminUserModel> list, OnUserClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserVH h, int pos) {
        AdminUserModel u = list.get(pos);
        h.txtEmail.setText(u.getEmail());
        h.txtName.setText(u.getName());

        h.itemView.setOnClickListener(v -> listener.onUserClick(u));
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class UserVH extends RecyclerView.ViewHolder {
        TextView txtEmail, txtName;

        public UserVH(@NonNull View v) {
            super(v);
            txtEmail = v.findViewById(R.id.txtUserEmail);
            txtName = v.findViewById(R.id.txtUserName);
        }
    }
}
