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

/**
 * Adapter that displays a list of organizer emails for the admin.
 * Allows the admin to remove an organizer along with all events they created.
 */
public class AdminOrganizerAdapter extends RecyclerView.Adapter<AdminOrganizerAdapter.ViewHolder> {

    private final List<String> organizers;
    private final Context context;

    /**
     * Creates the adapter with a list of organizer emails.
     *
     * @param organizers the list of organizer email addresses
     * @param context the context used for UI updates
     */
    public AdminOrganizerAdapter(List<String> organizers, Context context) {
        this.organizers = organizers;
        this.context = context;
    }

    /**
     * Inflates the layout for a single organizer row.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_organizer, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the organizer email to the row and sets up the removal action.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String email = organizers.get(position);
        holder.txtEmail.setText(email);

        holder.itemView.setOnClickListener(v -> removeOrganizer(email, position));
    }

    /**
     * Returns the number of organizers being shown.
     */
    @Override
    public int getItemCount() {
        return organizers.size();
    }

    /**
     * Removes an organizer by deleting all events created by them
     * and then removing the organizer from the displayed list.
     *
     * @param email the organizer's email
     * @param pos the position of the organizer in the list
     */
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

    /**
     * ViewHolder that holds references to the UI elements of an organizer row.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtEmail;

        /**
         * Initializes the email text view.
         *
         * @param itemView the row view
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEmail = itemView.findViewById(R.id.tvOrganizerEmail);
        }
    }
}
