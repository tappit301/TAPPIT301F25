package com.example.eventapp.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.ViewHolder> {

    private final List<String> organizers;
    private final Context context;

    public AdminOrganizerAdapter(List<String> organizers, Context context) {
        this.organizers = organizers;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String email = organizers.get(position);
        holder.txtEmail.setText(email);

        holder.itemView.setOnClickListener(v -> removeOrganizer(email, position));
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    private void removeOrganizer(String email, int pos) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .whereEqualTo("organizerEmail", email)
                .get()
                .addOnSuccessListener(snapshot -> {

                    snapshot.getDocuments().forEach(doc ->
                            db.collection("events").document(doc.getId()).delete()
                    );

                    organizers.remove(pos);
                    notifyItemRemoved(pos);

                    Toast.makeText(context, "Organizer removed", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEmail = itemView.findViewById(R.id.tvOrganizerEmail);
        }
    }
}
