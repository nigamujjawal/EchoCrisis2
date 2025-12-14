package com.uj.echocrisismain;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MedicalHelpActivity extends AppCompatActivity {

    Button btnHelplines, btnAmbulance, btnOnlineDoctor, btnHospitalLocator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_help);

        btnHelplines = findViewById(R.id.btnHelplines);
        btnAmbulance = findViewById(R.id.btnAmbulance);
        btnOnlineDoctor = findViewById(R.id.btnOnlineDoctor);
        btnHospitalLocator = findViewById(R.id.btnHospitalLocator);

        // Emergency Helplines
        btnHelplines.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:108")); // Example: Indian emergency number
            startActivity(intent);
        });

        // Ambulance Services
        btnAmbulance.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:102")); // Example: Ambulance service number
            startActivity(intent);
        });

        // Online Doctor Consultations
        btnOnlineDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.practo.com/consult")); // Example: Practo
            startActivity(intent);
        });

        // Hospital Locator
        btnHospitalLocator.setOnClickListener(v -> {
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=hospitals near me");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }
}