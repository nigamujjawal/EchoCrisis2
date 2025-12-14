package com.uj.echocrisismain;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter for displaying emergency contacts.
 */
public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ContactViewHolder> {

    private List<EmergencyContact> contacts;
    private OnContactDeleteListener deleteListener;

    public interface OnContactDeleteListener {
        void onDelete(int position);
    }

    public EmergencyContactAdapter(List<EmergencyContact> contacts, OnContactDeleteListener deleteListener) {
        this.contacts = contacts;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emergency_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        EmergencyContact contact = contacts.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhone());
        holder.tvRelationship.setText(contact.getRelationship());

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    public void updateContacts(List<EmergencyContact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvRelationship;
        View btnDelete;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            tvRelationship = itemView.findViewById(R.id.tvContactRelationship);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
        }
    }
}
