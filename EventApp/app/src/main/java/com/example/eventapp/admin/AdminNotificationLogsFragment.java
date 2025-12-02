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

/**
 * Fragment that displays all notification logs for the admin.
 * Retrieves log messages from Firestore and shows them in a list.
 */
public class AdminNotificationLogsFragment extends Fragment {

    private RecyclerView recycler;
    private final List<String> logs = new ArrayList<>();

    /**
     * Creates the fragment and assigns its layout resource.
     */
    public AdminNotificationLogsFragment() {
        super(R.layout.admin_notification_logs);
    }

    /**
     * Sets up the RecyclerView and loads log messages when the view is created.
     *
     * @param view the root view of the fragment
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recycler = view.findViewById(R.id.recyclerLogs);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadLogs();
    }

    /**
     * Loads all notification logs from Firestore and updates the list view.
     * Extracts the "message" field from each log document.
     */
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
