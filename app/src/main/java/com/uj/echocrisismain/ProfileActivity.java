package com.uj.echocrisismain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etEmergencyContact;
    private Button btnSaveEmergency;

    private static final String PREF_NAME = "sos_prefs";
    private static final String KEY_EMERGENCY = "emergency_number";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // same layout reuse

        mAuth = FirebaseAuth.getInstance();

        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        btnSaveEmergency = findViewById(R.id.btnSaveEmergency);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        etEmergencyContact.setText(prefs.getString(KEY_EMERGENCY, ""));

        btnSaveEmergency.setOnClickListener(v -> saveEmergency());
    }

    private void saveEmergency() {
        String number = etEmergencyContact.getText().toString().trim();

        if (number.isEmpty() || number.length() < 5) {
            Toast.makeText(this, "Valid emergency number daalo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save locally
        getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_EMERGENCY, number)
                .apply();

        // Save to Firestore if user exists
        if (mAuth.getCurrentUser() != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("emergency_number", number);

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(mAuth.getCurrentUser().getUid())
                    .set(map, SetOptions.merge());
        }

        Toast.makeText(this, "Emergency number saved", Toast.LENGTH_SHORT).show();

        // 👉 Setup completed → go to Login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
