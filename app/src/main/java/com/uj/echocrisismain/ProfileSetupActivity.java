package com.uj.echocrisismain;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ProfileSetupActivity extends AppCompatActivity {

    EditText etName, etPhone, etEmail, etDOB, etLocation, etState,
            etCrisisType, etRecoveryStage, etOccupation, etMedicalNeeds;
    Spinner spinnerGender;
    CheckBox checkUrgentHelp;
    Button btnSaveProfile;

    FirebaseFirestore db;
    private static final String TAG = "ProfileSetupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etDOB = findViewById(R.id.etDOB);
        etLocation = findViewById(R.id.etLocation);
        etState = findViewById(R.id.etState);
        etCrisisType = findViewById(R.id.etCrisisType);
        etRecoveryStage = findViewById(R.id.etRecoveryStage);
        etOccupation = findViewById(R.id.etOccupation);
        etMedicalNeeds = findViewById(R.id.etMedicalNeeds);
        spinnerGender = findViewById(R.id.spinnerGender);
        checkUrgentHelp = findViewById(R.id.checkUrgentHelp);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        etDOB.setOnClickListener(v -> showDatePicker());
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> etDOB.setText(d + "/" + (m + 1) + "/" + y),
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveProfileData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();

        UserProfile profile = new UserProfile(
                etName.getText().toString().trim(),
                etPhone.getText().toString().trim(),
                etEmail.getText().toString().trim(),
                etDOB.getText().toString().trim(),
                spinnerGender.getSelectedItem().toString(),
                etLocation.getText().toString().trim(),
                etState.getText().toString().trim(),
                etCrisisType.getText().toString().trim(),
                etRecoveryStage.getText().toString().trim(),
                etOccupation.getText().toString().trim(),
                etMedicalNeeds.getText().toString().trim(),
                checkUrgentHelp.isChecked()
        );

        db.collection("users").document(uid)
                .set(profile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();

                    // ✅ NEXT: Emergency Number Screen
                    Intent intent = new Intent(this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Profile save failed", e);
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                });
    }
}
