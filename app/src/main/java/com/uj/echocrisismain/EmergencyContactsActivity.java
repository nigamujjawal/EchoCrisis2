package com.uj.echocrisismain;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for managing multiple emergency contacts.
 * Contacts are stored locally in SharedPreferences and synced to Firestore.
 */
public class EmergencyContactsActivity extends AppCompatActivity
        implements EmergencyContactAdapter.OnContactDeleteListener {

    private static final String PREFS_NAME = "sos_prefs";
    private static final String KEY_CONTACTS = "emergency_contacts";

    private RecyclerView rvContacts;
    private TextView tvEmptyState;
    private FloatingActionButton fabAddContact;

    private EmergencyContactAdapter adapter;
    private List<EmergencyContact> contactList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvContacts = findViewById(R.id.rvContacts);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        fabAddContact = findViewById(R.id.fabAddContact);

        contactList = new ArrayList<>();
        adapter = new EmergencyContactAdapter(contactList, this);

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(adapter);

        fabAddContact.setOnClickListener(v -> showAddContactDialog());

        loadContacts();
    }

    private void loadContacts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String contactsJson = prefs.getString(KEY_CONTACTS, "[]");

        contactList.clear();
        try {
            JSONArray jsonArray = new JSONArray(contactsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                EmergencyContact contact = EmergencyContact.fromJson(jsonArray.getJSONObject(i));
                contactList.add(contact);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void saveContacts() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (EmergencyContact contact : contactList) {
                jsonArray.put(contact.toJson());
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply();

            // Sync to Firestore if user is logged in
            syncToFirestore();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving contacts", Toast.LENGTH_SHORT).show();
        }
    }

    private void syncToFirestore() {
        if (mAuth.getCurrentUser() != null) {
            List<Map<String, String>> contactMaps = new ArrayList<>();
            for (EmergencyContact contact : contactList) {
                contactMaps.add(contact.toMap());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("emergency_contacts", contactMaps);

            db.collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .set(data, SetOptions.merge());
        }
    }

    private void showAddContactDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);

        EditText etName = dialogView.findViewById(R.id.etDialogContactName);
        EditText etPhone = dialogView.findViewById(R.id.etDialogContactPhone);
        EditText etRelationship = dialogView.findViewById(R.id.etDialogContactRelationship);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String relationship = etRelationship.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (phone.length() < 5) {
                        Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    EmergencyContact newContact = new EmergencyContact(name, phone, relationship);
                    contactList.add(newContact);
                    adapter.notifyItemInserted(contactList.size() - 1);
                    saveContacts();
                    updateEmptyState();

                    Toast.makeText(this, "Contact added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to remove this contact?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    contactList.remove(position);
                    adapter.notifyItemRemoved(position);
                    saveContacts();
                    updateEmptyState();
                    Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateEmptyState() {
        if (contactList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }
}
