package com.example.eventapp.admin;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminNotificationLogsFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> logs = new ArrayList<>();

    public AdminNotificationLogsFragment() {
        super(R.layout.admin_notification_logs);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerLogs);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadLogs();
    }

    private void loadLogs() {
        AdminManager.getInstance().getNotificationLogs()
                .addOnSuccessListener(snapshot -> {
                    logs.clear();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        logs.add(doc.getString("message"));
                    }

                    recycler.setAdapter(new AdminNotificationLogAdapter(logs));
                });
    }
}
